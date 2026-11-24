package at.fhtw.swen3.genai.messaging;

import at.fhtw.swen3.genai.exception.GenAiException;
import at.fhtw.swen3.genai.service.SummaryService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class GenAiMessageListenerTest {

    private final SummaryService summaryService = mock(SummaryService.class);
    private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    private final GenAiMessageListener listener =
            new GenAiMessageListener(summaryService, rabbitTemplate, "paperless.summary");

    @Test
    void onGenAiJob_sendsSummaryToBackend() {
        when(summaryService.summarize("Rechnung 4711")).thenReturn("Zusammenfassung");

        listener.onGenAiJob(new GenAiMessage(1L, "Rechnung 4711"));

        verify(rabbitTemplate).convertAndSend("paperless.summary", new SummaryMessage(1L, "Zusammenfassung"));
    }

    @Test
    void onGenAiJob_skipsEmptyText() {
        listener.onGenAiJob(new GenAiMessage(2L, "  "));

        verifyNoInteractions(summaryService, rabbitTemplate);
    }

    @Test
    void onGenAiJob_rejectsToDeadLetterQueue_whenGeminiFails() {
        when(summaryService.summarize("Rechnung")).thenThrow(new GenAiException("Gemini request failed"));

        assertThatThrownBy(() -> listener.onGenAiJob(new GenAiMessage(3L, "Rechnung")))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
        verifyNoInteractions(rabbitTemplate);
    }
}
