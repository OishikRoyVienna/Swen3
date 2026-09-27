package at.fhtw.swen3.service;

import at.fhtw.swen3.dto.CommentDto;
import at.fhtw.swen3.dto.CommentRequest;
import at.fhtw.swen3.entity.Comment;
import at.fhtw.swen3.entity.Document;
import at.fhtw.swen3.exception.ResourceNotFoundException;
import at.fhtw.swen3.mapper.CommentMapper;
import at.fhtw.swen3.repository.CommentRepository;
import at.fhtw.swen3.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

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
        return CommentMapper.toDto(commentRepository.save(comment));
    }

    public List<CommentDto> getComments(Long documentId) {
        findDocumentOrThrow(documentId);
        return commentRepository.findByDocumentId(documentId).stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    public void deleteComment(Long documentId, Long commentId) {
        findDocumentOrThrow(documentId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
        if (!comment.getDocument().getId().equals(documentId)) {
            throw new ResourceNotFoundException("Comment " + commentId + " does not belong to document " + documentId);
        }
        commentRepository.delete(comment);
    }

    private Document findDocumentOrThrow(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));
    }
}
