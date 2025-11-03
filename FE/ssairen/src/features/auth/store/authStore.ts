import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { User, TokenResponse } from '../types/auth.types'

interface AuthState {
  // 상태
  user: User | null
  accessToken: string | null
  refreshToken: string | null
  isAuthenticated: boolean

  // 액션
  setAuth: (tokenResponse: TokenResponse) => void
  clearAuth: () => void
  updateUser: (user: Partial<User>) => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      // 초기 상태
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,

      // 로그인 성공 시 인증 정보 저장 (TokenResponse에서 User 객체 추출)
      setAuth: (tokenResponse) => {
        const user: User = {
          id: tokenResponse.userId,
          username: tokenResponse.username,
          userType: tokenResponse.userType,
          name: tokenResponse.name,
          email: undefined, // 백엔드 응답에 없음
          role: tokenResponse.rank, // 구급대원의 경우 rank를 role로 사용
          officialName: tokenResponse.officialName // 병원 공식 명칭
        }

        set({
          user,
          accessToken: tokenResponse.accessToken,
          refreshToken: tokenResponse.refreshToken,
          isAuthenticated: true,
        })
      },

      // 로그아웃 시 인증 정보 초기화
      clearAuth: () => {
        // localStorage에서 토큰 제거
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')

        set({
          user: null,
          accessToken: null,
          refreshToken: null,
          isAuthenticated: false,
        })
      },

      // 사용자 정보만 업데이트
      updateUser: (userUpdate) => {
        set((state) => ({
          user: state.user ? { ...state.user, ...userUpdate } : null,
        }))
      },
    }),
    {
      name: 'auth-storage', // localStorage key 이름
      // 저장할 필드 선택 (보안상 토큰은 제외할 수도 있음)
      partialize: (state) => ({
        user: state.user,
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
)
