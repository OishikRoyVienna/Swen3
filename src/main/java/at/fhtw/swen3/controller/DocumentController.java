package at.fhtw.swen3.controller;

import at.fhtw.swen3.dto.DocumentDto;
import at.fhtw.swen3.dto.DocumentRequest;
import at.fhtw.swen3.service.DocumentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<DocumentDto> create(@Valid @RequestBody DocumentRequest request) {
        log.info("POST /api/documents (filename '{}')", request.getFilename());
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.create(request));
    }

    @GetMapping
    public List<DocumentDto> getAll() {
        log.debug("GET /api/documents");
        return documentService.getAll();
    }

    @GetMapping("/{id}")
    public DocumentDto getById(@PathVariable Long id) {
        log.debug("GET /api/documents/{}", id);
        return documentService.getById(id);
    }

    @PutMapping("/{id}")
    public DocumentDto update(@PathVariable Long id, @Valid @RequestBody DocumentRequest request) {
        log.info("PUT /api/documents/{}", id);
        return documentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/documents/{}", id);
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
