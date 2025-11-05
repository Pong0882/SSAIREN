"""
응급실 검색 및 추천 라우터

백엔드로부터 환자 상태와 위치 정보를 받아
주변 응급실을 검색하고 GPT를 통해 적절한 병원을 추천합니다.
"""

from fastapi import APIRouter, HTTPException
from schemas.emergency_schemas import EmergencyRecommendationRequest, EmergencyRecommendationResponse
from services.emergency_service import recommend_emergency_hospitals

router = APIRouter(prefix="/emergency", tags=["emergency"])


@router.post("/recommend", response_model=EmergencyRecommendationResponse)
async def recommend_hospitals(request: EmergencyRecommendationRequest):
    """
    환자 상태와 위치를 기반으로 적합한 응급실 추천

    Args:
        request: {
            "patient_condition": "환자 상태 정보 (일반 텍스트 또는 JSON 문자열)",
            "latitude": 위도,
            "longitude": 경도,
            "radius": 검색 반경(km, 기본값 10)
        }

    Returns:
        EmergencyRecommendationResponse: {
            "success": bool,
            "recommended_hospitals": ["병원1", "병원2", ...],
            "total_hospitals_found": int,
            "hospitals_detail": [...],
            "gpt_reasoning": "추천 이유",
            "error_message": "오류 메시지 (실패 시)"
        }
    """
    try:
        # 입력 검증
        if request.radius <= 0 or request.radius > 100:
            raise HTTPException(status_code=400, detail="검색 반경은 1~100km 사이여야 합니다.")

        # 응급실 검색 및 추천
        result = recommend_emergency_hospitals(
            patient_condition=request.patient_condition,
            latitude=request.latitude,
            longitude=request.longitude,
            radius=request.radius
        )

        # 응답 생성 (병원 정보는 Dict 그대로 반환)
        return EmergencyRecommendationResponse(
            success=result["success"],
            recommended_hospitals=result["recommended_hospitals"],
            total_hospitals_found=result["total_hospitals_found"],
            hospitals_detail=result.get("hospitals_detail"),
            gpt_reasoning=result.get("gpt_reasoning"),
            error_message=result.get("error_message")
        )

    except HTTPException:
        raise
    except Exception as e:
        print(f"[응급실 추천 API 오류] {e}")
        raise HTTPException(status_code=500, detail=f"서버 오류: {str(e)}")
