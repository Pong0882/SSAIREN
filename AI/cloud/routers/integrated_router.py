"""
통합 텍스트 처리 API

대본(텍스트) → LLM 정제 및 JSON 구조화를 처리합니다.
"""

import os
from fastapi import APIRouter, HTTPException
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from typing import Optional

from services.llm_service import extract_ems_data_from_conversation

router = APIRouter(prefix="/integrated", tags=["integrated"])


# 요청 스키마
class ConversationRequest(BaseModel):
    conversation: str
    max_new_tokens: Optional[int] = 700
    temperature: Optional[float] = 0.1


@router.post("/process-conversation")
async def process_conversation(request: ConversationRequest):
    """
    대본(텍스트)을 받아서 LLM으로 정제 및 구조화된 JSON 추출
    
    전체 프로세스:
    1. 대본(텍스트) 입력
    2. GPT-5로 정제 및 응급 정보 추출
    3. 구조화된 JSON 반환
    
    Args:
        conversation: 응급 상황 대본 텍스트
        max_new_tokens: 최대 생성 토큰 수 (기본값: 700)
        temperature: 생성 temperature (기본값: 0.1)
    
    Returns:
        dict: 전체 ReportSectionType 스키마 구조 (모든 필드 포함)
        - 값이 있는 필드: 실제 값 입력
        - 값이 없는 필드: 빈 문자열(""), null, 또는 빈 배열([])
        {
            "ReportSectionType": {
                "patientInfo": {...},  // 모든 하위 필드 포함
                "dispatch": {...},     // 모든 하위 필드 포함
                "incidentType": {...}, // 모든 하위 필드 포함
                "assessment": {...}    // 모든 하위 필드 포함
            }
        }
    
    Example:
        POST /api/integrated/process-conversation
        {
            "conversation": "어디가 아프세요? 머리가 아파요. 고혈압 있습니다.",
            "max_new_tokens": 700,
            "temperature": 0.1
        }
        
        Response: (전체 스키마 구조 반환, 값이 없는 필드는 "", null, [] 사용)
        {
            "ReportSectionType": {
                "patientInfo": {
                    "reporter": {"phone": "", "reportMethod": "", "value": null},
                    "patient": {"name": "", "gender": "", "ageYears": null, "birthDate": "", "address": ""},
                    "guardian": {"name": "", "relation": "", "phone": ""},
                    "incidentLocation": {"text": ""}
                },
                "dispatch": {
                    "reportDatetime": "",
                    "departureTime": "",
                    "arrivalSceneTime": "",
                    "contactTime": "",
                    "distanceKm": null,
                    "departureSceneTime": "",
                    "arrivalHospitalTime": "",
                    "returnTime": "",
                    "dispatchType": "",
                    "sceneLocation": {"name": "", "value": null},
                    "symptoms": {
                        "disease": [{"name": "두통"}],
                        "trauma": [],
                        "otherSymptoms": []
                    }
                },
                "incidentType": {
                    "medicalHistory": {
                        "status": "있음",
                        "items": [{"name": "고혈압"}]
                    },
                    "category": "질병",
                    "subCategory": {"type": "", "name": "", "value": null},
                    "legalSuspicion": {"name": ""}
                },
                "assessment": {
                    "consciousness": {
                        "first": {"time": "", "state": ""},
                        "second": {"time": "", "state": ""}
                    },
                    "pupilReaction": {
                        "left": {"status": "", "reaction": ""},
                        "right": {"status": "", "reaction": ""}
                    },
                    "vitalSigns": {
                        "available": null,
                        "first": {"time": "", "bloodPressure": "", "pulse": null, "respiration": null, "temperature": null, "spo2": null, "bloodSugar": null},
                        "second": {"time": "", "bloodPressure": "", "pulse": null, "respiration": null, "temperature": null, "spo2": null, "bloodSugar": null}
                    },
                    "patientLevel": "",
                    "notes": {"cheifComplaint": "", "onset": "", "note": ""}
                }
            }
        }
    """
    try:
        # 대본이 비어있으면 에러
        if not request.conversation or not request.conversation.strip():
            raise HTTPException(
                status_code=400,
                detail="대본(conversation)이 비어있습니다."
            )
        
        print(f"[통합 API] 대본 처리 시작 (길이: {len(request.conversation)}자)")
        
        # GPT-5로 응급 정보 추출
        result = extract_ems_data_from_conversation(
            conversation=request.conversation,
            temperature=request.temperature
        )
        
        print(f"[통합 API] 처리 완료")
        
        # 순수 JSON 결과만 반환
        return JSONResponse(content=result)
        
    except HTTPException:
        raise
    except Exception as e:
        print(f"[통합 API] 오류 발생: {str(e)}")
        import traceback
        traceback.print_exc()
        
        raise HTTPException(
            status_code=500,
            detail=f"대본 처리 중 오류 발생: {str(e)}"
        )


@router.get("/health")
async def integrated_health_check():
    """
    통합 API 서비스 상태 확인
    
    Returns:
        dict: 서비스 상태 정보
    """
    # OpenAI API 키 확인
    api_key_configured = bool(os.getenv("OPENAI_API_KEY"))
    
    return {
        "status": "healthy" if api_key_configured else "warning",
        "service": "Integrated Conversation Processing Service",
        "features": [
            "LLM (GPT-5, 텍스트 정제 + 응급 정보 구조화)"
        ],
        "api_key_configured": api_key_configured,
        "message": "통합 서비스가 정상 작동 중입니다." if api_key_configured 
                   else "OpenAI API 키가 설정되지 않았습니다."
    }
