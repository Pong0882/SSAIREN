"""
응급실 검색 및 추천 스키마 정의

백엔드로부터 환자 상태와 위치 정보를 받아 주변 응급실을 검색하고
GPT를 통해 적절한 병원을 추천하는 API의 스키마입니다.
"""

from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any, Union


class EmergencyRecommendationRequest(BaseModel):
    """응급실 추천 요청"""
    patient_condition: str = Field(..., description="환자 상태 정보 (일반 텍스트 또는 JSON 문자열)")
    latitude: float = Field(..., description="현재 위치 위도")
    longitude: float = Field(..., description="현재 위치 경도")
    radius: int = Field(default=10, description="검색 반경(km, 기본값: 10km)")


class HospitalInfo(BaseModel):
    """병원 정보"""
    emergency_room_nickname: str = Field(..., description="응급실 닉네임")
    emergency_room_name: Optional[str] = Field(None, description="응급실 정식명")
    distance: Optional[str] = Field(None, description="거리(km)")
    address: str = Field(..., description="주소")
    emergency_institution_type: Optional[str] = Field(None, description="기관 유형")
    hotline_tel: Optional[str] = Field(None, description="전화번호")
    general_emergency_available: Optional[str] = Field(None, description="일반응급 가능 병상")
    general_emergency_total: Optional[str] = Field(None, description="일반응급 총 병상")
    child_emergency_available: Optional[str] = Field(None, description="소아응급 가능 병상")
    child_emergency_total: Optional[str] = Field(None, description="소아응급 총 병상")
    delivery_room_available: Optional[str] = Field(None, description="분만실 가능 여부")
    delivery_room_total: Optional[str] = Field(None, description="분만실 총 개수")
    npir_available: Optional[str] = Field(None, description="중증외상(NPIR) 가능 병상")
    npir_total: Optional[str] = Field(None, description="중증외상(NPIR) 총 병상")
    general_available: Optional[str] = Field(None, description="일반병상 가능")
    general_total: Optional[str] = Field(None, description="일반병상 총")
    er_messages: Optional[List[dict]] = Field(None, description="응급실 안내 메시지")
    unavailable_messages: Optional[List[dict]] = Field(None, description="진료 불가 정보")


class EmergencyRecommendationResponse(BaseModel):
    """응급실 추천 응답"""
    success: bool = Field(..., description="요청 성공 여부")
    recommended_hospitals: List[str] = Field(..., description="추천 가능한 병원 이름 리스트")
    total_hospitals_found: int = Field(..., description="검색된 총 병원 수")
    gpt_reasoning: Optional[str] = Field(None, description="상위 5개 우선순위 추천 이유 + 제외된 병원 이유")
    error_message: Optional[str] = Field(None, description="오류 메시지 (실패 시)")
