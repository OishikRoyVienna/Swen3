package at.fhtw.swen3.mapper;

import at.fhtw.swen3.dto.DocumentDto;
import at.fhtw.swen3.dto.DocumentRequest;
import at.fhtw.swen3.entity.Document;

import java.util.List;

public final class DocumentMapper {

    private DocumentMapper() {
    }

    public static Document toEntity(DocumentRequest request) {
        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setFilename(request.getFilename());
        document.setContentType(request.getContentType());
        return document;
    }

    public static DocumentDto toDto(Document document) {
        DocumentDto dto = new DocumentDto();
        dto.setId(document.getId());
        dto.setTitle(document.getTitle());
        dto.setFilename(document.getFilename());
        dto.setContentType(document.getContentType());
        dto.setUploadedAt(document.getUploadedAt());
        List<at.fhtw.swen3.dto.CommentDto> comments = document.getComments().stream()
                .map(CommentMapper::toDto)
                .toList();
        dto.setComments(comments);
        return dto;
    }
}
