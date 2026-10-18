package at.fhtw.swen3.controller;

import at.fhtw.swen3.dto.CommentDto;
import at.fhtw.swen3.dto.CommentRequest;
import at.fhtw.swen3.service.CommentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents/{documentId}/comments")
public class CommentController {

    private static final Logger log = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentDto> addComment(@PathVariable Long documentId,
                                                  @Valid @RequestBody CommentRequest request) {
        log.info("POST /api/documents/{}/comments", documentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.addComment(documentId, request));
    }

    @GetMapping
    public List<CommentDto> getComments(@PathVariable Long documentId) {
        log.debug("GET /api/documents/{}/comments", documentId);
        return commentService.getComments(documentId);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long documentId, @PathVariable Long commentId) {
        log.info("DELETE /api/documents/{}/comments/{}", documentId, commentId);
        commentService.deleteComment(documentId, commentId);
        return ResponseEntity.noContent().build();
    }
}
