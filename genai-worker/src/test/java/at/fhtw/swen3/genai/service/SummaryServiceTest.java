package at.fhtw.swen3.genai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;

import at.fhtw.swen3.genai.exception.GenAiException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SummaryServiceTest {

    // Deep Stubs: die Kette prompt().user(..).call().content() wird gemockt, Gemini wird nie aufgerufen
    private final ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
    private SummaryService summaryService;

    @BeforeEach
    void setUp() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class, RETURNS_SELF);
        when(builder.build()).thenReturn(chatClient);
        summaryService = new SummaryService(builder);
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn("Kurze Zusammenfassung");
    }

    @Test
    void summarize_returnsGeminiAnswer() {
        assertThat(summaryService.summarize("Rechnung 4711 über 249 Euro")).isEqualTo("Kurze Zusammenfassung");
    }

    @Test
    void summarize_truncatesVeryLongText() {
        summaryService.summarize("a".repeat(SummaryService.MAX_INPUT_CHARS + 500));

        ArgumentCaptor<String> sent = ArgumentCaptor.forClass(String.class);
        verify(chatClient.prompt(), atLeastOnce()).user(sent.capture());
        assertThat(sent.getValue()).hasSize(SummaryService.MAX_INPUT_CHARS);
    }

    @Test
    void summarize_wrapsGeminiError_inGenAiException() {
        when(chatClient.prompt().user(anyString()).call().content())
                .thenThrow(new RuntimeException("400 INVALID_ARGUMENT. API key not valid"));

        assertThatThrownBy(() -> summaryService.summarize("Rechnung"))
                .isInstanceOf(GenAiException.class)
                .hasMessageContaining("API key not valid");
    }

    @Test
    void summarize_throws_whenGeminiAnswerIsEmpty() {
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn("  ");

        assertThatThrownBy(() -> summaryService.summarize("Rechnung"))
                .isInstanceOf(GenAiException.class);
    }
}
