package at.fhtw.swen3.ocr.messaging;

import at.fhtw.swen3.ocr.exception.OcrProcessingException;
import at.fhtw.swen3.ocr.service.OcrService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OcrMessageListenerTest {

    @Mock
    private OcrService ocrService;

    @InjectMocks
    private OcrMessageListener listener;

    private final OcrMessage message = new OcrMessage(1L, "invoice.pdf", "application/pdf", LocalDateTime.now());

    @Test
    void onOcrJob_delegatesToService() {
        listener.onOcrJob(message);

        verify(ocrService).process(message);
    }

    @Test
    void onOcrJob_rejectsWithoutRequeue_whenProcessingFails() {
        doThrow(new OcrProcessingException("broken")).when(ocrService).process(message);

        assertThatThrownBy(() -> listener.onOcrJob(message))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class)
                .hasCauseInstanceOf(OcrProcessingException.class);
    }
}
