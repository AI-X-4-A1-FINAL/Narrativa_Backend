package com.nova.narrativa.domain.ttm.service;

import com.nova.narrativa.common.exception.NoMusicFileFoundException;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class MusicService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;


    @PreDestroy
    public void closeS3Client() {
        s3Client.close();
    }

    // S3에서 특정 태그를 가진 파일 가져오기
    public List<String> getFilesByGenre(String genre) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix("") // 전체 버킷에서 검색
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        List<String> matchingFiles = new ArrayList<>();

        for (S3Object s3Object : response.contents()) {
            String key = s3Object.key();

            System.out.println("Checking file: " + key);

            GetObjectTaggingRequest taggingRequest = GetObjectTaggingRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectTaggingResponse taggingResponse = s3Client.getObjectTagging(taggingRequest);

            taggingResponse.tagSet().forEach(tag ->
                    System.out.println("Key: " + tag.key() + ", Value: " + tag.value())
            );

            boolean isMatchingGenre = taggingResponse.tagSet().stream()
                    .anyMatch(tag -> tag.key().equals("Genre") && tag.value().trim().equalsIgnoreCase(genre.trim()));

            if (isMatchingGenre) {
                System.out.println("Adding file to matchingFiles: " + key);
                matchingFiles.add(key);
            }
        }


        return matchingFiles;
    }

    // Presigned URL 생성
    public String generatePresignedUrl(String key) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10)) // URL 유효시간
                .getObjectRequest(getRequest -> getRequest.bucket(bucketName).key(key))
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    // 특정 장르에서 랜덤 파일 선택
    public String getRandomFileByGenre(String genre) {
        List<String> files = getFilesByGenre(genre);

        if (files.isEmpty()) {
            throw new NoMusicFileFoundException(genre);
        }

        String randomFile = files.get(new Random().nextInt(files.size()));
        return generatePresignedUrl(randomFile);
    }

}
