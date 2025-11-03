"""
LLM 기반 STT 정제 결과물 스키마 정의

이 파일은 8개의 주요 카테고리로 구성된 응급 환자 정보 스키마를 정의합니다.
GPT-4.1 모델이 STT 결과물을 정제하여 생성하는 JSON 형식의 Pydantic 모델입니다.
"""

from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime


# ============================================================
# 1. 신고자 및 환자 기본 정보 (patientInfo)
# ============================================================

class ReporterInfo(BaseModel):
    """신고자 정보"""
    phone: Optional[str] = Field(None, description="신고자 전화번호 (예: 01012345678)")
    reportMethod: Optional[str] = Field(None, description="신고 방법: 일반전화 | 휴대전화 | 기타")
    isCustom: Optional[bool] = Field(None, description="신고 방법이 커스텀인지 여부")


class PatientBasicInfo(BaseModel):
    """환자 인적 사항"""
    name: Optional[str] = Field(None, description="환자 성명")
    gender: Optional[str] = Field(None, description="성별: 남성 | 여성")
    ageYears: Optional[int] = Field(None, description="나이(세)")
    birthDate: Optional[str] = Field(None, description="생년월일 (YYYY-MM-DD)")
    address: Optional[str] = Field(None, description="환자 주소")


class GuardianInfo(BaseModel):
    """보호자 정보"""
    name: Optional[str] = Field(None, description="보호자 성명")
    relation: Optional[str] = Field(None, description="보호자 관계 (예: 배우자, 자녀, 부모 등)")
    phone: Optional[str] = Field(None, description="보호자 연락처")


class IncidentLocationInfo(BaseModel):
    """환자 발생 위치"""
    text: Optional[str] = Field(None, description="환자 발생 위치 설명")


class PatientInfo(BaseModel):
    """환자 기본 정보 (카테고리 1)"""
    schema_version: Optional[int] = Field(None, description="JSON 스키마 버전 (형식 변경 시 추적용)")
    reporter: Optional[ReporterInfo] = Field(None, description="신고자 정보")
    patient: Optional[PatientBasicInfo] = Field(None, description="환자 인적 사항")
    guardian: Optional[GuardianInfo] = Field(None, description="보호자 정보")
    incidentLocation: Optional[IncidentLocationInfo] = Field(None, description="환자 발생 위치")
    createdAt: Optional[str] = Field(None, description="생성 시각 (ISO-8601)")
    updatedAt: Optional[str] = Field(None, description="수정 시각 (ISO-8601)")


# ============================================================
# 2. 주요 증상 (chiefComplaint)
# ============================================================


# ============================================================
# 3. 의식 상태 (consciousness)
# ============================================================


# ============================================================
# 4. 호흡 상태 (breathing)
# ============================================================


# ============================================================
# 5. 순환 상태 (circulation)
# ============================================================


# ============================================================
# 6. 병력 및 과거력 (medicalHistory)
# ============================================================

# ============================================================
# 7. 사고/외상 정보 (traumaInfo)
# ============================================================


# ============================================================
# 8. 응급 처치 및 조치 사항 (emergencyTreatment)
# ============================================================


# ============================================================
# 통합 응답 모델
# ============================================================

class LLMRefinedResult(BaseModel):
    """
    LLM이 반환하는 전체 정제 결과
    실제로 추출된 정보만 포함됩니다.
    """
    patientInfo: Optional[PatientInfo] = Field(None, description="환자 기본 정보")

# ============================================================
# API 요청/응답 모델
# ============================================================

class STTSegment(BaseModel):
    """STT 화자 분리 세그먼트"""
    id: str = Field(..., description="세그먼트 ID")
    speaker: str = Field(..., description="화자 구분 (A, B, C, D 등)")
    start: float = Field(..., description="시작 시간 (초)")
    end: float = Field(..., description="종료 시간 (초)")
    text: str = Field(..., description="발화 내용")


class STTRefineRequest(BaseModel):
    """STT 결과물 정제 요청"""
    transcription: str = Field(..., description="STT로 변환된 전체 텍스트")
    segments: list[STTSegment] = Field(..., description="화자별 세그먼트 목록")
    context: Optional[str] = Field(None, description="추가 맥락 정보")


class STTRefineResponse(BaseModel):
    """STT 결과물 정제 응답"""
    success: bool = Field(..., description="정제 성공 여부")
    original_text: str = Field(..., description="원본 STT 텍스트")
    corrected_text: str = Field(..., description="맥락 기반 교정된 텍스트")
    structured_data: LLMRefinedResult = Field(..., description="구조화된 환자 정보")
    model_used: str = Field(..., description="사용된 모델명")
    processing_time: float = Field(..., description="처리 시간 (초)")

