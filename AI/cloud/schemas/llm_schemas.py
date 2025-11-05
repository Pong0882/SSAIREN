"""
LLM 기반 STT 정제 결과물 스키마 정의

이 파일은 8개의 주요 카테고리로 구성된 응급 환자 정보 스키마를 정의합니다.
GPT-4.1 모델이 STT 결과물을 정제하여 생성하는 JSON 형식의 Pydantic 모델입니다.
"""

from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime


# ============================================================
# 1. 환자 정보 (patientInfo)
# ============================================================

class ReporterInfo(BaseModel):
    """신고자 정보"""
    phone: Optional[str] = Field(None, description="신고자 전화번호 (예: 01012345678)")
    reportMethod: Optional[str] = Field(None, description="신고 방법: 일반전화 | 휴대전화 | 기타")
    value: Optional[str] = Field(None, description="신고 방법이 '기타'일 경우 상세 내용")


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
# 2. 구급 출동 (dispatch)
# ============================================================

class SceneLocation(BaseModel):
    """환자 발생 장소"""
    name: Optional[str] = Field(None, description="장소명 (집 | 집단거주시설 | 도로 | 도로외교통지역 | 오락/문화/공시설 | 학교/교육시설 | 운동시설 | 상업시설 | 의료관련시설 | 공장/산업/건설시설 | 일차산업장 | 바다/강/산/논밭 | 기타)")
    value: Optional[str] = Field(None, description="장소가 '기타'일 경우 상세 내용")


class SymptomItem(BaseModel):
    """증상 항목"""
    name: str = Field(..., description="증상명")
    value: Optional[str] = Field(None, description="추가 상세 정보 (필요 시)")


class SymptomsInfo(BaseModel):
    """환자 증상 정보"""
    pain: Optional[list[SymptomItem]] = Field(None, description="통증 관련 증상")
    trauma: Optional[list[SymptomItem]] = Field(None, description="외상 관련 증상")
    otherSymptoms: Optional[list[SymptomItem]] = Field(None, description="기타 증상")


class Dispatch(BaseModel):
    """구급 출동 정보 (카테고리 2)"""
    schema_version: Optional[int] = Field(None, description="JSON 스키마 버전")
    reportDatetime: Optional[str] = Field(None, description="신고 일시 (ISO-8601)")
    departureTime: Optional[str] = Field(None, description="출동 시각 (HH:MM)")
    arrivalSceneTime: Optional[str] = Field(None, description="현장 도착 시각")
    contactTime: Optional[str] = Field(None, description="환자 접촉 시각")
    distanceKm: Optional[float] = Field(None, description="이동 거리 (km)")
    departureSceneTime: Optional[str] = Field(None, description="현장 출발 시각")
    arrivalHospitalTime: Optional[str] = Field(None, description="병원 도착 시각")
    returnTime: Optional[str] = Field(None, description="귀소 시간")
    dispatchType: Optional[str] = Field(None, description="출동 유형: 정상 | 오인 | 거짓 | 취소 | 기타")
    sceneLocation: Optional[SceneLocation] = Field(None, description="환자 발생 장소")
    symptoms: Optional[SymptomsInfo] = Field(None, description="환자 증상")
    createdAt: Optional[str] = Field(None, description="생성 시각 (ISO-8601)")
    updatedAt: Optional[str] = Field(None, description="수정 시각 (ISO-8601)")


# ============================================================
# 3. 환자 발생 유형 (incidentType)
# ============================================================

class MedicalHistoryItem(BaseModel):
    """병력 항목"""
    name: str = Field(..., description="병력명")
    value: Optional[str] = Field(None, description="추가 상세 내용 (암, 감염병, 신부전, 기타 등)")


class MedicalHistoryInfo(BaseModel):
    """병력 정보"""
    status: Optional[str] = Field(None, description="병력 유무: 있음 | 없음 | 미상")
    items: Optional[list[MedicalHistoryItem]] = Field(None, description="병력 목록")


