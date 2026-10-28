package at.fhtw.swen3.messaging;

/**
 * Sends OCR jobs to the OCR worker. The service layer does not know that RabbitMQ is used behind it.
 */
public interface OcrMessagePublisher {

    void send(OcrMessage message);
}
