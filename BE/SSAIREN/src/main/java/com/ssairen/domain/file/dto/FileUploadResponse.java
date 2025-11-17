package com.ssairen.domain.file.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 파일 업로드 응답 DTO
 */
@Getter
@Builder
public class FileUploadResponse {

    /**
     * 업로드된 파일의 고유 이름 (UUID 기반)
     */
    private String fileName;

    /**
     * 원본 파일명
     */
    private String originalFileName;

    /**
     * 파일 크기 (바이트)
     */
    private Long fileSize;

    /**
     * 파일 콘텐츠 타입 (MIME type)
     */
    private String contentType;

    /**
     * 파일이 저장된 버킷 이름
     */
    private String bucketName;

    /**
     * 파일 URL (MinIO에서 직접 접근 가능한 URL)
     */
    private String fileUrl;
}
