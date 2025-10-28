package com.ssairen.domain.file.service;

import com.ssairen.domain.file.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * MinIO 파일 스토리지 Service 인터페이스
 */
public interface MinioService {

    /**
     * 파일 업로드
     *
     * @param file 업로드할 파일
     * @return 업로드된 파일 정보
     */
    FileUploadResponse uploadFile(MultipartFile file);

    /**
     * 파일 삭제
     *
     * @param fileName 삭제할 파일명
     */
    void deleteFile(String fileName);

    /**
     * 파일 URL 조회
     *
     * @param fileName 조회할 파일명
     * @return 파일 접근 URL
     */
    String getFileUrl(String fileName);
}
