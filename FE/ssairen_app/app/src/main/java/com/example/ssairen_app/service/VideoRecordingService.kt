package com.example.ssairen_app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import android.annotation.SuppressLint
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.example.ssairen_app.MainActivity
import com.example.ssairen_app.R
import com.example.ssairen_app.data.ApiVideoUploader
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 백그라운드 비디오 녹화를 위한 Foreground Service
 * CameraX를 사용하여 비디오를 녹화하고, 녹화가 완료되면 MinIO로 업로드합니다.
 */
class VideoRecordingService : LifecycleService() {

    companion object {
        private const val TAG = "VideoRecordingService"
        private const val NOTIFICATION_CHANNEL_ID = "video_recording_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "비디오 녹화"
        private const val NOTIFICATION_ID = 1001

        // 자동 분할 주기: 8분 (FHD 1920x1080 @ 6Mbps 기준 약 360MB)
        private const val MAX_RECORDING_DURATION_MILLIS = 8 * 60 * 1000L

        const val ACTION_START_RECORDING = "ACTION_START_RECORDING"
        const val ACTION_STOP_RECORDING = "ACTION_STOP_RECORDING"
    }

    private val binder = LocalBinder()
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var outputFile: File? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val apiVideoUploader = ApiVideoUploader()

    // 세션 관리
    private var sessionFolderName: String? = null  // BODYCAM_20250106_123045
    private var sessionFolder: File? = null
    private var currentPartNumber = 1
    private var isRecordingSession = false
    private var autoRestartJob: Job? = null
    private val recordedFiles = mutableListOf<File>()  // 업로드 대기 파일들

    // 시간 추적 (파일명 생성용)
    private var sessionStartDate: Date? = null  // 세션 시작 날짜/시간
    private var partStartTime: Date? = null  // 현재 파트 시작 시간

    // 녹화 상태 콜백
    private var onRecordingStarted: (() -> Unit)? = null
    private var onRecordingStopped: ((File?) -> Unit)? = null
    private var onRecordingError: ((String) -> Unit)? = null
    private var onUploadComplete: ((String) -> Unit)? = null
    private var onRecordingProgress: ((Long) -> Unit)? = null  // 녹화 시간(초) 콜백

    inner class LocalBinder : Binder() {
        fun getService(): VideoRecordingService = this@VideoRecordingService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START_RECORDING -> {
                startForegroundWithNotification()
                startRecording()
            }
            ACTION_STOP_RECORDING -> {
                stopRecording()
            }
        }

