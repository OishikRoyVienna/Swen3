
package at.fhtw.swen3.service;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class RustFsStorageServiceIntegrationTest {

    @Test
    void uploadAndDownloadShouldReturnSameBytes() {

        // Verbindung zum lokal laufenden RustFS
        try (S3Client s3Client = S3Client.builder()
                .endpointOverride(URI.create("http://localhost:9000"))
                .region(Region.US_EAST_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        "rustfsadmin",
                                        "rustfsadmin"
                                )
                        )
                )
                .forcePathStyle(true)
                .build()) {

            // Bucket bei Bedarf für den Integrationstest erstellen
            boolean bucketExists = s3Client.listBuckets()
                    .buckets()
                    .stream()
                    .anyMatch(b -> b.name().equals("paperless-documents"));

            if (!bucketExists) {
                s3Client.createBucket(b -> b.bucket("paperless-documents"));
            }

            // Unser bereits implementierter Service
            RustFsStorageService storageService =
                    new RustFsStorageService(
                            s3Client,
                            "paperless-documents"
                    );

            // Eindeutiger Dateiname für jeden Testlauf
            String objectKey =
                    "test/" + UUID.randomUUID() + ".pdf";

            // Testdaten
            byte[] original =
                    "Hello from SWEN3!".getBytes(
                            StandardCharsets.UTF_8
                    );

            // 1. Hochladen
            storageService.upload(objectKey, original);

            // 2. Herunterladen
            byte[] downloaded =
                    storageService.download(objectKey);

            // 3. Vergleichen
            assertArrayEquals(original, downloaded);
        }
    }
}
