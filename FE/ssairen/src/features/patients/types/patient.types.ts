export interface RecordTime {
  hour: number;
  minute: number;
  second: number;
  nano: number;
}

export interface Patient {
  hospitalSelectionId: number;
  emergencyReportId: number;
  gender: string;
  age: number;
  recordTime: RecordTime;
  chiefComplaint: string;
  mentalStatus: string;
  status: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
  timestamp: string;
}

export interface PaginatedData {
  content: Patient[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface PatientsResponse {
  success: boolean;
  data: PaginatedData;
  message: string;
  timestamp: string;
}

export interface FetchPatientsParams {
  hospitalId: number;
  page?: number;
  size?: number;
  status?: "ACCEPTED" | "COMPLETED" | "ALL";
}
