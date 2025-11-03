import type { FetchPatientsParams, PatientsResponse, Patient } from '../types/patient.types';
import axiosInstance from '@/lib/apiClient';

export interface FetchPatientsResult {
  patients: Patient[];
  totalPages: number;
  totalElements: number;
  currentPage: number;
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

  const response = await axiosInstance<PatientsResponse>({
    method: 'GET',
    url: `/api/hospitals/${hospitalId}/patients?${params}`,
    requiresAuth: true, // interceptor에서 헤더 추가
  } as any);

  // API 응답 구조: { success, data: { content, page, size, totalElements, totalPages }, message, timestamp }
  const paginatedData = response.data.data;

  return {
    patients: paginatedData.content,
    totalPages: paginatedData.totalPages,
    totalElements: paginatedData.totalElements,
    currentPage: paginatedData.page,
  };
}
