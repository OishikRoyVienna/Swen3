package at.fhtw.swen3.ocr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Scheduled check of the dead-letter queue, so failed OCR jobs do not stay there unnoticed.
 */
@Service
public class DeadLetterQueueMonitor {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterQueueMonitor.class);

    private final AmqpAdmin amqpAdmin;
    private final String deadLetterQueueName;

    public DeadLetterQueueMonitor(AmqpAdmin amqpAdmin,
                                  @Value("${paperless.rabbitmq.ocr-dead-letter-queue}") String deadLetterQueueName) {
        this.amqpAdmin = amqpAdmin;
        this.deadLetterQueueName = deadLetterQueueName;
    }

    @Scheduled(initialDelay = 1, fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void checkDeadLetterQueue() {
        QueueInformation queue = amqpAdmin.getQueueInfo(deadLetterQueueName);
        if (queue != null && queue.getMessageCount() > 0) {
            log.warn("{} failed OCR job(s) waiting in dead-letter queue '{}'",
                    queue.getMessageCount(), deadLetterQueueName);
        }
    }
}
