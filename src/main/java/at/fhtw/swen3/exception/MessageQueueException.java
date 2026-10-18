package at.fhtw.swen3.exception;

/**
 * Messaging layer: a message could not be delivered to RabbitMQ (HTTP 503).
 */
public class MessageQueueException extends RuntimeException {

    public MessageQueueException(String message, Throwable cause) {
        super(message, cause);
    }
}
