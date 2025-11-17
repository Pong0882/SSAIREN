// 사용자 타입 (백엔드와 동일)
export type UserType = 'PARAMEDIC' | 'HOSPITAL'

// 로그인 요청 데이터 타입 (백엔드 LoginRequest와 일치)
export interface LoginRequest {
  userType: UserType
  username: string
  password: string
}

// 로그인 응답 데이터 타입 (백엔드 TokenResponse와 일치)
export interface TokenResponse {
  accessToken: string
  refreshToken: string
  userType: UserType
  userId: number
  username: string
  tokenType: string
  // 구급대원 상세 정보 (PARAMEDIC인 경우)
  name?: string
  rank?: string
  status?: string
  fireStateId?: number
  fireStateName?: string
  // 병원 상세 정보 (HOSPITAL인 경우)
  officialName?: string
}

// 백엔드 ApiResponse wrapper
export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
  error?: {
    message: string
    code?: string
  }
  status?: number
  timestamp?: string
}

// 사용자 정보 타입 (Zustand store용)
export interface User {
  id: number
  username: string
  userType: UserType
  name?: string
  email?: string
  role?: string
  officialName?: string // 병원 공식 명칭
}

// 로그인 폼 데이터 타입
export interface LoginFormData {
  userType: UserType
  username: string
  password: string
}

// 로그인 에러 타입
export interface AuthError {
  message: string
  code?: string
}
