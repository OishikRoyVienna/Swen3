package at.fhtw.swen3.ocr.messaging;

import at.fhtw.swen3.ocr.service.OcrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OcrMessageListener {

    private static final Logger log = LoggerFactory.getLogger(OcrMessageListener.class);

    private final OcrService ocrService;

    public OcrMessageListener(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    @RabbitListener(queues = "${paperless.rabbitmq.ocr-queue}")
    public void onOcrJob(OcrMessage message) {
        log.info("Received OCR job: {}", message);
        try {
            ocrService.process(message);
        } catch (RuntimeException e) {
            // Ungültiger Auftrag oder OCR-Fehler: nicht erneut zustellen, sondern in die Dead-Letter-Queue
            log.error("OCR job {} rejected, moving it to the dead-letter queue", message, e);
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}
