package at.fhtw.swen3.ocr.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Service
public class RustFsStorageService {

    private final S3Client s3Client;
    private final String bucket;

    public RustFsStorageService(
            S3Client s3Client,
            @Value("${paperless.storage.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    public byte[] download(String objectKey) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        return s3Client.getObjectAsBytes(request).asByteArray();
    }
}