class SubCategory(BaseModel):
    """세부 카테고리"""
    type: Optional[str] = Field(None, description="유형")
    name: str = Field(..., description="세부 항목명")
    value: Optional[str] = Field(None, description="추가 상세 내용 (동물/곤충, 그 밖의 탈 것, 기타 등)")


class LegalSuspicionInfo(BaseModel):
    """법적 의심 관련 정보"""
    name: Optional[str] = Field(None, description="범죄의심 세부: 경찰통보 | 경찰인계 | 긴급이송 | 관련기관 통보")


class IncidentType(BaseModel):
    """환자 발생 유형 (카테고리 3)"""
    schema_version: Optional[int] = Field(None, description="JSON 스키마 버전")
    medicalHistory: Optional[MedicalHistoryInfo] = Field(None, description="병력 정보")
    category: Optional[str] = Field(None, description="환자 발생 분류: 질병 | 질병외 | 기타")
    subCategory_traffic: Optional[SubCategory] = Field(None, description="교통사고 세부")
    subCategory_injury: Optional[SubCategory] = Field(None, description="그 외 손상 세부")
    subCategory_nonTrauma: Optional[SubCategory] = Field(None, description="비외상성 손상 세부")
    category_other: Optional[str] = Field(None, description="기타 분류")
    subCategory_other: Optional[SubCategory] = Field(None, description="기타 세부")
    legalSuspicion: Optional[LegalSuspicionInfo] = Field(None, description="법적 의심 관련")
    createdAt: Optional[str] = Field(None, description="생성 시각 (ISO-8601)")
    updatedAt: Optional[str] = Field(None, description="수정 시각 (ISO-8601)")


# ============================================================
# 4. 환자 평가 (patientAssessment)
# ============================================================

class ConsciousnessCheck(BaseModel):
    """의식 상태 체크"""
    time: Optional[str] = Field(None, description="측정 시각 (HH:MM)")
    state: Optional[str] = Field(None, description="의식 수준: A(명료) | V(음성에 반응) | P(통증에 반응) | U(무반응)")


class ConsciousnessInfo(BaseModel):
    """의식 상태 정보"""
    first: Optional[ConsciousnessCheck] = Field(None, description="1차 평가")
    second: Optional[ConsciousnessCheck] = Field(None, description="2차 평가")


class PupilStatus(BaseModel):
    """동공 상태"""
    status: Optional[str] = Field(None, description="상태: 정상 | 축동 | 산동 | 부동 | 측정불가")
    reaction: Optional[str] = Field(None, description="반응: 반응 | 무반응 | 측정불가")


class PupilReactionInfo(BaseModel):
    """동공 반응 정보"""
    left: Optional[PupilStatus] = Field(None, description="좌측 눈")
    right: Optional[PupilStatus] = Field(None, description="우측 눈")


class VitalSignsMeasurement(BaseModel):
    """활력징후 측정"""
    time: Optional[str] = Field(None, description="측정 시각 (HH:MM)")
    bloodPressure: Optional[str] = Field(None, description="혈압 (mmHg)")
    pulse: Optional[int] = Field(None, description="맥박 (회/min)")
    respiration: Optional[int] = Field(None, description="호흡 (회/min)")
    temperature: Optional[float] = Field(None, description="체온 (°C)")
    spo2: Optional[int] = Field(None, description="산소포화도 (%)")
    bloodSugar: Optional[int] = Field(None, description="혈당 (mg/dL)")


class VitalSignsInfo(BaseModel):
    """활력징후 정보"""
    available: Optional[str] = Field(None, description="측정 가능 여부: null(가능) | 불가 | 거부")
    first: Optional[VitalSignsMeasurement] = Field(None, description="1차 측정")
    second: Optional[VitalSignsMeasurement] = Field(None, description="2차 측정")


class NotesInfo(BaseModel):
    """기록 정보"""
    cheifComplaint: Optional[str] = Field(None, description="주호소")
    onset: Optional[str] = Field(None, description="발생 시각 (HH:MM)")
    note: Optional[str] = Field(None, description="상세 기록")


