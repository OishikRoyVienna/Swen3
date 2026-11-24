package at.fhtw.swen3.ocr.service;

import at.fhtw.swen3.ocr.exception.OcrProcessingException;
import at.fhtw.swen3.ocr.messaging.GenAiMessage;
import at.fhtw.swen3.ocr.messaging.OcrMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import at.fhtw.swen3.ocr.search.OcrDocumentRepository;
import at.fhtw.swen3.ocr.search.OcrDocument;

@Service
public class OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrService.class);

    private final RustFsStorageService rustFsStorageService;
    private final TesseractOcrService tesseractOcrService;
    private final OcrDocumentRepository ocrDocumentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final String genAiQueue;

    public OcrService(RustFsStorageService rustFsStorageService,
                      TesseractOcrService tesseractOcrService,
                      OcrDocumentRepository ocrDocumentRepository,
                      RabbitTemplate rabbitTemplate,
                      @Value("${paperless.rabbitmq.genai-queue}") String genAiQueue) {
        this.rustFsStorageService = rustFsStorageService;
        this.tesseractOcrService = tesseractOcrService;
        this.ocrDocumentRepository = ocrDocumentRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.genAiQueue = genAiQueue;
    }

    public void process(OcrMessage message) {
        validate(message);

        log.info("Starting OCR for document {} ('{}', {})",
                message.documentId(), message.filename(), message.contentType());

        byte[] pdfBytes = rustFsStorageService.download(message.objectKey());

        log.info("PDF for document {} downloaded from RustFS: {} bytes",
                message.documentId(), pdfBytes.length);

        String recognizedText = tesseractOcrService.recognize(pdfBytes);
        OcrDocument document = new OcrDocument(
                message.documentId().toString(),
                message.filename(),
                recognizedText
        );

        ocrDocumentRepository.save(document);

        // Nur die Länge loggen, nicht den Text selbst (Dokumente können persönliche Daten enthalten)
        log.info("OCR job for document {} finished: {} characters recognized and indexed in Elasticsearch",
                message.documentId(), recognizedText.length());

        // Erkannten Text an den GenAI-Worker weitergeben, der die Zusammenfassung erstellt
        rabbitTemplate.convertAndSend(genAiQueue, new GenAiMessage(message.documentId(), recognizedText));
        log.info("Text of document {} sent to queue '{}' for summarization", message.documentId(), genAiQueue);
    }

    private void validate(OcrMessage message) {
        if (message == null || message.documentId() == null) {
            throw new OcrProcessingException("OCR job has no document id");
        }

        if (message.filename() == null || message.filename().isBlank()) {
            throw new OcrProcessingException(
                    "OCR job for document " + message.documentId() + " has no filename");
        }

        if (message.objectKey() == null || message.objectKey().isBlank()) {
            throw new OcrProcessingException(
                    "OCR job for document " + message.documentId() + " has no RustFS object key");
        }
    }
}