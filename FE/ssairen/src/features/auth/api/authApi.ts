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

    // 토큰은 zustand store의 setAuth에서 저장하므로 여기서는 반환만
    return apiResponse.data
  } catch (error) {
    if (error instanceof Error) {
      throw error
    }
    throw new Error('네트워크 오류가 발생했습니다.')
  }
}

/**
 * 로그아웃 API 호출
 * 토큰 정리는 zustand store의 clearAuth에서 처리
 */
export const logoutApi = async (): Promise<void> => {
  await axiosInstance({
    method: 'POST',
    url: '/api/auth/logout',
    requiresAuth: true,
  } as any)
}

/**
 * 토큰 갱신 API 호출
 * @param refreshToken - 리프레시 토큰
 * @returns 새로운 accessToken과 refreshToken
 * 토큰 저장은 zustand store의 updateTokens에서 처리
 */
export const refreshTokenApi = async (refreshToken: string): Promise<TokenResponse> => {
  try {
    const response = await axiosInstance<ApiResponse<TokenResponse>>({
      method: 'POST',
      url: '/api/auth/refresh',
      data: { refreshToken },
      requiresAuth: false,
    } as any)

    const apiResponse = response.data

    if (!apiResponse.success || !apiResponse.data) {
      throw new Error(apiResponse.message || '토큰 갱신에 실패했습니다.')
    }

    return apiResponse.data
  } catch (error) {
    if (error instanceof Error) {
      throw error
    }
    throw new Error('토큰 갱신에 실패했습니다.')
  }
}

/**
 * 토큰 검증 및 사용자 정보 조회
 */
export const getCurrentUserApi = async () => {
  const response = await axiosInstance({
    method: 'GET',
    url: '/api/auth/me',
    requiresAuth: true,
  } as any)
  return response.data
}
