package com.naengpa.naengpamasterbackend.global.s3;

import com.naengpa.naengpamasterbackend.global.exception.ReceiptImageUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

// 업로드/삭제 전용 wrapper
@Component
public class S3Uploader {

    private final S3Client s3Client;
    private final String bucket;

    public S3Uploader(
            S3Client s3Client,
            @Value("${aws.s3.bucket}") String bucket
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    public String upload(MultipartFile file, String objectKey) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            return objectKey;
        } catch (IOException exception) {
            throw new ReceiptImageUploadException("영수증 이미지 파일을 읽을 수 없습니다.");
        } catch (RuntimeException exception) {
            throw new ReceiptImageUploadException("영수증 이미지 업로드에 실패했습니다.");
        }
    }

    public void delete(String objectKey) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        s3Client.deleteObject(request);
    }
}
