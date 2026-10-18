package at.fhtw.swen3.exception;

/**
 * Base class for errors raised in the service (business) layer.
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
