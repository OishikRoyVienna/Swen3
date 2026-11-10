
package at.fhtw.swen3.ocr.messaging;

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
}
