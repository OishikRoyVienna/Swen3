
package at.fhtw.swen3.service;

import at.fhtw.swen3.dto.DocumentDto;
import at.fhtw.swen3.dto.DocumentRequest;
import at.fhtw.swen3.entity.Document;
import at.fhtw.swen3.exception.ResourceNotFoundException;
import at.fhtw.swen3.mapper.DocumentMapper;
import at.fhtw.swen3.messaging.OcrMessage;
import at.fhtw.swen3.messaging.OcrMessagePublisher;
import at.fhtw.swen3.repository.DocumentRepository;
import at.fhtw.swen3.search.SearchDocumentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentServiceImpl implements DocumentService {

    private static final Logger log =
            LoggerFactory.getLogger(DocumentServiceImpl.class);

    private final DocumentRepository documentRepository;
    private final OcrMessagePublisher ocrMessageProducer;
    private final SearchDocumentRepository searchDocumentRepository;
    private final RustFsStorageService rustFsStorageService;

    public DocumentServiceImpl(
            DocumentRepository documentRepository,
            OcrMessagePublisher ocrMessageProducer,
            SearchDocumentRepository searchDocumentRepository,
            RustFsStorageService rustFsStorageService
    ) {
        this.documentRepository = documentRepository;
        this.ocrMessageProducer = ocrMessageProducer;
        this.searchDocumentRepository = searchDocumentRepository;
        this.rustFsStorageService = rustFsStorageService;
    }

    @Override
    @Transactional
    public DocumentDto create(DocumentRequest request) {
        Document document = DocumentMapper.toEntity(request);
        document.setUploadedAt(LocalDateTime.now());

        Document saved = Persistence.call(
                "save document",
                () -> documentRepository.save(document)
        );

        log.info(
                "Document {} saved (filename '{}')",
                saved.getId(),
                saved.getFilename()
        );

        ocrMessageProducer.send(OcrMessage.from(saved));
        return DocumentMapper.toDto(saved);
    }

    @Override
    @Transactional
    public DocumentDto createUploadedDocument(
            String title,
            String filename,
            String contentType,
            String objectKey
    ) {
        Document document = new Document();

        document.setTitle(title);
        document.setFilename(filename);
        document.setContentType(contentType);
        document.setObjectKey(objectKey);
        document.setUploadedAt(LocalDateTime.now());

        Document saved = Persistence.call(
                "save uploaded document",
                () -> documentRepository.save(document)
        );

        log.info(
                "Uploaded document {} saved (objectKey '{}')",
                saved.getId(),
                saved.getObjectKey()
        );

        ocrMessageProducer.send(OcrMessage.from(saved));

        return DocumentMapper.toDto(saved);
    }

    @Override
    public DocumentDto getById(Long id) {
        return DocumentMapper.toDto(findOrThrow(id));
    }

    @Override
    public List<DocumentDto> getAll() {
        return Persistence.call(
                        "load documents",
                        documentRepository::findAll
                ).stream()
                .map(DocumentMapper::toDto)
                .toList();
    }

    @Override
    public List<DocumentDto> search(String query) {
        if (query == null || query.isBlank()) {
            return getAll();
        }

        return searchDocumentRepository
                .findByContentContainingIgnoreCaseOrFilenameContainingIgnoreCase(
                        query.trim(),
                        query.trim()
                )
                .stream()
                .map(result -> {
                    try {
                        return documentRepository
                                .findById(Long.parseLong(result.getId()))
                                .map(DocumentMapper::toDto)
                                .orElse(null);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    public DocumentDto update(Long id, DocumentRequest request) {
        Document document = findOrThrow(id);

        document.setTitle(request.getTitle());
        document.setFilename(request.getFilename());
        document.setContentType(request.getContentType());

        Document saved = Persistence.call(
                "update document " + id,
                () -> documentRepository.save(document)
        );

        log.info("Document {} updated", id);

        return DocumentMapper.toDto(saved);
    }

    /**
     * Löscht ein Dokument aus:
     * 1. Elasticsearch
     * 2. RustFS
     * 3. PostgreSQL
     *
     * Bei Fehlern in den externen Systemen wird
     * der Datenbankeintrag nicht absichtlich gelöscht.
     */
    @Override
    @Transactional
    public void delete(Long id) {
        Document document = findOrThrow(id);

        String objectKey = document.getObjectKey();

        log.info("Deleting document {} from all storage systems", id);

        // 1. Elasticsearch-Eintrag entfernen.
        // Der Eintrag kann fehlen, z. B. wenn OCR noch nicht
        // abgeschlossen wurde.
        searchDocumentRepository.deleteById(id.toString());

        log.info("Document {} removed from Elasticsearch", id);

        // 2. Gespeicherte PDF aus RustFS entfernen.
        // Metadaten-Dokumente besitzen eventuell keinen Object Key.
        if (objectKey != null && !objectKey.isBlank()) {
            rustFsStorageService.delete(objectKey);

            log.info(
                    "PDF for document {} removed from RustFS (key '{}')",
                    id,
                    objectKey
            );
        }

        // 3. Zuletzt Datenbankeintrag löschen.
        Persistence.run(
                "delete document " + id,
                () -> documentRepository.delete(document)
        );

        log.info(
                "Document {} deletion completed",
                id
        );
    }

    private Document findOrThrow(Long id) {
        return Persistence.call(
                "load document " + id,
                () -> documentRepository.findById(id)
        ).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Document not found: " + id
                )
        );
    }
}
