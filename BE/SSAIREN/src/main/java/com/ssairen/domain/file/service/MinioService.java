package com.ssairen.domain.file.service;

import com.ssairen.domain.file.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * MinIO 파일 스토리지 Service 인터페이스
 */
public interface MinioService {

    /**
     * 오디오 파일 업로드
     *
     * @param file 업로드할 오디오 파일
     * @return 업로드된 파일 정보
     */
    FileUploadResponse uploadAudioFile(MultipartFile file);

    /**
     * 영상 파일 업로드
     *
     * @param file 업로드할 영상 파일
     * @return 업로드된 파일 정보
     */
    FileUploadResponse uploadVideoFile(MultipartFile file);

    /**
     * 파일 삭제
     *
     * @param fileName 삭제할 파일명
     * @param bucketName 버킷 이름
     */
    void deleteFile(String fileName, String bucketName);

    /**
     * 파일 URL 조회
     *
     * @param fileName 조회할 파일명
     * @param bucketName 버킷 이름
     * @return 파일 접근 URL
     */
    String getFileUrl(String fileName, String bucketName);
}
