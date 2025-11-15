package com.example.ssairen_app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ssairen_app.data.local.AuthManager
import com.example.ssairen_app.data.repository.ReportRepository
import com.example.ssairen_app.data.model.response.CreatedReportData
import com.example.ssairen_app.data.model.response.MyReportsData
import com.example.ssairen_app.data.model.response.CompleteReportData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReportRepository(AuthManager(application))

    companion object {
        private const val TAG = "ReportViewModel"
    }

    // 일지 생성 상태
    private val _createReportState = MutableLiveData<CreateReportState>()
    val createReportState: LiveData<CreateReportState> = _createReportState

    // 보고서 목록 상태
    private val _reportListState = MutableLiveData<ReportListState>()
    val reportListState: LiveData<ReportListState> = _reportListState

    // 현재 생성된 일지 ID 저장
    private val _currentReportId = MutableLiveData<Int?>()
    val currentReportId: LiveData<Int?> = _currentReportId

    // 무한 스크롤을 위한 상태 관리
    private var currentPage = 0
    private var isLoading = false
    private val allReports = mutableListOf<com.example.ssairen_app.data.model.response.MyEmergencyReport>()

    // UI에 노출되는 무한 스크롤 상태
    private val _hasMoreData = MutableLiveData<Boolean>(true)
    val hasMoreData: LiveData<Boolean> = _hasMoreData

    private val _isLoadingMore = MutableLiveData<Boolean>(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    // 보고서 작성 완료 상태
    private val _completeReportState = MutableLiveData<CompleteReportState>()
    val completeReportState: LiveData<CompleteReportState> = _completeReportState

    /**
     * 보고서 목록 조회 (초기 로드)
     */
    fun getReports() {
        viewModelScope.launch {
            Log.d(TAG, "🔄 초기화: 보고서 목록 조회 시작")
            Log.d(TAG, "🧵 현재 스레드: ${Thread.currentThread().name}")

            // 초기화
            currentPage = 0
            allReports.clear()

            _hasMoreData.postValue(true)
            isLoading = false

            _reportListState.postValue(ReportListState.Loading)
            Log.d(TAG, "📋 보고서 목록 조회 시작...")

            loadReportsPage()
        }
    }

    /**
     * 다음 페이지 로드 (무한 스크롤용)
     */
    fun loadMoreReports() {
        Log.d(TAG, "📞 loadMoreReports 호출됨")
        Log.d(TAG, "   - isLoading: $isLoading")
        Log.d(TAG, "   - hasMoreData: ${_hasMoreData.value}")
        Log.d(TAG, "   - currentPage: $currentPage")
        Log.d(TAG, "   - allReports.size: ${allReports.size}")

        if (isLoading) {
            Log.d(TAG, "⏸️ 이미 로딩 중이므로 중단")
            return
        }

        if (_hasMoreData.value == false) {
            Log.d(TAG, "⏸️ 더 이상 데이터가 없으므로 중단")
            return
        }

        viewModelScope.launch {
            loadReportsPage()
        }
    }

    /**
     * 페이지 로드 공통 로직
     */
    private suspend fun loadReportsPage() {
        if (isLoading) {
            Log.d(TAG, "⚠️ 이미 로딩 중! 중복 호출 차단")
            return
        }

        isLoading = true
        _isLoadingMore.postValue(true)

        Log.d(TAG, "📄 페이지 $currentPage 로딩 시작...")
        Log.d(TAG, "   - 현재 보고서 개수: ${allReports.size}")
        Log.d(TAG, "🧵 loadReportsPage 스레드: ${Thread.currentThread().name}")

        try {
            val result: Result<MyReportsData> = withContext(Dispatchers.IO) {
                Log.d(TAG, "🧵 API 호출 스레드: ${Thread.currentThread().name}")
                repository.getReports(currentPage, 10)
            }

            result.onSuccess { reportListData: MyReportsData ->
                val newReports = reportListData.emergencyReports

                Log.d(TAG, "✅ API 응답 성공!")
                Log.d(TAG, "   - API에서 받은 전체 데이터 개수: ${reportListData.emergencyReports.size}")
                Log.d(TAG, "   - 기존 데이터 개수: ${allReports.size}")

                if (newReports.isEmpty()) {
                    _hasMoreData.postValue(false)
                    Log.d(TAG, "🏁 더 이상 로드할 데이터가 없습니다")
                } else {
                    val existingIds = allReports.map { it.id }.toSet()
                    val uniqueNewReports = newReports.filter { it.id !in existingIds }

                    if (uniqueNewReports.isNotEmpty()) {
                        allReports.addAll(uniqueNewReports)
                        currentPage++
                        Log.d(TAG, "✅ 페이지 로드 성공")
                        Log.d(TAG, "   - ${uniqueNewReports.size}개 추가 (중복 제외)")
                        Log.d(TAG, "   - 총 ${allReports.size}개")
                        Log.d(TAG, "   - 다음 페이지: $currentPage")

                        uniqueNewReports.forEachIndexed { index, report ->
                            Log.d(TAG, "   [$index] report.id: ${report.id}, 재난번호: ${report.dispatchInfo.disasterNumber}")
                        }
                    } else {
                        Log.d(TAG, "⚠️ 모든 데이터가 중복입니다")
                    }

                    if (newReports.size < 10) {
                        _hasMoreData.postValue(false)
                        Log.d(TAG, "🏁 마지막 페이지 도달 (${newReports.size}개 < 10개)")
                    }
                }

                Log.d(TAG, "🔄 UI 업데이트 시도 - 총 ${allReports.size}개 보고서")
                Log.d(TAG, "🧵 UI 업데이트 스레드: ${Thread.currentThread().name}")

                val successState = ReportListState.Success(
                    MyReportsData(
                        paramedicInfo = reportListData.paramedicInfo,
                        emergencyReports = allReports.toList()
                    )
                )

                _reportListState.postValue(successState)
                Log.d(TAG, "✅ postValue 완료")

            }.onFailure { error: Throwable ->
                Log.e(TAG, "❌ 보고서 목록 조회 실패: ${error.message}")
                _reportListState.postValue(
                    ReportListState.Error(error.message ?: "보고서 목록 조회 실패")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 보고서 목록 조회 예외", e)
            _reportListState.postValue(
                ReportListState.Error(e.message ?: "보고서 목록 조회 오류")
            )
        } finally {
            isLoading = false
            _isLoadingMore.postValue(false)
            Log.d(TAG, "🏁 로딩 완료 (isLoading = false)")
        }
    }

    /**
     * 새 일지 등록
     */
    fun createReport(dispatchId: Int) {
        viewModelScope.launch {
            Log.d(TAG, "📝 새 일지 생성 시작... (Dispatch ID: $dispatchId)")

            _createReportState.postValue(CreateReportState.Loading)

            try {
                val result: Result<CreatedReportData> = withContext(Dispatchers.IO) {
                    repository.createReport(dispatchId)
                }

                result.onSuccess { reportData: CreatedReportData ->
                    Log.d(TAG, "✅ 일지 생성 성공 - ID: ${reportData.emergencyReportId}")
                    _createReportState.postValue(CreateReportState.Success(reportData))
                    _currentReportId.postValue(reportData.emergencyReportId)

                }.onFailure { error: Throwable ->
                    Log.e(TAG, "❌ 일지 생성 실패: ${error.message}")
                    _createReportState.postValue(
                        CreateReportState.Error(error.message ?: "일지 생성 실패")
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 일지 생성 예외", e)
                _createReportState.postValue(
                    CreateReportState.Error(e.message ?: "일지 생성 오류")
                )
            }
        }
    }

    /**
     * 일지 생성 상태 초기화
     */
    fun resetCreateState() {
        _createReportState.postValue(CreateReportState.Idle)
    }

    /**
     * 보고서 목록 상태 초기화
     */
    fun resetReportListState() {
        _reportListState.postValue(ReportListState.Idle)
    }

    // ==========================================
    // 보고서 작성 완료
    // ==========================================

    /**
     * 보고서 작성 완료 처리
     */
    fun completeReport(emergencyReportId: Int) {
        viewModelScope.launch {
            Log.d(TAG, "📝 보고서 작성 완료 처리 시작... (Report ID: $emergencyReportId)")

            _completeReportState.postValue(CompleteReportState.Loading)

            try {
                val result: Result<CompleteReportData> = withContext(Dispatchers.IO) {
                    repository.completeReport(emergencyReportId)
                }

                result.onSuccess { data ->
                    Log.d(TAG, "✅ 보고서 작성 완료 성공")
                    Log.d(TAG, "   - emergencyReportId: ${data.emergencyReportId}")
                    Log.d(TAG, "   - isCompleted: ${data.isCompleted}")

                    _completeReportState.postValue(CompleteReportState.Success(data))
                }.onFailure { error: Throwable ->
                    Log.e(TAG, "❌ 보고서 작성 완료 실패: ${error.message}")
                    _completeReportState.postValue(
                        CompleteReportState.Error(error.message ?: "작성 완료 실패")
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 보고서 작성 완료 예외", e)
                _completeReportState.postValue(
                    CompleteReportState.Error(e.message ?: "작성 완료 오류")
                )
            }
        }
    }

    /**
     * 작성 완료 상태 초기화
     */
    fun resetCompleteState() {
        _completeReportState.postValue(CompleteReportState.Idle)
    }
}

// ==========================================
// State 클래스들
// ==========================================

/**
 * 일지 생성 상태
 */
sealed class CreateReportState {
    object Idle : CreateReportState()
    object Loading : CreateReportState()
    data class Success(val reportData: CreatedReportData) : CreateReportState()
    data class Error(val message: String) : CreateReportState()
}

/**
 * 보고서 목록 조회 상태
 */
sealed class ReportListState {
    object Idle : ReportListState()
    object Loading : ReportListState()
    data class Success(val reportListData: MyReportsData) : ReportListState()
    data class Error(val message: String) : ReportListState()
}

/**
 * 보고서 작성 완료 상태
 */
sealed class CompleteReportState {
    object Idle : CompleteReportState()
    object Loading : CompleteReportState()
    data class Success(val data: CompleteReportData) : CompleteReportState()
    data class Error(val message: String) : CompleteReportState()
}