package at.fhtw.swen3.genai.messaging;

import at.fhtw.swen3.genai.exception.GenAiException;
import at.fhtw.swen3.genai.service.SummaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GenAiMessageListener {

    private static final Logger log = LoggerFactory.getLogger(GenAiMessageListener.class);

    private final SummaryService summaryService;
    private final RabbitTemplate rabbitTemplate;
    private final String summaryQueue;

    public GenAiMessageListener(SummaryService summaryService,
                                RabbitTemplate rabbitTemplate,
                                @Value("${paperless.rabbitmq.summary-queue}") String summaryQueue) {
        this.summaryService = summaryService;
        this.rabbitTemplate = rabbitTemplate;
        this.summaryQueue = summaryQueue;
    }

    @RabbitListener(queues = "${paperless.rabbitmq.genai-queue}")
    public void onGenAiJob(GenAiMessage message) {
        if (message.text() == null || message.text().isBlank()) {
            log.info("Document {} has no recognized text, skipping summary", message.documentId());
            return;
        }
        log.info("Summarizing document {} ({} characters) with Gemini", message.documentId(), message.text().length());
        long start = System.currentTimeMillis();

        String summary;
        try {
            summary = summaryService.summarize(message.text());
        } catch (GenAiException e) {
            // Nicht erneut zustellen, sondern in die Dead-Letter-Queue; der Upload selbst ist davon nicht betroffen
            log.error("Summary for document {} failed after {} ms, moving job to the dead-letter queue: {}",
                    message.documentId(), System.currentTimeMillis() - start, e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }

        // Zusammenfassung zurück ans Backend, das sie in der Datenbank speichert
        rabbitTemplate.convertAndSend(summaryQueue, new SummaryMessage(message.documentId(), summary));
        log.info("Summary for document {} ({} characters, Gemini took {} ms) sent to queue '{}'",
                message.documentId(), summary.length(), System.currentTimeMillis() - start, summaryQueue);
    }
}
