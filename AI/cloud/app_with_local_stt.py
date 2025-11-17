"""
로컬 Whisper STT가 추가된 FastAPI 앱 예시

기존 app.py를 이 파일로 교체하거나,
app.py에 stt_router_local 라우터만 추가하면 됩니다.

실행:
    uvicorn app_with_local_stt:app --reload --port 8000
"""

from fastapi import FastAPI
from pathlib import Path
from dotenv import load_dotenv

# 로컬 개발 시 config/.env 자동 로드
load_dotenv(Path(__file__).parent / "config" / ".env")

from routers.stt_router import router as stt_router
from routers.llm_router import router as llm_router
from routers.integrated_router import router as integrated_router
from routers.emergency_router import router as emergency_router
from routers.stt_to_json_router import router as stt_to_json_router
from routers.stt_router_local import router as stt_router_local  # 🆕 로컬 STT 라우터

app = FastAPI(
    title='CLOUD AI',
    description="클라우드 AI 및 온프레미스 AI를 기반으로 한 서비스를 제공하는 서버"
)

@app.get("/")
def read_root():
    return {
        "message": "Hello, Cloud AI!",
        "features": [
            "STT (OpenAI API)",
            "STT (Local Whisper)",  # 🆕
            "LLM",
            "Integrated",
            "Emergency"
        ]
    }

# 라우터 등록
app.include_router(stt_router, prefix="/api")
app.include_router(llm_router, prefix="/api")
app.include_router(integrated_router, prefix="/api")
app.include_router(emergency_router, prefix="/api")
app.include_router(stt_to_json_router, prefix="/api")
app.include_router(stt_router_local, prefix="/api")  # 🆕 로컬 Whisper STT
