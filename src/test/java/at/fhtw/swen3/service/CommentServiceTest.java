package at.fhtw.swen3.service;

import at.fhtw.swen3.dto.CommentDto;
import at.fhtw.swen3.dto.CommentRequest;
import at.fhtw.swen3.entity.Comment;
import at.fhtw.swen3.entity.Document;
import at.fhtw.swen3.exception.ResourceNotFoundException;
import at.fhtw.swen3.repository.CommentRepository;
import at.fhtw.swen3.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private DocumentRepository documentRepository;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentRepository, documentRepository);
    }

    private Document sampleDocument(Long id) {
        Document document = new Document();
        document.setId(id);
        document.setTitle("Invoice");
        document.setFilename("invoice.pdf");
        return document;
    }

    private Comment sampleComment(Long id, Document document) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setText("Looks good");
        comment.setDocument(document);
        return comment;
    }

    @Test
    void addComment_savesCommentLinkedToDocument() {
        Document document = sampleDocument(1L);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment toSave = invocation.getArgument(0);
            toSave.setId(10L);
            return toSave;
        });

        CommentRequest request = new CommentRequest();
        request.setText("Looks good");

        CommentDto result = commentService.addComment(1L, request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getText()).isEqualTo("Looks good");
        assertThat(result.getDocumentId()).isEqualTo(1L);
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void addComment_throws_whenDocumentMissing() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.addComment(99L, new CommentRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(commentRepository);
    }

    @Test
    void getComments_returnsCommentsForDocument() {
        Document document = sampleDocument(1L);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(commentRepository.findByDocumentId(1L))
                .thenReturn(List.of(sampleComment(10L, document), sampleComment(11L, document)));

        List<CommentDto> result = commentService.getComments(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CommentDto::getId).containsExactly(10L, 11L);
    }

    @Test
    void getComments_throws_whenDocumentMissing() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getComments(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteComment_removesComment_whenItBelongsToDocument() {
        Document document = sampleDocument(1L);
        Comment comment = sampleComment(10L, document);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1L, 10L);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_throws_whenCommentBelongsToDifferentDocument() {
        Document document = sampleDocument(1L);
        Document otherDocument = sampleDocument(2L);
        Comment comment = sampleComment(10L, otherDocument);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteComment(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void deleteComment_throws_whenCommentMissing() {
        Document document = sampleDocument(1L);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(commentRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.deleteComment(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
