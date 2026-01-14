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
import androidx.lifecycle.viewModelScope
import com.example.bfdream_android.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

// 상수 정의
private const val SERVICE_UUID_STRING = "12345678-1234-1234-1234-123456789ABC"
private const val RX_CHARACTERISTIC_UUID_STRING = "12345678-1234-1234-1234-123456789ABD"
private const val SCAN_PERIOD: Long = 5000 // 5초 스캔

class BTViewModel(private val context: Context) : ViewModel() {

    private val settingsRepository = SettingsRepository(context)

    // 항상 최신 설정값 유지 (Eagerly)
    val isSoundOn: StateFlow<Boolean> = settingsRepository.isSoundOnFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    // --- 상태 정의 ---
    sealed class BleConnectionState {
        data object Idle : BleConnectionState()
        data object Scanning : BleConnectionState()
        data object Connecting : BleConnectionState()
        data object Connected : BleConnectionState()
        data object Success : BleConnectionState()
        data class Error(val message: String) : BleConnectionState()
    }

    // --- StateFlow ---
    private val _connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Idle)
    val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _selectedBusId = MutableStateFlow<String?>(null)
    val selectedBusId: StateFlow<String?> = _selectedBusId.asStateFlow()

    // --- Bluetooth 관련 변수 ---
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var bluetoothGatt: BluetoothGatt? = null
    private var targetDevice: BluetoothDevice? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isScanning = false

    // [추가] 타겟 디바이스 이름 (예: BF_DREAM_2221)
    private var targetDeviceName: String? = null

    private val serviceUUID: UUID = UUID.fromString(SERVICE_UUID_STRING)
    private val rxCharacteristicUUID: UUID = UUID.fromString(RX_CHARACTERISTIC_UUID_STRING)

    private val TAG = "BTViewModel"

    // --- 공개 함수 ---

    fun toggleSound(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSoundOn(enable)
        }
    }

    fun selectBus(busId: String?) {
        if (_isSending.value || _connectionState.value is BleConnectionState.Connecting || _connectionState.value is BleConnectionState.Connected) {
            Log.w(TAG, "통신 중에는 버스를 변경할 수 없습니다.")
            return
        }
        _selectedBusId.value = busId
    }

    fun resetState() {
        Log.d(TAG, "상태 초기화")
        _connectionState.update { BleConnectionState.Idle }
        _isSending.update { false }
        _selectedBusId.value = null
        targetDeviceName = null // 초기화 시 타겟 이름도 초기화
    }

    // [수정] 버스 번호를 인자로 받아서 타겟 이름을 설정함
    @SuppressLint("MissingPermission")
    fun sendCourtesySeatNotification(busNumber: String) {
        val busId = _selectedBusId.value
        if (busId == null) {
            _connectionState.update { BleConnectionState.Error("버스를 먼저 선택해주세요.") }
            return
        }
        if (_isSending.value) { return }

        if (!hasRequiredPermissions()) {
            _connectionState.update { BleConnectionState.Error("블루투스 또는 위치 권한이 없습니다.") }
            return
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionState.update { BleConnectionState.Error("블루투스가 꺼져있습니다.") }
            return
        }

        // [중요] 타겟 이름 설정 (BF_DREAM_ + 버스번호)
        targetDeviceName = "BF_DREAM_$busNumber"
        Log.d(TAG, "타겟 디바이스 설정됨: $targetDeviceName")

        _isSending.update { true }
        _connectionState.update { BleConnectionState.Scanning }
        startScan()
    }

    // --- 스캔 로직 ---
    @SuppressLint("MissingPermission")
    private fun startScan() {
        if (isScanning) return

        val filters: List<ScanFilter>? = null
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        targetDevice = null
        isScanning = true
        Log.i(TAG, "BLE 스캔 시작 (타겟: $targetDeviceName)")

        bluetoothAdapter?.bluetoothLeScanner?.startScan(filters, settings, leScanCallback)

        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            if (isScanning) {
                stopScan()
                if (targetDevice == null) {
                    _connectionState.update { BleConnectionState.Error("주변에서 버스($targetDeviceName)를 찾을 수 없습니다.") }
                    _isSending.update { false }
                }
            }
        }, SCAN_PERIOD)
    }

    @SuppressLint("MissingPermission")
    private fun stopScan() {
        if (!isScanning) return
        isScanning = false
        handler.removeCallbacksAndMessages(null)
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(leScanCallback)
    }

    // --- 스캔 콜백 ---
    private val leScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            // 1. 서비스 UUID 확인
            val serviceUuids = result.scanRecord?.serviceUuids
            val hasOurService = serviceUuids?.any { it.uuid == serviceUUID } == true

            // 2. [추가] 디바이스 이름 확인 (result.scanRecord.deviceName이 더 정확할 수 있음)
            val scannedName = result.scanRecord?.deviceName ?: result.device.name

            // 3. 로그 (디버깅용)
            // Log.v(TAG, "기기 발견: $scannedName (UUID 일치: $hasOurService)")

            // 4. [수정] UUID와 이름이 모두 일치하는지 확인
            if (hasOurService && targetDevice == null) {
                if (scannedName == targetDeviceName) {
                    targetDevice = result.device
                    stopScan()
                    Log.i(TAG, "타겟 기기 발견! ($scannedName) 연결 시도...")
                    _connectionState.update { BleConnectionState.Connecting }
                    connectToDevice(targetDevice!!)
                } else {
                    Log.d(TAG, "UUID는 일치하지만 타겟 버스가 아님. 발견됨: $scannedName, 찾는것: $targetDeviceName")
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            _connectionState.update { BleConnectionState.Error("BLE 스캔 시작 실패 (코드: $errorCode)") }
            _isSending.update { false }
            isScanning = false
        }
    }

    // --- 연결 로직 (기존과 동일) ---
    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            _connectionState.update { BleConnectionState.Error("블루투스 연결 권한이 없습니다.") }
            _isSending.update { false }
            return
        }
        bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    @SuppressLint("MissingPermission")
    private fun disconnectGatt() {
        bluetoothGatt?.let { gatt ->
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                gatt.disconnect()
                gatt.close()
            }
        }
        bluetoothGatt = null
        targetDevice = null
        _isSending.update { false }
    }

    // --- GATT 콜백 (기존과 동일) ---
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    _connectionState.update { BleConnectionState.Connected }
                    handler.post {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED){
                            gatt.discoverServices()
                        }
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    val currentState = _connectionState.value
                    if (currentState !is BleConnectionState.Error && currentState !is BleConnectionState.Success) {
                        _connectionState.update { BleConnectionState.Idle }
                    }
                    bluetoothGatt?.close()
                    bluetoothGatt = null
                    _isSending.update{ false }
                }
            } else {
                _connectionState.update { BleConnectionState.Error("연결 실패 (코드: $status)") }
                disconnectGatt()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(serviceUUID)
                val characteristic = service?.getCharacteristic(rxCharacteristicUUID)

                if (service != null && characteristic != null) {
                    // 명령어 결정 (DEFAULT / SILENT)
                    val command = if (isSoundOn.value) "DEFAULT" else "SILENT"
                    Log.d(TAG, "전송할 명령어: $command (SoundOn: ${isSoundOn.value})")

                    sendData(gatt, characteristic, command)
                } else {
                    _connectionState.update { BleConnectionState.Error("서비스 또는 특성을 찾을 수 없습니다.") }
                    disconnectGatt()
                }
            } else {
                _connectionState.update { BleConnectionState.Error("서비스 탐색 실패") }
                disconnectGatt()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (characteristic.uuid == rxCharacteristicUUID) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    _connectionState.update { BleConnectionState.Success }
                } else {
                    _connectionState.update { BleConnectionState.Error("전송 실패 (코드: $status)") }
                }
                disconnectGatt()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendData(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, message: String) {
        val data = message.toByteArray(Charsets.UTF_8)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            _connectionState.update { BleConnectionState.Error("권한 부족") }
            disconnectGatt()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val result = gatt.writeCharacteristic(characteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            if (result != BluetoothStatusCodes.SUCCESS){
                _connectionState.update { BleConnectionState.Error("쓰기 실패 (코드: $result)") }
                disconnectGatt()
            }
        } else {
            characteristic.value = data
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            val success = gatt.writeCharacteristic(characteristic)
            if (!success) {
                _connectionState.update { BleConnectionState.Error("쓰기 실패") }
                disconnectGatt()
            }
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        val hasFineLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        val hasScanPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else true

        val hasConnectPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else true

        return (hasFineLocation || hasCoarseLocation) && hasScanPermission && hasConnectPermission
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
        disconnectGatt()
        handler.removeCallbacksAndMessages(null)
    }
}

class BTViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BTViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BTViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}