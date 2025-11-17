import { ReactNode } from "react";

interface TabsProps {
  children: ReactNode;
  className?: string;
}

interface TabButtonProps {
  active?: boolean;
  onClick?: () => void;
  children: ReactNode;
  className?: string;
}

export function Tabs({ children, className = "" }: TabsProps) {
  return <div className={`flex gap-2 ${className}`}>{children}</div>;
}

export function TabButton({
  active = false,
  onClick,
  children,
  className = "",
}: TabButtonProps) {
  const activeStyles = active
    ? "bg-sky-500 text-white border-2 border-sky-500 shadow-sm"
    : "bg-white text-gray-700 border-2 border-gray-300 hover:bg-gray-100 hover:border-gray-400";

  return (
    <button
      onClick={onClick}
      className={`min-w-40 px-8 py-2 rounded-lg font-medium transition-all whitespace-nowrap ${activeStyles} ${className}`}
    >
      {children}
    </button>
  );
}
