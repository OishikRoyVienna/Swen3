package at.fhtw.swen3.messaging;

/**
 * Ergebnis vom GenAI-Worker: Zusammenfassung eines Dokuments (gleiche JSON-Felder wie dort).
 */
public record SummaryMessage(Long documentId, String summary) {
}