class PatientAssessment(BaseModel):
    """환자 평가 (카테고리 4)"""
    schema_version: Optional[int] = Field(None, description="JSON 스키마 버전")
    consciousness: Optional[ConsciousnessInfo] = Field(None, description="의식 상태")
    pupilReaction: Optional[PupilReactionInfo] = Field(None, description="동공 반응")
    vitalSigns: Optional[VitalSignsInfo] = Field(None, description="활력징후")
    patientLevel: Optional[str] = Field(None, description="환자 분류: LEVEL1 | LEVEL2 | LEVEL3 | LEVEL4 | LEVEL5")
    notes: Optional[NotesInfo] = Field(None, description="기록")
    createdAt: Optional[str] = Field(None, description="생성 시각 (ISO-8601)")
    updatedAt: Optional[str] = Field(None, description="수정 시각 (ISO-8601)")


# ============================================================
# 5. 응급처치 (emergencyTreatment)
# ============================================================

class AirwayManagementInfo(BaseModel):
    """기도 확보 관련"""
    methods: Optional[list[str]] = Field(None, description="기도 확보 방법: 도수조작 | 기도유지기 | 기관삽관 | 성문외기도유지기 | 흡인기 | 기도폐쇄처치")


class OxygenTherapyInfo(BaseModel):
    """산소 공급 정보"""
    flowRateLpm: Optional[int] = Field(None, description="산소 공급량 (L/min)")
    device: Optional[str] = Field(None, description="사용 장비: 비관 | 안면마스크 | 비재호흡마스크 | BVM | 산소소생기 | 네뷸라이저 | 기타")


class AEDInfo(BaseModel):
    """자동제세동기(AED) 정보"""
    type: Optional[str] = Field(None, description="AED 사용 세부: shock | monitoring | 기타")
    value: Optional[str] = Field(None, description="AED 사용이 '기타'일 경우 상세 내용")


class CirculationInfo(BaseModel):
    """순환보조 정보"""
    type: Optional[str] = Field(None, description="순환보조 세부: 정맥로 확보 | 수액공급 확보")
    value: Optional[str] = Field(None, description="수액공급량 (cc) - type이 '수액공급 확보'일 경우 필수")


class EmergencyTreatment(BaseModel):
    """응급처치 (카테고리 5)"""
    schema_version: Optional[int] = Field(None, description="JSON 스키마 버전")
    airwayManagement: Optional[AirwayManagementInfo] = Field(None, description="기도 확보")
    oxygenTherapy: Optional[OxygenTherapyInfo] = Field(None, description="산소 공급")
    cpr: Optional[str] = Field(None, description="심폐소생술: 실시 | 거부 | DNR | 유보")
    ecg: Optional[bool] = Field(None, description="심전도 실시 여부")
    aed: Optional[AEDInfo] = Field(None, description="자동제세동기(AED)")
    notes: Optional[str] = Field(None, description="기타 비고")
    circulation: Optional[CirculationInfo] = Field(None, description="순환보조")
    drug: Optional[str] = Field(None, description="약물 비고")
    fixed: Optional[str] = Field(None, description="고정: 목뼈 | 척추 | 부목 | 머리")
    woundCare: Optional[str] = Field(None, description="상처처치: 지혈 | 상처 소독 처리")
    deliverytime: Optional[str] = Field(None, description="분만 시간 (HH:MM)")
    temperature: Optional[str] = Field(None, description="보온: 온 | 냉")
    createdAt: Optional[str] = Field(None, description="생성 시각 (ISO-8601)")
    updatedAt: Optional[str] = Field(None, description="수정 시각 (ISO-8601)")


# ============================================================
# 6. 의료지도 (medicalGuidance)
# ============================================================

