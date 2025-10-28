package com.ssairen.domain.file.controller;

import com.ssairen.domain.file.dto.FileUploadResponse;
import com.ssairen.domain.file.service.MinioService;
import com.ssairen.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드/다운로드 Controller
 * MinIO 객체 스토리지를 사용한 파일 관리
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File", description = "파일 관리 API")
public class FileController {

    private final MinioService minioService;

    /**
     * 파일 업로드
     * - 주로 오디오 파일 업로드에 사용 (STT 처리용)
     * - MultipartFile 형식으로 파일을 받아 MinIO에 저장
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "파일 업로드",
            description = "파일을 MinIO 객체 스토리지에 업로드합니다. 주로 오디오 파일 업로드에 사용됩니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "파일 업로드 성공",
                    content = @Content(schema = @Schema(implementation = FileUploadResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (빈 파일, 지원하지 않는 형식 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "파일 업로드 실패"
            )
    })
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @Parameter(description = "업로드할 파일", required = true)
            @RequestParam("file") MultipartFile file
    ) {
        log.info("파일 업로드 요청 - 파일명: {}, 크기: {} bytes",
                file.getOriginalFilename(), file.getSize());

        FileUploadResponse response = minioService.uploadFile(file);

        return ResponseEntity.ok(
                ApiResponse.success(response, "파일이 성공적으로 업로드되었습니다.")
        );
    }

    /**
     * 파일 삭제
     * - 파일명을 받아 MinIO에서 삭제
     */
    @DeleteMapping("/{fileName}")
    @Operation(
            summary = "파일 삭제",
            description = "MinIO에 저장된 파일을 삭제합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "파일 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "파일을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "파일 삭제 실패"
            )
    })
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @Parameter(description = "삭제할 파일명 (UUID 형식)", required = true)
            @PathVariable String fileName
    ) {
        log.info("파일 삭제 요청 - 파일명: {}", fileName);

        minioService.deleteFile(fileName);

        return ResponseEntity.ok(
                ApiResponse.success(null, "파일이 성공적으로 삭제되었습니다.")
        );
    }

    /**
     * 파일 URL 조회
     * - 파일명을 받아 접근 가능한 URL 반환
     * - 7일간 유효한 Presigned URL
     */
    @GetMapping("/{fileName}/url")
    @Operation(
            summary = "파일 URL 조회",
            description = "파일에 접근할 수 있는 임시 URL을 생성합니다 (7일간 유효)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "URL 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "파일을 찾을 수 없음"
            )
    })
    public ResponseEntity<ApiResponse<String>> getFileUrl(
            @Parameter(description = "조회할 파일명 (UUID 형식)", required = true)
            @PathVariable String fileName
    ) {
        log.info("파일 URL 조회 요청 - 파일명: {}", fileName);

        String fileUrl = minioService.getFileUrl(fileName);

        return ResponseEntity.ok(
                ApiResponse.success(fileUrl, "파일 URL을 조회했습니다.")
        );
    }
}
