package at.fhtw.swen3.ocr.messaging;

import java.time.LocalDateTime;

/**
 * OCR job as sent by the REST server (same JSON fields as at.fhtw.swen3.messaging.OcrMessage).
 */
public record OcrMessage(Long documentId, String filename, String contentType, LocalDateTime uploadedAt) {
}
