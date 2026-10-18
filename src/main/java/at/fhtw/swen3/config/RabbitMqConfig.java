package at.fhtw.swen3.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the OCR queue (plus its dead-letter queue) and sends messages as JSON.
 * The OCR worker declares the same queues with the same arguments, so either side can start first.
 */
@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue ocrQueue(@Value("${paperless.rabbitmq.ocr-queue}") String queueName,
                          @Value("${paperless.rabbitmq.ocr-dead-letter-queue}") String deadLetterQueueName) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange("")
                .deadLetterRoutingKey(deadLetterQueueName)
                .build();
    }

    @Bean
    public Queue ocrDeadLetterQueue(@Value("${paperless.rabbitmq.ocr-dead-letter-queue}") String deadLetterQueueName) {
        return QueueBuilder.durable(deadLetterQueueName).build();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
