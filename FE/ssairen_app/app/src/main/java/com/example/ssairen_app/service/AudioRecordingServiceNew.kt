package com.example.ssairen_app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ssairen_app.MainActivity
import com.example.ssairen_app.R
import com.example.ssairen_app.data.ApiAudioUploader
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*

/**
 * AudioRecord를 사용한 연속 오디오 녹음 서비스
 * 녹음 중에도 중간 전송 가능
 */
class AudioRecordingServiceNew : Service() {

    companion object {
        private const val TAG = "AudioRecordingNew"
        private const val NOTIFICATION_CHANNEL_ID = "audio_recording_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "오디오 녹음"
        private const val NOTIFICATION_ID = 2001

        const val ACTION_START_RECORDING = "ACTION_START_RECORDING"
        const val ACTION_STOP_RECORDING = "ACTION_STOP_RECORDING"
        const val ACTION_SEND_CURRENT = "ACTION_SEND_CURRENT"  // 중간 전송

        // 오디오 설정
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private val binder = LocalBinder()
    private var audioRecord: AudioRecord? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val apiAudioUploader = ApiAudioUploader()

    private var isRecording = false
    private var recordingJob: Job? = null
    private var currentOutputFile: File? = null
    private var sessionFolder: File? = null
    private var sessionFolderName: String? = null
    private var recordingStartTime: Date? = null

    // 콜백
    private var onRecordingStarted: (() -> Unit)? = null
    private var onRecordingStopped: ((File?) -> Unit)? = null
    private var onRecordingError: ((String) -> Unit)? = null
    private var onUploadComplete: ((String) -> Unit)? = null

    inner class LocalBinder : Binder() {
        fun getService(): AudioRecordingServiceNew = this@AudioRecordingServiceNew
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
            ACTION_SEND_CURRENT -> sendCurrentRecording()
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
        getSystemService(NotificationManager::class.java).notify(
            NOTIFICATION_ID,
            createNotification(contentText)
        )
    }

    fun startRecording() {
        if (isRecording) return

        try {
            recordingStartTime = Date()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            val userName = try {
                com.example.ssairen_app.data.api.RetrofitClient.getAuthManager().getUserName()
            } catch (e: Exception) {
                "사용자"
            }

            sessionFolderName = "${dateFormat.format(recordingStartTime)}/${timeFormat.format(recordingStartTime)}_$userName"

            val outputDir = getExternalFilesDir(null) ?: filesDir
            sessionFolder = File(File(outputDir, "audio_recordings"), sessionFolderName!!.replace("/", File.separator))
            sessionFolder?.mkdirs()

            currentOutputFile = File(sessionFolder, "recording.wav")

            // AudioRecord 초기화
            val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            // WAV 헤더 작성 (나중에 업데이트)
            writeWavHeader(currentOutputFile!!)

            // 녹음 시작
            audioRecord?.startRecording()
            isRecording = true

            // 백그라운드에서 계속 녹음
            recordingJob = serviceScope.launch {
                recordAudioData(currentOutputFile!!, bufferSize)
            }

            updateNotification("녹음 중...")
            onRecordingStarted?.invoke()
            Log.d(TAG, "Recording started: ${currentOutputFile?.name}")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            onRecordingError?.invoke("녹음 시작 실패: ${e.message}")
            isRecording = false
        }
    }

    private suspend fun recordAudioData(outputFile: File, bufferSize: Int) {
        val buffer = ByteArray(bufferSize)
        val fos = FileOutputStream(outputFile, true)  // append mode

        try {
            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                if (read > 0) {
                    fos.write(buffer, 0, read)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during recording", e)
        } finally {
            fos.close()
            // WAV 헤더 업데이트 (파일 크기 정보)
            updateWavHeader(outputFile)
        }
    }

    /**
     * 녹음 중지
     */
    fun stopRecording() {
        if (!isRecording) return

        try {
            isRecording = false
            recordingJob?.cancel()
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null

            currentOutputFile?.let { file ->
                if (file.exists()) {
                    Log.d(TAG, "Recording saved: ${file.name}, size: ${file.length()} bytes")
                    onRecordingStopped?.invoke(file)
                    serviceScope.launch {
                        uploadFile(file)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    /**
     * ✅ 현재까지 녹음된 내용을 전송 (녹음은 계속 진행)
     */
    fun sendCurrentRecording() {
        if (!isRecording) {
            Log.w(TAG, "Not recording, cannot send current")
            return
        }

        currentOutputFile?.let { file ->
            try {
                // 현재 파일을 복사해서 전송
                val timeStamp = SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())
                val copyFile = File(sessionFolder, "partial_$timeStamp.wav")

                // 파일 복사
                file.copyTo(copyFile, overwrite = true)

                // WAV 헤더 업데이트
                updateWavHeader(copyFile)

                Log.d(TAG, "Sending current recording: ${copyFile.name}, size: ${copyFile.length()} bytes")

                // 백그라운드에서 업로드
                serviceScope.launch {
                    uploadFile(copyFile)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send current recording", e)
            }
        }
    }

    private suspend fun uploadFile(file: File) {
        withContext(Dispatchers.IO) {
            try {
                // ✅ 전역 구급일지 ID 가져오기
                val emergencyReportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()

                if (emergencyReportId <= 0) {
                    Log.e(TAG, "Invalid emergency report ID: $emergencyReportId")
                    updateNotification("업로드 실패: 구급일지 ID 없음")
                    return@withContext
                }

                updateNotification("업로드 중...")
                apiAudioUploader.uploadAudio(file, emergencyReportId.toLong()) { progress ->
                    updateNotification("업로드 중... ($progress%)")
                }.onSuccess { uploadResult ->
                    Log.d(TAG, "Upload successful: ${uploadResult.fileName}")

                    // STT 구조화된 데이터 로그 출력
                    uploadResult.sttData?.let { sttData ->
                        val patientName = sttData.reportSectionType.patientInfo.patient.name ?: "없음"
                        val gender = sttData.reportSectionType.patientInfo.patient.gender ?: "없음"
                        val age = sttData.reportSectionType.patientInfo.patient.ageYears?.toString() ?: "없음"

                        Log.d(TAG, "===== STT 구조화 데이터 =====")
                        Log.d(TAG, "환자명: $patientName")
                        Log.d(TAG, "성별: $gender")
                        Log.d(TAG, "나이: $age")
                        Log.d(TAG, "============================")
                    }

                    onUploadComplete?.invoke(uploadResult.fileName)
                    apiAudioUploader.deleteLocalFile(file)
                    updateNotification("업로드 완료")
                }.onFailure { error ->
                    Log.e(TAG, "Upload failed: ${file.name}", error)
                    updateNotification("업로드 실패")
                }

                delay(2000)
                if (!isRecording) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    updateNotification("녹음 중...")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Upload exception", e)
            }
        }
    }

    /**
     * WAV 파일 헤더 작성 (초기)
     */
    private fun writeWavHeader(file: File) {
        val fos = FileOutputStream(file)
        val header = ByteArray(44)

        // RIFF 헤더
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        // 파일 크기 (나중에 업데이트)
        header[4] = 0
        header[5] = 0
        header[6] = 0
        header[7] = 0

        // WAVE
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        // fmt 청크
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16  // fmt 청크 크기
        header[17] = 0
        header[18] = 0
        header[19] = 0

        // 오디오 포맷 (1 = PCM)
        header[20] = 1
        header[21] = 0

        // 채널 수 (1 = 모노)
        header[22] = 1
        header[23] = 0

        // 샘플 레이트 (44100)
        writeInt(header, 24, SAMPLE_RATE)

        // 바이트 레이트 (SampleRate * NumChannels * BitsPerSample/8)
        val byteRate = SAMPLE_RATE * 1 * 16 / 8
        writeInt(header, 28, byteRate)

        // 블록 정렬 (NumChannels * BitsPerSample/8)
        header[32] = 2
        header[33] = 0

        // 비트 퍼 샘플 (16)
        header[34] = 16
        header[35] = 0

        // data 청크
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()

        // data 크기 (나중에 업데이트)
        header[40] = 0
        header[41] = 0
        header[42] = 0
        header[43] = 0

        fos.write(header)
        fos.close()
    }

    /**
     * WAV 파일 헤더 업데이트 (파일 크기 정보)
     */
    private fun updateWavHeader(file: File) {
        val raf = RandomAccessFile(file, "rw")
        val fileSize = raf.length()

        // RIFF 청크 크기 업데이트 (파일 크기 - 8)
        raf.seek(4)
        writeInt32(raf, (fileSize - 8).toInt())

        // data 청크 크기 업데이트 (파일 크기 - 44)
        raf.seek(40)
        writeInt32(raf, (fileSize - 44).toInt())

        raf.close()
    }

    private fun writeInt(buffer: ByteArray, offset: Int, value: Int) {
        buffer[offset] = (value and 0xff).toByte()
        buffer[offset + 1] = ((value shr 8) and 0xff).toByte()
        buffer[offset + 2] = ((value shr 16) and 0xff).toByte()
        buffer[offset + 3] = ((value shr 24) and 0xff).toByte()
    }

    private fun writeInt32(raf: RandomAccessFile, value: Int) {
        val bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
        raf.write(bytes)
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
        stopRecording()
        serviceScope.cancel()
    }
}
