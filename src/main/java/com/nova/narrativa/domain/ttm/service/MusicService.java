package com.nova.narrativa.domain.ttm.service;

import com.nova.narrativa.common.exception.NoMusicFileFoundException;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MusicService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.music-bucket}")
    private String bucketName;

    private static final Duration PRESIGNED_URL_DURATION = Duration.ofMinutes(10);

    @PreDestroy
    public void closeS3Client() {
        log.info("Closing S3 client...");
        s3Client.close();
    }

    /**
     * Fetch files with a specific genre tag from S3.
     *
     * @param genre The genre tag to filter files by.
     * @return A list of S3 object keys matching the specified genre.
     */
    public List<String> getFilesByGenre(String genre) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix("") // Search the entire bucket
                .build();

        ListObjectsV2Response response;
        try {
            response = s3Client.listObjectsV2(request);
        } catch (S3Exception e) {
            log.error("Failed to list objects from S3 bucket: {}", bucketName, e);
            throw new RuntimeException("Error listing files from S3 bucket: " + bucketName, e);
        }

        List<String> matchingFiles = response.contents().stream()
                .filter(s3Object -> {
                    String key = s3Object.key();
                    log.debug("Checking file: {}", key);

                    try {
                        GetObjectTaggingResponse taggingResponse = s3Client.getObjectTagging(
                                GetObjectTaggingRequest.builder()
                                        .bucket(bucketName)
                                        .key(key)
                                        .build()
                        );

                        boolean isMatchingGenre = taggingResponse.tagSet().stream()
                                .anyMatch(tag -> "Genre".equalsIgnoreCase(tag.key()) &&
                                        genre.trim().equalsIgnoreCase(tag.value().trim()));

                        if (isMatchingGenre) {
                            log.debug("File matched for genre '{}': {}", genre, key);
                        }

                        return isMatchingGenre;

                    } catch (S3Exception e) {
                        log.error("Error fetching tags for file: {}", key, e);
                        return false;
                    }
                })
                .map(S3Object::key)
                .collect(Collectors.toList());

        log.info("Found {} files matching genre '{}'", matchingFiles.size(), genre);
        return matchingFiles;
    }

    /**
     * Generate a presigned URL for a specific S3 object key.
     *
     * @param key The S3 object key.
     * @return The presigned URL.
     */
    public String generatePresignedUrl(String key) {
        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(PRESIGNED_URL_DURATION)
                    .getObjectRequest(getRequest -> getRequest.bucket(bucketName).key(key))
                    .build();

            String presignedUrl = s3Presigner.presignGetObject(presignRequest).url().toString();
            log.debug("Generated presigned URL for key '{}': {}", key, presignedUrl);
            return presignedUrl;
        } catch (S3Exception e) {
            log.error("Error generating presigned URL for key: {}", key, e);
            throw new RuntimeException("Failed to generate presigned URL for file: " + key, e);
        }
    }

    /**
     * Fetch a random file for a specific genre and return its presigned URL.
     *
     * @param genre The genre tag to filter files by.
     * @return A presigned URL of a random file matching the genre.
     */
    public String getRandomFileByGenre(String genre) {
        List<String> files = getFilesByGenre(genre);

        if (files.isEmpty()) {
            log.warn("No files found for genre '{}'", genre);
            throw new NoMusicFileFoundException(genre);
        }

        String randomFile = files.get(new Random().nextInt(files.size()));
        log.info("Randomly selected file for genre '{}': {}", genre, randomFile);
        return generatePresignedUrl(randomFile);
    }
}
