import axiosInstance from '@/lib/apiClient';
import type {
  ApiResponse,
  TimeStatisticsParams,
  TimeStatisticsResponse,
  PatientStatisticsParams,
  PatientStatisticsResponse,
  DisasterTypeStatisticsParams,
  DisasterTypeStatisticsResponse,
} from '../types/statistics.types';

/**
 * 시간별 통계 조회 API
 */
export async function fetchTimeStatisticsApi({
  hospitalId,
  startDate,
  endDate,
}: TimeStatisticsParams): Promise<TimeStatisticsResponse> {
  const response = await axiosInstance<ApiResponse<TimeStatisticsResponse>>({
    method: 'POST',
    url: `/api/hospitals/${hospitalId}/statistics/time`,
    data: {
      startDate,
      endDate,
    },
    requiresAuth: true,
  } as any);

  const apiResponse = response.data;

  if (!apiResponse.success || !apiResponse.data) {
    throw new Error(apiResponse.message || '통계 조회에 실패했습니다.');
  }

  return apiResponse.data;
}

/**
 * 환자 통계 조회 API
 */
export async function fetchPatientStatisticsApi({
  hospitalId,
  startDate,
  endDate,
}: PatientStatisticsParams): Promise<PatientStatisticsResponse> {
  const response = await axiosInstance<ApiResponse<PatientStatisticsResponse>>({
    method: 'POST',
    url: `/api/hospitals/${hospitalId}/statistics/patient`,
    data: {
      startDate,
      endDate,
    },
    requiresAuth: true,
  } as any);

  const apiResponse = response.data;

  if (!apiResponse.success || !apiResponse.data) {
    throw new Error(apiResponse.message || '환자 통계 조회에 실패했습니다.');
  }

  return apiResponse.data;
}

/**
 * 재난 유형별 통계 조회 API
 */
export async function fetchDisasterTypeStatisticsApi({
  hospitalId,
  startDate,
  endDate,
}: DisasterTypeStatisticsParams): Promise<DisasterTypeStatisticsResponse> {
  const response = await axiosInstance<ApiResponse<DisasterTypeStatisticsResponse>>({
    method: 'POST',
    url: `/api/hospitals/${hospitalId}/statistics/disaster-type`,
    data: {
      startDate,
      endDate,
    },
    requiresAuth: true,
  } as any);

  const apiResponse = response.data;

  if (!apiResponse.success || !apiResponse.data) {
    throw new Error(apiResponse.message || '재난 유형별 통계 조회에 실패했습니다.');
  }

  return apiResponse.data;
}
