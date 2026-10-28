package at.fhtw.swen3.service;

import at.fhtw.swen3.dto.DocumentDto;
import at.fhtw.swen3.dto.DocumentRequest;

import java.util.List;

/**
 * Business logic for documents. The controller only knows this interface, not the implementation.
 */
public interface DocumentService {

    DocumentDto create(DocumentRequest request);

    DocumentDto getById(Long id);

    List<DocumentDto> getAll();

    DocumentDto update(Long id, DocumentRequest request);

    void delete(Long id);
}
