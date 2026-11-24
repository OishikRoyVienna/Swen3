package at.fhtw.swen3.genai.exception;

/**
 * Worker-Schicht: Gemini konnte keine Zusammenfassung liefern. Der Auftrag geht in die Dead-Letter-Queue.
 */
public class GenAiException extends RuntimeException {

    public GenAiException(String message) {
        super(message);
    }

    public GenAiException(String message, Throwable cause) {
        super(message, cause);
    }
}
