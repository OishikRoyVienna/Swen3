package at.fhtw.swen3.ocr.service;

import at.fhtw.swen3.ocr.exception.OcrProcessingException;
import at.fhtw.swen3.ocr.messaging.OcrMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * "Empty" OCR step: validates the job and logs it. Real text recognition (e.g. Tesseract) follows in a later sprint.
 */
@Service
public class OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrService.class);

    public void process(OcrMessage message) {
        validate(message);
        log.info("Starting OCR for document {} ('{}', {})",
                message.documentId(), message.filename(), message.contentType());

        // TODO later sprint: load the file from storage, run OCR, send the recognised text back
        log.info("OCR is not implemented yet, skipping text recognition for document {}", message.documentId());

        log.info("OCR job for document {} finished", message.documentId());
    }

    private void validate(OcrMessage message) {
        if (message == null || message.documentId() == null) {
            throw new OcrProcessingException("OCR job has no document id");
        }
        if (message.filename() == null || message.filename().isBlank()) {
            throw new OcrProcessingException("OCR job for document " + message.documentId() + " has no filename");
        }
    }
}
