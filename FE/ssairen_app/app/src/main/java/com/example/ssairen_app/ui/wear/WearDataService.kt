// WearDataService.kt
package com.example.ssairen_app.ui.wear

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ssairen_app.R
import com.google.android.gms.wearable.*
import java.nio.charset.StandardCharsets

/**
 * Wear OS로부터 심박수 및 산소포화도 데이터를 수신하는 백그라운드 서비스
 */
class WearDataService : Service(), MessageClient.OnMessageReceivedListener,
    DataClient.OnDataChangedListener {

    private lateinit var messageClient: MessageClient
    private lateinit var dataClient: DataClient

    companion object {
        private const val TAG = "WearDataService"

        // Wear 모듈에서 정의한 경로들
        private const val HR_MSG_PATH = "/hr_msg"
        private const val HR_DATA_PATH = "/heart_rate"
        private const val SPO2_MSG_PATH = "/spo2_msg"
        private const val SPO2_DATA_PATH = "/spo2"
        private const val STATUS_ERROR_PATH = "/status_error"
        private const val STATUS_INFO_PATH = "/status_info"

        // 알림 관련 상수
        private const val CHANNEL_ID = "vital_signs_alert"
        private const val CHANNEL_NAME = "생체신호 이상 알림"
        private const val NOTIFICATION_ID_HR_LOW = 1001
        private const val NOTIFICATION_ID_HR_HIGH = 1002
        private const val NOTIFICATION_ID_SPO2_LOW = 1003

        // 이상치 기준값
        private const val HR_LOW_THRESHOLD = 50
        private const val HR_HIGH_THRESHOLD = 110
        private const val SPO2_LOW_THRESHOLD = 93
    }

    private lateinit var notificationManager: NotificationManager
    private lateinit var vibrator: Vibrator

    // 중복 알림 방지를 위한 플래그
    private var lastHrAlertTime = 0L
    private var lastSpo2AlertTime = 0L
    private val ALERT_COOLDOWN = 10000L // 10초 쿨다운

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "✅ WearDataService 시작")

        // Wearable API 클라이언트 초기화
        messageClient = Wearable.getMessageClient(this)
        dataClient = Wearable.getDataClient(this)

        // 리스너 등록
        messageClient.addListener(this)
        dataClient.addListener(this)

        // 알림 관련 초기화
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        // 알림 채널 생성
        createNotificationChannel()

        Log.d(TAG, "✅ MessageClient 및 DataClient 리스너 등록 완료")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "WearDataService onStartCommand")
        return START_STICKY // 서비스가 종료되어도 자동 재시작
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * 알림 채널 생성 (Android 8.0 이상)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "환자 생체신호 이상 감지 시 알림"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
                // 커스텀 소리 (beep.mp3)
                val soundUri = Uri.parse("android.resource://${applicationContext.packageName}/${R.raw.beep}")
                setSound(
                    soundUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "✅ 알림 채널 생성 완료 (beep.mp3)")
        }
    }

    /**
     * 생체신호 이상 알림 발송
     */
    private fun sendVitalSignAlert(
        notificationId: Int,
        title: String,
        message: String,
        priority: Int = NotificationCompat.PRIORITY_HIGH
    ) {
        // MainActivity로 이동하는 Intent
        val intent = Intent(this, Class.forName("com.example.ssairen_app.MainActivity"))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 커스텀 소리 URI
        val soundUri = Uri.parse("android.resource://$packageName/${R.raw.beep}")

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
            // 커스텀 소리 (beep.mp3)
            .setSound(soundUri)
            .build()

        notificationManager.notify(notificationId, notification)

        // 추가 진동 (더 강하게)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 500, 250, 500, 250, 500),
                    -1
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 500, 250, 500, 250, 500), -1)
        }

        Log.d(TAG, "🚨 알림 발송: $title - $message")
    }

    /**
     * 심박수 이상치 체크 및 알림
     */
    private fun checkHeartRateAlert(hr: Int) {
        val currentTime = System.currentTimeMillis()

        // 쿨다운 시간 체크
        if (currentTime - lastHrAlertTime < ALERT_COOLDOWN) {
            return
        }

        when {
            hr > 0 && hr <= HR_LOW_THRESHOLD -> {
                sendVitalSignAlert(
                    NOTIFICATION_ID_HR_LOW,
                    "⚠️ 심박수 저하 경고",
                    "환자의 심박수가 ${hr}BPM으로 정상 범위보다 낮습니다. (기준: ${HR_LOW_THRESHOLD}BPM 이하)"
                )
                lastHrAlertTime = currentTime
            }
            hr >= HR_HIGH_THRESHOLD -> {
                sendVitalSignAlert(
                    NOTIFICATION_ID_HR_HIGH,
                    "⚠️ 심박수 상승 경고",
                    "환자의 심박수가 ${hr}BPM으로 정상 범위보다 높습니다. (기준: ${HR_HIGH_THRESHOLD}BPM 이상)"
                )
                lastHrAlertTime = currentTime
            }
        }
    }

    /**
     * 산소포화도 이상치 체크 및 알림
     */
    private fun checkSpO2Alert(spo2: Int) {
        val currentTime = System.currentTimeMillis()

        // 쿨다운 시간 체크
        if (currentTime - lastSpo2AlertTime < ALERT_COOLDOWN) {
            return
        }

        if (spo2 > 0 && spo2 <= SPO2_LOW_THRESHOLD) {
            sendVitalSignAlert(
                NOTIFICATION_ID_SPO2_LOW,
                "⚠️ 산소포화도 저하 경고",
                "환자의 산소포화도가 ${spo2}%로 정상 범위보다 낮습니다. (기준: ${SPO2_LOW_THRESHOLD}% 이하)"
            )
            lastSpo2AlertTime = currentTime
        }
    }

    // ========= Message API 수신 (실시간 데이터) =========
    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "📩 메시지 수신! 경로: ${messageEvent.path}, 데이터 크기: ${messageEvent.data.size}")

        when (messageEvent.path) {
            HR_MSG_PATH -> {
                // 심박수 메시지 수신
                val hrString = String(messageEvent.data, StandardCharsets.UTF_8)
                val hr = hrString.toIntOrNull() ?: 0
                Log.d(TAG, "❤️ 심박수 수신: $hr BPM (원본 문자열: '$hrString')")

                // ViewModel 업데이트
                val viewModel = WearDataViewModel.getInstanceOrNull()
                if (viewModel != null) {
                    viewModel.updateHeartRate(hr)
                    Log.d(TAG, "✅ ViewModel에 심박수 업데이트 완료")
                } else {
                    Log.e(TAG, "❌ ViewModel이 null입니다! 심박수 업데이트 실패")
                }

                // 심박수 이상치 체크 및 알림
                checkHeartRateAlert(hr)
            }

            SPO2_MSG_PATH -> {
                // 산소포화도 메시지 수신
                val spo2String = String(messageEvent.data, StandardCharsets.UTF_8)
                val spo2 = spo2String.toIntOrNull() ?: 0
                Log.d(TAG, "🫁 산소포화도 수신: $spo2% (원본 문자열: '$spo2String')")

                // ViewModel 업데이트
                val viewModel = WearDataViewModel.getInstanceOrNull()
                if (viewModel != null) {
                    viewModel.updateSpO2(spo2)
                    // SpO2 정상 수신 시 에러 메시지 클리어
                    if (spo2 > 0) {
                        viewModel.updateSpo2ErrorMessage("")
                    }
                    Log.d(TAG, "✅ ViewModel에 산소포화도 업데이트 완료")
                } else {
                    Log.e(TAG, "❌ ViewModel이 null입니다! 산소포화도 업데이트 실패")
                }

                // 산소포화도 이상치 체크 및 알림
                checkSpO2Alert(spo2)
            }

            STATUS_ERROR_PATH -> {
                // SpO2 에러 메시지 수신
                val errorMessage = String(messageEvent.data, StandardCharsets.UTF_8)
                Log.d(TAG, "⚠️ SpO2 에러 메시지 수신: '$errorMessage'")

                val viewModel = WearDataViewModel.getInstanceOrNull()
                if (viewModel != null) {
                    viewModel.updateSpo2ErrorMessage(errorMessage)
                    Log.d(TAG, "✅ ViewModel에 에러 메시지 업데이트 완료")
                } else {
                    Log.e(TAG, "❌ ViewModel이 null입니다! 에러 메시지 업데이트 실패")
                }
            }

            STATUS_INFO_PATH -> {
                // HR 상태 메시지 수신 (재정비 중 등)
                val statusMessage = String(messageEvent.data, StandardCharsets.UTF_8)
                Log.d(TAG, "ℹ️ HR 상태 메시지 수신: '$statusMessage'")

                val viewModel = WearDataViewModel.getInstanceOrNull()
                if (viewModel != null) {
                    viewModel.updateHrStatusMessage(statusMessage)
                    Log.d(TAG, "✅ ViewModel에 상태 메시지 업데이트 완료")
                } else {
                    Log.e(TAG, "❌ ViewModel이 null입니다! 상태 메시지 업데이트 실패")
                }
            }

            else -> {
                Log.w(TAG, "⚠️ 알 수 없는 메시지 경로: ${messageEvent.path}")
            }
        }
    }

    // ========= DataItem API 수신 (백업 데이터) =========
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                Log.d(TAG, "DataItem 수신: ${dataItem.uri.path}")

                when (dataItem.uri.path) {
                    HR_DATA_PATH -> {
                        // 심박수 DataItem 수신
                        val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                        val hr = dataMap.getFloat("heart_rate_value", 0f).toInt()
                        Log.d(TAG, "심박수 DataItem: $hr BPM")

                        WearDataViewModel.getInstanceOrNull()?.updateHeartRate(hr)
                    }

                    SPO2_DATA_PATH -> {
                        // 산소포화도 DataItem 수신
                        val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                        val spo2 = dataMap.getFloat("spo2_value", 0f).toInt()
                        Log.d(TAG, "산소포화도 DataItem: $spo2%")

                        WearDataViewModel.getInstanceOrNull()?.updateSpO2(spo2)
                    }
                }
            }
        }
        dataEvents.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "WearDataService 종료")

        // 리스너 제거
        messageClient.removeListener(this)
        dataClient.removeListener(this)
    }
}
