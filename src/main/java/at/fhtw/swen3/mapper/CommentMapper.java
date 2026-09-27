package at.fhtw.swen3.mapper;

import at.fhtw.swen3.dto.CommentDto;
import at.fhtw.swen3.dto.CommentRequest;
import at.fhtw.swen3.entity.Comment;
import at.fhtw.swen3.entity.Document;

public final class CommentMapper {

    private CommentMapper() {
    }

    public static Comment toEntity(CommentRequest request, Document document) {
        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setDocument(document);
        return comment;
    }

    public static CommentDto toDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setDocumentId(comment.getDocument().getId());
        return dto;
    }
}
