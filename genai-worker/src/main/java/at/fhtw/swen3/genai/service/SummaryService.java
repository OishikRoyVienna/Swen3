package at.fhtw.swen3.genai.service;

import at.fhtw.swen3.genai.exception.GenAiException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.stereotype.Service;

/**
 * Erstellt mit Google Gemini (über Spring AI) eine kurze Zusammenfassung eines Dokumenttexts.
 */
@Service
public class SummaryService {

    // Begrenzt die Eingabe, damit sehr lange Dokumente nicht unnötig viele Tokens kosten
    static final int MAX_INPUT_CHARS = 20_000;

    private final ChatClient chatClient;

    public SummaryService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("Du fasst Dokumente zusammen. Antworte auf Deutsch in höchstens fünf Sätzen, "
                        + "sachlich und ohne Einleitung. Nenne wichtige Angaben wie Beträge, Daten und Absender.")
                .build();
    }

    public String summarize(String text) {
        String input = text.length() > MAX_INPUT_CHARS ? text.substring(0, MAX_INPUT_CHARS) : text;
        String summary;
        try {
            // kurzzeitige Fehler (Netzwerk, 429, 5xx) wiederholt Spring AI selbst, siehe spring.ai.retry.*
            summary = chatClient.prompt().user(input).call().content();
        } catch (RuntimeException e) {
            // die eigentliche Ursache (z. B. "API key not valid") steckt meist eine Ebene tiefer
            throw new GenAiException("Gemini request failed: " + NestedExceptionUtils.getMostSpecificCause(e).getMessage(), e);
        }
        if (summary == null || summary.isBlank()) {
            throw new GenAiException("Gemini returned an empty summary");
        }
        return summary;
    }
}
