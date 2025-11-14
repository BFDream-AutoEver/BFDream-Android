package com.example.bfdream_android.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

// 상수 정의 (ESP32 코드와 일치해야 함)
private const val SERVICE_UUID_STRING = "12345678-1234-1234-1234-123456789ABC"
private const val RX_CHARACTERISTIC_UUID_STRING = "12345678-1234-1234-1234-123456789ABD"
//private const val TX_CHARACTERISTIC_UUID_STRING = "12345678-1234-1234-1234-123456789ABE" // 필요 시 사용
//private const val BUS_DEVICE_NAME_PREFIX = "BF_DREAM_" // 이름 필터 사용 안 함
private const val SCAN_PERIOD: Long = 10000 // 10초 스캔

// 클래스 이름을 BTViewModel로 변경
class BTViewModel(private val context: Context) : ViewModel() {

    // --- 상태 정의 ---
    sealed class BleConnectionState {
        data object Idle : BleConnectionState() // 초기 상태
        data object Scanning : BleConnectionState() // 스캔 중
        data object Connecting : BleConnectionState() // 연결 중
        data object Connected : BleConnectionState() // 연결됨 (Characteristic 탐색 중)
        data object Success : BleConnectionState() // 전송 성공
        data class Error(val message: String) : BleConnectionState() // 오류 발생
    }

