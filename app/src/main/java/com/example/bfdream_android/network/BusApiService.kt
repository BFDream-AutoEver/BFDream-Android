package com.example.bfdream_android.network

import com.example.bfdream_android.BuildConfig
import com.example.bfdream_android.data.ArrivalInfoResponse
import com.example.bfdream_android.data.StationByPosResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface BusApiService {

    // (필수) 서울시 공공데이터 포털에서 발급받은 개인 인증키를 넣어야 합니다.
    // (보안을 위해 build.gradle의 BuidConfig나 local.properties에 숨기는 것을 권장합니다)
    companion object {
        private const val BASE_URL = "http://ws.bus.go.kr/api/rest/"

        // TODO: 여기에 실제 API 키를 입력하세요
        private const val API_KEY = BuildConfig.API_KEY

        private val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        private val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // API 요청/응답 로그 확인
            .build()

        val instance: BusApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(SimpleXmlConverterFactory.create()) // XML 변환기
                .build()
                .create(BusApiService::class.java)
        }
    }

    /**
     * 1. 좌표 기반 주변 정류소 검색
     * @param tmX TM(Katec) X좌표
     * @param tmY TM(Katec) Y좌표
     * @param radius 검색 반경 (미터)
     */
    @GET("stationinfo/getStationByPos")
    suspend fun getStationsByPosition(
        @Query("serviceKey") serviceKey: String = API_KEY,
        @Query("tmX") tmX: Double,
        @Query("tmY") tmY: Double,
        @Query("radius") radius: Int = 500 // 500m 반경
    ): StationByPosResponse

    /**
     * 2. 특정 정류소의 버스 도착 정보
     * @param arsId 정류소 ARS ID (e.g., "05189")
     */
    @GET("stationinfo/getStationByUid")
    suspend fun getArrivalInfoByStation(
        @Query("serviceKey") serviceKey: String = API_KEY,
        @Query("arsId") arsId: String
    ): ArrivalInfoResponse
}
