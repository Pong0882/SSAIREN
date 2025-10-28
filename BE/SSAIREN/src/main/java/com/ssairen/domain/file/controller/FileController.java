package com.ssairen.domain.file.controller;

import com.ssairen.domain.ai.service.SttService;
import com.ssairen.domain.file.dto.AudioUploadWithSttResponse;
import com.ssairen.domain.file.dto.FileUploadResponse;
import com.ssairen.domain.file.dto.SttResponse;
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
    private final SttService sttService;

    /**
     * 오디오 파일 업로드
     * - STT 처리용 오디오 파일 업로드
     * - 지원 형식: wav, mp3, m4a, aac, flac
     * - 최대 크기: 50MB
     */
    @PostMapping(value = "/upload-audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "오디오 파일 업로드",
            description = "오디오 파일을 MinIO 객체 스토리지에 업로드합니다. STT 처리에 사용됩니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "파일 업로드 성공",
                    content = @Content(schema = @Schema(implementation = FileUploadResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (빈 파일, 지원하지 않는 형식, 크기 초과 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "파일 업로드 실패"
            )
    })
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadAudioFile(
            @Parameter(description = "업로드할 오디오 파일", required = true)
            @RequestParam("file") MultipartFile file
    ) {
        log.info("오디오 파일 업로드 요청 - 파일명: {}, 크기: {} bytes",
                file.getOriginalFilename(), file.getSize());

        FileUploadResponse response = minioService.uploadAudioFile(file);

        return ResponseEntity.ok(
                ApiResponse.success(response, "오디오 파일이 성공적으로 업로드되었습니다.")
        );
    }

    /**
     * 영상 파일 업로드
     * - 영상 파일 업로드 및 저장
     * - 지원 형식: mp4, avi, mov, mkv, wmv, flv, webm
     * - 최대 크기: 500MB
     */
    @PostMapping(value = "/upload-video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "영상 파일 업로드",
            description = "영상 파일을 MinIO 객체 스토리지에 업로드합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "파일 업로드 성공",
                    content = @Content(schema = @Schema(implementation = FileUploadResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (빈 파일, 지원하지 않는 형식, 크기 초과 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "파일 업로드 실패"
            )
    })
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadVideoFile(
            @Parameter(description = "업로드할 영상 파일", required = true)
            @RequestParam("file") MultipartFile file
    ) {
        log.info("영상 파일 업로드 요청 - 파일명: {}, 크기: {} bytes",
                file.getOriginalFilename(), file.getSize());

        FileUploadResponse response = minioService.uploadVideoFile(file);

        return ResponseEntity.ok(
                ApiResponse.success(response, "영상 파일이 성공적으로 업로드되었습니다.")
        );
    }

    /**
     * 파일 삭제
     * - 파일명과 버킷명을 받아 MinIO에서 삭제
     */
    @DeleteMapping("/{bucketName}/{fileName}")
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
            @Parameter(description = "버킷 이름 (audio-files 또는 video-files)", required = true)
            @PathVariable String bucketName,
            @Parameter(description = "삭제할 파일명 (UUID 형식)", required = true)
            @PathVariable String fileName
    ) {
        log.info("파일 삭제 요청 - 버킷: {}, 파일명: {}", bucketName, fileName);

        minioService.deleteFile(fileName, bucketName);

        return ResponseEntity.ok(
                ApiResponse.success(null, "파일이 성공적으로 삭제되었습니다.")
        );
    }

    /**
     * 파일 URL 조회
     * - 파일명과 버킷명을 받아 접근 가능한 URL 반환
     * - 7일간 유효한 Presigned URL
     */
    @GetMapping("/{bucketName}/{fileName}/url")
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
            @Parameter(description = "버킷 이름 (audio-files 또는 video-files)", required = true)
            @PathVariable String bucketName,
            @Parameter(description = "조회할 파일명 (UUID 형식)", required = true)
            @PathVariable String fileName
    ) {
        log.info("파일 URL 조회 요청 - 버킷: {}, 파일명: {}", bucketName, fileName);

        String fileUrl = minioService.getFileUrl(fileName, bucketName);

        return ResponseEntity.ok(
                ApiResponse.success(fileUrl, "파일 URL을 조회했습니다.")
        );
    }

    /**
     * 오디오 파일 업로드 + STT 변환 통합 API
     * - 오디오 파일을 MinIO에 저장하고, AI 서버에서 STT 처리
     * - 파일 정보와 STT 결과를 함께 반환
     */
    @PostMapping(value = "/upload-audio-with-stt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "오디오 파일 업로드 + STT 변환",
            description = "오디오 파일을 MinIO에 업로드하고, AI 서버를 통해 음성을 텍스트로 변환합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "업로드 및 STT 변환 성공",
                    content = @Content(schema = @Schema(implementation = AudioUploadWithSttResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (빈 파일, 지원하지 않는 형식, 크기 초과 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "파일 업로드 또는 STT 변환 실패"
            )
    })
    public ResponseEntity<ApiResponse<AudioUploadWithSttResponse>> uploadAudioWithStt(
            @Parameter(description = "업로드할 오디오 파일", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "언어 코드 (선택사항, 예: ko, en, ja)")
            @RequestParam(value = "language", required = false, defaultValue = "ko") String language
    ) {
        log.info("오디오 파일 업로드 + STT 요청 - 파일명: {}, 언어: {}, 크기: {} bytes",
                file.getOriginalFilename(), language, file.getSize());

        // 1. MinIO에 오디오 파일 저장
        FileUploadResponse fileUploadResponse = minioService.uploadAudioFile(file);
        log.info("MinIO 업로드 완료 - 파일명: {}", fileUploadResponse.getFileName());

        // 2. AI 서버로 STT 요청
        SttResponse sttResponse = sttService.convertSpeechToText(file, language);
        log.info("STT 변환 완료 - 텍스트 길이: {} 문자", sttResponse.getText().length());

        // 3. 통합 응답 생성
        AudioUploadWithSttResponse response = AudioUploadWithSttResponse.builder()
                .fileInfo(fileUploadResponse)
                .sttResult(sttResponse)
                .build();

        return ResponseEntity.ok(
                ApiResponse.success(response, "오디오 파일 업로드 및 STT 변환이 완료되었습니다.")
        );
    }
}
