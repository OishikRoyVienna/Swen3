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

        log.info("Document {} indexed in Elasticsearch", message.documentId());

        log.info("OCR completed for document {}. Recognized {} characters.",
                message.documentId(), recognizedText.length());

        log.info("Recognized text for document {}:\n{}",
                message.documentId(), recognizedText);

        log.info("OCR job for document {} finished", message.documentId());
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