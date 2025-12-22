package com.example.bfdream_android.data

import androidx.compose.ui.graphics.Color
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

// --- UI용 데이터 클래스 ---
data class BusStop(
    val arsId: String,
    val name: String,
    val direction: String,
    val buses: List<BusInfo>
)

data class BusInfo(
    val id: String,
    val number: String,
    val color: Color,
    val arrivalTime: String,
    val adirection: String,
    val congestionStatus: String,
    val congestionColor: Color
)

// CSV 파일 읽기용 데이터 클래스
data class LocalBusRoute(
    val routeId: String,
    val routeName: String,
    val nodeId: String,
    val arsId: String,
    val stationName: String
)

// --- API 응답 모델 ---
@Root(name = "msgHeader", strict = false)
data class MsgHeader(
    @field:Element(name = "headerCd", required = false)
    var headerCd: String? = null,
    @field:Element(name = "headerMsg", required = false)
    var headerMsg: String? = null
)

@Root(name = "ServiceResult", strict = false)
data class StationByPosResponse(
    @field:Element(name = "msgHeader", required = false)
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
    @field:Element(name = "arsId", required = false)
    var arsId: String? = null,
    @field:Element(name = "stationNm", required = false)
    var stationName: String? = null,
    @field:Element(name = "nextStn", required = false)
    var nextStation: String? = null,
    @field:Element(name = "dist", required = false)
    var dist: String? = null
)

@Root(name = "ServiceResult", strict = false)
data class ArrivalInfoResponse(
    @field:Element(name = "msgHeader", required = false)
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
    @field:Element(name = "busRouteId", required = false)
    var busRouteId: String? = null,
    @field:Element(name = "busRouteAbrv", required = false)
    var busNumber: String? = null,
    @field:Element(name = "arrmsg1", required = false)
    var arrivalMsg1: String? = null,
    @field:Element(name = "adirection", required = false)
    var adirection: String? = null,

    // [수정] 혼잡도 관련 태그 3종 세트 (API 버전에 따라 다름)
    @field:Element(name = "congestion", required = false)
    var congestion: String? = null,
    @field:Element(name = "congestion1", required = false)
    var congestion1: String? = null,
    @field:Element(name = "reride_Num1", required = false) // XML에서 주로 사용되는 태그
    var rerideNum1: String? = null
)

// --- 유틸리티 및 상수 ---

val CongestionComfort = Color(0xFF00C853) // 여유 (Green)
val CongestionNormal = Color(0xFFFFB300)  // 보통 (Yellow)
val CongestionCrowded = Color(0xFFD32F2F) // 혼잡 (Red)
val CongestionUnknown = Color.Gray

val BusBlue = Color(0xFF356DE6)
val BusGreen = Color(0xFF43C065)
val BusRed = Color(0xFFE33735)
val BusYellow = Color(0xFFF7D121)

fun getBusColorByRouteType(routeType: String?): Color {
    return when (routeType) {
        "1", "2", "5", "6" -> BusGreen
        "3" -> BusBlue
        "4" -> BusRed
        "7" -> BusYellow
        else -> Color.Gray
    }
}

fun getBusColorByNumber(busNumber: String?): Color {
    if (busNumber.isNullOrBlank()) return Color.Gray
    val trimmed = busNumber.trim()
    if (trimmed.startsWith("N", ignoreCase = true)) return BusBlue

    val digits = trimmed.filter { it.isDigit() }
    val length = digits.length

    return when {
        length == 4 && digits.startsWith('9') -> BusRed
        length == 3 -> BusBlue
        length == 4 -> BusGreen
        length == 2 -> BusYellow
        trimmed.any { !it.isDigit() && !it.equals('N', ignoreCase = true) } -> BusGreen
        else -> Color.Gray
    }
}

// 혼잡도 코드 -> 텍스트 변환
fun getCongestionStatus(code: String?): String {
    return when (code) {
        "0", "3" -> "여유"
        "4" -> "보통"
        "5" -> "혼잡"
        "6" -> "매우 혼잡"
        else -> ""
    }
}

// 혼잡도 코드 -> 색상 변환
fun getCongestionColor(code: String?): Color {
    return when (code) {
        "0", "3" -> CongestionComfort
        "4" -> CongestionNormal
        "5", "6" -> CongestionCrowded
        else -> CongestionUnknown
    }
}