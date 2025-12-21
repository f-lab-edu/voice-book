package com.sj.voicebook.member.service.impl;

import com.sj.voicebook.member.service.ImageService;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ImageService implements ImageService {

    private final S3Template s3Template; // Spring Cloud AWS가 제공하는 편리한 업로더
    private final S3Presigner s3Presigner; // 우리가 만든 URL 발급기

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    // 1. 이미지 업로드 (S3에 저장 후 파일명 반환)
    @Override
    public String upload(MultipartFile image) {
        // 파일 이름이 겹치지 않게 UUID 사용 (예: uuid_profile.jpg)
        String originalFilename = image.getOriginalFilename();
        String key = UUID.randomUUID() + "_" + originalFilename;

        try (InputStream inputStream = image.getInputStream()) {
            // S3Template을 이용해 아주 간단하게 업로드
            s3Template.upload(bucketName, key, inputStream);

            // ★ 중요: DB에는 긴 URL이 아니라 이 'key(파일명)'만 저장합니다.
            return key;
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }

    // 2. 조회용 URL 발급 (파일명 -> 임시 URL 변환)
    @Override
    public String getImageUrl(String key) {
        if (key == null) return null;

        // "이 버킷의, 이 파일(key)을, GET(조회) 하겠다"는 요청 생성
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        // "위 요청을 10분 동안만 허가하는 서명(URL)" 생성
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10)) // 유효시간 10분
                .getObjectRequest(getObjectRequest)
                .build();

        // 최종 URL 반환
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }
}
