package com.example.ssairen_app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ssairen_app.MainActivity
import com.example.ssairen_app.R
import com.example.ssairen_app.data.ApiAudioUploader
import com.example.ssairen_app.viewmodel.ActivityViewModel  // ✅ 추가
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 백그라운드 오디오 녹음을 위한 Foreground Service
 * MediaRecorder를 사용하여 오디오를 녹음하고, 녹화가 완료되면 백엔드로 업로드합니다.
 */
class AudioRecordingService : Service() {

    companion object {
        private const val TAG = "AudioRecordingService"
        private const val NOTIFICATION_CHANNEL_ID = "audio_recording_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "오디오 녹음"
        private const val NOTIFICATION_ID = 2001

        const val ACTION_START_RECORDING = "ACTION_START_RECORDING"
        const val ACTION_STOP_RECORDING = "ACTION_STOP_RECORDING"

        // ✅ 추가: ViewModel 인스턴스를 저장하기 위한 변수
        private var viewModelInstance: ActivityViewModel? = null

        /**
         * ViewModel 인스턴스를 설정합니다.
         * Activity/Fragment에서 호출하여 Service에서 접근할 수 있도록 합니다.
         */
        fun setViewModel(viewModel: ActivityViewModel) {
            viewModelInstance = viewModel
            Log.d(TAG, "✅ ViewModel 인스턴스 설정됨")
        }
    }

    private val binder = LocalBinder()
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val apiAudioUploader = ApiAudioUploader()

    // 세션 관리
    private var sessionFolderName: String? = null
    private var sessionFolder: File? = null
    private var isRecording = false
    private val recordedFiles = mutableListOf<File>()

    // 시간 추적
    private var recordingStartTime: Date? = null

    // 녹음 상태 콜백
    private var onRecordingStarted: (() -> Unit)? = null
    private var onRecordingStopped: ((File?) -> Unit)? = null
    private var onRecordingError: ((String) -> Unit)? = null
    private var onUploadComplete: ((String) -> Unit)? = null

    inner class LocalBinder : Binder() {
        fun getService(): AudioRecordingService = this@AudioRecordingService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECORDING -> {
                startForegroundWithNotification()
                startRecording()
            }
            ACTION_STOP_RECORDING -> stopRecording()
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "오디오 녹음 알림"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun startForegroundWithNotification() {
        startForeground(NOTIFICATION_ID, createNotification("녹음 중..."))
    }

    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("SSAIREN 오디오 녹음")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(contentText: String) {
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, createNotification(contentText))
    }

    @Suppress("DEPRECATION")
    fun startRecording() {
        if (isRecording) return
        try {
            recordingStartTime = Date() as Date?
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            val userName = try {
                com.example.ssairen_app.data.api.RetrofitClient.getAuthManager().getUserName()
            } catch (e: Exception) { "사용자" }

            sessionFolderName = "${dateFormat.format(recordingStartTime)}/${timeFormat.format(recordingStartTime)}_$userName"

            val outputDir = getExternalFilesDir(null) ?: filesDir
            sessionFolder = File(File(outputDir, "audio_recordings"), sessionFolderName!!.replace("/", File.separator)).apply { mkdirs() }

            outputFile = File(sessionFolder, "${timeFormat.format(recordingStartTime)}.m4a")

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(this) else MediaRecorder()
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile?.absolutePath)
                prepare()
                start()
            }

            isRecording = true
            updateNotification("녹음 중...")
            onRecordingStarted?.invoke()
            Log.d(TAG, "Recording started: ${outputFile?.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            onRecordingError?.invoke("녹음 시작 실패: ${e.message}")
            isRecording = false
        }
    }

    fun stopRecording() {
        if (!isRecording) return
        try {
            mediaRecorder?.apply { stop(); release() }
            mediaRecorder = null
            isRecording = false

            outputFile?.let { file ->
                if (file.exists()) {
                    recordedFiles.add(file)
                    Log.d(TAG, "Recording saved: ${file.name}")
                    onRecordingStopped?.invoke(file)
                    uploadAllFiles()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun uploadAllFiles() {
        serviceScope.launch {
            try {
                recordedFiles.forEachIndexed { index, file ->
                    updateNotification("업로드 중... (${index + 1}/${recordedFiles.size})")
                    apiAudioUploader.uploadAudio(file) { progress ->
                        updateNotification("업로드 중... (${index + 1}/${recordedFiles.size} - $progress%)")
                    }.onSuccess { uploadResult ->
                        Log.d(TAG, "Upload successful: ${uploadResult.fileName}")

                        // ✅ 수정: STT 구조화된 데이터를 ViewModel로 전달
                        uploadResult.sttData?.let { sttData ->
                            Log.d(TAG, "===== STT 구조화 데이터 =====")

                            val patientName = sttData.reportSectionType.patientInfo.patient.name ?: "없음"
                            val gender = sttData.reportSectionType.patientInfo.patient.gender ?: "없음"
                            val age = sttData.reportSectionType.patientInfo.patient.ageYears?.toString() ?: "없음"
                            val chiefComplaint = sttData.reportSectionType.assessment.notes.cheifComplaint ?: "없음"

                            Log.d(TAG, "환자명: $patientName")
                            Log.d(TAG, "성별: $gender")
                            Log.d(TAG, "나이: $age")
                            Log.d(TAG, "주호소: $chiefComplaint")
                            Log.d(TAG, "============================")

                            // ✅ ViewModel로 STT 데이터 전달
                            if (viewModelInstance != null) {
                                viewModelInstance?.updateSttData(sttData)
                                Log.d(TAG, "✅ STT 데이터를 ViewModel로 전달 완료")
                            } else {
                                Log.w(TAG, "⚠️ ViewModel 인스턴스가 설정되지 않았습니다. STT 데이터를 전달할 수 없습니다.")
                            }
                        }

                        apiAudioUploader.deleteLocalFile(file)
                    }.onFailure { error ->
                        Log.e(TAG, "Upload failed: ${file.name}", error)
                    }
                }
                updateNotification("업로드 완료")
                delay(3000)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            } catch (e: Exception) {
                Log.e(TAG, "Upload exception", e)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    fun setRecordingCallbacks(
        onStarted: () -> Unit,
        onStopped: (File?) -> Unit,
        onError: (String) -> Unit,
        onUploadComplete: (String) -> Unit = {}
    ) {
        this.onRecordingStarted = onStarted
        this.onRecordingStopped = onStopped
        this.onRecordingError = onError
        this.onUploadComplete = onUploadComplete
    }

    fun isCurrentlyRecording() = isRecording

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        serviceScope.cancel()
    }
}