"""
STT → JSON 추출 라우터
파인튜닝된 LoRA LLM을 사용한 대화 텍스트 → JSON 추출
"""
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from typing import Dict, Any
from services.lora_llm_service import get_lora_llm_service

router = APIRouter(prefix="/stt-to-json", tags=["STT to JSON"])

class ConversationToJsonRequest(BaseModel):
    """대화 텍스트 → JSON 요청"""
    conversation: str = Field(..., description="대화 텍스트 (STT 결과 또는 직접 입력)")
    max_new_tokens: int = Field(700, ge=400, le=1500, description="최대 생성 토큰")
    temperature: float = Field(0.1, ge=0.0, le=1.0, description="샘플링 온도")

@router.post("/text-to-json")
async def text_to_json(request: ConversationToJsonRequest):
    """
    대화 텍스트 → JSON 추출 (STT 결과 직접 입력)
    
    ## 용도
    - STT가 이미 완료된 경우
    - 테스트용 대화 직접 입력
    """
    try:
        print(f"🤖 JSON 추출 시작 (대화 길이: {len(request.conversation)}자)")
        
        llm_service = get_lora_llm_service()
        result = llm_service.extract_json_from_conversation(
            conversation=request.conversation,
            max_tokens=request.max_new_tokens,
            temperature=request.temperature
        )
        
        if result["success"]:
            print(f"✅ JSON 추출 완료")
            return {
                "success": True,
                "extracted_json": result["json"],
                "raw_output": result["raw_text"]
            }
        else:
            raise HTTPException(
                status_code=500,
                detail=f"JSON 파싱 실패: {result['error']}"
            )
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"처리 실패: {str(e)}")

@router.get("/health")
async def health_check():
    """헬스 체크"""
    try:
        llm_service = get_lora_llm_service()
        return {
            "status": "healthy",
            "model_loaded": llm_service._model is not None,
            "device": "CPU"  # llama-cpp-python은 CPU 전용
        }
    except Exception as e:
        raise HTTPException(status_code=503, detail=f"서비스 불가: {str(e)}")

