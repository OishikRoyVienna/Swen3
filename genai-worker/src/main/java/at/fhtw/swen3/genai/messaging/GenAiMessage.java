package at.fhtw.swen3.genai.messaging;

/**
 * Auftrag vom OCR-Worker: erkannter Text eines Dokuments (gleiche JSON-Felder wie dort).
 */
public record GenAiMessage(Long documentId, String text) {
}
