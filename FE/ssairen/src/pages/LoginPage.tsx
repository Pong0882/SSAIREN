import { AuthLayout } from '@/components/layout'
import { LoginForm } from '@/components/blocks'
import { useLogin } from '@/features/auth/hooks/useLogin'

export default function LoginPage() {
  const {
    formData,
    isLoading,
    error,
    handleInputChange,
    handleSubmit,
    handleForgotPassword,
  } = useLogin()

  return (
    <AuthLayout>
      <LoginForm
        formData={formData}
        onInputChange={handleInputChange}
        onSubmit={handleSubmit}
        isLoading={isLoading}
        error={error}
        onForgotPassword={handleForgotPassword}
      />
    </AuthLayout>
  )
}
