import { FormEvent } from 'react'
import { Input, Button } from '../atoms'
import { LoginFormData, UserType } from '@/features/auth/types/auth.types'

interface LoginFormProps {
  formData: LoginFormData
  onInputChange: (field: keyof LoginFormData, value: string | UserType) => void
  onSubmit: (e: FormEvent<HTMLFormElement>) => void
  isLoading?: boolean
  error?: string
  onForgotPassword?: () => void
}

export default function LoginForm({
  formData,
  onInputChange,
  onSubmit,
  isLoading = false,
  error,
  onForgotPassword
}: LoginFormProps) {
  return (
    <div className="space-y-6">
      {/* 타이틀 */}
      <h1 className="text-3xl font-bold text-center text-blue-500">
        로그인
      </h1>

      {/* 로그인 폼 */}
      <form onSubmit={onSubmit} className="space-y-4">
        {/* 에러 메시지 */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg text-sm">
            {error}
          </div>
        )}

        {/* ID 입력 */}
        <Input
          type="text"
          placeholder="병원명"
          value={formData.username}
          onChange={(e) => onInputChange('username', e.target.value)}
          fullWidth
          required
          disabled={isLoading}
          className="bg-white text-gray-900 placeholder:text-gray-400"
        />

        {/* PW 입력 */}
        <Input
          type="password"
          placeholder="비밀번호"
          value={formData.password}
          onChange={(e) => onInputChange('password', e.target.value)}
          fullWidth
          required
          disabled={isLoading}
          className="bg-white text-gray-900 placeholder:text-gray-400"
        />

        {/* 로그인 버튼 */}
        <Button
          type="submit"
          variant="primary"
          fullWidth
          loading={isLoading}
        >
          로그인하기
        </Button>

        {/* 비밀번호 찾기 */}
        {onForgotPassword && (
          <div className="text-center">
            <button
              type="button"
              onClick={onForgotPassword}
              className="text-sm text-neutral-400 hover:text-neutral-200 transition-colors"
            >
              비밀번호 찾기
            </button>
          </div>
        )}
      </form>
    </div>
  )
}
