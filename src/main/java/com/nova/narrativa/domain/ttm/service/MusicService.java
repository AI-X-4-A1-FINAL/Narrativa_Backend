package com.nova.narrativa.domain.ttm.service;

import com.nova.narrativa.common.exception.NoMusicFileFoundException;
import com.nova.narrativa.domain.ttm.dto.MusicFileDTO;
import com.nova.narrativa.domain.ttm.entity.MusicGeneration;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
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
    private static final Duration ADMIN_PRESIGNED_URL_DURATION = Duration.ofMinutes(30);

    @PreDestroy
    public void closeS3Client() {
//        log.info("Closing S3 client...");
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
//                    log.debug("Checking file: {}", key);

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

//        log.info("Found {} files matching genre '{}'", matchingFiles.size(), genre);
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
//            log.debug("Generated presigned URL for key '{}': {}", key, presignedUrl);
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
//        log.info("Randomly selected file for genre '{}': {}", genre, randomFile);
        return generatePresignedUrl(randomFile);
    }

    /**
     * 관리자용 - 모든 음악 파일 목록 조회
     */
    public List<MusicFileDTO> getAllMusicFiles() {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);

            return response.contents().stream()
                    .map(obj -> {
                        // 파일의 태그 정보 조회
                        GetObjectTaggingResponse taggingResponse = s3Client.getObjectTagging(
                                GetObjectTaggingRequest.builder()
                                        .bucket(bucketName)
                                        .key(obj.key())
                                        .build()
                        );

                        // 파일의 메타데이터 조회
                        HeadObjectResponse metadata = s3Client.headObject(
                                HeadObjectRequest.builder()
                                        .bucket(bucketName)
                                        .key(obj.key())
                                        .build()
                        );

                        String genre = taggingResponse.tagSet().stream()
                                .filter(tag -> "Genre".equalsIgnoreCase(tag.key()))
                                .map(Tag::value)
                                .findFirst()
                                .orElse("Unknown");

                        return new MusicFileDTO(
                                obj.key(),
                                obj.size(),
                                metadata.contentType(),
                                obj.lastModified(),
                                generateAdminPresignedUrl(obj.key()),
                                genre
                        );
                    })
                    .collect(Collectors.toList());
        } catch (S3Exception e) {
            log.error("Error listing music files from S3: ", e);
            throw new RuntimeException("Failed to list music files", e);
        }
    }

    private String getGenreFolderPath(MusicGeneration.Genre genre) {
        return switch (genre) {
            case MYSTERY -> "DetectiveMystery";
            case SIMULATION -> "RaisingSimulation";
            case ROMANCE -> "RomanticFantasy";
            case SURVIVAL -> "SurvivalHorror";
        };
    }

    /**
     * 관리자용 - 음악 파일 업로드
     */
    public MusicFileDTO uploadMusic(MultipartFile file, MusicGeneration.Genre genre) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = Objects.requireNonNull(originalFilename).substring(originalFilename.lastIndexOf("."));

            String formattedGenre = formatGenre(genre.name());
            String folderPath = getGenreFolderPath(genre);

            // 현재 폴더의 파일들을 조회하여 다음 번호 결정
            int nextNumber = getNextFileNumber(folderPath + "/");

            // 최종 파일명 생성 (예: DetectiveMystery/DetectiveMystery_001.mp3)
            String filename = String.format("%s/%s_%03d%s",
                    folderPath, folderPath, nextNumber, extension);

            // 파일 업로드
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 장르 태그 추가
            s3Client.putObjectTagging(PutObjectTaggingRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .tagging(Tagging.builder()
                            .tagSet(Tag.builder()
                                    .key("Genre")
                                    .value(formattedGenre)
                                    .build())
                            .build())
                    .build());

            return new MusicFileDTO(
                    filename,
                    file.getSize(),
                    file.getContentType(),
                    java.time.Instant.now(),
                    generateAdminPresignedUrl(filename),
                    formattedGenre
            );

        } catch (IOException | S3Exception e) {
            log.error("Error uploading music file: ", e);
            throw new RuntimeException("Failed to upload music file", e);
        }
    }

    private String formatGenre(String genre) {
        return genre.charAt(0) + genre.substring(1).toLowerCase();
    }

    private int getNextFileNumber(String folderPath) {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(folderPath)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);

            int maxNumber = 0;
            for (S3Object object : response.contents()) {
                String key = object.key();
                // 파일명에서 숫자 추출 (예: DetectiveMystery_001.mp3 -> 1)
                if (key.matches(".*_\\d{3}\\.[^.]+$")) {
                    int number = Integer.parseInt(key.substring(key.lastIndexOf("_") + 1, key.lastIndexOf(".")));
                    maxNumber = Math.max(maxNumber, number);
                }
            }

            return maxNumber + 1;
        } catch (S3Exception e) {
            log.error("Error getting next file number: ", e);
            throw new RuntimeException("Failed to get next file number", e);
        }
    }

    /**
     * 관리자용 - 음악 파일 삭제
     */
    public void deleteMusic(String filename) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build();

            s3Client.deleteObject(deleteRequest);
//            log.info("Successfully deleted file: {}", filename);
        } catch (S3Exception e) {
            log.error("Error deleting music file: {}", filename, e);
            throw new RuntimeException("Failed to delete music file: " + filename, e);
        }
    }

    /**
     * 관리자용 - 더 긴 유효기간의 Presigned URL 생성
     */
    private String generateAdminPresignedUrl(String key) {
        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(ADMIN_PRESIGNED_URL_DURATION)
                    .getObjectRequest(getRequest -> getRequest.bucket(bucketName).key(key))
                    .build();

            String presignedUrl = s3Presigner.presignGetObject(presignRequest).url().toString();
//            log.debug("Generated admin presigned URL for key '{}': {}", key, presignedUrl);
            return presignedUrl;
        } catch (S3Exception e) {
            log.error("Error generating admin presigned URL for key: {}", key, e);
            throw new RuntimeException("Failed to generate admin presigned URL for file: " + key, e);
        }
    }
}