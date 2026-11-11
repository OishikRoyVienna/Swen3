package at.fhtw.swen3.ocr.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

@Service
public class TesseractOcrService {

    public String recognize(byte[] pdfBytes) {
        Path tempDir = null;

        try {
            tempDir = Files.createTempDirectory("ocr-");

            Path pdfFile = tempDir.resolve("document.pdf");
            Files.write(pdfFile, pdfBytes);

            // PDF-Seiten mit Ghostscript in Bilder umwandeln
            runCommand(
                    "gs",
                    "-dSAFER",
                    "-dBATCH",
                    "-dNOPAUSE",
                    "-sDEVICE=pnggray",
                    "-r300",
                    "-sOutputFile=" + tempDir.resolve("page-%03d.png"),
                    pdfFile.toString()
            );

            StringBuilder result = new StringBuilder();

            // Jede Seite mit Tesseract auslesen
            try (Stream<Path> files = Files.list(tempDir)) {
                for (Path image : files
                        .filter(p -> p.getFileName().toString().endsWith(".png"))
                        .sorted()
                        .toList()) {

                    String text = runCommand(
                            "tesseract",
                            image.toString(),
                            "stdout",
                            "-l",
                            "eng"
                    );

                    result.append(text).append("\n");
                }
            }

            return result.toString().trim();

        } catch (IOException e) {
            throw new IllegalStateException("OCR processing failed", e);

        } finally {
            if (tempDir != null) {
                deleteDirectory(tempDir);
            }
        }
    }

    private String runCommand(String... command) throws IOException {
        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();

            String output;
            try (var input = process.getInputStream()) {
                output = new String(
                        input.readAllBytes(),
                        StandardCharsets.UTF_8
                );
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IOException(
                        "Command failed: " + String.join(" ", command)
                                + "\n" + output
                );
            }

            return output;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("OCR command interrupted", e);
        }
    }

    private void deleteDirectory(Path directory) {
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths
                    .sorted(Comparator.reverseOrder())
                    .toList()) {
                Files.deleteIfExists(path);
            }
        } catch (IOException ignored) {
            // Temporäre Dateien bestmöglich entfernen
        }
    }
}