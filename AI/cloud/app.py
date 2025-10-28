from fastapi import FastAPI
from pathlib import Path
from dotenv import load_dotenv

# 로컬 개발 시 config/.env 자동 로드
load_dotenv(Path(__file__).parent / "config" / ".env")

from routers.stt_router import router as stt_router

app = FastAPI(title='CLOUD AI', description = "클라우드 AI를 기반으로 한 서비스를 제공하는 서버")

@app.get("/")
def read_root():
    return {"message": "Hello, Cloud AI!"}

app.include_router(stt_router, prefix="/api")
