import { LoginRequest, TokenResponse, ApiResponse } from '../types/auth.types'
import axiosInstance from '@/lib/apiClient'

/**
 * 로그인 API 호출
 * @param credentials - 사용자 인증 정보 (userType, username, password)
 * @returns 로그인 성공 시 토큰과 사용자 정보 반환
 */
export const loginApi = async (credentials: LoginRequest): Promise<TokenResponse> => {
  try {
    // 로그인은 인증이 필요 없으므로 requiresAuth: false
    const response = await axiosInstance<ApiResponse<TokenResponse>>({
      method: 'POST',
      url: '/api/auth/login',
      data: credentials,
      requiresAuth: false,
    } as any)

    const apiResponse = response.data

    if (!apiResponse.success || !apiResponse.data) {
      throw new Error(apiResponse.message || '로그인에 실패했습니다.')
    }

    const tokenData = apiResponse.data

    // 토큰을 로컬 스토리지에 저장
    if (tokenData.accessToken) {
      localStorage.setItem('accessToken', tokenData.accessToken)
    }
    if (tokenData.refreshToken) {
      localStorage.setItem('refreshToken', tokenData.refreshToken)
    }

    return tokenData
  } catch (error) {
    if (error instanceof Error) {
      throw error
    }
    throw new Error('네트워크 오류가 발생했습니다.')
  }
}

/**
 * 로그아웃 API 호출
 */
export const logoutApi = async (): Promise<void> => {
  try {
    await axiosInstance({
      method: 'POST',
      url: '/api/auth/logout',
      requiresAuth: true,
    } as any)
  } finally {
    // 에러 여부와 관계없이 로컬 스토리지 정리
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }
}

/**
 * 토큰 검증 및 사용자 정보 조회
 */
export const getCurrentUserApi = async () => {
  try {
    const response = await axiosInstance({
      method: 'GET',
      url: '/api/auth/me',
      requiresAuth: true,
    } as any)
    return response.data
  } catch (error) {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    throw error
  }
}
