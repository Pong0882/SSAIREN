import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useAuthStore } from "@/features/auth/store/authStore";

export default function Sidebar() {
  const navigate = useNavigate();
  const location = useLocation();
  const clearAuth = useAuthStore((state) => state.clearAuth);
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const [isStatsExpanded, setIsStatsExpanded] = useState(true);

  const handleLogout = () => {
    clearAuth();
    navigate("/login");
  };

  const isActive = (path: string) => location.pathname === path;

  return (
    <aside
      className={`${
        isSidebarCollapsed ? "w-20" : "w-64"
      } bg-neutral-900 flex flex-col transition-all duration-300`}
    >
      {/* 로고/타이틀 영역 + 토글 버튼 */}
      <div
        className={`h-[88px] px-6 border-b border-neutral-700 flex items-center ${
          isSidebarCollapsed ? "justify-center" : "justify-between"
        }`}
      >
        {!isSidebarCollapsed && (
          <div>
            <h2 className="text-xl font-bold text-white whitespace-nowrap">
              SSAIREN
            </h2>
            <p className="text-sm text-gray-400 mt-1 whitespace-nowrap">
              환자 관리 시스템
            </p>
          </div>
        )}
        <button
          onClick={() => setIsSidebarCollapsed(!isSidebarCollapsed)}
          className="p-3 text-gray-400 hover:text-white hover:bg-neutral-800 rounded-lg transition-colors flex-shrink-0 min-w-[44px] min-h-[44px] flex items-center justify-center"
        >
          <svg
            className="w-6 h-6"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            {isSidebarCollapsed ? (
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M13 5l7 7-7 7M5 5l7 7-7 7"
              />
            ) : (
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M11 19l-7-7 7-7m8 14l-7-7 7-7"
              />
            )}
          </svg>
        </button>
      </div>

      {/* 메뉴 항목들 */}
      <nav className="flex-1 px-4 py-6">
        <div className="space-y-2">
          {/* 대시보드 */}
          <button
            onClick={() => navigate("/")}
            className={`w-full h-[44px] px-4 rounded-lg transition-colors ${
              isActive("/")
                ? "text-white bg-blue-600"
                : "text-gray-400 hover:bg-neutral-800"
            }`}
          >
            <div className="h-full flex items-center">
              <svg
                className="w-5 h-5 flex-shrink-0"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
                />
              </svg>
              {!isSidebarCollapsed && (
                <span className="font-medium whitespace-nowrap ml-3">
                  대시보드
                </span>
              )}
            </div>
          </button>

          {/* 통계/리포트 (확장 가능) */}
          <div>
            <button
              onClick={() => setIsStatsExpanded(!isStatsExpanded)}
              className="w-full h-[44px] px-4 text-gray-400 rounded-lg hover:bg-neutral-800 transition-colors"
            >
              <div className="h-full flex items-center justify-between">
                <div className="flex items-center">
                  <svg
                    className="w-5 h-5 flex-shrink-0"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
                    />
                  </svg>
                  {!isSidebarCollapsed && (
                    <span className="font-medium whitespace-nowrap ml-3">
                      통계/리포트
                    </span>
                  )}
                </div>
                {!isSidebarCollapsed && (
                  <svg
                    className={`w-4 h-4 transition-transform ${
                      isStatsExpanded ? "rotate-180" : ""
                    }`}
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M19 9l-7 7-7-7"
                    />
                  </svg>
                )}
              </div>
            </button>

            {/* 하위 메뉴 */}
            {isStatsExpanded && !isSidebarCollapsed && (
              <div className="ml-4 mt-1 space-y-1">
                <button
                  onClick={() => navigate("/stats/time")}
                  className={`w-full h-[40px] px-4 rounded-lg transition-colors text-sm ${
                    isActive("/stats/time")
                      ? "text-white bg-blue-600"
                      : "text-gray-400 hover:bg-neutral-800"
                  }`}
                >
                  <div className="h-full flex items-center">
                    <span className="font-medium whitespace-nowrap">
                      시간 분석
                    </span>
                  </div>
                </button>
                <button
                  onClick={() => navigate("/stats/patient")}
                  className={`w-full h-[40px] px-4 rounded-lg transition-colors text-sm ${
                    isActive("/stats/patient")
                      ? "text-white bg-blue-600"
                      : "text-gray-400 hover:bg-neutral-800"
                  }`}
                >
                  <div className="h-full flex items-center">
                    <span className="font-medium whitespace-nowrap">
                      환자 통계
                    </span>
                  </div>
                </button>
                <button
                  onClick={() => navigate("/stats/type")}
                  className={`w-full h-[40px] px-4 rounded-lg transition-colors text-sm ${
                    isActive("/stats/type")
                      ? "text-white bg-blue-600"
                      : "text-gray-400 hover:bg-neutral-800"
                  }`}
                >
                  <div className="h-full flex items-center">
                    <span className="font-medium whitespace-nowrap">
                      재난 유형
                    </span>
                  </div>
                </button>
              </div>
            )}
          </div>
        </div>
      </nav>

      {/* 로그아웃 버튼 */}
      <div className="p-4 border-t border-neutral-700">
        <button
          onClick={handleLogout}
          className="w-full h-[44px] px-4 text-white bg-neutral-800 rounded-lg hover:bg-neutral-700 transition-colors"
        >
          <div className="h-full flex items-center">
            <svg
              className="w-5 h-5 flex-shrink-0"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
              />
            </svg>
            {!isSidebarCollapsed && (
              <span className="font-medium whitespace-nowrap ml-3">
                로그아웃
              </span>
            )}
          </div>
        </button>
      </div>
    </aside>
  );
}
