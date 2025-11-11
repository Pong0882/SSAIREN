from fastapi import APIRouter, UploadFile, File, HTTPException
from fastapi.responses import StreamingResponse, JSONResponse
from services.stt_service_local import transcribe_audio_stream, transcribe_audio_full
import os
import tempfile
import json

router = APIRouter()

@router.post("/stt/local/stream", summary="로컬 Whisper STT (스트리밍)")
async def stt_local_stream(
    file: UploadFile = File(..., description="오디오 파일 (mp3, wav, m4a 등)"),
    language: str = "ko"
):
    """
    로컬 Faster-Whisper 모델을 사용한 음성-텍스트 변환 (스트리밍)

    - **file**: 오디오 파일 업로드
    - **language**: 언어 코드 (기본값: ko)

    Returns:
        스트리밍 응답 (Server-Sent Events 형식)
    """
    # 임시 파일로 저장
    temp_file = None
    try:
        # 임시 파일 생성
        suffix = os.path.splitext(file.filename)[1] or ".mp3"
        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as temp_file:
            content = await file.read()
            temp_file.write(content)
            temp_file_path = temp_file.name

        # 스트리밍 제너레이터
        def generate():
            try:
                for segment in transcribe_audio_stream(temp_file_path, language):
                    # Server-Sent Events 형식으로 전송
                    yield f"data: {json.dumps(segment, ensure_ascii=False)}\n\n"
            finally:
                # 처리 완료 후 임시 파일 삭제
                if os.path.exists(temp_file_path):
                    os.unlink(temp_file_path)

        return StreamingResponse(
            generate(),
            media_type="text/event-stream",
            headers={
                "Cache-Control": "no-cache",
                "Connection": "keep-alive",
            }
        )

    except Exception as e:
        # 에러 발생 시 임시 파일 정리
        if temp_file and os.path.exists(temp_file.name):
            os.unlink(temp_file.name)
        raise HTTPException(status_code=500, detail=f"STT 처리 실패: {str(e)}")


@router.post("/stt/local/full", summary="로컬 Whisper STT (전체 텍스트)")
async def stt_local_full(
    file: UploadFile = File(..., description="오디오 파일 (mp3, wav, m4a 등)"),
    language: str = "ko"
):
    """
    로컬 Faster-Whisper 모델을 사용한 음성-텍스트 변환 (전체 텍스트 반환)

    - **file**: 오디오 파일 업로드
    - **language**: 언어 코드 (기본값: ko)

    Returns:
        {
            "text": "전체 변환된 텍스트",
            "segments": [...],
            "language": "ko",
            "duration": 1.23
        }
    """
    # 임시 파일로 저장
    temp_file = None
    try:
        # 임시 파일 생성
        suffix = os.path.splitext(file.filename)[1] or ".mp3"
        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as temp_file:
            content = await file.read()
            temp_file.write(content)
            temp_file_path = temp_file.name

        # STT 처리
        result = transcribe_audio_full(temp_file_path, language)

        return JSONResponse(content=result)

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"STT 처리 실패: {str(e)}")

    finally:
        # 처리 완료 후 임시 파일 삭제
        if temp_file and os.path.exists(temp_file.name):
            os.unlink(temp_file.name)


@router.get("/stt/local/health", summary="로컬 Whisper 모델 헬스체크")
async def stt_local_health():
    """
    로컬 Whisper 모델 상태 확인

    Returns:
        모델 로드 상태 및 설정 정보
    """
    try:
        from services.stt_service_local import _whisper_model, get_whisper_model

        model_size = os.getenv("WHISPER_MODEL_SIZE", "medium")
        device = os.getenv("WHISPER_DEVICE", "cpu")
        compute_type = os.getenv("WHISPER_COMPUTE_TYPE", "int8")

        is_loaded = _whisper_model is not None

        # 모델이 아직 로드되지 않았다면 로드 시도
        if not is_loaded:
            get_whisper_model()
            is_loaded = True

        return {
            "status": "healthy" if is_loaded else "not_loaded",
            "model_size": model_size,
            "device": device,
            "compute_type": compute_type,
            "model_loaded": is_loaded
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"헬스체크 실패: {str(e)}")
