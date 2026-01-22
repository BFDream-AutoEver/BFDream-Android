package com.example.bfdream_android.data

import androidx.compose.ui.graphics.Color
import com.example.bfdream_android.ui.theme.bl_CircularBus
import com.example.bfdream_android.ui.theme.bl_FeederBus_TownBus
import com.example.bfdream_android.ui.theme.bl_TrunkBus_NBus
import com.example.bfdream_android.ui.theme.bl_WideAreaBus
import com.example.bfdream_android.ui.theme.state_Comfort
import com.example.bfdream_android.ui.theme.state_Crowded
import com.example.bfdream_android.ui.theme.state_Normal
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
    val type: String,
    val arrivalTime: String,
    val adirection: String,
    val congestionStatus: String,
    val congestionColor: Color
)

enum class BusRouteType(val displayName: String) {
    GANGSEON("간선"),   // 파랑 (3자리)
    JISEON("지선"),     // 초록 (4자리)
    SUNHWAN("순환"),    // 노랑 (2자리)
    GWANGYEOK("광역"),  // 빨강 (9로 시작하는 4자리)
    MAEUL("마을"),      // 초록 (기타 문자 포함)
    SIMYA("심야"),      // 파랑 (N으로 시작)
    GONGHANG("공항"),   // iOS에서는 별도 색상, 여기선 회색 또는 초록 사용 가능
    UNKNOWN("");

    val color: Color
        get() = when (this) {
            GANGSEON, SIMYA -> bl_TrunkBus_NBus
            JISEON, MAEUL -> bl_FeederBus_TownBus
            SUNHWAN -> bl_CircularBus
            GWANGYEOK -> bl_WideAreaBus
            GONGHANG -> Color.Gray // 공항버스는 iOS에서 별도 색상이므로 구분 (필요시 bl_WideAreaBus 등 변경)
            UNKNOWN -> Color.Gray
        }

    companion object {
        fun from(busNumber: String?): BusRouteType {
            if (busNumber.isNullOrBlank()) return UNKNOWN
            val trimmed = busNumber.trim()

            // 심야버스: N으로 시작
            if (trimmed.uppercase().startsWith("N")) {
                return SIMYA
            }

            // 숫자만 추출
            val digits = trimmed.filter { it.isDigit() }
            val length = digits.length

            if (length == 0) return UNKNOWN

            // 공항버스: 6으로 시작하는 4자리
            if (length == 4 && digits.startsWith("6")) {
                return GONGHANG
            }

            // 광역버스: 9로 시작하는 4자리
            if (length == 4 && digits.startsWith("9")) {
                return GWANGYEOK
            }

            // 간선버스: 3자리
            if (length == 3) {
                return GANGSEON
            }

            // 지선버스: 4자리 (위의 6, 9 시작 제외됨)
            if (length == 4) {
                return JISEON
            }

            // 순환버스: 2자리
            if (length == 2) {
                return SUNHWAN
            }

            // 마을버스: 숫자가 아닌 문자가 포함된 경우 (N 제외)
            if (trimmed.any { !it.isDigit() && !it.equals('N', ignoreCase = true) }) {
                return MAEUL
            }

            return UNKNOWN
        }
    }
}

// CSV 파일 읽기용 데이터 클래스 (기존 유지)
data class LocalBusRoute(
    val routeId: String,
    val routeName: String,
    val nodeId: String,
    val arsId: String,
    val stationName: String
)

// --- API 응답 모델 (기존 유지) ---
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

    @field:Element(name = "congestion", required = false)
    var congestion: String? = null,
    @field:Element(name = "congestion1", required = false)
    var congestion1: String? = null,
    @field:Element(name = "reride_Num1", required = false)
    var rerideNum1: String? = null
)

// --- 유틸리티 및 상수 ---

val CongestionComfort = state_Comfort
val CongestionNormal = state_Normal
val CongestionCrowded = state_Crowded
val CongestionUnknown = Color.Gray

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
