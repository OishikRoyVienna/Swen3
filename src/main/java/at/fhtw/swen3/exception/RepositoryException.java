package at.fhtw.swen3.exception;

/**
 * Persistence layer: a database operation failed (HTTP 500).
 */
public class RepositoryException extends RuntimeException {

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
