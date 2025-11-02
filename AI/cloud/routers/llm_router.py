"""
LLM 기반 STT 정제 API 라우터

STT 결과물을 GPT-4.1 모델로 정제하는 API 엔드포인트를 제공합니다.
"""

from fastapi import APIRouter, HTTPException
from fastapi.responses import JSONResponse

from schemas.llm_schemas import (
    STTRefineRequest,
    STTRefineResponse,
    LLMRefinedResult
)
from services.llm_service import refine_stt_result

router = APIRouter(prefix="/llm", tags=["llm"])


@router.post("/refine-stt", response_model=STTRefineResponse)
async def refine_stt(request: STTRefineRequest):
    """
    STT 결과물 정제 API (화자 분리 지원)
    
    3단계 프로세스로 STT 대화를 정제합니다:
    1. 화자별 세그먼트를 대화 형식으로 변환
    2. 맥락 기반 단어 교정 (예: '두동이' -> '두통이', '삽형급실' -> '응급실')
    3. 구조화된 JSON 형식으로 변환 (실제 정보만 포함)
    
    Args:
        request: STTRefineRequest
            - transcription: STT 전체 텍스트 (필수)
            - segments: 화자별 세그먼트 리스트 (필수)
            - context: 추가 맥락 정보 (선택)
    
    Returns:
        STTRefineResponse: 정제된 결과
            - success: 성공 여부
            - original_text: 원본 텍스트
            - corrected_text: 교정된 대화 텍스트
            - structured_data: 구조화된 JSON 데이터 (8개 카테고리)
            - model_used: 사용된 모델명
            - processing_time: 처리 시간(초)
    
    Raises:
        HTTPException: 정제 프로세스 실패 시
    
    Example:
        POST /api/llm/refine-stt
        {
            "transcription": "안녕하세요. 어디가 불편하신가요? 머리가 아파요.",
            "segments": [
                {
                    "id": "seg_0",
                    "speaker": "A",
                    "start": 0.0,
                    "end": 2.5,
                    "text": "안녕하세요. 어디가 불편하신가요?"
                },
                {
                    "id": "seg_1",
                    "speaker": "B",
                    "start": 2.6,
                    "end": 4.0,
                    "text": "머리가 아파요."
                }
            ],
            "context": "119 신고"
        }
        
        Response:
        {
            "success": true,
            "original_text": "안녕하세요. 어디가 불편하신가요? 머리가 아파요.",
            "corrected_text": "[화자 A]: 안녕하세요. 어디가 불편하신가요?\\n[화자 B]: 머리가 아파요.",
            "structured_data": {
                "chiefComplaint": {
                    "schema_version": 1,
                    "mainSymptom": "두통"
                }
            },
            "model_used": "gpt-4.1-2025-04-14",
            "processing_time": 3.45
        }
    """
    try:
        # STT 결과 정제 실행 (화자 분리 포함)
        result = refine_stt_result(
            transcription=request.transcription,
            segments=request.segments,
            context=request.context
        )
        
        # 성공 여부 확인
        if not result["success"]:
            raise HTTPException(
                status_code=500,
                detail=f"정제 프로세스 실패: {result.get('error', '알 수 없는 오류')}"
            )
        
        # 응답 데이터 구성
        response_data = {
            "success": result["success"],
            "original_text": result["original_text"],
            "corrected_text": result["corrected_text"],
            "structured_data": result["structured_data"],
            "model_used": result["model_used"],
            "processing_time": result["processing_time"]
        }
        
        return JSONResponse(content=response_data)
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"서버 오류: {str(e)}"
        )


@router.get("/health")
async def health_check():
    """
    LLM 서비스 상태 확인
    
    Returns:
        dict: 서비스 상태 정보
    """
    import os
    
    # OpenAI API 키 설정 여부 확인
    api_key_configured = bool(os.getenv("OPENAI_API_KEY"))
    
    return {
        "status": "healthy" if api_key_configured else "warning",
        "service": "LLM STT Refine Service",
        "model": "gpt-4.1-2025-04-14",
        "api_key_configured": api_key_configured,
        "message": "서비스가 정상 작동 중입니다." if api_key_configured 
                   else "OpenAI API 키가 설정되지 않았습니다."
    }



