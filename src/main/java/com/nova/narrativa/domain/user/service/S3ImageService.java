package com.nova.narrativa.domain.user.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.nova.narrativa.domain.user.error.S3CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ImageService {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${aws.s3.images-bucket}")
    private String bucketName;

    @Value("${aws.s3.profile-img-dir:profile}")
    private String profileImgDir;

    /**
     * 이미지 업로드
     */
    public String upload(MultipartFile image) {
        if (image.isEmpty() || Objects.isNull(image.getOriginalFilename())) {
            throw new IllegalArgumentException("빈 파일입니다.");
        }
        return uploadImage(image);
    }

    /**
     * 파일 확장자 유효성 검사 및 S3 업로드 처리
     */
    private String uploadImage(MultipartFile image) {
        validateImageFileExtension(image.getOriginalFilename());
        try {
            return uploadImageToS3(image);
        } catch (IOException e) {
            log.error("IMG 업로드 에러", e);
            throw new RuntimeException("IMG 업로드 에러");
        }
    }

    /**
     * 이미지 파일 확장자 유효성 검사
     */
    private void validateImageFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new IllegalArgumentException("파일 확장자가 존재하지 않습니다.");
        }

        String extension = filename.substring(lastDotIndex + 1).toLowerCase();
        List<String> allowedExtensionList = Arrays.asList("jpg", "jpeg", "png", "gif");

        if (!allowedExtensionList.contains(extension)) {
            throw new IllegalArgumentException("유효하지 않은 이미지 파일 확장자명 입니다.");
        }
    }

    /**
     * S3에 이미지 업로드
     */
    private String uploadImageToS3(MultipartFile image) throws IOException {
        String originalFilename = image.getOriginalFilename();
        String s3FileName = profileImgDir + "/" + UUID.randomUUID().toString().substring(0, 10) + originalFilename;

        log.info("Uploading file to S3: {}", s3FileName);

        byte[] bytes = image.getBytes();

        // 업로드 요청 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3FileName)
                .contentType(image.getContentType())
                .build();


        // 동기 업로드
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));

        // 업로드된 S3 파일 URL 반환
        return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(s3FileName)).toExternalForm();
    }

    /**
     * S3에서 이미지 삭제
     */
    public void deleteImageFromS3(String imageAddress) {
        String key = getKeyFromImageAddress(imageAddress);
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            log.error("IMG 삭제 에러", e);
            throw new RuntimeException("IMG 삭제 에러");
        }
    }

    /**
     * 이미지 주소로부터 S3 키 추출
     */
    private String getKeyFromImageAddress(String imageAddress) {
        try {
            URL url = new URL(imageAddress);
            String decodedKey = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8);

            if (decodedKey.startsWith("/")) {
                decodedKey = decodedKey.substring(1);
            }

            log.info("Decoded S3 key: {}", decodedKey);
            return decodedKey;
        } catch (Exception e) {
            log.error("URL decoding error: {}", imageAddress, e);
            throw new RuntimeException("URL decoding 에러: " + imageAddress);
        }
    }

    // AWS S3 저장 후, 응답받은 Presigned URL을 이용하여 해당 img 접근 가능 url 가져오기
    public String getS3ImgByPresignedUrl(String filename) {
        String imgUrl = "";
        try {
            // S3 버킷에서 객체 목록 가져오기
            ListObjectsV2Response listObjects = s3Client.listObjectsV2(builder -> builder.bucket(bucketName));

            listObjects.contents().forEach(s3Object -> {
                String key = s3Object.key();
                String presignedUrl = generatePresignedUrl(key);
            });

            log.info("S3 이미지 URL 목록 생성 완료");
        } catch (Exception e) {
            log.error("S3 객체 목록 조회 실패", e);
            throw new S3CustomException("S3 객체 목록 조회 실패");
        }

        return imgUrl;

    }

    // 이미지를 가져오는 메서드 추가
    public Map<String, String> getAllPresignedImageUrls() {
        Map<String, String> imageUrls = new HashMap<>();

        try {
            // S3 버킷에서 객체 목록 가져오기
            ListObjectsV2Response listObjects = s3Client.listObjectsV2(builder -> builder.bucket(bucketName));

            listObjects.contents().forEach(s3Object -> {
                String key = s3Object.key();
                String presignedUrl = generatePresignedUrl(key);
                imageUrls.put(key, presignedUrl);
            });

            log.info("S3 이미지 URL 목록 생성 완료");
        } catch (Exception e) {
            log.error("S3 객체 목록 조회 실패", e);
            throw new S3CustomException("S3 객체 목록 조회 실패");
        }

        return imageUrls;
    }

    /**
     * Presigned URL 생성
     */
    private String generatePresignedUrl(String key) {
        try {
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(builder ->
                    builder.getObjectRequest(getObjectBuilder -> getObjectBuilder.bucket(bucketName).key(key))
                            .signatureDuration(Duration.ofMinutes(10))
            );

            return presignedRequest.url().toString();
        } catch (Exception e) {
            log.error("Presigned URL 생성 실패: {}", key, e);
            throw new S3CustomException("Presigned URL 생성 실패: " + key);
        }
    }

    /**
     * 주어진 S3 버킷명, 폴더명, 파일명으로 S3에서 파일을 조회할 수 있는 Presigned URL을 생성하는 메서드
     */
    public String getPresignedUrl(String filePath) {

        try {
            // Presigned URL 요청을 생성
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();

            // Presigned URL을 생성 (유효 기간은 60분으로 설정)
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presigner ->
                    presigner.getObjectRequest(getObjectRequest)
                            .signatureDuration(Duration.ofMinutes(60)) // URL 유효 시간 설정
            );

            // 생성된 Presigned URL 반환
            URL presignedUrl = presignedRequest.url();
            log.info("Presigned URL 생성 성공: {}", presignedUrl);
            return presignedUrl.toString();
        } catch (Exception e) {
            log.error("Presigned URL 생성 실패", e);
            throw new RuntimeException("S3에서 Presigned URL을 생성할 수 없습니다.");
        }
    }
}