import os
import tempfile
import json
from fastapi import APIRouter, UploadFile, File
from fastapi.responses import StreamingResponse

from ..services.stt_service import transcribe_audio_stream

router = APIRouter(prefix="/stt", tags=["stt"])

@router.post("/whisper")
async def stt_whisper(
    file: UploadFile = File(...),
    language: str = None
):
    """오디오 파일을 업로드하면 텍스트로 변환 (스트리밍)
    
    Args:
        file: 오디오 파일 (wav, mp3, m4a 등)
        language: 언어 코드 (선택사항, 미지정 시 자동 감지. 예: 'ko', 'en', 'ja')
    """
    # 임시 파일로 저장
    suffix = os.path.splitext(file.filename or "audio.mp3")[1]
    with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
        tmp.write(await file.read())
        tmp_path = tmp.name
    
    def event_generator():
        """SSE(Server-Sent Events) 형식으로 스트림 이벤트 전송"""
        try:
            for event in transcribe_audio_stream(tmp_path, language):
                # OpenAI 스트림 이벤트를 JSON으로 직렬화
                # event 객체의 구조에 따라 적절히 변환
                if hasattr(event, 'model_dump'):
                    event_data = event.model_dump()
                elif hasattr(event, 'dict'):
                    event_data = event.dict()
                else:
                    event_data = {"chunk": str(event)}
                
                # SSE 형식: data: {json}\n\n
                yield f"data: {json.dumps(event_data, ensure_ascii=False)}\n\n"
        finally:
            # 스트림 완료 후 임시 파일 삭제
            if os.path.exists(tmp_path):
                os.remove(tmp_path)
    
    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
        }
    )

