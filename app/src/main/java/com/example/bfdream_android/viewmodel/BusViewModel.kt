package com.example.bfdream_android.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bfdream_android.data.BusDataRepository
import com.example.bfdream_android.data.BusInfo
import com.example.bfdream_android.data.BusStop
// [수정] iOS 로직인 getBusColorByNumber를 import합니다.
import com.example.bfdream_android.data.getBusColorByNumber
import com.example.bfdream_android.data.getCongestionColor
import com.example.bfdream_android.data.getCongestionStatus
import com.example.bfdream_android.network.BusApiService
// [수정] CoordinateConverter import 제거
// import com.example.bfdream_android.utils.CoordinateConverter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

// --- UI 상태 ---
sealed class BusApiState {
    data object Idle : BusApiState()
    data object Loading : BusApiState()
    data class Success(val busStops: List<BusStop>) : BusApiState()
    data class Error(val message: String) : BusApiState()
}

class BusViewModel(
    private val context: Context,
    private val busApi: BusApiService,
    private val busRepo: BusDataRepository
) : ViewModel() {

    private val _busApiState = MutableStateFlow<BusApiState>(BusApiState.Idle)
    val busApiState = _busApiState.asStateFlow()

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val TAG = "BusViewModel"

    init {
        loadBusDataFromCurrentLocation()
        startAutoRefresh()
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun loadBusDataFromCurrentLocation() {
        Log.d(TAG, "loadBusDataFromCurrentLocation: 시작")

        viewModelScope.launch {
            val isRefresh = _busApiState.value is BusApiState.Success

            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _busApiState.value = BusApiState.Loading
            }

            if (!hasLocationPermission()) {
                Log.w(TAG, "위치 권한 없음")
                _busApiState.value = BusApiState.Error("위치 권한이 없습니다. 앱 설정에서 권한을 허용해주세요.")
                if (isRefresh) _isRefreshing.value = false
                return@launch
            }

            // 2. 현재 위치 가져오기
            val location = getCurrentLocation()
            if (location == null) {
                Log.w(TAG, "위치 정보 가져오기 실패 (null)")
                _busApiState.value = BusApiState.Error("현재 위치를 가져오는데 실패했습니다.")
                if (isRefresh) _isRefreshing.value = false
                return@launch
            }
            Log.d(TAG, "현재 위치(WGS84): lat=${location.latitude}, lon=${location.longitude}")

            try {
                // 3. API 호출: 주변 정류장 조회
                val stationsResponse = busApi.getStationsByPosition(
                    tmX = location.longitude,
                    tmY = location.latitude
                )

                if (stationsResponse.msgHeader?.headerCd != "0") {
                    val errorMsg = stationsResponse.msgHeader?.headerMsg ?: "알 수 없는 에러"
                    Log.w(TAG, "API 1 (getStationByPos) 에러: $errorMsg")
                    _busApiState.value = BusApiState.Error(errorMsg)
                    return@launch
                }

                val nearbyStations = stationsResponse.msgBody?.itemList ?: emptyList()
                if (nearbyStations.isEmpty()) {
                    Log.w(TAG, "주변 정류장 없음")
                    _busApiState.value = BusApiState.Success(emptyList())
                    return@launch
                }

                // 거리순 정렬
                val sortedStations = nearbyStations.sortedBy { it.dist?.toIntOrNull() ?: Int.MAX_VALUE }

                // 4. 각 정류장의 버스 도착 정보 조회 (병렬 처리)
                val busStops = sortedStations.map { station ->
                    async {
                        try {
                            val arsId = station.arsId ?: return@async null
                            val arrivalResponse = busApi.getArrivalInfoByStation(arsId = arsId)
                            Log.d("qwer", "$arrivalResponse")

                            if (arrivalResponse.msgHeader?.headerCd != "0") {
                                return@async null
                            }

                            val allBuses = arrivalResponse.msgBody?.itemList ?: emptyList()

                            // 7. [필터링 & 매핑]
                            val filteredBuses = allBuses
                                .filter { bus ->
                                    val busNumber = bus.busNumber
                                    val busRouteId = bus.busRouteId
                                    busNumber != null && busRouteId != null
                                }
                                .map { bus ->
                                    val busNumber = bus.busNumber!!

                                    // [수정] 3가지 태그 중 하나라도 값이 있으면 사용합니다.
                                    val congestionCode = bus.congestion
                                        ?: bus.congestion1
                                        ?: bus.rerideNum1

                                    // 8. [데이터 가공]
                                    BusInfo(
                                        id = bus.busRouteId!!,
                                        number = busNumber,
                                        color = getBusColorByNumber(busNumber),
                                        arrivalTime = bus.arrivalMsg1 ?: "정보 없음",
                                        adirection = bus.adirection ?: "",
                                        // [수정] 찾은 코드를 전달
                                        congestionStatus = getCongestionStatus(congestionCode),
                                        congestionColor = getCongestionColor(congestionCode)
                                    )
                                }
                                .sortedBy { it.arrivalTime }

                            // 버스가 하나라도 있으면 정류장 정보 생성
                            if (filteredBuses.isNotEmpty()) {
                                BusStop(
                                    arsId = arsId,
                                    name = station.stationName ?: "이름 없음",
                                    direction = station.nextStation ?: "방향 정보 없음",
                                    buses = filteredBuses
                                )
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "정류장[${station.arsId}] 처리 중 예외", e)
                            null
                        }
                    }
                }.awaitAll().filterNotNull()

                if (busStops.isEmpty()) {
                    // 주변에 정류장은 있지만 버스 정보가 없는 경우
                    _busApiState.value = BusApiState.Success(emptyList())
                } else {
                    // 가장 가까운 정류장 하나만 선택해서 보여줌
                    val closestValidStop = busStops.first()
                    _busApiState.value = BusApiState.Success(listOf(closestValidStop))
                }

            } catch (e: Exception) {
                Log.e(TAG, "API 처리 중 예외 발생", e)
                _busApiState.value = BusApiState.Error("버스 정보를 가져오는 중 오류가 발생했습니다: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            // viewModel이 살아있는 동안 무한 반복
            while (true) {
                delay(60000L) // 60,000ms = 1분 대기
                Log.d(TAG, "자동 갱신 실행 (1분 경과)")
                loadBusDataFromCurrentLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(): android.location.Location? {
        val cancellationTokenSource = CancellationTokenSource()

        try {
            val lastLocation = fusedLocationClient.lastLocation.await()
            if (lastLocation != null) {
                val timeSinceLast = (System.currentTimeMillis() - lastLocation.time) / 1000
                if (timeSinceLast < 60) {
                    return lastLocation
                }
            }

            return withTimeoutOrNull(10_000) {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).await()
            }

        } catch (e: Exception) {
            Log.e(TAG, "위치 정보 요청 실패", e)
            return null
        }
    }

    private fun hasLocationPermission(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }
}

class BusViewModelFactory(
    private val context: Context,
    private val busApi: BusApiService,
    private val busRepo: BusDataRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BusViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BusViewModel(context.applicationContext, busApi, busRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}