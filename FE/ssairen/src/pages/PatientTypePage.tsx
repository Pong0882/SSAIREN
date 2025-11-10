import Sidebar from "@/components/layout/Sidebar";
import { Header } from "@/components";

export default function PatientTypePage() {
  return (
    <div className="h-screen flex overflow-hidden">
      <Sidebar />

      {/* 메인 컨텐츠 */}
      <div className="flex-1 flex flex-col overflow-hidden bg-gray-50">
        <Header />

        {/* Main Content */}
        <div className="flex-1 flex flex-col p-4 sm:p-6 lg:p-8 min-h-0">
          <div className="max-w-7xl mx-auto w-full flex flex-col h-full">
            {/* 통계 컨텐츠 영역 */}
            <div className="bg-white rounded-lg shadow-lg p-6 flex-1">
              <p className="text-gray-600">
                재난 유형별 환자 분류 통계 차트가 여기에 표시됩니다.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
