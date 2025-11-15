package com.example.bfdream_android.data

import androidx.compose.ui.graphics.Color
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

// --- UI용 데이터 클래스 ---

/**
 * MainScreen의 BusStopCard에 표시될 최종 데이터 모델
 */
data class BusStop(
    val arsId: String,          // 정류소 고유 ARS ID (e.g., "05189")
    val name: String,           // 정류소 이름 (e.g., "한아름공원")
    val direction: String,      // 다음 정류소 (방향)
    val buses: List<BusInfo>    // 이 정류장에 도착할 *필터링된* 버스 목록
)

/**
 * MainScreen의 BusRow에 표시될 최종 데이터 모델
 */
data class BusInfo(
    val id: String,             // 버스 노선 고유 ID (e.g., "100100211")
    val number: String,         // 버스 번호 (e.g., "2415")
    val color: Color,           // 버스 유형에 따른 색상
    val arrivalTime: String,    // 도착 예정 시간 (e.g., "3분", "곧 도착")
    val adirection: String      // 방면
)

/**
 * assets/bus_route_stops.csv 파일에서 읽어올 데이터 모델
 */
data class LocalBusRoute(
    val routeId: String,
    val routeName: String, // 노선명 (e.g., "2415")
    val nodeId: String,
    val arsId: String,     // 정류소 ARS ID (e.g., "05189")
    val stationName: String
)

/**
 * API 응답 공통 헤더 (에러 코드 및 메시지 확인용)
 */
@Root(name = "msgHeader", strict = false)
data class MsgHeader(
    @field:Element(name = "headerCd", required = false)
    var headerCd: String? = null, // 0: 정상, 1~: 에러
    @field:Element(name = "headerMsg", required = false)
    var headerMsg: String? = null // 에러 메시지
)

// --- API 응답(XML) 파싱을 위한 데이터 클래스 ---

// 1. getStationByPos (주변 정류소) 응답
@Root(name = "ServiceResult", strict = false)
data class StationByPosResponse(
    @field:Element(name = "msgHeader", required = false) // [추가]
    var msgHeader: MsgHeader? = null,
    @field:Element(name = "msgBody", required = false)
    var msgBody: StationByPosMsgBody? = null
)

@Root(name = "msgBody", strict = false)
data class StationByPosMsgBody(
    @field:ElementList(name = "itemList", inline = true, required = false)
    var itemList: List<StationByPosItem>? = null
)

@Root(name = "itemList", strict = false)
data class StationByPosItem(
    @field:Element(name = "arsId", required = false) // 정류소 ARS ID
    var arsId: String? = null,
    @field:Element(name = "stationNm", required = false) // 정류소 이름
    var stationName: String? = null,
    @field:Element(name = "nextStn", required = false) // 다음 정류소 (방향)
    var nextStation: String? = null,
    @field:Element(name = "dist", required = false) // 거리 (미터)
var dist: String? = null
)

// 2. getStationByUid (특정 정류소 도착 정보) 응답
@Root(name = "ServiceResult", strict = false)
data class ArrivalInfoResponse(
    @field:Element(name = "msgHeader", required = false) // [추가]
    var msgHeader: MsgHeader? = null,
    @field:Element(name = "msgBody", required = false)
    var msgBody: ArrivalInfoMsgBody? = null
)

@Root(name = "msgBody", strict = false)
data class ArrivalInfoMsgBody(
    @field:ElementList(name = "itemList", inline = true, required = false)
    var itemList: List<ArrivalInfoItem>? = null
)

@Root(name = "itemList", strict = false)
data class ArrivalInfoItem(
    @field:Element(name = "busRouteId", required = false) // 버스 노선 ID
    var busRouteId: String? = null,
    @field:Element(name = "busRouteAbrv", required = false) // 버스 번호
    var busNumber: String? = null,
    @field:Element(name = "arrmsg1", required = false) // 첫 번째 도착 메시지
    var arrivalMsg1: String? = null,
    @field:Element(name = "adirection", required = false) // 방면
    var adirection: String? = null
)

// --- 유틸리티 ---

/**
 * 서울시 버스 API의 routeType을 기반으로 iOS의 에셋 색상과 동일하게 매핑
 */
fun getBusColorByRouteType(routeType: String?): Color {
    return when (routeType) {
        "1", // 공항
        "2", // 마을
        "5", // 지선
        "6" -> BusGreen // FeederBus_TownBus (Green)
        "3" -> BusBlue  // 간선, N(심야) Bus (Blue)
        "4" -> BusRed  // 광역
        "7" -> BusYellow // 순환 (Yellow)
        else -> Color.Gray
    }
}

/**
 * [신규] iOS 로직과 동일하게 버스 번호(rtNm)를 분석하여 색상 반환
 * (iOS: BusRouteType.from(busNumber:))
 */
fun getBusColorByNumber(busNumber: String?): Color {
    if (busNumber.isNullOrBlank()) return Color.Gray
    val trimmed = busNumber.trim()

    // 심야버스 (N으로 시작)
    if (trimmed.startsWith("N", ignoreCase = true)) {
        return BusBlue // TrunkBus_NBus (iOS: .simya)
    }

    // 숫자만 추출
    val digits = trimmed.filter { it.isDigit() }
    val length = digits.length

    if (length == 0) return Color.Gray

    // 광역버스 (9로 시작하는 4자리)
    if (length == 4 && digits.startsWith('9')) {
        return BusRed // WideAreaBus (iOS: .gwangyeok)
    }

    // 간선버스 (3자리)
    if (length == 3) {
        return BusBlue // TrunkBus_NBus (iOS: .gangseon)
    }

    // 지선버스 (4자리, 9로 시작하지 않음)
    if (length == 4) {
        return BusGreen // FeederBus_TownBus (iOS: .jiseon)
    }

    // 순환버스 (2자리)
    if (length == 2) {
        return BusYellow // CircularBus (iOS: .sunhwan)
    }

    // 마을버스 (숫자 외 문자가 포함됨, e.g., "광진05")
    if (trimmed.any { !it.isDigit() && !it.equals('N', ignoreCase = true) }) {
        return BusGreen // FeederBus_TownBus (iOS: .maeul)
    }

    // 공항버스 (iOS 코드에서는 별도 판별 로직 없음)
    // 기타
    return Color.Gray // (iOS: .unknown)
}
