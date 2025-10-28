package com.ssairen.domain.file.service;

import com.ssairen.config.MinioProperties;
import com.ssairen.domain.file.dto.FileUploadResponse;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 파일 스토리지 Service 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    /**
     * 파일 업로드
     * - 파일명을 UUID로 생성하여 중복 방지
     * - 원본 파일의 확장자를 유지
     *
     * @param file 업로드할 파일
     * @return 업로드된 파일 정보
     */
    @Override
    public FileUploadResponse uploadFile(MultipartFile file) {
        // 빈 파일 체크
        if (file.isEmpty()) {
            log.error("빈 파일 업로드 시도");
            throw new CustomException(ErrorCode.EMPTY_FILE);
        }

        try {
            // 원본 파일명과 확장자 추출
            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            // UUID 기반 고유 파일명 생성
            String fileName = UUID.randomUUID().toString() + extension;
            String bucketName = minioProperties.getBucketName();

            log.info("파일 업로드 시작 - 원본명: {}, 저장명: {}, 크기: {} bytes",
                    originalFileName, fileName, file.getSize());

            // MinIO에 파일 업로드
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            log.info("파일 업로드 완료 - 파일명: {}", fileName);

            // 파일 URL 생성
            String fileUrl = getFileUrl(fileName);

            return FileUploadResponse.builder()
                    .fileName(fileName)
                    .originalFileName(originalFileName)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .bucketName(bucketName)
                    .fileUrl(fileUrl)
                    .build();

        } catch (Exception e) {
            log.error("파일 업로드 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 파일 삭제
     *
     * @param fileName 삭제할 파일명
     */
    @Override
    public void deleteFile(String fileName) {
        try {
            String bucketName = minioProperties.getBucketName();
            log.info("파일 삭제 시작 - 버킷: {}, 파일명: {}", bucketName, fileName);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );

            log.info("파일 삭제 완료 - 파일명: {}", fileName);

        } catch (Exception e) {
            log.error("파일 삭제 실패 - 파일명: {}, 에러: {}", fileName, e.getMessage(), e);
            throw new CustomException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    /**
     * 파일 접근 URL 조회
     * - 7일간 유효한 Presigned URL 생성
     *
     * @param fileName 조회할 파일명
     * @return 파일 접근 URL
     */
    @Override
    public String getFileUrl(String fileName) {
        try {
            String bucketName = minioProperties.getBucketName();

            // 7일간 유효한 Presigned URL 생성
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );

            log.debug("파일 URL 생성 - 파일명: {}", fileName);
            return url;

        } catch (Exception e) {
            log.error("파일 URL 조회 실패 - 파일명: {}, 에러: {}", fileName, e.getMessage(), e);
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }
    }
}
