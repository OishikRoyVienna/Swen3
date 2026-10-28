package at.fhtw.swen3.service;

import at.fhtw.swen3.dto.CommentDto;
import at.fhtw.swen3.dto.CommentRequest;

import java.util.List;

/**
 * Business logic for comments of a document.
 */
public interface CommentService {

    CommentDto addComment(Long documentId, CommentRequest request);

    List<CommentDto> getComments(Long documentId);

    void deleteComment(Long documentId, Long commentId);
}
