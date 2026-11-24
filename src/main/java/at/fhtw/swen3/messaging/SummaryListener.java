package at.fhtw.swen3.messaging;

import at.fhtw.swen3.exception.ResourceNotFoundException;
import at.fhtw.swen3.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Empfängt die Zusammenfassungen vom GenAI-Worker und speichert sie über den Service in der Datenbank.
 */
@Component
public class SummaryListener {

    private static final Logger log = LoggerFactory.getLogger(SummaryListener.class);

    private final DocumentService documentService;

    public SummaryListener(DocumentService documentService) {
        this.documentService = documentService;
    }

    @RabbitListener(queues = "${paperless.rabbitmq.summary-queue}")
    public void onSummary(SummaryMessage message) {
        log.info("Received summary for document {}", message.documentId());
        try {
            documentService.saveSummary(message.documentId(), message.summary());
        } catch (ResourceNotFoundException e) {
            // Dokument wurde gelöscht, bevor die Zusammenfassung fertig war: kein Fehler, einfach verwerfen
            log.warn("Summary for document {} discarded: {}", message.documentId(), e.getMessage());
        }
    }
}
