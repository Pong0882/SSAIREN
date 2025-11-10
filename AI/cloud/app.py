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
from routers.stt_router_local import router as stt_router_local  

app = FastAPI(title='CLOUD AI', description = "클라우드 AI를 기반으로 한 서비스를 제공하는 서버")

@app.get("/")
def read_root():
    return {"message": "Hello, Cloud AI!"}

# 라우터 등록
app.include_router(stt_router, prefix="/api")
app.include_router(llm_router, prefix="/api")
app.include_router(integrated_router, prefix="/api")  # 통합 API
app.include_router(emergency_router, prefix="/api")  # 응급실 추천 API
app.include_router(stt_to_json_router, prefix="/api")  # ✨ 파인튜닝 LoRA 모델
app.include_router(stt_router_local, prefix="/api")  # 로컬 Whisper STT
