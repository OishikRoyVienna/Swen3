package at.fhtw.swen3.messaging;

import at.fhtw.swen3.exception.MessageQueueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OcrMessageProducer implements OcrMessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(OcrMessageProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final String queueName;

    public OcrMessageProducer(RabbitTemplate rabbitTemplate,
                              @Value("${paperless.rabbitmq.ocr-queue}") String queueName) {
        this.rabbitTemplate = rabbitTemplate;
        this.queueName = queueName;
    }

    @Override
    public void send(OcrMessage message) {
        try {
            // default exchange: the routing key is the queue name
            rabbitTemplate.convertAndSend(queueName, message);
            log.info("OCR job for document {} sent to queue '{}'", message.documentId(), queueName);
        } catch (AmqpException e) {
            log.error("Sending OCR job for document {} to queue '{}' failed: {}",
                    message.documentId(), queueName, e.getMessage());
            throw new MessageQueueException(
                    "OCR job for document " + message.documentId() + " could not be queued", e);
        }
    }
}
