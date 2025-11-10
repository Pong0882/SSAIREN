import axiosInstance from '@/lib/apiClient';
import type {
  ApiResponse,
  TimeStatisticsParams,
  TimeStatisticsResponse,
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
