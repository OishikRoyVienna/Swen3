package at.fhtw.swen3.messaging;

import at.fhtw.swen3.entity.Document;
import java.time.LocalDateTime;

/**
 * OCR job as sent by the REST server.
 */
public record OcrMessage(
        Long documentId,
        String filename,
        String contentType,
        LocalDateTime uploadedAt,
        String objectKey
) {
    public static OcrMessage from(Document document) {
        return new OcrMessage(
                document.getId(),
                document.getFilename(),
                document.getContentType(),
                document.getUploadedAt(),
                document.getObjectKey()
        );
    }
}