package at.fhtw.swen3.ocr.messaging;

/**
 * Auftrag an den GenAI-Worker: erkannter Text eines Dokuments, der zusammengefasst werden soll.
 */
public record GenAiMessage(Long documentId, String text) {
}
