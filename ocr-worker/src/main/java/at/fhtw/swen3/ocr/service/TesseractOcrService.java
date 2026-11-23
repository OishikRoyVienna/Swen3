package at.fhtw.swen3.ocr.service;

import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Service
public class TesseractOcrService {

    public String recognize(byte[] pdfBytes) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("ocr-");
            Path pdfFile = tempDir.resolve("document.pdf");
            Files.write(pdfFile, pdfBytes);

            // PDF-Seiten mit Ghostscript in Bilder umwandeln (300 dpi, Graustufen)
            runCommand("gs", "-dSAFER", "-dBATCH", "-dNOPAUSE", "-sDEVICE=pnggray", "-r300",
                    "-sOutputFile=" + tempDir.resolve("page-%03d.png"), pdfFile.toString());

            // Jede Seite mit Tesseract auslesen
            StringBuilder result = new StringBuilder();
            try (Stream<Path> files = Files.list(tempDir)) {
                for (Path image : files.filter(p -> p.toString().endsWith(".png")).sorted().toList()) {
                    result.append(runCommand("tesseract", image.toString(), "stdout", "-l", "eng")).append("\n");
                }
            }
            return result.toString().trim();
        } catch (IOException e) {
            throw new IllegalStateException("OCR processing failed", e);
        } finally {
            deleteQuietly(tempDir);
        }
    }

    private String runCommand(String... command) throws IOException {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            String output;
            try (var input = process.getInputStream()) {
                output = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
            if (process.waitFor() != 0) {
                throw new IOException("Command failed: " + String.join(" ", command) + "\n" + output);
            }
            return output;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("OCR command interrupted", e);
        }
    }

    // Temporäre Dateien bestmöglich entfernen
    private void deleteQuietly(Path directory) {
        if (directory == null) {
            return;
        }
        try {
            FileSystemUtils.deleteRecursively(directory);
        } catch (IOException ignored) {
            // nicht kritisch
        }
    }
}
