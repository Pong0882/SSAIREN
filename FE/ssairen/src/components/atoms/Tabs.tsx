import { ReactNode } from 'react'

interface TabsProps {
  children: ReactNode
  className?: string
}

interface TabButtonProps {
  active?: boolean
  onClick?: () => void
  children: ReactNode
  className?: string
}

export function Tabs({ children, className = '' }: TabsProps) {
  return (
    <div className={`flex gap-2 ${className}`}>
      {children}
    </div>
  )
}

export function TabButton({ active = false, onClick, children, className = '' }: TabButtonProps) {
  const activeStyles = active
    ? 'bg-primary-500 text-white border-2 border-primary-500'
    : 'bg-transparent text-white border-2 border-white hover:bg-neutral-700 hover:border-neutral-700'

  return (
    <button
      onClick={onClick}
      className={`min-w-40 px-8 py-2 rounded-lg font-medium transition-all whitespace-nowrap ${activeStyles} ${className}`}
    >
      {children}
    </button>
  )
}
