package at.fhtw.swen3.dto;

/**
 * PDF-Datei eines Dokuments mit dem Dateinamen für den Browser.
 */
public record DocumentFile(String filename, byte[] data) {
}
