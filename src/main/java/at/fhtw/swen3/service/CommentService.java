package at.fhtw.swen3.service;

import at.fhtw.swen3.dto.CommentDto;
import at.fhtw.swen3.dto.CommentRequest;
import at.fhtw.swen3.entity.Comment;
import at.fhtw.swen3.entity.Document;
import at.fhtw.swen3.exception.ResourceNotFoundException;
import at.fhtw.swen3.mapper.CommentMapper;
import at.fhtw.swen3.repository.CommentRepository;
import at.fhtw.swen3.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final DocumentRepository documentRepository;

    public CommentService(CommentRepository commentRepository, DocumentRepository documentRepository) {
        this.commentRepository = commentRepository;
        this.documentRepository = documentRepository;
    }

    public CommentDto addComment(Long documentId, CommentRequest request) {
        Document document = findDocumentOrThrow(documentId);
        Comment comment = CommentMapper.toEntity(request, document);
        comment.setCreatedAt(LocalDateTime.now());
        Comment saved = Persistence.call("save comment", () -> commentRepository.save(comment));
        log.info("Comment {} added to document {}", saved.getId(), documentId);
        return CommentMapper.toDto(saved);
    }

    public List<CommentDto> getComments(Long documentId) {
        findDocumentOrThrow(documentId);
        return Persistence.call("load comments", () -> commentRepository.findByDocumentId(documentId)).stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    public void deleteComment(Long documentId, Long commentId) {
        findDocumentOrThrow(documentId);
        Comment comment = Persistence.call("load comment " + commentId, () -> commentRepository.findById(commentId))
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
        if (!comment.getDocument().getId().equals(documentId)) {
            throw new ResourceNotFoundException("Comment " + commentId + " does not belong to document " + documentId);
        }
        Persistence.run("delete comment " + commentId, () -> commentRepository.delete(comment));
        log.info("Comment {} deleted from document {}", commentId, documentId);
    }

    private Document findDocumentOrThrow(Long documentId) {
        return Persistence.call("load document " + documentId, () -> documentRepository.findById(documentId))
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));
    }
}
