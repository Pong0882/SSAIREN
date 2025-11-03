import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, Input, Modal } from '@/components'
import { useAuthStore } from '@/features/auth/store/authStore'
import { logoutApi } from '@/features/auth/api/authApi'

function HomePage() {
  const navigate = useNavigate()
  const { user, isAuthenticated, clearAuth } = useAuthStore()
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  const handleSubmit = () => {
    setIsLoading(true)
    setTimeout(() => {
      setIsLoading(false)
      alert(`Email: ${email}, Password: ${password}`)
    }, 2000)
  }

  const handleLogout = async () => {
    try {
      await logoutApi()
      clearAuth()
      navigate('/login')
    } catch (error) {
      // 에러가 나도 로컬 상태는 정리
      clearAuth()
      navigate('/login')
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 p-8">
      <div className="max-w-4xl mx-auto">
        {/* 사용자 정보 헤더 */}
        {isAuthenticated && user && (
          <div className="bg-white rounded-xl shadow-lg p-4 mb-6 flex justify-between items-center">
            <div>
              <p className="text-sm text-gray-600">로그인 사용자</p>
              <p className="text-lg font-bold text-gray-800">{user.username}</p>
              {user.email && <p className="text-sm text-gray-500">{user.email}</p>}
            </div>
            <Button variant="danger" size="sm" onClick={handleLogout}>
              로그아웃
            </Button>
          </div>
        )}

        {!isAuthenticated && (
          <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-4 mb-6 flex justify-between items-center">
            <p className="text-yellow-800">로그인이 필요합니다.</p>
            <Button variant="primary" size="sm" onClick={() => navigate('/login')}>
              로그인
            </Button>
          </div>
        )}

        <h1 className="text-4xl font-bold text-gray-800 mb-2 text-center">
          SSAIREN 🏥
        </h1>
        <p className="text-lg text-gray-600 mb-8 text-center">
          Component Demo - Hospital Management System
        </p>

        {/* Button Demo */}
        <div className="bg-white rounded-xl shadow-lg p-6 mb-6">
          <h2 className="text-2xl font-bold text-gray-800 mb-4">Button Components</h2>
          <div className="space-y-4">
            <div className="flex flex-wrap gap-3">
              <Button variant="primary">Primary</Button>
              <Button variant="secondary">Secondary</Button>
              <Button variant="danger">Danger</Button>
              <Button variant="neutral">Neutral</Button>
              <Button variant="outline">Outline</Button>
            </div>

            <div className="flex flex-wrap gap-3 items-center">
              <Button variant="primary" size="sm">Small</Button>
              <Button variant="primary" size="md">Medium</Button>
              <Button variant="primary" size="lg">Large</Button>
            </div>

            <div className="flex flex-wrap gap-3">
              <Button variant="primary" loading>Loading...</Button>
              <Button variant="secondary" disabled>Disabled</Button>
            </div>

            <Button variant="primary" fullWidth onClick={() => setIsModalOpen(true)}>
              Open Modal (Full Width)
            </Button>
          </div>
        </div>

        {/* Input Demo */}
        <div className="bg-white rounded-xl shadow-lg p-6 mb-6">
          <h2 className="text-2xl font-bold text-gray-800 mb-4">Input Components</h2>
          <div className="space-y-4">
            <Input
              label="Email"
              type="email"
              placeholder="Enter your email"
              fullWidth
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />

            <Input
              label="Password"
              type="password"
              placeholder="Enter your password"
              fullWidth
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              helperText="Must be at least 8 characters"
            />

            <Input
              label="Email with Error"
              type="email"
              placeholder="test@example.com"
              fullWidth
              error="This email is already taken"
            />

            <Input
              placeholder="Search with icon..."
              fullWidth
              leftIcon={
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              }
            />

            <Button variant="primary" fullWidth onClick={handleSubmit} loading={isLoading}>
              Submit
            </Button>
          </div>
        </div>

        {/* Modal Demo */}
        <Modal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          title="Welcome to SSAIREN"
          size="md"
        >
          <div className="space-y-4">
            <p className="text-gray-600">
              This is a modal component demo. You can close it by:
            </p>
            <ul className="list-disc list-inside text-gray-600 space-y-2">
              <li>Clicking the X button</li>
              <li>Pressing ESC key</li>
              <li>Clicking outside the modal</li>
            </ul>

            <div className="flex gap-3 pt-4">
              <Button variant="primary" onClick={() => setIsModalOpen(false)}>
                Confirm
              </Button>
              <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
                Cancel
              </Button>
            </div>
          </div>
        </Modal>
      </div>
    </div>
  )
}

export default HomePage