    // --- StateFlow ---
    private val _connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Idle)
    val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _selectedBusId = MutableStateFlow<String?>(null) // 선택된 버스 ID (Private)
    val selectedBusId: StateFlow<String?> = _selectedBusId.asStateFlow() // 선택된 버스 ID (Public)

    // --- Bluetooth 관련 변수 ---
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var bluetoothGatt: BluetoothGatt? = null
    private var targetDevice: BluetoothDevice? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isScanning = false

    // UUID 객체
    private val serviceUUID: UUID = UUID.fromString(SERVICE_UUID_STRING)
    private val rxCharacteristicUUID: UUID = UUID.fromString(RX_CHARACTERISTIC_UUID_STRING)
    //private val txCharacteristicUUID: UUID = UUID.fromString(TX_CHARACTERISTIC_UUID_STRING)

    // 로그 태그 변경
    private val TAG = "BTViewModel"

    // --- 공개 함수 ---

    /** 버스 ID 선택 */
    fun selectBus(busId: String?) {
        // 이미 연결/전송 중일 때는 선택 변경 방지 (선택사항)
        if (_isSending.value || _connectionState.value is BleConnectionState.Connecting || _connectionState.value is BleConnectionState.Connected) {
            Log.w(TAG, "통신 중에는 버스를 변경할 수 없습니다.")
            return
        }
        _selectedBusId.value = busId
        Log.d(TAG, "선택된 버스 ID: $busId")
    }

    /** 상태 초기화 */
    fun resetState() {
        Log.d(TAG, "상태 초기화")
        _connectionState.update { BleConnectionState.Idle }
        _isSending.update { false }
        // 필요하다면 _selectedBusId.value = null 추가
    }


    /** 배려석 알림 전송 시작 */
    @SuppressLint("MissingPermission") // 권한 체크는 MainActivity에서 수행
    fun sendCourtesySeatNotification() {
        Log.d(TAG, "sendCourtesySeatNotification 호출됨")
        val busId = _selectedBusId.value
        if (busId == null) {
            _connectionState.update { BleConnectionState.Error("버스를 먼저 선택해주세요.") }
            return
        }
        if (_isSending.value) {
            Log.w(TAG, "이미 전송 중입니다.")
            return
        }

        // 권한 재확인
        if (!hasRequiredPermissions()) {
            _connectionState.update { BleConnectionState.Error("블루투스 또는 위치 권한이 없습니다.") }
            return
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionState.update { BleConnectionState.Error("블루투스가 꺼져있습니다.") }
            return
        }

        _isSending.update { true }
        _connectionState.update { BleConnectionState.Scanning }
        startScan()
    }

    // --- 스캔 로직 ---
    @SuppressLint("MissingPermission")
    private fun startScan() {
        if (isScanning) {
            Log.w(TAG, "이미 스캔 중입니다.")
            return
        }

        // --- 수정: 스캔 필터 제거 ---
        // val scanFilter = ScanFilter.Builder()
        //     .setServiceUuid(ParcelUuid(serviceUUID))
        //     .build()
        // val filters = listOf(scanFilter)
        val filters: List<ScanFilter>? = null // 필터 없이 모든 기기 스캔

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        targetDevice = null // 이전 타겟 초기화
        isScanning = true
        Log.i(TAG, "BLE 스캔 시작 (필터 없음)") // 로그 메시지 수정

        // 스캔 시작 (filters 인자에 null 전달)
        bluetoothAdapter?.bluetoothLeScanner?.startScan(filters, settings, leScanCallback)

        // 스캔 타임아웃 설정
        handler.removeCallbacksAndMessages(null) // 이전 타임아웃 제거
        handler.postDelayed({
            if (isScanning) {
                stopScan()
                // --- 수정: 필터 없으므로 타임아웃 시 '찾지 못함' 대신 스캔 종료만 로깅 ---
                Log.i(TAG, "스캔 타임아웃 (10초)")
                // 필터가 없으면 특정 기기를 찾았는지 여부를 판단하기 어려우므로,
                // 타임아웃 시 에러 상태 대신 Idle 상태로 돌리거나,
                // 혹은 UI에서 "스캔 완료, 기기를 찾을 수 없습니다" 등으로 표시하도록 유도할 수 있습니다.
                // 여기서는 일단 Idle 상태로 돌립니다.
                if (targetDevice == null) { // 여전히 연결할 기기를 못 찾았다면
                    _connectionState.update { BleConnectionState.Idle } // Idle로 변경 (Error 대신)
                    _isSending.update { false }
                }
            }
        }, SCAN_PERIOD)
    }

    @SuppressLint("MissingPermission")
    private fun stopScan() {
        if (!isScanning) return
        isScanning = false
        // 스캔 중지 전에 핸들러 콜백 제거
        handler.removeCallbacksAndMessages(null)
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(leScanCallback)
        Log.i(TAG, "BLE 스캔 중지")
    }

    // --- 스캔 콜백 ---
    private val leScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission") // 권한 체크는 이미 수행됨
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val deviceName = result.device.name ?: "N/A"
            Log.d(TAG, "발견된 기기 이름: $deviceName, 주소: ${result.device.address}, RSSI: ${result.rssi}")

            // --- 수정: 서비스 UUID를 가지고 있는지 확인하는 로직 추가 ---
            // result.scanRecord?.serviceUuids가 null이 아니고, 우리가 찾는 serviceUUID를 포함하는 경우에만 연결 시도
            val serviceUuids = result.scanRecord?.serviceUuids
            val hasOurService = serviceUuids?.any { it.uuid == serviceUUID } == true

            if (hasOurService && targetDevice == null) {
                targetDevice = result.device
                stopScan() // 기기 찾으면 스캔 중지
                Log.i(TAG, "서비스 UUID 일치 기기(${deviceName}) 발견, 연결 시도...")
                _connectionState.update { BleConnectionState.Connecting }
                connectToDevice(targetDevice!!) // Null 아님 보장
            } else if (!hasOurService) {
                // Log.v(TAG, "기기($deviceName)는 우리의 서비스 UUID를 포함하지 않음") // 너무 많은 로그 방지
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e(TAG, "BLE 스캔 실패, 에러 코드: $errorCode")
            _connectionState.update { BleConnectionState.Error("BLE 스캔 시작 실패 (코드: $errorCode)") }
            _isSending.update { false }
            isScanning = false
            handler.removeCallbacksAndMessages(null) // 실패 시 핸들러 콜백 제거
        }
    }

    // --- 연결 로직 ---
    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        // 권한 재확인
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            _connectionState.update { BleConnectionState.Error("블루투스 연결 권한이 없습니다.") }
            _isSending.update { false }
            return
        }
        _connectionState.update { BleConnectionState.Connecting }
        Log.i(TAG, "GATT 서버 연결 시도: ${device.address}")
        // autoConnect는 false로 설정하여 빠른 연결 시도
        // TRANSPORT_LE 명시
        bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    @SuppressLint("MissingPermission")
    private fun disconnectGatt() {
        bluetoothGatt?.let { gatt ->
            Log.i(TAG, "GATT 연결 해제 및 리소스 정리 시작")
            // 권한 확인 후 disconnect/close 호출
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                gatt.disconnect()
                gatt.close() // 리소스 해제
                Log.i(TAG, "GATT 연결 해제 및 리소스 정리 완료")
            } else {
                Log.w(TAG, "GATT 연결 해제 위한 BLUETOOTH_CONNECT 권한 없음")
            }
        }
        bluetoothGatt = null
        targetDevice = null // 다음 스캔을 위해 타겟 초기화
        _isSending.update { false } // 전송 상태 초기화
        // resetState()는 성공/실패 시 외부에서 호출되도록 유도
    }


    // --- GATT 콜백 ---
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission") // 권한 체크는 이미 수행됨
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "GATT 서버 연결 성공: $deviceAddress")
                    _connectionState.update { BleConnectionState.Connected }
                    // 서비스 탐색 시작 (메인 스레드에서 시작 권장, 핸들러 사용)
                    handler.post {
                        Log.d(TAG, "서비스 탐색 시작")
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED){
                            gatt.discoverServices()
                        } else {
                            Log.w(TAG, "서비스 탐색 위한 BLUETOOTH_CONNECT 권한 없음")
                            _connectionState.update { BleConnectionState.Error("블루투스 연결 권한 부족") }
                            disconnectGatt()
                        }
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "GATT 서버 연결 해제됨: $deviceAddress")
                    // 에러 상태나 성공 상태가 아니라면 Idle로 돌림 (예: 단순 연결 해제)
                    if (_connectionState.value !is BleConnectionState.Error && _connectionState.value !is BleConnectionState.Success) {
                        _connectionState.update { BleConnectionState.Idle }
                    }
                    // disconnectGatt()은 여기서 호출하지 않음 (disconnect 요청 시 또는 에러/성공 시 호출)
                    // GATT 객체 정리는 disconnectGatt() 내에서 수행됨
                    bluetoothGatt?.close() // disconnect 후 close 호출 보장
                    bluetoothGatt = null
                    targetDevice = null
                    _isSending.update{ false }
                }
            } else {
                // GATT_FAILURE 또는 다른 에러 코드 (e.g., 133)
                Log.w(TAG, "GATT 연결 상태 변경 오류: $deviceAddress, 상태 코드: $status, 새로운 상태: $newState")
                val errorMsg = when(status) {
                    133 -> "GATT 오류 133 (기기 연결 해제됨)"
                    8 -> "GATT 연결 시간 초과"
                    else -> "GATT 연결 실패 (코드: $status)"
                }
                _connectionState.update { BleConnectionState.Error(errorMsg) }
                disconnectGatt() // 에러 발생 시 정리
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "서비스 발견 성공")
                val service = gatt.getService(serviceUUID)
                if (service == null) {
                    Log.w(TAG, "요청한 서비스 UUID($serviceUUID)를 찾을 수 없음")
                    _connectionState.update { BleConnectionState.Error("필수 서비스를 찾을 수 없습니다.") }
                    disconnectGatt()
                    return
                }
                Log.d(TAG,"서비스 찾음: ${service.uuid}")

                val characteristic = service.getCharacteristic(rxCharacteristicUUID)
                if (characteristic == null) {
                    Log.w(TAG, "요청한 RX Characteristic UUID($rxCharacteristicUUID)를 찾을 수 없음")
                    _connectionState.update { BleConnectionState.Error("데이터 전송 채널을 찾을 수 없습니다.") }
                    disconnectGatt()
                    return
                }
                Log.d(TAG,"RX Characteristic 찾음: ${characteristic.uuid}")

                // Characteristic 찾음, 데이터 전송 시도
                sendData(gatt, characteristic, "COURTESY_SEAT")

            } else {
                Log.w(TAG, "서비스 발견 실패, 상태 코드: $status")
                _connectionState.update { BleConnectionState.Error("서비스 탐색 실패 (코드: $status)") }
                disconnectGatt()
            }
        }

        @SuppressLint("MissingPermission") // 권한 체크는 이미 수행됨
        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (characteristic.uuid == rxCharacteristicUUID) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "데이터 전송 성공!")
                    _connectionState.update { BleConnectionState.Success }
                } else {
                    Log.w(TAG, "데이터 전송 실패, 상태 코드: $status")
                    _connectionState.update { BleConnectionState.Error("데이터 전송 실패 (코드: $status)") }
                }
                // 성공/실패 여부와 관계없이 연결 해제
                disconnectGatt()
            }
        }
        // onCharacteristicRead, onCharacteristicChanged 등 필요한 콜백 추가 가능
    }

    // --- 데이터 전송 함수 ---
    @SuppressLint("MissingPermission") // 권한 체크는 이미 수행됨
    private fun sendData(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, message: String) {
        val data = message.toByteArray(Charsets.UTF_8)
        Log.i(TAG, "데이터 전송 시도: $message to ${characteristic.uuid}")

        // 권한 확인
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "데이터 전송 위한 BLUETOOTH_CONNECT 권한 없음")
            _connectionState.update { BleConnectionState.Error("블루투스 연결 권한 부족") }
            disconnectGatt()
            return
        }

        // Android 13 (API 33) 이상에서는 writeCharacteristic API 사용법 변경
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val result = gatt.writeCharacteristic(characteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            Log.d(TAG, "writeCharacteristic 결과 (API 33+): $result")
            // 결과 코드가 SUCCESS가 아니면 즉시 실패 처리
            if (result != BluetoothStatusCodes.SUCCESS){
                Log.e(TAG, "writeCharacteristic 즉시 실패 (API 33+), 코드: $result")
                _connectionState.update { BleConnectionState.Error("데이터 쓰기 요청 실패 (코드: $result)") }
                disconnectGatt()
            }
            // 성공 시에는 onCharacteristicWrite 콜백 기다림
        } else {
            // 이전 버전 처리 (Deprecated 방식)
            characteristic.value = data // deprecated in API 33
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            val success = gatt.writeCharacteristic(characteristic) // deprecated in API 33
            Log.d(TAG, "writeCharacteristic 결과 (API 32-): $success")
            if (!success) {
                Log.e(TAG, "writeCharacteristic 즉시 실패 (API 32-)")
                _connectionState.update { BleConnectionState.Error("데이터 쓰기 요청 실패") }
                disconnectGatt()
            }
            // 성공 시에는 onCharacteristicWrite 콜백 기다림
        }
    }


    // --- 권한 확인 함수 ---
    private fun hasRequiredPermissions(): Boolean {
        val hasFineLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        // Android 12 (API 31) 이상 권한
        val hasScanPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            true // 이전 버전은 BLUETOOTH_ADMIN과 위치 권한으로 처리됨
        }
        val hasConnectPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true // 이전 버전은 BLUETOOTH 권한으로 처리됨
        }

        // 안드로이드 10 (API 29) 이상에서는 Fine Location 또는 Coarse Location 중 하나만 있어도 스캔 가능
        // 안드로이드 9 (API 28) 이하에서는 Coarse Location만 있어도 스캔 가능
        // 하지만 정확한 스캔을 위해 Fine Location을 권장
        val locationGranted = hasFineLocation || hasCoarseLocation

        Log.d(TAG, "권한 확인: 위치=$locationGranted, 스캔=$hasScanPermission, 연결=$hasConnectPermission")

        // 위치 권한은 항상 필요 (BLE 스캔 때문)
        // 안드로이드 12 이상이면 SCAN, CONNECT 권한 추가 확인
        return locationGranted && hasScanPermission && hasConnectPermission
    }

    // ViewModel 소멸 시 리소스 정리
    @SuppressLint("MissingPermission")
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel 소멸, 리소스 정리")
        stopScan() // 진행 중인 스캔 중지
        disconnectGatt() // 연결된 GATT 해제
        handler.removeCallbacksAndMessages(null) // 핸들러 메시지 제거
    }
}

// ViewModel Factory (Context 주입 위해 필요)
class BTViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BTViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BTViewModel(context.applicationContext) as T // ApplicationContext 사용 권장
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
