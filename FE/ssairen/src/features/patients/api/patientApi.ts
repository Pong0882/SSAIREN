import type { FetchPatientsParams, PatientsResponse, Patient } from '../types/patient.types';
import axiosInstance from '@/lib/apiClient';

export interface FetchPatientsResult {
  patients: Patient[];
  totalPages: number;
  totalElements: number;
  currentPage: number;
}

export interface AcceptPatientResponse {
  success: boolean;
  message: string;
  data?: any;
}

export async function fetchPatientsApi({
  hospitalId,
  page = 1,
  size = 10,
  status = 'ALL',
}: FetchPatientsParams): Promise<FetchPatientsResult> {
  const params = new URLSearchParams({
    page: String(page - 1), // API가 0-based indexing을 사용한다고 가정
    size: String(size),
  });

  if (status !== 'ALL') {
    params.append('status', status);
  }

  console.log('📤 API 요청:', {
    hospitalId,
    page: page - 1,
    size,
    status,
    url: `/api/hospitals/${hospitalId}/patients?${params}`,
  });

  const response = await axiosInstance<PatientsResponse>({
    method: 'GET',
    url: `/api/hospitals/${hospitalId}/patients?${params}`,
    requiresAuth: true, // interceptor에서 헤더 추가
  } as any);

  // API 응답 구조: { success, data: { content, page, size, totalElements, totalPages }, message, timestamp }
  const paginatedData = response.data.data;

  console.log('📥 API 응답:', {
    받은데이터개수: paginatedData.content.length,
    요청한size: size,
    totalPages: paginatedData.totalPages,
    totalElements: paginatedData.totalElements,
    currentPage: paginatedData.page,
  });

  return {
    patients: paginatedData.content,
    totalPages: paginatedData.totalPages,
    totalElements: paginatedData.totalElements,
    currentPage: paginatedData.page,
  };
}

/**
 * 환자 수용 가능 API
 * @param hospitalSelectionId - 병원 선택 ID
 */
export async function acceptPatientApi(
  hospitalSelectionId: number
): Promise<AcceptPatientResponse> {
  console.log('✅ 수용가능 API 호출:', { hospitalSelectionId });

  const response = await axiosInstance<AcceptPatientResponse>({
    method: 'PATCH',
    url: `/api/hospital-selection/${hospitalSelectionId}`,
    data: {
      status: 'ACCEPTED',
    },
    requiresAuth: true,
  } as any);

  console.log('✅ 수용가능 API 응답:', response.data);

  return response.data;
}

/**
 * 환자 거절 API
 * @param hospitalSelectionId - 병원 선택 ID
 */
export async function rejectPatientApi(
  hospitalSelectionId: number
): Promise<AcceptPatientResponse> {
  console.log('❌ 거절 API 호출:', { hospitalSelectionId });

  const response = await axiosInstance<AcceptPatientResponse>({
    method: 'PATCH',
    url: `/api/hospital-selection/${hospitalSelectionId}`,
    data: {
      status: 'REJECTED',
    },
    requiresAuth: true,
  } as any);

  console.log('❌ 거절 API 응답:', response.data);

  return response.data;
}

/**
 * 전화 요망 API
 * @param hospitalSelectionId - 병원 선택 ID
 */
export async function callRequestApi(
  hospitalSelectionId: number
): Promise<AcceptPatientResponse> {
  console.log('📞 전화요망 API 호출:', { hospitalSelectionId });

  const response = await axiosInstance<AcceptPatientResponse>({
    method: 'PATCH',
    url: `/api/hospital-selection/${hospitalSelectionId}`,
    data: {
      status: 'CALLREQUEST',
    },
    requiresAuth: true,
  } as any);

  console.log('📞 전화요망 API 응답:', response.data);

  return response.data;
}
