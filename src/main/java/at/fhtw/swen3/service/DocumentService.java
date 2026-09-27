package at.fhtw.swen3.service;

import at.fhtw.swen3.dto.DocumentDto;
import at.fhtw.swen3.dto.DocumentRequest;
import at.fhtw.swen3.entity.Document;
import at.fhtw.swen3.exception.ResourceNotFoundException;
import at.fhtw.swen3.mapper.DocumentMapper;
import at.fhtw.swen3.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public DocumentDto create(DocumentRequest request) {
        Document document = DocumentMapper.toEntity(request);
        document.setUploadedAt(LocalDateTime.now());
        return DocumentMapper.toDto(documentRepository.save(document));
    }

    public DocumentDto getById(Long id) {
        return DocumentMapper.toDto(findOrThrow(id));
    }

    public List<DocumentDto> getAll() {
        return documentRepository.findAll().stream()
                .map(DocumentMapper::toDto)
                .toList();
    }

    public DocumentDto update(Long id, DocumentRequest request) {
        Document document = findOrThrow(id);
        document.setTitle(request.getTitle());
        document.setFilename(request.getFilename());
        document.setContentType(request.getContentType());
        return DocumentMapper.toDto(documentRepository.save(document));
    }

    public void delete(Long id) {
        Document document = findOrThrow(id);
        documentRepository.delete(document);
    }

    private Document findOrThrow(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));
    }
}
