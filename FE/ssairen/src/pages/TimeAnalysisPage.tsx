import { useState, useEffect, forwardRef, useMemo } from "react";
import Sidebar from "@/components/layout/Sidebar";
import { Header } from "@/components";
import { useAuthStore } from "@/features/auth/store/authStore";
import { fetchTimeStatisticsApi } from "@/features/statistics/api/statisticsApi";
import type { TimeStatisticsResponse } from "@/features/statistics/types/statistics.types";
import {
  BarChart,
  Bar,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  Area,
  ReferenceArea,
  LabelList
} from "recharts";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";

type RangeKey = "week" | "month" | "all" | "custom";

export default function TimeAnalysisPage() {
  const { user } = useAuthStore();
  const [data, setData] = useState<TimeStatisticsResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 현재 “선택된” 범위(프리셋/커스텀 구분용)
  const [dateRange, setDateRange] = useState<RangeKey>("month");

  // 화면과 API가 참조하는 실제 범위
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

  // 유틸
  const fmt = (d: Date) => d.toISOString().split("T")[0];

  const calcPresetRange = (key: Exclude<RangeKey, "custom">) => {
    const end = new Date(); // 오늘
    const start = new Date(end);
    if (key === "week") start.setDate(end.getDate() - 7);
    else if (key === "month") start.setMonth(end.getMonth() - 1);
    else start.setFullYear(end.getFullYear() - 1); // all
    // 시간부 정규화(선택 사항)
    start.setHours(0, 0, 0, 0);
    end.setHours(23, 59, 59, 999);
    return { start, end };
  };

  // 프리셋 버튼 클릭 시: 실제 Date 상태도 채워 넣기
  const applyPreset = (key: Exclude<RangeKey, "custom">) => {
    const { start, end } = calcPresetRange(key);
    setDateRange(key);
    setStartDate(start);
    setEndDate(end);
  };

  // 최초 마운트 시 기본(month) 채움
  useEffect(() => {
    if (startDate && endDate) return;
    const { start, end } = calcPresetRange("month");
    setStartDate(start);
    setEndDate(end);
  }, []); // once

  // 데이터 로드: startDate/endDate가 모두 있을 때만
  useEffect(() => {
    if (!user?.id || !startDate || !endDate) return;

    (async () => {
      setLoading(true);
      setError(null);
      try {
        const result = await fetchTimeStatisticsApi({
          hospitalId: user.id,
          startDate: fmt(startDate),
          endDate: fmt(endDate),
        });
        setData(result);
        // console.log("📊 시간 통계 조회 성공:", result);
      } catch (err) {
        setError(err instanceof Error ? err.message : "통계 조회에 실패했습니다.");
      } finally {
        setLoading(false);
      }
    })();
  }, [user?.id, startDate, endDate]);

  // 커스텀 활성(파란색) 여부: 사용자가 DatePicker로 직접 선택 & 둘 다 존재
  const isCustomActive = dateRange === "custom" && !!startDate && !!endDate;

  // 요일별 차트 데이터
  const dayOfWeekData = data
    ? [
        { name: "월", count: data.byDayOfWeek.MONDAY || 0 },
        { name: "화", count: data.byDayOfWeek.TUESDAY || 0 },
        { name: "수", count: data.byDayOfWeek.WEDNESDAY || 0 },
        { name: "목", count: data.byDayOfWeek.THURSDAY || 0 },
        { name: "금", count: data.byDayOfWeek.FRIDAY || 0 },
        { name: "토", count: data.byDayOfWeek.SATURDAY || 0 },
        { name: "일", count: data.byDayOfWeek.SUNDAY || 0 },
      ]
    : [];

  // 시간대별 차트 데이터 (0~23시)
  const hourlyData = data
    ? Array.from({ length: 24 }, (_, hour) => ({
        name: `${hour}시`,
        count: parseInt(data.byHour[hour.toString()]) || 0,
      }))
    : [];

  return (
    <div className="h-screen flex overflow-hidden">
      <Sidebar />

      {/* 메인 컨텐츠 */}
      <div className="flex-1 flex flex-col overflow-hidden bg-gray-50">
        <Header />

        {/* Main Content */}
        <div className="flex-1 flex flex-col p-4 sm:p-6 lg:p-8 min-h-0">
          <div className="max-w-full h-full flex flex-col gap-4">
            {/* 상단: 필터 행 */}
            <div className="flex gap-3 items-stretch">
              {/* 프리셋 */}
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
                  // 끝이 없으면 임시로 “오늘” 이후 선택 방지용 minDate만 맞춤
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
                  {/* 좌측: 메인 수치 + 배지 2개 */}
                  <div className="flex items-center gap-4 min-w-0">
                    <div className="flex items-center gap-3">
                      <span className="text-sm leading-none text-gray-600 whitespace-nowrap">총 수용 건수</span>
                      <span className="text-xl leading-none font-bold text-sky-500 whitespace-nowrap">
                        {data.totalCount}건
                      </span>
                    </div>
                  </div>

                  {/* 우측: 상세 팝오버 트리거 */}
                  <PopoverSummary
                    startDate={data.startDate}
                    endDate={data.endDate}
                    byHour={data.byHour}
                    byDayOfWeek={data.byDayOfWeek}
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

            {/* 하단: 요일별/시간대별 차트 */}
            {!loading && !error && data && (
              <div className="flex-1 grid grid-cols-1 lg:grid-cols-2 gap-4 min-h-0">
                {/* 요일별 바차트 */}
                <div className="bg-white rounded-lg shadow-lg p-6 flex flex-col min-h-0">
                  <h3 className="text-lg font-semibold text-gray-900 mb-4">요일별 환자 수용 건수</h3>
                  <div className="flex-1 min-h-0">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={dayOfWeekData} margin={{ top: 8, right: 12, bottom: 4, left: 8 }}>
                        {/* 가로 격자선만 아주 옅게 */}
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
                          formatter={(v: number) => [`${v}건`, "수용 건수"]}
                          labelStyle={{ color: "#111827" }}
                          contentStyle={{ borderRadius: 10, borderColor: "#E5E7EB" }}
                        />
                        <Bar dataKey="count" name="수용 건수" fill="#0ea5e9" radius={[6, 6, 0, 0]} maxBarSize={40}>
                        </Bar>
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </div>

                {/* 시간대별 라인차트 */}
                <div className="bg-white rounded-lg shadow-lg p-6 flex flex-col min-h-0">
                  <h3 className="text-lg font-semibold text-gray-900 mb-4">시간대별 환자 수용 건수</h3>
                  <div className="flex-1 min-h-0">
                    <ResponsiveContainer width="100%" height="100%">
                      <LineChart data={hourlyData} margin={{ top: 8, right: 12, bottom: 4, left: 8 }}>
                        {/* 가로 격자선만 아주 옅게 */}
                        <CartesianGrid vertical={false} stroke="#E5E7EB" strokeOpacity={0.35} />
                        <XAxis
                          dataKey="name"
                          tick={{ fill: "#6B7280", fontSize: 12 }}
                          axisLine={{ stroke: "#E5E7EB" }}
                          tickLine={false}
                          interval={1}
                        />
                        <YAxis
                          allowDecimals={false}
                          tick={{ fill: "#6B7280", fontSize: 12 }}
                          axisLine={{ stroke: "#E5E7EB" }}
                          tickLine={false}
                          domain={[0, "dataMax + 1"]}
                        />
                        <Tooltip
                          formatter={(v: number) => [`${v}건`, "수용 건수"]}
                          labelFormatter={(label) => `시간대: ${label}`}
                          contentStyle={{ borderRadius: 10, borderColor: "#E5E7EB" }}
                          labelStyle={{ color: "#111827" }}
                        />
                        <Line
                          type="monotone"
                          dataKey="count"
                          stroke="#0ea5e9"
                          strokeWidth={2}
                          dot={false}
                          activeDot={{ r: 4 }}
                        />
                      </LineChart>
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
  byHour,
  byDayOfWeek,
  totalCount,
}: {
  startDate: string;
  endDate: string;
  byHour: Record<string, number>;
  byDayOfWeek: Record<string, number>;
  totalCount: number;
}) {
  const [open, setOpen] = useState(false);

  const peakHour = useMemo(() => {
    const entries = Object.entries(byHour || {});
    if (!entries.length) return "-";
    const [h] = entries.reduce((a,b) => (Number(a[1]) >= Number(b[1]) ? a : b));
    return `${h}시`;
  }, [byHour]);

  const dayKorean: Record<string,string> = {
    MONDAY: "월", TUESDAY: "화", WEDNESDAY: "수",
    THURSDAY: "목", FRIDAY: "금", SATURDAY: "토", SUNDAY: "일"
  };

  const topDays = useMemo(() => {
    const entries = Object.entries(byDayOfWeek || {});
    return entries
      .sort((a,b) => Number(b[1]) - Number(a[1]))
      .slice(0,3)
      .map(([k,v]) => `${dayKorean[k] || k}(${v})`);
  }, [byDayOfWeek]);

  // 스파크라인 위한 간단 배열(0~23)
  const hourly = useMemo(() => (
    Array.from({length:24}, (_,h) => ({ x:h, y: Number(byHour?.[String(h)] || 0) }))
  ), [byHour]);

  return (
    <div className="relative">
      <button
        onClick={() => setOpen(o => !o)}
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
              <div className="text-sm font-medium text-gray-900">{startDate} ~ {endDate}</div>
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
            <StatPill label="피크 시간" value={peakHour} />
            <StatPill label="Top 요일" value={topDays[0] || "-"} />
          </div>

          {/* 미니 스파크라인 */}
          <div className="mt-4">
            <div className="text-xs text-gray-500 mb-1">시간대 스파크라인</div>
            <div className="h-16 w-full">
              <svg viewBox="0 0 100 30" preserveAspectRatio="none" className="w-full h-full">
                {/* 축소 변환: 24포인트를 100px 폭에, y는 최대값 기준 정규화 */}
                {(() => {
                  const maxY = Math.max(1, ...hourly.map(p => p.y));
                  const points = hourly.map((p, i) => {
                    const x = (i / 23) * 100;
                    const y = 30 - (p.y / maxY) * 28; // 상단 2px 마진
                    return `${x},${y}`;
                  }).join(" ");
                  return (
                    <>
                      <polyline
                        points={points}
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="1.5"
                        className="text-sky-500"
                      />
                      {/* 포인트 점 */}
                      {hourly.map((p,i) => {
                        const x = (i / 23) * 100;
                        const y = 30 - (p.y / Math.max(1, ...hourly.map(pp=>pp.y))) * 28;
                        return <circle key={i} cx={x} cy={y} r="0.9" className="fill-sky-500" />;
                      })}
                    </>
                  );
                })()}
              </svg>
            </div>
          </div>

          {/* Top3 요일 리스트 */}
          <div className="mt-3">
            <div className="text-xs text-gray-500 mb-1">요일 Top3</div>
            <div className="text-sm text-gray-800">{topDays.join(" · ") || "-"}</div>
          </div>
        </div>
      )}
    </div>
  );
}

function StatPill({label, value}:{label:string; value:string}) {
  return (
    <div className="rounded-lg border border-gray-200 px-3 py-2">
      <div className="text-[11px] text-gray-500">{label}</div>
      <div className="text-sm font-medium text-gray-900 mt-0.5">{value}</div>
    </div>
  );
}