class RequestMethodInfo(BaseModel):
    """의료지도 요청 방법"""
    type: Optional[str] = Field(None, description="요청 방법: 일반전화 | 휴대전화 | 무전기 | 기타")
    value: Optional[str] = Field(None, description="휴대전화일 경우 음성/화상, 기타 선택 시 상세 내용")


class GuidanceAgencyInfo(BaseModel):
    """의료지도 기관 정보"""
    type: Optional[str] = Field(None, description="기관 유형: 소방 | 병원 | 기타")
    value: Optional[str] = Field(None, description="기관 유형이 '기타'일 경우 상세 내용")


class GuidanceDoctorInfo(BaseModel):
    """의료지도 의사 정보"""
    name: Optional[str] = Field(None, description="의료지도 의사 성명")


class GuidanceItem(BaseModel):
    """의료지도 세부 지시 항목"""
    name: str = Field(..., description="지시 항목명")
    value: Optional[str] = Field(None, description="추가 상세 내용 (기타 선택 시 등)")


class GuidanceContentInfo(BaseModel):
    """의료지도 내용"""
    emergencyTreatment: Optional[list[GuidanceItem]] = Field(None, description="응급처치 관련 지시")
    medication: Optional[list[GuidanceItem]] = Field(None, description="약물 투여 관련 지시")
    hospitalRequest: Optional[bool] = Field(None, description="병원 선정 지시 여부")
    patientEvaluation: Optional[bool] = Field(None, description="환자 평가 지시 여부")
    cprTransfer: Optional[bool] = Field(None, description="CPR 유보/중단 여부")
    transferRefusal: Optional[bool] = Field(None, description="이송 거절 여부")
    transferRejection: Optional[bool] = Field(None, description="이송 거부 여부")
    notes: Optional[str] = Field(None, description="기타 비고")


class MedicalGuidance(BaseModel):
    """의료지도 (카테고리 6)"""
    schema_version: Optional[int] = Field(None, description="JSON 스키마 버전")
    contactStatus: Optional[str] = Field(None, description="의료지도 연결 여부: 연결 | 미연결")
    requestTime: Optional[str] = Field(None, description="의료지도 요청 시각 (HH:MM)")
    requestMethod: Optional[RequestMethodInfo] = Field(None, description="의료지도 요청 방법")
    guidanceAgency: Optional[GuidanceAgencyInfo] = Field(None, description="의료지도 기관")
    guidanceDoctor: Optional[GuidanceDoctorInfo] = Field(None, description="의료지도 의사")
    guidanceContent: Optional[GuidanceContentInfo] = Field(None, description="의료지도 내용")
    createdAt: Optional[str] = Field(None, description="생성 시각 (ISO-8601)")
    updatedAt: Optional[str] = Field(None, description="수정 시각 (ISO-8601)")


# ============================================================
# 7. 환자 이송 (patientTransport)
# ============================================================

class ReceiverSignInfo(BaseModel):
    """환자 인수자 서명 정보"""
    type: Optional[str] = Field(None, description="서명 이미지 MIME 타입 (예: image/png, image/jpeg)")
    data: Optional[str] = Field(None, description="Base64로 인코딩된 서명 이미지")


class RetransportReason(BaseModel):
    """재이송 사유"""
    type: str = Field(..., description="재이송 사유 유형")
    name: Optional[list[str]] = Field(None, description="병상부족 선택 시 병상 세부 (예: 응급실, 중환자실 등)")
    value: Optional[str] = Field(None, description="기타 또는 추가 설명이 필요한 경우")


