package at.fhtw.swen3.genai.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    // Gleiche Deklaration wie im OCR-Worker, damit es egal ist, wer zuerst startet.
    // Fehlgeschlagene Aufträge wandern in die Dead-Letter-Queue statt verloren zu gehen.
    @Bean
    public Queue genAiQueue(@Value("${paperless.rabbitmq.genai-queue}") String queueName,
                            @Value("${paperless.rabbitmq.genai-dead-letter-queue}") String deadLetterQueueName) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange("")
                .deadLetterRoutingKey(deadLetterQueueName)
                .build();
    }

    @Bean
    public Queue genAiDeadLetterQueue(@Value("${paperless.rabbitmq.genai-dead-letter-queue}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Queue summaryQueue(@Value("${paperless.rabbitmq.summary-queue}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