        return START_STICKY
    }

    /**
     * Notification 채널 생성 (Android 8.0 이상)
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "바디캠 비디오 녹화 알림"
            setShowBadge(false)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Foreground Service 시작 및 알림 표시
     */
    private fun startForegroundWithNotification() {
        val notification = createNotification("녹화 중...")
        startForeground(NOTIFICATION_ID, notification)
    }

    /**
     * 알림 생성
     */
    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("SSAIREN 바디캠")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher) // 실제 아이콘으로 변경 필요
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * 알림 업데이트
     */
    private fun updateNotification(contentText: String) {
        val notification = createNotification(contentText)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * 비디오 녹화 시작 (새 세션)
     */
    fun startRecording() {
        // 새 세션 시작
        if (!isRecordingSession) {
            isRecordingSession = true
            currentPartNumber = 1
            recordedFiles.clear()

            // 세션 시작 시간 저장
            sessionStartDate = Date()

            // 세션 폴더 생성 (날짜/시작시간_사용자명 구조)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            val dateFolder = dateFormat.format(sessionStartDate)
            val timeFolder = timeFormat.format(sessionStartDate)
            // TokenManager에서 사용자 이름 조회
            val userName = try {
                com.example.ssairen_app.data.api.RetrofitClient.getTokenManager().getUserName()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get user name from TokenManager, using default", e)
                "사용자"
            }

            sessionFolderName = "$dateFolder/${timeFolder}_$userName"  // 예: 2025-01-06/12:30:45_김민지

            val outputDir = getExternalFilesDir(null) ?: filesDir
            sessionFolder = File(outputDir, sessionFolderName!!.replace("/", File.separator)).apply {
                if (!exists()) {
                    mkdirs()
                }
            }

            Log.d(TAG, "New recording session started: $sessionFolderName")
        }

        startRecordingPart()
    }

    /**
     * 파트 녹화 시작 (내부 메서드)
     * 권한은 UI 레벨(BodyCamScreen)에서 체크되므로 여기서는 suppress
     */
    @SuppressLint("MissingPermission")
    private fun startRecordingPart() {
        serviceScope.launch {
            try {
                Log.d(TAG, "Starting recording part $currentPartNumber...")

                // 카메라 프로바이더 가져오기
                val cameraProvider = ProcessCameraProvider.getInstance(this@VideoRecordingService).get()

                // 이전 바인딩 해제
                cameraProvider.unbindAll()

                // Recorder 설정 - FHD (1080p) 화질, 비트레이트 제한
                val recorder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.from(
                            Quality.FHD,  // FHD 1920x1080
                            FallbackStrategy.higherQualityOrLowerThan(Quality.FHD)
                        )
                    )
                    // 비트레이트 제한 (6 Mbps = 약 45MB/분, 8분 = 360MB)
                    .setTargetVideoEncodingBitRate(6_000_000)  // 6 Mbps
                    .build()

                videoCapture = VideoCapture.withOutput(recorder)

                // 카메라 선택 (전면 카메라)
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                try {
                    // 카메라 바인딩
                    cameraProvider.bindToLifecycle(
                        this@VideoRecordingService,
                        cameraSelector,
                        videoCapture
                    )

                    // 출력 파일 생성
                    outputFile = createOutputFile()
                    val fileOutputOptions = FileOutputOptions.Builder(outputFile!!).build()

                    // 녹화 시작
                    recording = videoCapture?.output
                        ?.prepareRecording(this@VideoRecordingService, fileOutputOptions)
                        ?.withAudioEnabled()
                        ?.start(ContextCompat.getMainExecutor(this@VideoRecordingService)) { event ->
                            when (event) {
                                is VideoRecordEvent.Start -> {
                                    // 파트 시작 시간 저장
                                    partStartTime = Date()

                                    Log.d(TAG, "Recording part $currentPartNumber started at ${getCurrentTime()}")
                                    updateNotification("녹화 중 (Part $currentPartNumber)... (${getCurrentTime()})")

                                    if (currentPartNumber == 1) {
                                        onRecordingStarted?.invoke()
                                    }

                                    // 8분 후 자동 재시작 타이머
                                    startAutoRestartTimer()
                                }
                                is VideoRecordEvent.Finalize -> {
                                    // 타이머 취소
                                    autoRestartJob?.cancel()

                                    if (event.hasError()) {
                                        Log.e(TAG, "Recording error: ${event.error}")
                                        onRecordingError?.invoke("녹화 오류: ${event.error}")
                                        outputFile = null
                                        onRecordingStopped?.invoke(outputFile)
                                    } else {
                                        Log.d(TAG, "Recording part $currentPartNumber finalized: ${outputFile?.absolutePath}")

                                        // 파일명을 시작시간_종료시간.mp4로 변경
                                        outputFile?.let { file ->
                                            val renamedFile = renameFileWithTimeRange(file)
                                            outputFile = renamedFile
                                            recordedFiles.add(renamedFile)
                                            Log.d(TAG, "Renamed to ${renamedFile.name} and added to recorded files list")
                                        }

                                        // 세션이 계속되는 경우 (자동 재시작)
                                        if (isRecordingSession) {
                                            currentPartNumber++
                                            Log.d(TAG, "Starting next part: $currentPartNumber")
                                            startRecordingPart()
                                        } else {
                                            // 세션 종료 - 모든 파일 업로드
                                            Log.d(TAG, "Recording session ended, uploading ${recordedFiles.size} files")
                                            onRecordingStopped?.invoke(outputFile)
                                            uploadAllFiles()
                                        }
                                    }
                                }
                                is VideoRecordEvent.Status -> {
                                    // 녹화 상태 업데이트 (파일 크기, 시간 등)
                                    val duration = event.recordingStats.recordedDurationNanos / 1_000_000_000
                                    updateNotification("녹화 중 (Part $currentPartNumber)... (${duration}초)")
                                    onRecordingProgress?.invoke(duration)
                                }
                            }
                        }

                } catch (e: Exception) {
                    Log.e(TAG, "Camera binding failed", e)
                    onRecordingError?.invoke("카메라 초기화 실패: ${e.message}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to start recording", e)
                onRecordingError?.invoke("녹화 시작 실패: ${e.message}")
            }
        }
    }

    /**
     * 비디오 녹화 중지 (세션 종료)
     */
    fun stopRecording() {
        Log.d(TAG, "Stopping video recording session...")
        updateNotification("녹화 중지 중...")

        // 세션 종료 플래그 설정 (Finalize 이벤트에서 자동 재시작 방지)
        isRecordingSession = false

        // 타이머 취소
        autoRestartJob?.cancel()

        // 현재 녹화 중지
        recording?.stop()
        recording = null
    }

    /**
     * 출력 파일 생성 (세션 폴더 내에 part 번호로 임시 생성)
     */
    private fun createOutputFile(): File {
        val fileName = "part_${String.format("%03d", currentPartNumber)}.mp4"
        return File(sessionFolder, fileName)
    }

    /**
     * 파일명을 시작시간_종료시간.mp4로 변경
     */
    private fun renameFileWithTimeRange(file: File): File {
        val endTime = Date()
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val startTimeStr = partStartTime?.let { timeFormat.format(it) } ?: "00:00:00"
        val endTimeStr = timeFormat.format(endTime)

        // 파일명: 12:30:45_12:37:45.mp4
        val newFileName = "${startTimeStr}_${endTimeStr}.mp4"
        val newFile = File(file.parent, newFileName)

        if (file.renameTo(newFile)) {
            Log.d(TAG, "File renamed: ${file.name} -> ${newFile.name}")
            return newFile
        } else {
            Log.w(TAG, "Failed to rename file, using original name")
            return file
        }
    }

    /**
     * 8분 후 자동으로 다음 파트 녹화 시작
     */
    private fun startAutoRestartTimer() {
        // 이전 타이머 취소
        autoRestartJob?.cancel()

        autoRestartJob = serviceScope.launch {
            delay(MAX_RECORDING_DURATION_MILLIS)

            if (isRecordingSession) {
                Log.d(TAG, "Auto-restarting recording for next part...")
                restartRecordingPart()
            }
        }
    }

    /**
     * 현재 파트 녹화를 중지하고 다음 파트 시작
     */
    private fun restartRecordingPart() {
        Log.d(TAG, "Restarting recording part...")

        // 현재 녹화 중지 (Finalize 이벤트에서 자동으로 다음 파트 시작)
        recording?.stop()
        recording = null
    }

    /**
     * 현재 시간 문자열 반환
     */
    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }

    /**
     * 세션의 모든 파일을 백엔드로 업로드
     */
    private fun uploadAllFiles() {
        serviceScope.launch {
            try {
                val totalFiles = recordedFiles.size
                if (totalFiles == 0) {
                    Log.w(TAG, "No files to upload")
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return@launch
                }

                Log.d(TAG, "Uploading $totalFiles files from session: $sessionFolderName")

                var successCount = 0
                var failCount = 0

                recordedFiles.forEachIndexed { index, file ->
                    updateNotification("업로드 중... (${index + 1}/$totalFiles)")

                    val result = apiVideoUploader.uploadVideo(file) { progress ->
                        updateNotification("업로드 중... (${index + 1}/$totalFiles - $progress%)")
                    }

                    result.onSuccess { uploadResult ->
                        successCount++
                        Log.d(TAG, "Upload successful [${index + 1}/$totalFiles]: ${uploadResult.fileName}")

                        // 업로드 성공 후 로컬 파일 삭제
                        apiVideoUploader.deleteLocalFile(file)
                    }.onFailure { error ->
                        failCount++
                        Log.e(TAG, "Upload failed [${index + 1}/$totalFiles]: ${file.name}", error)
                    }
                }

                // 업로드 완료
                if (failCount == 0) {
                    updateNotification("모든 파일 업로드 완료 ($successCount/$totalFiles)")
                    onUploadComplete?.invoke(sessionFolderName ?: "")
                } else {
                    updateNotification("업로드 완료 (성공: $successCount, 실패: $failCount)")
                    onRecordingError?.invoke("일부 파일 업로드 실패: $failCount/$totalFiles")
                }

                // 잠시 후 서비스 종료
                delay(3000)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()

            } catch (e: Exception) {
                Log.e(TAG, "Upload exception", e)
                updateNotification("업로드 오류: ${e.message}")
                onRecordingError?.invoke("업로드 오류: ${e.message}")

                delay(5000)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    /**
     * 콜백 설정
     */
    fun setRecordingCallbacks(
        onStarted: () -> Unit,
        onStopped: (File?) -> Unit,
        onError: (String) -> Unit,
        onUploadComplete: (String) -> Unit = {},
        onProgress: (Long) -> Unit = {}
    ) {
        this.onRecordingStarted = onStarted
        this.onRecordingStopped = onStopped
        this.onRecordingError = onError
        this.onUploadComplete = onUploadComplete
        this.onRecordingProgress = onProgress
    }

    /**
     * 현재 녹화 상태 확인
     */
    fun isCurrentlyRecording(): Boolean {
        return isRecordingSession && recording != null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        recording?.stop()
        serviceScope.cancel()
    }
}
