import { useState, useEffect, forwardRef, useMemo } from "react";
import Sidebar from "@/components/layout/Sidebar";
import { Header } from "@/components";
import { useAuthStore } from "@/features/auth/store/authStore";
import { fetchDisasterTypeStatisticsApi } from "@/features/statistics/api/statisticsApi";
import type { DisasterTypeStatisticsResponse } from "@/features/statistics/types/statistics.types";
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

export default function PatientTypePage() {
  const { user } = useAuthStore();
  const [data, setData] = useState<DisasterTypeStatisticsResponse | null>(null);
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
        const result = await fetchDisasterTypeStatisticsApi({
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

  // 재난 유형 차트 데이터
  const disasterTypeData = data
    ? Object.entries(data.byDisasterType).map(([name, value]) => ({
        name,
        value,
      }))
    : [];

  // 재난 세부 유형 TOP 10 데이터
  const disasterSubtypeData = data
    ? Object.entries(data.byDisasterSubtype)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 10)
        .map(([name, count]) => ({
          name,
          count,
        }))
    : [];

  // 차트 색상
  const DISASTER_TYPE_COLORS = [
    "#0ea5e9", // 파랑
    "#f97316", // 주황
    "#10b981", // 초록
    "#f59e0b", // 노랑
    "#ef4444", // 빨강
    "#8b5cf6", // 보라
    "#ec4899", // 핑크
    "#6b7280", // 회색
  ];

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
                    byDisasterType={data.byDisasterType}
                    byDisasterSubtype={data.byDisasterSubtype}
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

            {/* 하단: 차트 2개 */}
            {!loading && !error && data && (
              <div className="flex-1 grid grid-cols-1 lg:grid-cols-2 gap-4 min-h-0">
                {/* 재난 유형 분포 파이 차트 */}
                <div className="bg-white rounded-lg shadow-lg p-6 flex flex-col min-h-0">
                  <h3 className="text-lg font-semibold text-gray-900 mb-4">재난 유형별 분포</h3>
                  <div className="flex-1 min-h-0">
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie
                          data={disasterTypeData}
                          cx="50%"
                          cy="50%"
                          labelLine={false}
                          label={(entry) => (entry.value > 0 ? `${entry.name}: ${entry.value}건` : "")}
                          outerRadius={100}
                          fill="#8884d8"
                          dataKey="value"
                        >
                          {disasterTypeData.map((entry, index) => (
                            <Cell
                              key={`cell-${index}`}
                              fill={DISASTER_TYPE_COLORS[index % DISASTER_TYPE_COLORS.length]}
                            />
                          ))}
                        </Pie>
                        <Tooltip formatter={(value: number) => `${value}건`} />
                        <Legend />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                </div>

                {/* 재난 세부 유형 TOP 10 바 차트 */}
                <div className="bg-white rounded-lg shadow-lg p-6 flex flex-col min-h-0">
                  <h3 className="text-lg font-semibold text-gray-900 mb-4">
                    재난 세부 유형 TOP 10
                  </h3>
                  <div className="flex-1 min-h-0">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart
                        data={disasterSubtypeData}
                        margin={{ top: 8, right: 12, bottom: 4, left: 8 }}
                      >
                        <CartesianGrid vertical={false} stroke="#E5E7EB" strokeOpacity={0.35} />
                        <XAxis
                          dataKey="name"
                          tick={{ fill: "#6B7280", fontSize: 12 }}
                          axisLine={{ stroke: "#E5E7EB" }}
                          tickLine={false}
                          angle={-45}
                          textAnchor="end"
                          height={80}
                        />
                        <YAxis
                          allowDecimals={false}
                          tick={{ fill: "#6B7280", fontSize: 12 }}
                          axisLine={{ stroke: "#E5E7EB" }}
                          tickLine={false}
                        />
                        <Tooltip
                          formatter={(v: number) => [`${v}건`, "수용 건수"]}
                          labelStyle={{ color: "#111827" }}
                          contentStyle={{ borderRadius: 10, borderColor: "#E5E7EB" }}
                        />
                        <Bar dataKey="count" fill="#0ea5e9" radius={[6, 6, 0, 0]} maxBarSize={60} />
                      </BarChart>
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
  byDisasterType,
  byDisasterSubtype,
  totalCount,
}: {
  startDate: string;
  endDate: string;
  byDisasterType: Record<string, number>;
  byDisasterSubtype: Record<string, number>;
  totalCount: number;
}) {
  const [open, setOpen] = useState(false);

  // 가장 많은 재난 유형 (Top 1)
  const topDisasterType = useMemo(() => {
    const entries = Object.entries(byDisasterType || {});
    if (!entries.length) return { type: "-", count: 0, ratio: 0 };
    const [type, count] = entries.reduce((a, b) => (Number(a[1]) >= Number(b[1]) ? a : b));
    const ratio = totalCount > 0 ? Math.round((Number(count) / totalCount) * 100) : 0;
    return { type, count: Number(count), ratio };
  }, [byDisasterType, totalCount]);

  // 재난 유형 다양성 (몇 가지 유형)
  const typeVariety = useMemo(() => {
    return Object.keys(byDisasterType || {}).length;
  }, [byDisasterType]);

  // 재난 유형 Top 3
  const topTypes = useMemo(() => {
    const entries = Object.entries(byDisasterType || {});
    return entries
      .sort((a, b) => Number(b[1]) - Number(a[1]))
      .slice(0, 3)
      .map(([k, v]) => `${k}(${v})`);
  }, [byDisasterType]);

  // 재난 세부 유형 Top 3
  const topSubtypes = useMemo(() => {
    const entries = Object.entries(byDisasterSubtype || {});
    return entries
      .sort((a, b) => Number(b[1]) - Number(a[1]))
      .slice(0, 3)
      .map(([k, v]) => `${k}(${v})`);
  }, [byDisasterSubtype]);

  // 재난 유형별 분포 미니 차트 데이터
  const typeDistribution = useMemo(() => {
    const entries = Object.entries(byDisasterType || {})
      .sort((a, b) => Number(b[1]) - Number(a[1]))
      .slice(0, 5);
    const maxCount = entries.length > 0 ? Number(entries[0][1]) : 1;
    return entries.map(([name, count]) => ({
      name,
      count: Number(count),
      percentage: maxCount > 0 ? (Number(count) / maxCount) * 100 : 0,
    }));
  }, [byDisasterType]);

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
          className="absolute right-0 top-12 z-20 w-[420px] bg-white border border-gray-200 rounded-xl shadow-xl p-5"
          role="dialog"
        >
          {/* 헤더 */}
          <div className="flex items-start justify-between mb-4">
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

          {/* 메인 통계 카드 */}
          <div className="grid grid-cols-2 gap-3 mb-4">
            <div className="bg-gradient-to-br from-sky-50 to-blue-50 rounded-lg p-3 border border-sky-100">
              <div className="text-xs text-sky-600 font-medium mb-1">총 수용 건수</div>
              <div className="text-2xl font-bold text-sky-700">{totalCount}건</div>
              <div className="text-xs text-sky-500 mt-1">{typeVariety}가지 유형</div>
            </div>
            <div className="bg-gradient-to-br from-orange-50 to-amber-50 rounded-lg p-3 border border-orange-100">
              <div className="text-xs text-orange-600 font-medium mb-1">최다 유형</div>
              <div className="text-lg font-bold text-orange-700 truncate">{topDisasterType.type}</div>
              <div className="text-xs text-orange-500 mt-1">
                {topDisasterType.count}건 ({topDisasterType.ratio}%)
              </div>
            </div>
          </div>

          {/* 재난 유형 분포 미니 바 */}
          <div className="mb-4">
            <div className="text-xs text-gray-600 font-medium mb-2">재난 유형 분포 Top 5</div>
            <div className="space-y-2">
              {typeDistribution.map((item, idx) => (
                <div key={idx} className="flex items-center gap-2">
                  <div className="w-16 text-xs text-gray-600 truncate" title={item.name}>
                    {item.name}
                  </div>
                  <div className="flex-1 h-6 bg-gray-100 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-gradient-to-r from-sky-400 to-blue-500 rounded-full flex items-center justify-end pr-2"
                      style={{ width: `${item.percentage}%` }}
                    >
                      {item.percentage > 20 && (
                        <span className="text-[10px] text-white font-medium">{item.count}</span>
                      )}
                    </div>
                  </div>
                  {item.percentage <= 20 && (
                    <div className="w-8 text-xs text-gray-600 text-right">{item.count}</div>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* 재난 유형 Top3 */}
          <div className="mb-3">
            <div className="text-xs text-gray-600 font-medium mb-2">주요 재난 유형 Top3</div>
            <div className="flex gap-2">
              {topTypes.slice(0, 3).map((type, idx) => (
                <div
                  key={idx}
                  className={`flex-1 px-2 py-2 rounded-lg text-center text-xs ${
                    idx === 0
                      ? "bg-sky-100 text-sky-700 border border-sky-200 font-medium"
                      : "bg-gray-100 text-gray-700 border border-gray-200"
                  }`}
                >
                  {type || "-"}
                </div>
              ))}
            </div>
          </div>

          {/* 재난 세부 유형 Top3 */}
          <div>
            <div className="text-xs text-gray-600 font-medium mb-2">세부 유형 Top3</div>
            <div className="text-sm text-gray-800">
              {topSubtypes.slice(0, 3).join(" · ") || "-"}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
