package at.fhtw.swen3.ocr.service;

import at.fhtw.swen3.ocr.exception.OcrProcessingException;
import at.fhtw.swen3.ocr.messaging.OcrMessage;
import org.junit.jupiter.api.Test;
import at.fhtw.swen3.ocr.search.OcrDocumentRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OcrServiceTest {

    private final RustFsStorageService rustFsStorageService =
            mock(RustFsStorageService.class);

    private final TesseractOcrService tesseractOcrService =
            mock(TesseractOcrService.class);

    private final OcrDocumentRepository ocrDocumentRepository =
            mock(OcrDocumentRepository.class);

    private final OcrService ocrService =
            new OcrService(
                    rustFsStorageService,
                    tesseractOcrService,
                    ocrDocumentRepository
            );

    @Test
    void process_acceptsValidJob() {
        OcrMessage message = new OcrMessage(
                1L, "invoice.pdf", "application/pdf",
                LocalDateTime.now(), "documents/invoice.pdf"
        );

        byte[] pdfBytes = new byte[]{1, 2, 3};

        when(rustFsStorageService.download("documents/invoice.pdf"))
                .thenReturn(pdfBytes);

        when(tesseractOcrService.recognize(pdfBytes))
                .thenReturn("Hello World");

        assertThatCode(() -> ocrService.process(message))
                .doesNotThrowAnyException();

        verify(rustFsStorageService).download("documents/invoice.pdf");
        verify(tesseractOcrService).recognize(pdfBytes);
    }

    @Test
    void process_throws_whenDocumentIdMissing() {
        OcrMessage message = new OcrMessage(
                null, "invoice.pdf", "application/pdf",
                LocalDateTime.now(), "documents/invoice.pdf"
        );

        assertThatThrownBy(() -> ocrService.process(message))
                .isInstanceOf(OcrProcessingException.class)
                .hasMessageContaining("no document id");

        verifyNoInteractions(rustFsStorageService, tesseractOcrService);
    }

    @Test
    void process_throws_whenFilenameBlank() {
        OcrMessage message = new OcrMessage(
                3L, " ", "application/pdf",
                LocalDateTime.now(), "documents/invoice.pdf"
        );

        assertThatThrownBy(() -> ocrService.process(message))
                .isInstanceOf(OcrProcessingException.class)
                .hasMessageContaining("no filename");

        verifyNoInteractions(rustFsStorageService, tesseractOcrService);
    }

    @Test
    void process_throws_whenObjectKeyMissing() {
        OcrMessage message = new OcrMessage(
                4L, "invoice.pdf", "application/pdf",
                LocalDateTime.now(), null
        );

        assertThatThrownBy(() -> ocrService.process(message))
                .isInstanceOf(OcrProcessingException.class)
                .hasMessageContaining("no RustFS object key");

        verifyNoInteractions(rustFsStorageService, tesseractOcrService);
    }
}