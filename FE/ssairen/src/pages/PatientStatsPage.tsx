import { useState, useEffect, forwardRef, useMemo } from "react";
import Sidebar from "@/components/layout/Sidebar";
import { Header } from "@/components";
import { useAuthStore } from "@/features/auth/store/authStore";
import { fetchPatientStatisticsApi } from "@/features/statistics/api/statisticsApi";
import type { PatientStatisticsResponse } from "@/features/statistics/types/statistics.types";
import {
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";

type RangeKey = "week" | "month" | "all" | "custom";

export default function PatientStatsPage() {
  const { user } = useAuthStore();
  const [data, setData] = useState<PatientStatisticsResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [dateRange, setDateRange] = useState<RangeKey>("month");
  const [startDate, setStartDate] = useState<Date | null>(null);
  const [endDate, setEndDate] = useState<Date | null>(null);

  // DatePicker customInput용 공통 버튼
  const DateButton = forwardRef<HTMLButtonElement, React.ComponentProps<"button">>(
    ({ children, className = "", ...props }, ref) => (
      <button
        ref={ref}
        type="button"
        className={`h-14 w-full px-5 text-base leading-none font-medium rounded-lg transition-colors flex items-center justify-between gap-4 min-w-[200px] ${className}`}
        {...props}
      >
        {children}
      </button>
    )
  );
  DateButton.displayName = "DateButton";

  const fmt = (d: Date) => d.toISOString().split("T")[0];

  const calcPresetRange = (key: Exclude<RangeKey, "custom">) => {
    const end = new Date();
    const start = new Date(end);
    if (key === "week") start.setDate(end.getDate() - 7);
    else if (key === "month") start.setMonth(end.getMonth() - 1);
    else start.setFullYear(end.getFullYear() - 1);
    start.setHours(0, 0, 0, 0);
    end.setHours(23, 59, 59, 999);
    return { start, end };
  };

  const applyPreset = (key: Exclude<RangeKey, "custom">) => {
    const { start, end } = calcPresetRange(key);
    setDateRange(key);
    setStartDate(start);
    setEndDate(end);
  };

  useEffect(() => {
    if (startDate && endDate) return;
    const { start, end } = calcPresetRange("month");
    setStartDate(start);
    setEndDate(end);
  }, []);

  useEffect(() => {
    if (!user?.id || !startDate || !endDate) return;

    (async () => {
      setLoading(true);
      setError(null);
      try {
        const result = await fetchPatientStatisticsApi({
          hospitalId: user.id,
          startDate: fmt(startDate),
          endDate: fmt(endDate),
        });
        setData(result);
      } catch (err) {
        setError(err instanceof Error ? err.message : "통계 조회에 실패했습니다.");
      } finally {
        setLoading(false);
      }
    })();
  }, [user?.id, startDate, endDate]);

  const isCustomActive = dateRange === "custom" && !!startDate && !!endDate;

  // 성별 차트 데이터 (전체 표시)
  const genderData = data
    ? [
        { name: "남성", value: data.byGender.M || 0 },
        { name: "여성", value: data.byGender.F || 0 },
      ]
    : [];

  // 연령대 차트 데이터 (전체 표시)
  const ageGroupData = data
    ? [
        { name: "0-9", count: data.byAgeGroup["0-9"] || 0 },
        { name: "10-19", count: data.byAgeGroup["10-19"] || 0 },
        { name: "20-29", count: data.byAgeGroup["20-29"] || 0 },
        { name: "30-39", count: data.byAgeGroup["30-39"] || 0 },
        { name: "40-49", count: data.byAgeGroup["40-49"] || 0 },
        { name: "50-59", count: data.byAgeGroup["50-59"] || 0 },
        { name: "60-69", count: data.byAgeGroup["60-69"] || 0 },
        { name: "70-79", count: data.byAgeGroup["70-79"] || 0 },
        { name: "80+", count: data.byAgeGroup["80+"] || 0 },
      ]
    : [];

  // 의식 상태 차트 데이터 (전체 표시)
  const mentalStatusData = data
    ? [
        { name: "의식 명료", value: data.byMentalStatus.ALERT || 0 },
        { name: "언어 반응", value: data.byMentalStatus.VERBAL || 0 },
        { name: "통증 반응", value: data.byMentalStatus.PAIN || 0 },
        { name: "무반응", value: data.byMentalStatus.UNRESPONSIVE || 0 },
      ]
    : [];

  const GENDER_COLORS = ["#0ea5e9", "#f97316"];
  const MENTAL_STATUS_COLORS = ["#10b981", "#f59e0b", "#ef4444", "#6b7280"];

  return (
    <div className="h-screen flex overflow-hidden">
      <Sidebar />

      <div className="flex-1 flex flex-col overflow-hidden bg-gray-50">
        <Header />

        <div className="flex-1 flex flex-col p-4 sm:p-6 lg:p-8 min-h-0">
          <div className="max-w-full h-full flex flex-col gap-4">
            {/* 상단: 필터 행 */}
            <div className="flex gap-3 items-stretch">
              <button
                className={`h-14 px-5 text-base leading-none font-medium rounded-lg transition-colors ${
                  dateRange === "week"
                    ? "bg-sky-500 text-white shadow-sm"
                    : "bg-white text-gray-700 hover:bg-gray-100 border border-gray-200"
                }`}
                onClick={() => applyPreset("week")}
              >
                최근 일주일
              </button>

              <button
                className={`h-14 px-5 text-base leading-none font-medium rounded-lg transition-colors ${
                  dateRange === "month"
                    ? "bg-sky-500 text-white shadow-sm"
                    : "bg-white text-gray-700 hover:bg-gray-100 border border-gray-200"
                }`}
                onClick={() => applyPreset("month")}
              >
                최근 한 달
              </button>

              <button
                className={`h-14 px-5 text-base leading-none font-medium rounded-lg transition-colors ${
                  dateRange === "all"
                    ? "bg-sky-500 text-white shadow-sm"
                    : "bg-white text-gray-700 hover:bg-gray-100 border border-gray-200"
                }`}
                onClick={() => applyPreset("all")}
              >
                전체 기간
              </button>

              {/* 시작 날짜 */}
              <DatePicker
                selected={startDate}
                onChange={(date: Date | null) => {
                  setDateRange("custom");
                  setStartDate(date);
                }}
                selectsStart
                startDate={startDate}
                endDate={endDate}
                maxDate={new Date()}
                dateFormat="yyyy-MM-dd"
                wrapperClassName="self-stretch"
                customInput={
                  <DateButton
                    className={
                      isCustomActive
                        ? "bg-sky-500 text-white border border-sky-500 hover:bg-sky-500"
                        : "bg-white text-gray-900 border border-gray-300 hover:bg-gray-50"
                    }
                  >
                    <span className={isCustomActive ? "text-white" : "text-gray-700"}>
                      시작 날짜
                    </span>
                    <span className={isCustomActive ? "text-white" : "text-gray-900"}>
                      {startDate ? fmt(startDate) : ""}
                    </span>
                  </DateButton>
                }
              />

              {/* 종료 날짜 */}
              <DatePicker
                selected={endDate}
                onChange={(date: Date | null) => {
                  setDateRange("custom");
                  setEndDate(date);
                }}
                selectsEnd
                startDate={startDate}
                endDate={endDate}
                minDate={startDate || undefined}
                maxDate={new Date()}
                dateFormat="yyyy-MM-dd"
                wrapperClassName="self-stretch"
                customInput={
                  <DateButton
                    className={
                      isCustomActive
                        ? "bg-sky-500 text-white border border-sky-500 hover:bg-sky-500"
                        : "bg-white text-gray-900 border border-gray-300 hover:bg-gray-50"
                    }
                  >
                    <span className={isCustomActive ? "text-white" : "text-gray-700"}>
                      종료 날짜
                    </span>
                    <span className={isCustomActive ? "text-white" : "text-gray-900"}>
                      {endDate ? fmt(endDate) : ""}
                    </span>
                  </DateButton>
                }
              />

              {/* 총 수용 건수 */}
              {data && (
                <div className="relative flex-1 h-14 bg-white rounded-lg shadow-sm border border-gray-200 px-5 flex items-center justify-between">
                  <div className="flex items-center gap-4 min-w-0">
                    <div className="flex items-center gap-3">
                      <span className="text-sm leading-none text-gray-600 whitespace-nowrap">
                        총 수용 건수
                      </span>
                      <span className="text-xl leading-none font-bold text-sky-500 whitespace-nowrap">
                        {data.totalCount}건
                      </span>
                    </div>
                  </div>

                  {/* 우측: 상세 팝오버 트리거 */}
                  <PopoverSummary
                    startDate={data.startDate}
                    endDate={data.endDate}
                    byGender={data.byGender}
                    byAgeGroup={data.byAgeGroup}
                    byMentalStatus={data.byMentalStatus}
                    totalCount={data.totalCount}
                  />
                </div>
              )}
            </div>

            {/* 로딩/에러 상태 */}
            {loading && (
              <div className="flex-1 bg-white rounded-lg shadow-lg flex items-center justify-center">
                <p className="text-gray-600">데이터를 불러오는 중...</p>
              </div>
            )}
            {error && (
              <div className="flex-1 bg-white rounded-lg shadow-lg flex items-center justify-center">
                <p className="text-red-600">{error}</p>
              </div>
            )}

            {/* 하단: 차트 3개 */}
            {!loading && !error && data && (
              <div className="flex-1 grid grid-cols-1 lg:grid-cols-3 gap-4 min-h-0">
                {/* 성별 분포 파이 차트 */}
                <div className="bg-white rounded-lg shadow-lg p-6 flex flex-col min-h-0">
                  <h3 className="text-lg font-semibold text-gray-900 mb-4">성별 분포</h3>
                  <div className="flex-1 min-h-0">
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie
                          data={genderData}
                          cx="50%"
                          cy="50%"
                          labelLine={false}
                          label={(entry) => (entry.value > 0 ? `${entry.value}명` : "")}
                          outerRadius={80}
                          fill="#8884d8"
                          dataKey="value"
                        >
                          {genderData.map((entry, index) => (
                            <Cell
                              key={`cell-${index}`}
                              fill={GENDER_COLORS[index % GENDER_COLORS.length]}
                            />
                          ))}
                        </Pie>
                        <Tooltip />
                        <Legend />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                </div>

                {/* 연령대 분포 바 차트 */}
                <div className="bg-white rounded-lg shadow-lg p-6 flex flex-col min-h-0">
                  <h3 className="text-lg font-semibold text-gray-900 mb-4">연령대 분포</h3>
                  <div className="flex-1 min-h-0">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={ageGroupData} margin={{ top: 8, right: 12, bottom: 4, left: 8 }}>
                        <CartesianGrid vertical={false} stroke="#E5E7EB" strokeOpacity={0.35} />
                        <XAxis
                          dataKey="name"
                          tick={{ fill: "#6B7280", fontSize: 12 }}
                          axisLine={{ stroke: "#E5E7EB" }}
                          tickLine={false}
                        />
                        <YAxis
                          allowDecimals={false}
                          tick={{ fill: "#6B7280", fontSize: 12 }}
                          axisLine={{ stroke: "#E5E7EB" }}
                          tickLine={false}
                        />
                        <Tooltip
                          formatter={(v: number) => [`${v}명`, "수용 건수"]}
                          labelStyle={{ color: "#111827" }}
                          contentStyle={{ borderRadius: 10, borderColor: "#E5E7EB" }}
                        />
                        <Bar dataKey="count" fill="#0ea5e9" radius={[6, 6, 0, 0]} maxBarSize={40} />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </div>

                {/* 의식 상태 분포 도넛 차트 */}
                <div className="bg-white rounded-lg shadow-lg p-6 flex flex-col min-h-0">
                  <h3 className="text-lg font-semibold text-gray-900 mb-4">의식 상태 분포</h3>
                  <div className="flex-1 min-h-0">
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie
                          data={mentalStatusData}
                          cx="50%"
                          cy="50%"
                          labelLine={false}
                          label={(entry) => (entry.value > 0 ? `${entry.value}명` : "")}
                          outerRadius={80}
                          innerRadius={40}
                          fill="#8884d8"
                          dataKey="value"
                        >
                          {mentalStatusData.map((entry, index) => (
                            <Cell
                              key={`cell-${index}`}
                              fill={MENTAL_STATUS_COLORS[index % MENTAL_STATUS_COLORS.length]}
                            />
                          ))}
                        </Pie>
                        <Tooltip />
                        <Legend />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

function PopoverSummary({
  startDate,
  endDate,
  byGender,
  byAgeGroup,
  byMentalStatus,
  totalCount,
}: {
  startDate: string;
  endDate: string;
  byGender: Record<string, number>;
  byAgeGroup: Record<string, number>;
  byMentalStatus: Record<string, number>;
  totalCount: number;
}) {
  const [open, setOpen] = useState(false);

  // 성별 비율 계산
  const genderRatio = useMemo(() => {
    const total = (byGender.M || 0) + (byGender.F || 0);
    if (total === 0) return { male: 0, female: 0 };
    return {
      male: Math.round(((byGender.M || 0) / total) * 100),
      female: Math.round(((byGender.F || 0) / total) * 100),
    };
  }, [byGender]);

  // Top 3 연령대
  const topAgeGroups = useMemo(() => {
    const entries = Object.entries(byAgeGroup || {});
    return entries
      .sort((a, b) => Number(b[1]) - Number(a[1]))
      .slice(0, 3)
      .map(([k, v]) => `${k}세(${v})`);
  }, [byAgeGroup]);

  // 주요 의식 상태
  const mentalStatusNames: Record<string, string> = {
    ALERT: "의식 명료",
    VERBAL: "언어 반응",
    PAIN: "통증 반응",
    UNRESPONSIVE: "무반응",
  };

  const topMentalStatus = useMemo(() => {
    const entries = Object.entries(byMentalStatus || {});
    if (!entries.length) return "-";
    const [status, count] = entries.reduce((a, b) => (Number(a[1]) >= Number(b[1]) ? a : b));
    return `${mentalStatusNames[status] || status}(${count})`;
  }, [byMentalStatus]);

  // 연령대 스파크라인 데이터
  const ageGroupChart = useMemo(() => {
    const ageOrder = ["0-9", "10-19", "20-29", "30-39", "40-49", "50-59", "60-69", "70-79", "80+"];
    return ageOrder.map((age, i) => ({ x: i, y: Number(byAgeGroup?.[age] || 0) }));
  }, [byAgeGroup]);

  return (
    <div className="relative">
      <button
        onClick={() => setOpen((o) => !o)}
        className="text-xs leading-none px-3 py-2 rounded-md border border-gray-200 hover:bg-gray-50 text-gray-600"
        aria-expanded={open}
      >
        상세 보기
      </button>

      {open && (
        <div
          className="absolute right-0 top-12 z-20 w-96 bg-white border border-gray-200 rounded-xl shadow-xl p-4"
          role="dialog"
        >
          <div className="flex items-start justify-between">
            <div>
              <div className="text-sm text-gray-500">선택 기간</div>
              <div className="text-sm font-medium text-gray-900">
                {startDate} ~ {endDate}
              </div>
            </div>
            <button
              className="text-gray-400 hover:text-gray-600"
              onClick={() => setOpen(false)}
              aria-label="닫기"
            >
              ✕
            </button>
          </div>

          <div className="mt-3 grid grid-cols-3 gap-3">
            <StatPill label="총 건수" value={`${totalCount}건`} />
            <StatPill
              label="성별 비율"
              value={`남 ${genderRatio.male}% / 여 ${genderRatio.female}%`}
            />
            <StatPill label="주요 의식" value={topMentalStatus} />
          </div>

          {/* 미니 연령대 스파크라인 */}
          <div className="mt-4">
            <div className="text-xs text-gray-500 mb-1">연령대 분포</div>
            <div className="h-16 w-full">
              <svg viewBox="0 0 100 30" preserveAspectRatio="none" className="w-full h-full">
                {(() => {
                  const maxY = Math.max(1, ...ageGroupChart.map((p) => p.y));
                  const points = ageGroupChart
                    .map((p, i) => {
                      const x = (i / 8) * 100;
                      const y = 30 - (p.y / maxY) * 28;
                      return `${x},${y}`;
                    })
                    .join(" ");
                  return (
                    <>
                      <polyline
                        points={points}
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="1.5"
                        className="text-sky-500"
                      />
                      {ageGroupChart.map((p, i) => {
                        const x = (i / 8) * 100;
                        const y = 30 - (p.y / maxY) * 28;
                        return <circle key={i} cx={x} cy={y} r="0.9" className="fill-sky-500" />;
                      })}
                    </>
                  );
                })()}
              </svg>
            </div>
          </div>

          {/* Top3 연령대 리스트 */}
          <div className="mt-3">
            <div className="text-xs text-gray-500 mb-1">연령대 Top3</div>
            <div className="text-sm text-gray-800">{topAgeGroups.join(" · ") || "-"}</div>
          </div>
        </div>
      )}
    </div>
  );
}

function StatPill({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg border border-gray-200 px-3 py-2">
      <div className="text-[11px] text-gray-500">{label}</div>
      <div className="text-sm font-medium text-gray-900 mt-0.5">{value}</div>
    </div>
  );
}
