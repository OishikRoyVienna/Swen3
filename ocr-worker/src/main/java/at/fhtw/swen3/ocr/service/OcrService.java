package at.fhtw.swen3.ocr.service;

import at.fhtw.swen3.ocr.exception.OcrProcessingException;
import at.fhtw.swen3.ocr.messaging.OcrMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import at.fhtw.swen3.ocr.search.OcrDocumentRepository;
import at.fhtw.swen3.ocr.search.OcrDocument;

@Service
public class OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrService.class);

    private final RustFsStorageService rustFsStorageService;
    private final TesseractOcrService tesseractOcrService;
    private final OcrDocumentRepository ocrDocumentRepository;

    public OcrService(RustFsStorageService rustFsStorageService,
                      TesseractOcrService tesseractOcrService,
                      OcrDocumentRepository ocrDocumentRepository) {
        this.rustFsStorageService = rustFsStorageService;
        this.tesseractOcrService = tesseractOcrService;
        this.ocrDocumentRepository = ocrDocumentRepository;
    }

    public void process(OcrMessage message) {
        validate(message);

        log.info("Starting OCR for document {} ('{}', {})",
                message.documentId(), message.filename(), message.contentType());

        byte[] pdfBytes = rustFsStorageService.download(message.objectKey());

        log.info("PDF for document {} downloaded from RustFS: {} bytes",
                message.documentId(), pdfBytes.length);

        String recognizedText = tesseractOcrService.recognize(pdfBytes);
        OcrDocument document = new OcrDocument(
                message.documentId().toString(),
                message.filename(),
                recognizedText
        );

        ocrDocumentRepository.save(document);

        // Nur die Länge loggen, nicht den Text selbst (Dokumente können persönliche Daten enthalten)
        log.info("OCR job for document {} finished: {} characters recognized and indexed in Elasticsearch",
                message.documentId(), recognizedText.length());
    }

    private void validate(OcrMessage message) {
        if (message == null || message.documentId() == null) {
            throw new OcrProcessingException("OCR job has no document id");
        }

        if (message.filename() == null || message.filename().isBlank()) {
            throw new OcrProcessingException(
                    "OCR job for document " + message.documentId() + " has no filename");
        }

        if (message.objectKey() == null || message.objectKey().isBlank()) {
            throw new OcrProcessingException(
                    "OCR job for document " + message.documentId() + " has no RustFS object key");
        }
    }
}