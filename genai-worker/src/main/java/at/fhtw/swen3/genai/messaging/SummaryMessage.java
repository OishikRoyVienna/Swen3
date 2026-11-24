package at.fhtw.swen3.genai.messaging;

/**
 * Ergebnis an das Backend: Zusammenfassung eines Dokuments.
 */
public record SummaryMessage(Long documentId, String summary) {
}
