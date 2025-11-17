import { useLocation } from 'react-router-dom'
import { useAuthStore } from '@/features/auth/store/authStore'

export default function Header() {
  const location = useLocation()
  const user = useAuthStore((state) => state.user)

  const getPageTitle = () => {
    switch (location.pathname) {
      case '/':
        return '환자 목록'
      case '/stats/time':
        return '시간 분석'
      case '/stats/patient':
        return '환자 통계'
      case '/stats/type':
        return '재난 유형'
      default:
        return '환자 관리 시스템'
    }
  }

  return (
    <header className="bg-white border-b border-gray-200 px-4 sm:px-6 lg:px-8 py-4 shadow-sm">
      <div className="flex justify-between items-center">
        <h1 className="text-lg sm:text-xl lg:text-2xl font-bold text-gray-900">
          {getPageTitle()}
        </h1>

        <div className="flex items-center gap-3 sm:gap-4">
          <span className="text-sm sm:text-base text-gray-700">
            <span className="hidden sm:inline">{user?.officialName || user?.name || user?.username}님 안녕하세요!</span>
            <span className="sm:hidden">{user?.officialName || user?.name || user?.username}님</span>
          </span>
        </div>
      </div>
    </header>
  )
}