class TransportInfo(BaseModel):
    """이송 정보"""
    hospitalName: Optional[str] = Field(None, description="이송 병원명")
    regionType: Optional[str] = Field(None, description="관할 구분: 관할 | 타시·도")
    arrivalTime: Optional[str] = Field(None, description="도착 시각 (HH:MM)")
    distanceKm: Optional[float] = Field(None, description="이송 거리 (km)")
    selectedBy: Optional[str] = Field(None, description="의료기관 선정자: 구급대 | 119상황실 | 구급상황센터 | 환자보호자 | 병원수용곤란등 | 기타")
    selectedByValue: Optional[str] = Field(None, description="선정자가 '기타'일 경우 상세 내용")
    retransportReason: Optional[list[RetransportReason]] = Field(None, description="재이송 사유")
    receiver: Optional[str] = Field(None, description="환자 인수자: 의사 | 간호사 | 응급구조사 | 기타")
    receiverValue: Optional[str] = Field(None, description="환자 인수자가 '기타'일 경우 상세 내용")
    receiverSign: Optional[ReceiverSignInfo] = Field(None, description="환자 인수자 서명 정보")


class PatientTransport(BaseModel):
    """환자 이송 (카테고리 7)"""
    schema_version: Optional[int] = Field(None, description="JSON 스키마 버전")
    firstTransport: Optional[TransportInfo] = Field(None, description="1차 이송 정보")
    secondTransport: Optional[TransportInfo] = Field(None, description="2차 이송 정보")
    createdAt: Optional[str] = Field(None, description="생성 시각 (ISO-8601)")
    updatedAt: Optional[str] = Field(None, description="수정 시각 (ISO-8601)")


# ============================================================
# 8. 세부상황표 (detailReport)
# ============================================================

class DoctorDetail(BaseModel):
    """의사 정보"""
    affiliation: Optional[str] = Field(None, description="소속 기관")
    name: Optional[str] = Field(None, description="의사 성명")
    signature: Optional[str] = Field(None, description="의사 서명 데이터(Base64)")


class PersonnelDetail(BaseModel):
    """인원 정보"""
    grade: Optional[str] = Field(None, description="구급자 등급: 1급 | 2급 | 간호사 | 구급교육 | 기타")
    rank: Optional[str] = Field(None, description="계급")
    name: Optional[str] = Field(None, description="성명")
    signature: Optional[str] = Field(None, description="서명 데이터(Base64)")


class ObstaclesInfo(BaseModel):
    """장애 요인 정보"""
    type: Optional[str] = Field(None, description="장애 요인 유형")
    value: Optional[str] = Field(None, description="장애 요인이 '기타'일 경우 상세 내용")


class DetailReport(BaseModel):
    """세부상황표 (카테고리 8)"""
    schema_version: Optional[int] = Field(None, description="JSON 스키마 버전")
    doctor: Optional[DoctorDetail] = Field(None, description="의사 정보")
    paramedic1: Optional[PersonnelDetail] = Field(None, description="구급대원 1")
    paramedic2: Optional[PersonnelDetail] = Field(None, description="구급대원 2")
    driver: Optional[PersonnelDetail] = Field(None, description="운전 요원")
    other: Optional[PersonnelDetail] = Field(None, description="기타 인원")
    obstacles: Optional[ObstaclesInfo] = Field(None, description="장애 요인")
    createdAt: Optional[str] = Field(None, description="생성 시각 (ISO-8601)")
    updatedAt: Optional[str] = Field(None, description="수정 시각 (ISO-8601)")


# ============================================================
# 통합 응답 모델
# ============================================================

class LLMRefinedResult(BaseModel):
    """
    LLM이 반환하는 전체 정제 결과
    실제로 추출된 정보만 포함됩니다.
    """
    patientInfo: Optional[PatientInfo] = Field(None, description="환자 기본 정보")
    dispatch: Optional[Dispatch] = Field(None, description="구급 출동 정보")
    incidentType: Optional[IncidentType] = Field(None, description="환자 발생 유형")
    patientAssessment: Optional[PatientAssessment] = Field(None, description="환자 평가")
    emergencyTreatment: Optional[EmergencyTreatment] = Field(None, description="응급처치")
    medicalGuidance: Optional[MedicalGuidance] = Field(None, description="의료지도")
    patientTransport: Optional[PatientTransport] = Field(None, description="환자 이송")
    detailReport: Optional[DetailReport] = Field(None, description="세부상황표")

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

