import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/features/auth/store/authStore'
import Button from '../atoms/Button'

export default function Header() {
  const navigate = useNavigate()
  const user = useAuthStore((state) => state.user)
  const clearAuth = useAuthStore((state) => state.clearAuth)

  const handleLogout = () => {
    clearAuth()
    navigate('/login')
  }

  return (
    <header className="bg-neutral-800 border-b border-neutral-700 px-4 sm:px-6 lg:px-8 py-4">
      <div className="max-w-7xl mx-auto flex justify-between items-center">
        <h1 className="text-lg sm:text-xl lg:text-2xl font-bold text-white">
          환자 관리 시스템
        </h1>

        <div className="flex items-center gap-3 sm:gap-4">
          <span className="text-sm sm:text-base text-white">
            <span className="hidden sm:inline">{user?.officialName || user?.name || user?.username}님 안녕하세요!</span>
            <span className="sm:hidden">{user?.officialName || user?.name || user?.username}님</span>
          </span>
          <Button
            variant="outline"
            size="md"
            onClick={handleLogout}
            className="!border-white !text-white hover:!bg-white hover:!text-neutral-800 px-5 py-2"
          >
            로그아웃
          </Button>
        </div>
      </div>
    </header>
  )
}
