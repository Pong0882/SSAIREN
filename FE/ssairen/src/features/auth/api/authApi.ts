import { LoginRequest, TokenResponse, ApiResponse } from '../types/auth.types'

// API 기본 URL (환경변수로 관리)
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

/**
 * 로그인 API 호출
 * @param credentials - 사용자 인증 정보 (userType, username, password)
 * @returns 로그인 성공 시 토큰과 사용자 정보 반환
 */
export const loginApi = async (credentials: LoginRequest): Promise<TokenResponse> => {
  try {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(credentials),
    })

    if (!response.ok) {
      // HTTP 에러 처리
      const errorData: ApiResponse<null> = await response.json().catch(() => null)
      throw new Error(errorData?.error?.message || errorData?.message || '로그인에 실패했습니다.')
    }

    // 백엔드는 ApiResponse<TokenResponse> 형식으로 응답
    const apiResponse: ApiResponse<TokenResponse> = await response.json()

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
    const token = localStorage.getItem('accessToken')

    if (token) {
      await fetch(`${API_BASE_URL}/auth/logout`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      })
    }
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
  const token = localStorage.getItem('accessToken')

  if (!token) {
    throw new Error('인증 토큰이 없습니다.')
  }

  const response = await fetch(`${API_BASE_URL}/auth/me`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  })

  if (!response.ok) {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    throw new Error('인증이 만료되었습니다.')
  }

  return response.json()
}
