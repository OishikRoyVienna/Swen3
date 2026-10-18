package at.fhtw.swen3.ocr.exception;

/**
 * Worker layer: an OCR job could not be processed. The message is moved to the dead-letter queue.
 */
public class OcrProcessingException extends RuntimeException {

    public OcrProcessingException(String message) {
        super(message);
    }

    public OcrProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
