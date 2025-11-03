import { useState, FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { LoginFormData, UserType } from '../types/auth.types'
import { loginApi } from '../api/authApi'
import { useAuthStore } from '../store/authStore'

export const useLogin = () => {
  const navigate = useNavigate()
  const setAuth = useAuthStore((state) => state.setAuth)

  const [formData, setFormData] = useState<LoginFormData>({
    userType: 'HOSPITAL', // 병원 전용 웹페이지 - 고정값
    username: '',
    password: '',
  })
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string>('')

  // 입력 필드 변경 핸들러
  const handleInputChange = (field: keyof LoginFormData, value: string | UserType) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }))
    // 입력 시 에러 메시지 초기화
    if (error) setError('')
  }

  // 로그인 제출 핸들러
  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()

    // 유효성 검사
    if (!formData.username || !formData.password) {
      setError('병원명과 비밀번호를 입력해주세요.')
      return
    }

    setIsLoading(true)
    setError('')

    try {
      const response = await loginApi({
        userType: 'HOSPITAL', // 병원 전용 웹페이지 - 항상 HOSPITAL로 고정
        username: formData.username,
        password: formData.password,
      })

      // Zustand Store에 인증 정보 저장 (TokenResponse 전체 전달)
      setAuth(response)

      // 로그인 성공 시 메인 페이지로 이동
      navigate('/patientListPage')
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message)
      } else {
        setError('로그인 중 오류가 발생했습니다.')
      }
    } finally {
      setIsLoading(false)
    }
  }

  // 비밀번호 찾기 핸들러
  const handleForgotPassword = () => {
    // 비밀번호 찾기 기능 (추후 구현)
    // navigate('/forgot-password')
  }

  return {
    formData,
    isLoading,
    error,
    handleInputChange,
    handleSubmit,
    handleForgotPassword,
  }
}
