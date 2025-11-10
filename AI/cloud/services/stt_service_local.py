import os
from faster_whisper import WhisperModel
from typing import Optional, Dict, Any, Generator
import time

# Whisper 모델 싱글톤 (메모리 효율을 위해 한 번만 로드)
_whisper_model: Optional[WhisperModel] = None

def get_whisper_model() -> WhisperModel:
    """
    Faster-Whisper 모델 인스턴스 반환 (lazy initialization)

    환경변수:
        WHISPER_MODEL_SIZE: 모델 크기 (tiny/base/small/medium/large-v3, 기본: medium)
        WHISPER_DEVICE: 디바이스 (cpu/cuda, 기본: cpu)
        WHISPER_COMPUTE_TYPE: 연산 타입 (int8/float16/float32, 기본: int8)
    """
    global _whisper_model

    if _whisper_model is None:
        # 환경변수에서 설정 읽기
        model_size = os.getenv("WHISPER_MODEL_SIZE", "medium")
        device = os.getenv("WHISPER_DEVICE", "cpu")  # "cpu" or "cuda"
        compute_type = os.getenv("WHISPER_COMPUTE_TYPE", "int8")  # CPU: "int8", GPU: "float16"

        print(f"🔄 Loading Whisper model: {model_size} on {device} with {compute_type}")
        start_time = time.time()

        _whisper_model = WhisperModel(
            model_size,
            device=device,
            compute_type=compute_type,
            download_root="./models/whisper"  # 모델 저장 위치
        )

        load_time = time.time() - start_time
        print(f"✅ Whisper model loaded in {load_time:.2f}s")

    return _whisper_model


def transcribe_audio_stream(
    audio_file_path: str,
    language: str = "ko"
) -> Generator[Dict[str, Any], None, None]:
    """
    Faster-Whisper를 사용한 음성-텍스트 변환 (세그먼트 스트리밍)

    Args:
        audio_file_path: 오디오 파일 경로 (.mp3, .wav, .m4a 등)
        language: 언어 코드 (기본값: "ko")

    Yields:
        세그먼트 정보를 포함한 딕셔너리
        {
            "start": float,  # 시작 시간 (초)
            "end": float,    # 종료 시간 (초)
            "text": str,     # 변환된 텍스트
            "avg_logprob": float,      # 평균 로그 확률
            "no_speech_prob": float    # 무음 확률
        }
    """
    model = get_whisper_model()

    print(f"🎤 Transcribing: {audio_file_path}")
    start_time = time.time()

    # Whisper 추론 수행 (정확도 최우선 설정)
    segments, info = model.transcribe(
        audio_file_path,
        language=language,
        # 정확도 향상 파라미터
        beam_size=10,  # 빔 서치 크기 (5→10, 느리지만 정확함)
        best_of=5,  # 상위 5개 후보 중 최선 선택
        temperature=0.0,  # 결정적 출력 (0.0 = 가장 정확)
        condition_on_previous_text=True,  # 이전 컨텍스트 활용
        # 음성 품질 향상
        vad_filter=True,  # Voice Activity Detection (무음 제거)
        vad_parameters=dict(
            min_silence_duration_ms=500,  # 500ms 이상 무음 제거
            threshold=0.5  # VAD 임계값
        )
    )

    print(f"🌍 Detected language: {info.language} (probability: {info.language_probability:.2f})")

    segment_count = 0
    # 세그먼트를 순회하며 yield
    for segment in segments:
        segment_count += 1
        yield {
            "start": round(segment.start, 2),
            "end": round(segment.end, 2),
            "text": segment.text.strip(),
            "avg_logprob": round(segment.avg_logprob, 3),
            "no_speech_prob": round(segment.no_speech_prob, 3)
        }

    total_time = time.time() - start_time
    print(f"✅ Transcription completed: {segment_count} segments in {total_time:.2f}s")


def transcribe_audio_full(audio_file_path: str, language: str = "ko") -> Dict[str, Any]:
    """
    Faster-Whisper를 사용한 음성-텍스트 변환 (전체 텍스트 반환)

    Args:
        audio_file_path: 오디오 파일 경로
        language: 언어 코드 (기본값: "ko")

    Returns:
        {
            "text": str,           # 전체 변환된 텍스트
            "segments": list,      # 세그먼트 리스트
            "language": str,       # 감지된 언어
            "duration": float      # 처리 시간 (초)
        }
    """
    start_time = time.time()
    segments = list(transcribe_audio_stream(audio_file_path, language))
    duration = time.time() - start_time

    full_text = " ".join([seg["text"] for seg in segments])

    return {
        "text": full_text,
        "segments": segments,
        "language": language,
        "duration": round(duration, 2)
    }
