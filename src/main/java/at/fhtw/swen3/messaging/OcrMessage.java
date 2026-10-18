package at.fhtw.swen3.messaging;

import at.fhtw.swen3.entity.Document;

import java.time.LocalDateTime;

/**
 * Message sent to the OCR worker after a document was uploaded.
 */
public record OcrMessage(Long documentId, String filename, String contentType, LocalDateTime uploadedAt) {

    public static OcrMessage from(Document document) {
        return new OcrMessage(document.getId(), document.getFilename(), document.getContentType(),
                document.getUploadedAt());
    }
}
