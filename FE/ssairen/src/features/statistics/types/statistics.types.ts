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

// 환자 통계 응답
export interface PatientStatisticsResponse {
  byGender: Record<string, number>; // { "M": 120, "F": 95 }
  byAgeGroup: Record<string, number>; // { "0-9": 5, "10-19": 12, "20-29": 25, ... }
  byMentalStatus: Record<string, number>; // { "ALERT": 80, "VERBAL": 30, "PAIN": 15, "UNRESPONSIVE": 10 }
  startDate: string;
  endDate: string;
  totalCount: number;
}

// 환자 통계 API 파라미터
export interface PatientStatisticsParams {
  hospitalId: number;
  startDate: string;
  endDate: string;
}

// 재난 유형별 통계 응답
export interface DisasterTypeStatisticsResponse {
  byDisasterType: Record<string, number>; // { "질병": 120, "교통사고": 95, "추락": 30, ... }
  byDisasterSubtype: Record<string, number>; // { "심정지": 45, "뇌졸중": 75, ... }
  startDate: string;
  endDate: string;
  totalCount: number;
}

// 재난 유형별 통계 API 파라미터
export interface DisasterTypeStatisticsParams {
  hospitalId: number;
  startDate: string;
  endDate: string;
}
