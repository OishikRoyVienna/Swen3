
package at.fhtw.swen3.controller;

import at.fhtw.swen3.dto.DocumentDto;
import at.fhtw.swen3.dto.DocumentRequest;
import at.fhtw.swen3.entity.Document;
import at.fhtw.swen3.repository.DocumentRepository;
import at.fhtw.swen3.service.DocumentService;
import at.fhtw.swen3.service.RustFsStorageService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private static final Logger log =
            LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService documentService;
    private final RustFsStorageService rustFsStorageService;

    // NEU: Repository zum Abrufen des gespeicherten PDF-Objektschlüssels
    private final DocumentRepository documentRepository;

    public DocumentController(
            DocumentService documentService,
            RustFsStorageService rustFsStorageService,
            DocumentRepository documentRepository
    ) {
        this.documentService = documentService;
        this.rustFsStorageService = rustFsStorageService;
        this.documentRepository = documentRepository;
    }

    // Dokument-Metadaten erstellen
    @PostMapping
    public ResponseEntity<DocumentDto> create(
            @Valid @RequestBody DocumentRequest request
    ) {
        log.info("POST /api/documents (filename '{}')",
                request.getFilename());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.create(request));
    }

    // PDF mit optionalem Titel hochladen.
    // Speicherung in RustFS und PostgreSQL, OCR über RabbitMQ.
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String requestedTitle
    ) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Bitte eine PDF-Datei auswählen.");
        }

        byte[] data = file.getBytes();

        // PDF-Dateisignatur prüfen
        if (data.length < 5
                || data[0] != '%'
                || data[1] != 'P'
                || data[2] != 'D'
                || data[3] != 'F'
                || data[4] != '-') {

            return ResponseEntity.badRequest()
                    .body("Nur PDF-Dateien sind erlaubt.");
        }

        // Dateinamen ermitteln
        String filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            filename = "upload.pdf";
        }

        // Titel aus Formular verwenden, falls vorhanden.
        // Sonst Titel aus dem Dateinamen ableiten.
        String title;

        if (requestedTitle != null && !requestedTitle.isBlank()) {
            title = requestedTitle.trim();
        } else {
            title = filename.toLowerCase().endsWith(".pdf")
                    ? filename.substring(0, filename.length() - 4)
                    : filename;
        }

        // Eindeutigen Objektschlüssel erzeugen
        String objectKey =
                "documents/" + UUID.randomUUID() + ".pdf";

        // PDF in RustFS speichern
        rustFsStorageService.upload(objectKey, data);

        // Dokument einmalig in PostgreSQL speichern
        // und OCR-Auftrag an RabbitMQ senden
        documentService.createUploadedDocument(
                title,
                filename,
                "application/pdf",
                objectKey
        );

        log.info(
                "PDF '{}' mit Titel '{}' in RustFS gespeichert unter '{}'",
                filename,
                title,
                objectKey
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        "PDF erfolgreich gespeichert. Object Key: "
                                + objectKey
                );
    }

    // Alle Dokumente abrufen
    @GetMapping
    public List<DocumentDto> getAll() {
        log.debug("GET /api/documents");
        return documentService.getAll();
    }

    // Volltextsuche über Elasticsearch
    @GetMapping("/search")
    public List<DocumentDto> searchDocuments(
            @RequestParam("q") String query
    ) {
        return documentService.search(query);
    }

    // Dokument anhand der ID abrufen
    @GetMapping("/{id}")
    public DocumentDto getById(@PathVariable Long id) {
        log.debug("GET /api/documents/{}", id);
        return documentService.getById(id);
    }

    // NEU: Gespeicherte PDF-Datei für die Vorschau abrufen
    // GET /api/documents/{id}/file
    @GetMapping(
            value = "/{id}/file",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> getDocumentFile(
            @PathVariable Long id
    ) {
        log.info("GET /api/documents/{}/file", id);

        // Dokument aus PostgreSQL laden
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Dokument nicht gefunden."
                ));

        // Prüfen, ob eine PDF-Datei gespeichert wurde
        String objectKey = document.getObjectKey();

        if (objectKey == null || objectKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Für dieses Dokument ist keine PDF-Datei gespeichert."
            );
        }

        // PDF aus RustFS herunterladen
        byte[] pdfData = rustFsStorageService.download(objectKey);

        // Dateiname für Browser ermitteln
        String filename = document.getFilename();

        if (filename == null || filename.isBlank()) {
            filename = "dokument.pdf";
        }

        String encodedFilename = URLEncoder.encode(
                filename,
                StandardCharsets.UTF_8
        ).replace("+", "%20");

        // PDF direkt im Browser anzeigen statt herunterladen
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfData.length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename*=UTF-8''" + encodedFilename
                )
                .header("X-Content-Type-Options", "nosniff")
                .body(pdfData);
    }

    // Dokument aktualisieren
    @PutMapping("/{id}")
    public DocumentDto update(
            @PathVariable Long id,
            @Valid @RequestBody DocumentRequest request
    ) {
        log.info("PUT /api/documents/{}", id);
        return documentService.update(id, request);
    }

    // Dokument löschen
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/documents/{}", id);
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
