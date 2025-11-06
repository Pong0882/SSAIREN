// WearDataService.kt
package com.example.ssairen_app.ui.wear

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
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
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "✅ WearDataService 시작")

        // Wearable API 클라이언트 초기화
        messageClient = Wearable.getMessageClient(this)
        dataClient = Wearable.getDataClient(this)

        // 리스너 등록
        messageClient.addListener(this)
        dataClient.addListener(this)

        Log.d(TAG, "✅ MessageClient 및 DataClient 리스너 등록 완료")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "WearDataService onStartCommand")
        return START_STICKY // 서비스가 종료되어도 자동 재시작
    }

    override fun onBind(intent: Intent?): IBinder? = null

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
