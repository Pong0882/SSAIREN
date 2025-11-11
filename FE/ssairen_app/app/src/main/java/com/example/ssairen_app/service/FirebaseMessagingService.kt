package com.example.ssairen_app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ssairen_app.MainActivity
import com.example.ssairen_app.R
import com.example.ssairen_app.data.websocket.DispatchMessage
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson

/**
 * Firebase Cloud Messaging 서비스
 *
 * 역할:
 * 1. 앱이 포그라운드(실행 중)일 때: WebSocket으로 이미 받음 → 푸시 무시
 * 2. 앱이 백그라운드/종료 상태일 때: 푸시 알림 표시
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM_Service"
        private const val CHANNEL_ID = "dispatch_channel"
        private const val NOTIFICATION_ID = 1001

        // ✅ 앱이 포그라운드인지 확인하는 플래그
        var isAppInForeground = false
    }

    /**
     * FCM 메시지 수신
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "📩 FCM Message received from: ${message.from}")

        // ✅ 앱이 포그라운드면 무시 (WebSocket이 이미 처리함)
        if (isAppInForeground) {
            Log.d(TAG, "✅ App is in foreground, ignoring FCM (WebSocket will handle it)")
            return
        }

        // ✅ 백그라운드면 푸시 알림 표시
        Log.d(TAG, "📱 App is in background, showing notification")

        // Data payload 파싱
        val data = message.data
        Log.d(TAG, "📦 Data payload: $data")

        // 출동 데이터 추출
        val disasterNumber = data["disasterNumber"] ?: "UNKNOWN"
        val disasterType = data["disasterType"] ?: "긴급출동"
        val disasterSubtype = data["disasterSubtype"] ?: ""
        val dispatchLevel = data["dispatchLevel"] ?: ""
        val location = data["locationAddress"] ?: "위치 정보 없음"

        // 제목 생성 (예: "화재 | 실전")
        val title = buildString {
            append(disasterType)
            if (dispatchLevel.isNotEmpty()) append(" | $dispatchLevel")
        }

        // 알림 표시
        showNotification(
            title = title,
            message = "$location${if (disasterSubtype.isNotEmpty()) " - $disasterSubtype" else ""}",
            data = data
        )
    }

    /**
     * 새로운 FCM 토큰 생성 시 호출
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔑 New FCM token: $token")

        // TODO: 새 토큰을 서버에 전송 (필요시)
        // sendTokenToServer(token)
    }

    /**
     * 푸시 알림 표시
     */
    private fun showNotification(title: String, message: String, data: Map<String, String>) {
        // 알림 채널 생성 (Android 8.0 이상)
        createNotificationChannel()

        // 알림 클릭 시 MainActivity로 이동 (모든 출동 데이터 포함)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("from_notification", true)

            // 모든 출동 데이터를 Intent에 담기
            putExtra("disasterNumber", data["disasterNumber"])
            putExtra("disasterType", data["disasterType"])
            putExtra("disasterSubtype", data["disasterSubtype"])
            putExtra("dispatchLevel", data["dispatchLevel"])
            putExtra("locationAddress", data["locationAddress"])
            putExtra("reporterName", data["reporterName"])
            putExtra("reporterPhone", data["reporterPhone"])
            putExtra("incidentDescription", data["incidentDescription"])
            putExtra("dispatchStation", data["dispatchStation"])
            putExtra("dispatchOrder", data["dispatchOrder"])
            putExtra("fireStateId", data["fireStateId"])
            putExtra("paramedicId", data["paramedicId"])
            putExtra("date", data["date"])
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 알림 빌드
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)  // 아이콘
            .setContentTitle("🚨 $title")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)  // 클릭 시 자동 삭제
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))  // 진동 패턴
            .build()

        // 알림 표시
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        Log.d(TAG, "✅ Notification shown: $title - $message")
    }

    /**
     * 알림 채널 생성 (Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "출동 지령 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "구급 출동 지령을 받는 채널입니다"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
