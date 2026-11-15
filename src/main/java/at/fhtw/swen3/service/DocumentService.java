package at.fhtw.swen3.service;

import at.fhtw.swen3.dto.DocumentDto;
import at.fhtw.swen3.dto.DocumentRequest;

import java.util.List;

public interface DocumentService {

    DocumentDto create(DocumentRequest request);

    DocumentDto createUploadedDocument(
            String title,
            String filename,
            String contentType,
            String objectKey
    );

    DocumentDto getById(Long id);

    List<DocumentDto> getAll();
    List<DocumentDto> search(String query);

    DocumentDto update(Long id, DocumentRequest request);

    void delete(Long id);
}