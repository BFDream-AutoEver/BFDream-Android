package com.example.bfdream_android.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * assets/bus_route_stops.csv 파일을 읽고,
 * 우리가 관심있는 버스 목록을 메모리에 저장하는 클래스
 */
class BusDataRepository(private val context: Context) {

    private val TAG = "BusDataRepository"

    // Key: 정류소 ARS ID (e.g., "05189")
    // Value: 해당 정류소에서 우리가 관심있는 버스 노선명 목록 (e.g., ["2415", "2221", "광진05"])
    private var allowedBusMap: Map<String, Set<String>> = emptyMap()

    /**
     * 앱 실행 시 한 번만 호출하여 CSV 데이터를 메모리로 로드
     */
    suspend fun loadCsvData() {
        if (allowedBusMap.isNotEmpty()) return // 이미 로드됨

        Log.d(TAG, "CSV 데이터 로드 시작...")
        val localRoutes = mutableListOf<LocalBusRoute>()
        withContext(Dispatchers.IO) {
            try {
                context.assets.open("bus_route_stops.csv").use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        // 첫 번째 줄(헤더)은 건너뜀
                        reader.readLine()

                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val tokens = line!!.split(",")
                            if (tokens.size >= 7) {
                                localRoutes.add(
                                    LocalBusRoute(
                                        routeId = tokens[0],
                                        routeName = tokens[1],
                                        nodeId = tokens[2],
                                        arsId = tokens[3],
                                        stationName = tokens[4]
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "CSV 파일 로드 실패", e)
            }
        }

        // 읽어온 데이터를 Map으로 변환하여 필터링에 용이하게 함
        allowedBusMap = localRoutes
            .groupBy { it.arsId } // 정류소 ARS ID로 그룹화
            .mapValues { entry ->
                entry.value.map { it.routeName }.toSet() // 각 정류소의 버스 번호 목록을 Set으로
            }

        Log.d(TAG, "CSV 데이터 로드 완료. ${allowedBusMap.size}개 정류소 정보 로드됨.")
    }

    /**
     * 이 정류소(arsId)에서 이 버스(busNumber)가 우리가 관심있는 버스인지 확인
     */
    fun isBusAllowed(arsId: String, busNumber: String): Boolean {
        return allowedBusMap[arsId]?.contains(busNumber) ?: false
    }
}
