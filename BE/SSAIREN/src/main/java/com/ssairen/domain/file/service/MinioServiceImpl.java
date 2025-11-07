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
import java.util.Arrays;
import java.util.List;
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

    // 지원하는 오디오 파일 확장자
    private static final List<String> AUDIO_EXTENSIONS = Arrays.asList(".wav", ".mp3", ".m4a", ".aac", ".flac");

    // 지원하는 영상 파일 확장자
    private static final List<String> VIDEO_EXTENSIONS = Arrays.asList(".mp4", ".avi", ".mov", ".mkv", ".wmv", ".flv", ".webm");

    /**
     * 오디오 파일 업로드
     */
    @Override
    public FileUploadResponse uploadAudioFile(MultipartFile file) {
        // 파일 유효성 검증
        validateFile(file);
        validateAudioFile(file);

        String bucketName = minioProperties.getBucketName();
        return uploadFile(file, bucketName, "오디오");
    }

    /**
     * 영상 파일 업로드
     */
    @Override
    public FileUploadResponse uploadVideoFile(MultipartFile file) {
        // 파일 유효성 검증
        validateFile(file);
        validateVideoFile(file);

        String bucketName = minioProperties.getVideoBucketName();
        return uploadFile(file, bucketName, "영상");
    }

    /**
     * 파일 업로드 공통 로직
     */
    private FileUploadResponse uploadFile(MultipartFile file, String bucketName, String fileType) {
        try {
            // 원본 파일명과 확장자 추출
            String originalFileName = file.getOriginalFilename();
            String extension = getFileExtension(originalFileName);

            // 파일명 결정: 경로가 포함된 경우(바디캠) 원본명 사용, 아니면 UUID
            String fileName;
            if (originalFileName != null && originalFileName.contains("/")) {
                // 바디캠 영상: 날짜/시간/파일명 구조 유지
                fileName = originalFileName;
                log.info("{} 파일 업로드 시작 (경로 유지) - 원본명: {}, 크기: {} bytes",
                        fileType, originalFileName, file.getSize());
            } else {
                // 일반 파일: UUID 기반 고유 파일명 생성
                fileName = UUID.randomUUID().toString() + extension;
                log.info("{} 파일 업로드 시작 - 원본명: {}, 저장명: {}, 크기: {} bytes",
                        fileType, originalFileName, fileName, file.getSize());
            }

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

            log.info("{} 파일 업로드 완료 - 파일명: {}", fileType, fileName);

            // 파일 URL 생성
            String fileUrl = getFileUrl(fileName, bucketName);

            return FileUploadResponse.builder()
                    .fileName(fileName)
                    .originalFileName(originalFileName)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .bucketName(bucketName)
                    .fileUrl(fileUrl)
                    .build();

        } catch (Exception e) {
            log.error("{} 파일 업로드 실패: {}", fileType, e.getMessage(), e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 파일 삭제
     */
    @Override
    public void deleteFile(String fileName, String bucketName) {
        try {
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
     */
    @Override
    public String getFileUrl(String fileName, String bucketName) {
        try {
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

    /**
     * 파일 기본 유효성 검증
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.error("빈 파일 업로드 시도");
            throw new CustomException(ErrorCode.EMPTY_FILE);
        }
    }

    /**
     * 오디오 파일 유효성 검증
     */
    private void validateAudioFile(MultipartFile file) {
        // 파일 크기 체크
        long maxSizeBytes = minioProperties.getMaxAudioFileSize() * 1024L * 1024L; // MB to Bytes
        if (file.getSize() > maxSizeBytes) {
            log.error("오디오 파일 크기 초과 - 크기: {} bytes, 최대: {} bytes", file.getSize(), maxSizeBytes);
            throw new CustomException(ErrorCode.AUDIO_FILE_TOO_LARGE);
        }

        // 파일 확장자 체크
        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        if (!AUDIO_EXTENSIONS.contains(extension)) {
            log.error("지원하지 않는 오디오 형식 - 확장자: {}", extension);
            throw new CustomException(ErrorCode.INVALID_AUDIO_FORMAT);
        }
    }

    /**
     * 영상 파일 유효성 검증
     */
    private void validateVideoFile(MultipartFile file) {
        // 파일 크기 체크
        long maxSizeBytes = minioProperties.getMaxVideoFileSize() * 1024L * 1024L; // MB to Bytes
        if (file.getSize() > maxSizeBytes) {
            log.error("영상 파일 크기 초과 - 크기: {} bytes, 최대: {} bytes", file.getSize(), maxSizeBytes);
            throw new CustomException(ErrorCode.VIDEO_FILE_TOO_LARGE);
        }

        // 파일 확장자 체크
        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        if (!VIDEO_EXTENSIONS.contains(extension)) {
            log.error("지원하지 않는 영상 형식 - 확장자: {}", extension);
            throw new CustomException(ErrorCode.INVALID_VIDEO_FORMAT);
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
