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
import com.example.bfdream_android.network.BusApiService
// [수정] CoordinateConverter import 제거
// import com.example.bfdream_android.utils.CoordinateConverter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
    data class Success(val busStops: List<BusStop>) : BusApiState() // [수정] BusStop 리스트
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
        // ViewModel 생성 시 CSV 로드 및 버스 정보 로드 시작
        viewModelScope.launch {
            busRepo.loadCsvData() // CSV 데이터 먼저 로드
            loadBusDataFromCurrentLocation()
        }
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    /**
     * 공개 함수: 새로고침 또는 초기 로드
     */
    fun loadBusDataFromCurrentLocation() {
        Log.d(TAG, "loadBusDataFromCurrentLocation: 시작")
        // [삭제] _busApiState.value = BusApiState.Loading (이것이 깜빡임의 주 원인)

        viewModelScope.launch {
            // [수정] 1. '새로고침'인지 '초기 로딩'인지 먼저 판별합니다.
            // (MainScreen의 '주변' 관련 에러 UI도 Success로 바뀌므로, isRefreshing이 true가 됩니다.)
            val isRefresh = _busApiState.value is BusApiState.Success

            if (isRefresh) {
                _isRefreshing.value = true // 새로고침 아이콘만 돌립니다.
            } else {
                _busApiState.value = BusApiState.Loading // 전체 로딩 스피너를 표시합니다.
            }

            // [수정] 2. 권한/위치 확인은 그 다음에 수행합니다.
            if (!hasLocationPermission()) {
                Log.w(TAG, "위치 권한 없음")
                _busApiState.value = BusApiState.Error("위치 권한이 없습니다. 앱 설정에서 권한을 허용해주세요.")
                if (isRefresh) _isRefreshing.value = false // [추가] 새로고침 중단
                return@launch
            }

            // 2. 현재 위치 가져오기 (WGS84)
            val location = getCurrentLocation()
            if (location == null) {
                Log.w(TAG, "위치 정보 가져오기 실패 (null)")
                _busApiState.value = BusApiState.Error("현재 위치를 가져오는데 실패했습니다.")
                if (isRefresh) _isRefreshing.value = false // [추가] 새로고침 중단
                return@launch
            }
            Log.d(TAG, "현재 위치(WGS84): lat=${location.latitude}, lon=${location.longitude}")

            // [삭제] isRefresh 관련 중복 로직 삭제
            // val isRefresh = ...
            // if (isRefresh) ...

            // 3. [수정] WGS84 -> TM 좌표 변환 로직 *제거*
            try {
                // 4. [수정] API 1 호출: tmX에 경도(longitude), tmY에 위도(latitude) 전달
                Log.d(TAG, "API 호출 (WGS84): tmX(lon)=${location.longitude}, tmY(lat)=${location.latitude}")
                val stationsResponse = busApi.getStationsByPosition(
                    tmX = location.longitude, // tmX = 경도
                    tmY = location.latitude   // tmY = 위도
                )

                // [수정] API 응답 헤더부터 확인
                if (stationsResponse.msgHeader?.headerCd != "0") {
                    val errorMsg = stationsResponse.msgHeader?.headerMsg ?: "알 수 없는 에러"
                    Log.w(TAG, "API 1 (getStationByPos) 에러: $errorMsg")
                    _busApiState.value = BusApiState.Error(errorMsg)
                    return@launch // finally가 실행됩니다.
                }

                val nearbyStations = stationsResponse.msgBody?.itemList ?: emptyList()
                if (nearbyStations.isEmpty()) {
                    Log.w(TAG, "주변 정류장 없음")
                    // [수정] '에러'가 아닌 '성공(빈 리스트)'으로 처리합니다.
                    _busApiState.value = BusApiState.Success(emptyList())
                    return@launch // finally가 실행됩니다.
                }
                Log.d(TAG, "주변 정류장 ${nearbyStations.size}개 발견")
                Log.d(TAG, nearbyStations.toString()) // [디버깅]

                // [수정] 1. API가 반환한 정류장 목록을 거리(dist) 기준으로 오름차순 정렬
                val sortedStations = nearbyStations.sortedBy { it.dist?.toIntOrNull() ?: Int.MAX_VALUE }

                // 5. [수정] 2. 정렬된 목록(sortedStations)을 사용해 병렬 조회
                val busStops = sortedStations.map { station ->
                    async { // 병렬 실행
                        Log.d(TAG, "정류장[${station.arsId}] 도착 정보 조회 시작")
                        try {
                            val arsId = station.arsId ?: return@async null
                            val arrivalResponse = busApi.getArrivalInfoByStation(arsId = arsId)

                            if (arrivalResponse.msgHeader?.headerCd != "0") {
                                Log.w(TAG, "API 2 (getStationByUid) 에러: ${arrivalResponse.msgHeader?.headerMsg}")
                                return@async null
                            }

                            val allBuses = arrivalResponse.msgBody?.itemList ?: emptyList()
                            Log.d(TAG, "정류장 arsId: $arsId")
                            Log.d(TAG, "allBuses: $allBuses")

                            // 7. [필터링]
                            val filteredBuses = allBuses
                                .filter { bus ->
                                    val busNumber = bus.busNumber
                                    val busRouteId = bus.busRouteId
                                    busNumber != null && busRouteId != null && busRepo.isBusAllowed(arsId, busNumber)
                                }
                                .map { bus ->
                                    val busNumber = bus.busNumber!!
                                    // 8. [데이터 가공]
                                    BusInfo(
                                        id = bus.busRouteId!!,
                                        number = busNumber,
                                        // [수정] iOS 로직과 동일하게 버스 번호로 색상 판별
                                        color = getBusColorByNumber(busNumber),
                                        arrivalTime = bus.arrivalMsg1 ?: "정보 없음",
                                        adirection = bus.adirection ?: "",
                                    )
                                }
                                .sortedBy { it.arrivalTime }

                            // 9. 관심 버스가 1대 이상 있는 정류장만
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
                            null // [수정] 크래시 대신 null을 반환하여 다음 정류장 처리 계속
                        }
                    }
                }.awaitAll().filterNotNull() // 모든 병렬 작업이 끝날 때까지 기다리고, null이 아닌 것만 필터링

                if (busStops.isEmpty()) {
                    Log.w(TAG, "주변에 관심 버스가 있는 정류장이 없음")
                    // [수정] '에러'가 아닌 '성공(빈 리스트)'으로 처리합니다.
                    _busApiState.value = BusApiState.Success(emptyList())
                } else {
                    // [수정] 3. 필터링된 정류장 목록(busStops)은 이미 거리순으로 정렬되어 있음
                    // 이 중에서 가장 첫 번째(가장 가까운) 정류장만 선택
                    val closestValidStop = busStops.first()
                    Log.d(TAG, "최종 정류장 1개(가장 가까운) 생성 완료: ${closestValidStop.name}")

                    // MainScreen의 LazyColumn은 리스트를 받으므로, 아이템이 1개인 리스트를 생성
                    _busApiState.value = BusApiState.Success(listOf(closestValidStop))
                }

            } catch (e: Exception) {
                Log.e(TAG, "API 처리 중 예외 발생", e)
                _busApiState.value = BusApiState.Error("버스 정보를 가져오는 중 오류가 발생했습니다: ${e.message}")
            } finally {
                // [수정] isRefresh 여부와 관계없이, 로직이 끝나면 항상 '새로고침' 상태를 끕니다.
                _isRefreshing.value = false
            }
        }
    }

    /**
     * FusedLocationProviderClient를 사용해 현재 위치를 가져옵니다.
     * (캐시된 위치가 1분 이내면 사용, 아니면 새로 요청)
     */
    @SuppressLint("MissingPermission") // 권한 체크는 상위에서 수행
    private suspend fun getCurrentLocation(): android.location.Location? {
        val cancellationTokenSource = CancellationTokenSource()

        try {
            // 1. 캐시된 마지막 위치 확인
            val lastLocation = fusedLocationClient.lastLocation.await()
            if (lastLocation != null) {
                val timeSinceLast = (System.currentTimeMillis() - lastLocation.time) / 1000
                if (timeSinceLast < 60) { // 1분(60초) 이내의 캐시된 위치가 있으면 사용
                    Log.d(TAG, "캐시된 위치 사용 (경과: ${timeSinceLast}초)")
                    return lastLocation
                }
            }

            // 2. 캐시가 없거나 오래됐으면 새로 요청 (High Accuracy, 타임아웃 10초)
            Log.d(TAG, "새로운 위치 정보 요청...")

            return withTimeoutOrNull(10_000) { // 10초
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).await()
            }
            // 10초가 지나면 withTimeoutOrNull이 null을 반환합니다.

        } catch (e: Exception) {
            Log.e(TAG, "위치 정보 요청 실패", e)
            return null
        }
    }

    /**
     * 위치 권한(ACCESS_COARSE_LOCATION 또는 ACCESS_FINE_LOCATION)이 있는지 확인합니다.
     */
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

// --- ViewModel Factory ---
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