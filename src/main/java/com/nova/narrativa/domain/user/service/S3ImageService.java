package com.nova.narrativa.domain.user.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import com.nova.narrativa.domain.user.error.S3CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3ImageService {

    private final AmazonS3 s3ImgClient;

    @Value("${aws.s3.images-storage-buckets}")
    private String bucketName;

    private String PROFILE_IMG_DIR = "profile/";
    private String SURVIVAL_IMG_DIR = "survival_images/";

    public String upload(MultipartFile image) {
        if(image.isEmpty() || Objects.isNull(image.getOriginalFilename())){
            throw new S3CustomException("빈 파일 입니다.");
        }
        return this.uploadImage(image);
    }

    private String uploadImage(MultipartFile image) throws S3Exception {
        this.validateImageFileExtention(image.getOriginalFilename());
        try {
            return this.uploadImageToS3(image);
        } catch (IOException e) {
            throw new S3CustomException("IMG 업로드 에러");
        }
    }

    private void validateImageFileExtention(String filename) throws S3Exception {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new S3CustomException("파일 확장자가 존재하지 않습니다.");
        }

        String extention = filename.substring(lastDotIndex + 1).toLowerCase();
        List<String> allowedExtentionList = Arrays.asList("jpg", "jpeg", "png", "gif");

        if (!allowedExtentionList.contains(extention)) {
            throw new S3CustomException("유효하지 않은 이미지 파일 확장자명 입니다.");
        }
    }

    private String uploadImageToS3(MultipartFile image) throws IOException, S3Exception {
        String originalFilename = image.getOriginalFilename(); //원본 파일 명
        String extention = originalFilename.substring(originalFilename.lastIndexOf(".")); //확장자 명

        String s3FileName = PROFILE_IMG_DIR + "/" + UUID.randomUUID().toString().substring(0, 10) + originalFilename; //변경된 파일 명
        log.info("s3FileName: {}", s3FileName);

        InputStream is = image.getInputStream();
        byte[] bytes = IOUtils.toByteArray(is);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/" + extention);
        metadata.setContentLength(bytes.length);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        try{
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(bucketName, s3FileName, byteArrayInputStream, metadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead);
            s3ImgClient.putObject(putObjectRequest); // put image to S3
        }catch (Exception e){
            log.info("exception: {}", e.toString());
            throw new S3CustomException("IMG S3 전송 실패");
        }finally {
            byteArrayInputStream.close();
            is.close();
        }

        return s3ImgClient.getUrl(bucketName, s3FileName).toString();
    }

    public void deleteImageFromS3(String imageAddress) throws S3Exception {
        String key = getKeyFromImageAddress(imageAddress);
        try{
            s3ImgClient.deleteObject(new DeleteObjectRequest(bucketName, key));
        }catch (Exception e){
            throw new S3CustomException("IMG 삭제 기타 에러");
        }
    }

    private String getKeyFromImageAddress(String imageAddress){
        try{
            URL url = new URL(imageAddress);
            String decodingKey = URLDecoder.decode(url.getPath(), "UTF-8");
            return decodingKey.substring(1); // 맨 앞의 '/' 제거
        }catch (MalformedURLException | UnsupportedEncodingException e){
            throw new S3CustomException("IMG 삭제 잘못된 URL 경로 or 인코딩 에러");
        }
    }

//    public String testUpload(MultipartFile image) throws IOException {
//        String originalFilename = image.getOriginalFilename();
////        String fileName = changeFileName(originalFilename);
//
//        String fileName = PROFILE_IMG_DIR + "/" + UUID.randomUUID().toString().substring(0, 10) + originalFilename;
//        log.info("fileName: {}", fileName);
//        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentType(image.getContentType());
//        metadata.setContentLength(image.getSize());
//
//        s3ImgClient.putObject(bucketName, fileName, image.getInputStream(), metadata);
//
//        return s3ImgClient.getUrl(bucketName, fileName).toString();
//    }
}
