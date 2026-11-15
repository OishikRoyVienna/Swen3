
package at.fhtw.swen3.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;


@Service
public class RustFsStorageService {

    private final S3Client s3Client;
    private final String bucketName;
    private volatile boolean bucketReady;

    public RustFsStorageService(
            S3Client s3Client,
            @Value("${paperless.storage.bucket}") String bucketName
    ) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public void upload(String objectKey, byte[] data) {
        ensureBucketExists();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType("application/pdf")
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromBytes(data)
        );
    }

    public byte[] download(String objectKey) {

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        return s3Client.getObjectAsBytes(request)
                .asByteArray();
    }

    // Auf einer frischen Installation gibt es den Bucket noch nicht: beim ersten Upload anlegen
    private void ensureBucketExists() {
        if (bucketReady) {
            return;
        }
        try {
            s3Client.headBucket(b -> b.bucket(bucketName));
        } catch (S3Exception e) {
            if (e.statusCode() != 404) {
                throw e;
            }
            s3Client.createBucket(b -> b.bucket(bucketName));
        }
        bucketReady = true;
    }
}
