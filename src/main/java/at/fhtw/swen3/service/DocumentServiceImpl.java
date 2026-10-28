package at.fhtw.swen3.service;

import at.fhtw.swen3.dto.DocumentDto;
import at.fhtw.swen3.dto.DocumentRequest;
import at.fhtw.swen3.entity.Document;
import at.fhtw.swen3.exception.ResourceNotFoundException;
import at.fhtw.swen3.mapper.DocumentMapper;
import at.fhtw.swen3.messaging.OcrMessage;
import at.fhtw.swen3.messaging.OcrMessagePublisher;
import at.fhtw.swen3.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentServiceImpl implements DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentServiceImpl.class);

    private final DocumentRepository documentRepository;
    private final OcrMessagePublisher ocrMessageProducer;

    public DocumentServiceImpl(DocumentRepository documentRepository, OcrMessagePublisher ocrMessageProducer) {
        this.documentRepository = documentRepository;
        this.ocrMessageProducer = ocrMessageProducer;
    }

    /**
     * Saves the document and queues an OCR job for it. If the job cannot be queued, the
     * MessageQueueException rolls the transaction back, so no document is stored without an OCR job.
     */
    @Override
    @Transactional
    public DocumentDto create(DocumentRequest request) {
        Document document = DocumentMapper.toEntity(request);
        document.setUploadedAt(LocalDateTime.now());

        Document saved = Persistence.call("save document", () -> documentRepository.save(document));
        log.info("Document {} saved (filename '{}')", saved.getId(), saved.getFilename());

        ocrMessageProducer.send(OcrMessage.from(saved));
        return DocumentMapper.toDto(saved);
    }

    @Override
    public DocumentDto getById(Long id) {
        return DocumentMapper.toDto(findOrThrow(id));
    }

    @Override
    public List<DocumentDto> getAll() {
        return Persistence.call("load documents", documentRepository::findAll).stream()
                .map(DocumentMapper::toDto)
                .toList();
    }

    @Override
    public DocumentDto update(Long id, DocumentRequest request) {
        Document document = findOrThrow(id);
        document.setTitle(request.getTitle());
        document.setFilename(request.getFilename());
        document.setContentType(request.getContentType());
        Document saved = Persistence.call("update document " + id, () -> documentRepository.save(document));
        log.info("Document {} updated", id);
        return DocumentMapper.toDto(saved);
    }

    @Override
    public void delete(Long id) {
        Document document = findOrThrow(id);
        Persistence.run("delete document " + id, () -> documentRepository.delete(document));
        log.info("Document {} deleted", id);
    }

    private Document findOrThrow(Long id) {
        return Persistence.call("load document " + id, () -> documentRepository.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));
    }
}
