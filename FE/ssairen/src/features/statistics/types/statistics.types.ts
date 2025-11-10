// 공통 API 응답 래퍼
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
  timestamp: string;
}

// 통계 조회 요청
export interface StatisticsRequest {
  startDate: string; // "2025-01-01" 형식
  endDate: string; // "2025-01-31" 형식
}

// 시간별 통계 응답
export interface TimeStatisticsResponse {
  byDayOfWeek: Record<string, number>; // { "MONDAY": 45, "TUESDAY": 52, ... }
  byHour: Record<string, number>; // { "0": 12, "1": 8, ..., "23": 15 }
  startDate: string;
  endDate: string;
  totalCount: number;
}

// 시간별 통계 API 파라미터
export interface TimeStatisticsParams {
  hospitalId: number;
  startDate: string;
  endDate: string;
}
