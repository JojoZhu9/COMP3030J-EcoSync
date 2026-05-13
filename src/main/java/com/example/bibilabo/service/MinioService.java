package com.example.bibilabo.service;

import io.minio.*;
import io.minio.messages.Bucket;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class MinioService {

    private static final Logger log = LoggerFactory.getLogger(MinioService.class);

    @Value("${app.minio.endpoint}")
    private String endpoint;

    @Value("${app.minio.access-key}")
    private String accessKey;

    @Value("${app.minio.secret-key}")
    private String secretKey;

    @Value("${app.minio.bucket}")
    private String bucket;

    @Value("${app.upload.dir:./uploads/products}")
    private String uploadDir;

    private MinioClient client;

    @PostConstruct
    public void init() {
        client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        try {
            boolean found = false;
            for (Bucket b : client.listBuckets()) {
                if (bucket.equals(b.name())) { found = true; break; }
            }
            if (!found) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("MinIO bucket '{}' created", bucket);
            }

            String policy = """
                    {
                      "Version": "2012-10-17",
                      "Statement": [
                        {
                          "Effect": "Allow",
                          "Principal": {"AWS": ["*"]},
                          "Action": ["s3:GetObject"],
                          "Resource": ["arn:aws:s3:::%s/*"]
                        }
                      ]
                    }
                    """.formatted(bucket);
            client.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucket).config(policy).build());

            seedImages();
        } catch (Exception e) {
            log.warn("MinIO init failed (will retry on first upload): {}", e.getMessage());
        }
    }

    private void seedImages() {
        Path dir = Paths.get(uploadDir);
        if (!Files.isDirectory(dir)) return;

        try (var stream = Files.list(dir)) {
            stream.filter(f -> f.toString().endsWith(".jpg")).forEach(f -> {
                String key = "products/" + f.getFileName().toString();
                try {
                    client.statObject(StatObjectArgs.builder().bucket(bucket).object(key).build());
                    // already exists, skip
                } catch (Exception e) {
                    try (InputStream in = Files.newInputStream(f)) {
                        client.putObject(PutObjectArgs.builder()
                                .bucket(bucket).object(key)
                                .stream(in, Files.size(f), -1)
                                .contentType("image/jpeg")
                                .build());
                        log.info("Seeded: {}", key);
                    } catch (Exception ex) {
                        log.warn("Failed to seed {}: {}", f.getFileName(), ex.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            log.warn("Seed scan failed: {}", e.getMessage());
        }
    }

    public String upload(MultipartFile file, String filename) throws Exception {
        ensureBucket();
        String key = "products/" + filename;
        client.putObject(PutObjectArgs.builder()
                .bucket(bucket).object(key)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());
        return filename;
    }

    private void ensureBucket() {
        try {
            boolean found = false;
            for (Bucket b : client.listBuckets()) {
                if (bucket.equals(b.name())) { found = true; break; }
            }
            if (!found) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("MinIO unavailable", e);
        }
    }
}
