package at.fhtw.swen3.exception;

/**
 * Service layer: a requested document or comment does not exist (HTTP 404).
 */
public class ResourceNotFoundException extends ServiceException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
