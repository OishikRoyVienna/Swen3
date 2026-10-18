package at.fhtw.swen3.ocr.service;

import at.fhtw.swen3.ocr.exception.OcrProcessingException;
import at.fhtw.swen3.ocr.messaging.OcrMessage;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OcrServiceTest {

    private final OcrService ocrService = new OcrService();

    @Test
    void process_acceptsValidJob() {
        OcrMessage message = new OcrMessage(1L, "invoice.pdf", "application/pdf", LocalDateTime.now());

        assertThatCode(() -> ocrService.process(message)).doesNotThrowAnyException();
    }

    @Test
    void process_throws_whenDocumentIdMissing() {
        OcrMessage message = new OcrMessage(null, "invoice.pdf", "application/pdf", LocalDateTime.now());

        assertThatThrownBy(() -> ocrService.process(message))
                .isInstanceOf(OcrProcessingException.class)
                .hasMessageContaining("no document id");
    }

    @Test
    void process_throws_whenFilenameBlank() {
        OcrMessage message = new OcrMessage(3L, " ", "application/pdf", LocalDateTime.now());

        assertThatThrownBy(() -> ocrService.process(message))
                .isInstanceOf(OcrProcessingException.class)
                .hasMessageContaining("no filename");
    }
}
