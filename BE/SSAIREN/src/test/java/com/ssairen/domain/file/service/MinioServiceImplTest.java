package com.ssairen.domain.file.service;

import com.ssairen.config.MinioProperties;
import com.ssairen.domain.file.dto.FileUploadResponse;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioServiceImplTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioProperties minioProperties;

    @InjectMocks
    private MinioServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(minioProperties.getBucketName()).thenReturn("audio-bucket");
        lenient().when(minioProperties.getVideoBucketName()).thenReturn("video-bucket");
        lenient().when(minioProperties.getMaxAudioFileSize()).thenReturn(50); // 50MB
        lenient().when(minioProperties.getMaxVideoFileSize()).thenReturn(500); // 500MB
    }

    @Test
    @DisplayName("오디오 파일 업로드 - 성공")
    void uploadAudioFile_success() throws Exception {
        // given
        MockMultipartFile audioFile = new MockMultipartFile(
                "file",
                "test-audio.wav",
                "audio/wav",
                "test audio content".getBytes()
        );

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://minio-url/audio-bucket/file.wav");

        // when
        FileUploadResponse response = service.uploadAudioFile(audioFile);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOriginalFileName()).isEqualTo("test-audio.wav");
        assertThat(response.getBucketName()).isEqualTo("audio-bucket");
        assertThat(response.getFileUrl()).isNotNull();
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("영상 파일 업로드 - 성공")
    void uploadVideoFile_success() throws Exception {
        // given
        MockMultipartFile videoFile = new MockMultipartFile(
                "file",
                "test-video.mp4",
                "video/mp4",
                "test video content".getBytes()
        );

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://minio-url/video-bucket/file.mp4");

        // when
        FileUploadResponse response = service.uploadVideoFile(videoFile);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOriginalFileName()).isEqualTo("test-video.mp4");
        assertThat(response.getBucketName()).isEqualTo("video-bucket");
        assertThat(response.getFileUrl()).isNotNull();
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("오디오 파일 업로드 - 빈 파일")
    void uploadAudioFile_emptyFile() throws Exception {
        // given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.wav",
                "audio/wav",
                new byte[0]
        );

        // when & then
        assertThatThrownBy(() -> service.uploadAudioFile(emptyFile))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMPTY_FILE);

        verify(minioClient, never()).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("오디오 파일 업로드 - null 파일")
    void uploadAudioFile_nullFile() throws Exception {
        // when & then
        assertThatThrownBy(() -> service.uploadAudioFile(null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMPTY_FILE);
    }

    @Test
    @DisplayName("오디오 파일 업로드 - 파일 크기 초과")
    void uploadAudioFile_fileTooLarge() throws Exception {
        // given
        byte[] largeContent = new byte[51 * 1024 * 1024]; // 51MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-audio.wav",
                "audio/wav",
                largeContent
        );

        // when & then
        assertThatThrownBy(() -> service.uploadAudioFile(largeFile))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUDIO_FILE_TOO_LARGE);

        verify(minioClient, never()).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("오디오 파일 업로드 - 지원하지 않는 형식")
    void uploadAudioFile_invalidFormat() throws Exception {
        // given
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> service.uploadAudioFile(invalidFile))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_AUDIO_FORMAT);

        verify(minioClient, never()).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("영상 파일 업로드 - 파일 크기 초과")
    void uploadVideoFile_fileTooLarge() throws Exception {
        // given
        when(minioProperties.getMaxVideoFileSize()).thenReturn(1); // 1MB for testing

        byte[] largeContent = new byte[2 * 1024 * 1024]; // 2MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-video.mp4",
                "video/mp4",
                largeContent
        );

        // when & then
        assertThatThrownBy(() -> service.uploadVideoFile(largeFile))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VIDEO_FILE_TOO_LARGE);

        verify(minioClient, never()).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("영상 파일 업로드 - 지원하지 않는 형식")
    void uploadVideoFile_invalidFormat() throws Exception {
        // given
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.doc",
                "application/msword",
                "test content".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> service.uploadVideoFile(invalidFile))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_VIDEO_FORMAT);

        verify(minioClient, never()).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("오디오 파일 업로드 - 바디캠 파일 (경로 포함)")
    void uploadAudioFile_bodycamPath() throws Exception {
        // given
        MockMultipartFile bodycamFile = new MockMultipartFile(
                "file",
                "2024/01/15/audio.wav",
                "audio/wav",
                "test audio content".getBytes()
        );

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://minio-url/audio-bucket/2024/01/15/audio.wav");

        // when
        FileUploadResponse response = service.uploadAudioFile(bodycamFile);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getFileName()).isEqualTo("2024/01/15/audio.wav");
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("파일 삭제 - 성공")
    void deleteFile_success() throws Exception {
        // given
        String fileName = "test-file.wav";
        String bucketName = "audio-bucket";

        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        // when
        service.deleteFile(fileName, bucketName);

        // then
        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    @DisplayName("파일 삭제 - 실패")
    void deleteFile_failure() throws Exception {
        // given
        String fileName = "test-file.wav";
        String bucketName = "audio-bucket";

        doThrow(new RuntimeException("Delete failed"))
                .when(minioClient).removeObject(any(RemoveObjectArgs.class));

        // when & then
        assertThatThrownBy(() -> service.deleteFile(fileName, bucketName))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_DELETE_FAILED);
    }

    @Test
    @DisplayName("파일 URL 조회 - 성공")
    void getFileUrl_success() throws Exception {
        // given
        String fileName = "test-file.wav";
        String bucketName = "audio-bucket";
        String expectedUrl = "http://minio-url/audio-bucket/test-file.wav?signature=xxx";

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(expectedUrl);

        // when
        String url = service.getFileUrl(fileName, bucketName);

        // then
        assertThat(url).isEqualTo(expectedUrl);
        verify(minioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    @DisplayName("파일 URL 조회 - 실패")
    void getFileUrl_failure() throws Exception {
        // given
        String fileName = "non-existent.wav";
        String bucketName = "audio-bucket";

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenThrow(new RuntimeException("File not found"));

        // when & then
        assertThatThrownBy(() -> service.getFileUrl(fileName, bucketName))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NOT_FOUND);
    }

    @Test
    @DisplayName("오디오 파일 업로드 - MinIO 예외 발생")
    void uploadAudioFile_minioException() throws Exception {
        // given
        MockMultipartFile audioFile = new MockMultipartFile(
                "file",
                "test-audio.wav",
                "audio/wav",
                "test audio content".getBytes()
        );

        doThrow(new RuntimeException("MinIO error"))
                .when(minioClient).putObject(any(PutObjectArgs.class));

        // when & then
        assertThatThrownBy(() -> service.uploadAudioFile(audioFile))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_UPLOAD_FAILED);
    }

    @Test
    @DisplayName("다양한 오디오 형식 지원 확인")
    void uploadAudioFile_variousFormats() throws Exception {
        // given
        String[] audioFormats = {".wav", ".mp3", ".m4a", ".aac", ".flac"};

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://minio-url/audio-bucket/file");

        // when & then
        for (String format : audioFormats) {
            MockMultipartFile audioFile = new MockMultipartFile(
                    "file",
                    "test" + format,
                    "audio/*",
                    "test content".getBytes()
            );

            FileUploadResponse response = service.uploadAudioFile(audioFile);
            assertThat(response).isNotNull();
        }

        verify(minioClient, times(audioFormats.length)).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("다양한 영상 형식 지원 확인")
    void uploadVideoFile_variousFormats() throws Exception {
        // given
        String[] videoFormats = {".mp4", ".avi", ".mov", ".mkv", ".wmv", ".flv", ".webm"};

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://minio-url/video-bucket/file");

        // when & then
        for (String format : videoFormats) {
            MockMultipartFile videoFile = new MockMultipartFile(
                    "file",
                    "test" + format,
                    "video/*",
                    "test content".getBytes()
            );

            FileUploadResponse response = service.uploadVideoFile(videoFile);
            assertThat(response).isNotNull();
        }

        verify(minioClient, times(videoFormats.length)).putObject(any(PutObjectArgs.class));
    }
}
