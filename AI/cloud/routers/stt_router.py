import os
import tempfile
from fastapi import APIRouter, UploadFile, File

from ..services.stt_service import transcribe_audio

router = APIRouter(prefix="/stt", tags=["stt"])

@router.post("/whisper")
async def stt_whisper(
    file: UploadFile = File(...),
    language: str = "en"
):
    """오디오 파일을 업로드하면 텍스트로 변환"""
    # 임시 파일로 저장
    suffix = os.path.splitext(file.filename or "audio.mp3")[1]
    with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
        tmp.write(await file.read())
        tmp_path = tmp.name
    
    try:
        text = transcribe_audio(tmp_path, language)
        return {"text": text}
    finally:
        os.remove(tmp_path)

