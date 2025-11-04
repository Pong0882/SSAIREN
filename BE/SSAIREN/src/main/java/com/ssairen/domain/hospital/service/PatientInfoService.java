package com.ssairen.domain.hospital.service;

import com.ssairen.domain.hospital.dto.PatientInfoCreateRequest;
import com.ssairen.domain.hospital.dto.PatientInfoResponse;

/**
 * 환자 정보 서비스 인터페이스
 */
public interface PatientInfoService {

    /**
     * 환자 정보 생성
     *
     * @param request     환자 정보 생성 요청
     * @param paramedicId 구급대원 ID (권한 검증용)
     * @return 생성된 환자 정보
     */
    PatientInfoResponse createPatientInfo(PatientInfoCreateRequest request, Integer paramedicId);

    /**
     * 환자 정보 조회 (구급일지 ID로)
     *
     * @param emergencyReportId 구급일지 ID
     * @param paramedicId       구급대원 ID (권한 검증용)
     * @return 환자 정보
     */
    PatientInfoResponse getPatientInfo(Long emergencyReportId, Integer paramedicId);

    /**
     * 환자 정보 수정
     *
     * @param emergencyReportId 구급일지 ID
     * @param request           환자 정보 수정 요청
     * @param paramedicId       구급대원 ID (권한 검증용)
     * @return 수정된 환자 정보
     */
    PatientInfoResponse updatePatientInfo(Long emergencyReportId, PatientInfoCreateRequest request, Integer paramedicId);
}
