package com.example.bfdream_android.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bfdream_android.R

data class BusInfo(val id: String, val number: String, val color: Color)
data class BusStop(val id: String, val name: String, val direction: String, val buses: List<BusInfo>)

val mockBusStops = listOf(
    BusStop(
        id = "stop_1",
        name = "한아름공원",
        direction = "건대입구역사거리 건대병원 방면",
        buses = listOf(
            BusInfo("bus_721", "721", Color(0xFF3F72AF)),
            BusInfo("bus_147", "147", Color(0xFF3F72AF)),
            BusInfo("bus_2222", "2222", Color(0xFF50A250))
        )
    ),
//    BusStop(
//        id = "stop_2",
//        name = "다른 정류장",
//        direction = "다른 방면",
//        buses = listOf(
//            BusInfo("bus_100", "100", Color(0xFFD32F2F)),
//            BusInfo("bus_200", "200", Color(0xFF3F72AF))
//        )
//    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToHelp: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var selectedBusId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        Image(
                            painter = painterResource(R.drawable.main_logo),
                            contentDescription = "맘편한 이동",
                            modifier = Modifier.size(26.dp).padding(end = 4.dp)
                        )
                        Text(
                            text = "맘편한 이동",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHelp) {
                        Image(
                            painter = painterResource(R.drawable.main_help),
                            contentDescription = "도움말",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Image(
                            painter = painterResource(R.drawable.main_info),
                            contentDescription = "앱 정보",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFA1ACF9)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFA1ACF9))
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // 1. 그림자 레이어
                if (!isPressed) { // 눌리지 않았을 때만 그림자 표시
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(y = 4.dp) // 그림자를 4.dp 만큼 아래로 이동
                            .background(
                                color = Color.Black.copy(alpha = 0.2f), // 그림자 색
                                shape = CircleShape
                            )
                            .blur(radius = 10.dp) // 그림자를 흐리게
                    )
                }

                // 2. 버튼 레이어
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier.fillMaxSize(), // Box 크기에 맞춤
                    shape = CircleShape, // 원형으로 변경
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White // 흰색 배경
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,  // 기본 그림자 0 (수동 처리)
                        pressedElevation = 0.dp   // 눌렀을 때 그림자 0
                    ),
                    interactionSource = interactionSource // (중요) isPressed 상태 공유
                ) {
                    Image(
                        painter = painterResource(R.drawable.main_button),
                        contentDescription = "알림 발송 버튼",
                        modifier = Modifier.size(180.dp) // 버튼 크기(200dp)보다 작게 설정
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 주변 정류장 정보
            Text(
                text = "버스 선택 후, 알림을 울려주세요!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 버스 목록 (목업 데이터)
            BusStopList(
                busStops = mockBusStops,
                selectedBusId = selectedBusId,
                onBusSelected = { newBusId ->
                    selectedBusId = if (selectedBusId == newBusId) null else newBusId
                }
            )
        }
    }
}

@Composable
fun BusStopList(
    busStops: List<BusStop>,
    selectedBusId: String?,
    onBusSelected: (String) -> Unit
) {
    // 이미지의 "알림 가능한 버스 선택" 타이틀
    Text(
        text = "알림 가능한 버스 선택",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(busStops) { stop ->
            BusStopCard(
                stop = stop,
                selectedBusId = selectedBusId,
                onBusSelected = onBusSelected
            )
        }
    }
}

@Composable
fun BusStopCard(
    stop: BusStop,
    selectedBusId: String?,
    onBusSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White) // 배경 흰색
    ) {
        Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
            // --- 정류장 헤더 (이름, 방향, 새로고침) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top // 상단 정렬
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stop.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = stop.direction,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = { /* TODO: 새로고침 로직 */ }) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "새로고침",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- 버스 목록 ---
            stop.buses.forEachIndexed { index, bus ->
                BusRow(
                    bus = bus,
                    isSelected = bus.id == selectedBusId,
                    onClick = { onBusSelected(bus.id) }
                )
                // 마지막 항목 뒤에는 구분선 X
                if (index < stop.buses.lastIndex) {
                    Divider(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BusRow(
    bus: BusInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // 행 전체 클릭 가능
            .padding(vertical = 16.dp), // 수직 패딩
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = bus.number,
            color = bus.color,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
            contentDescription = if (isSelected) "선택됨" else "선택 안됨",
            tint = if (isSelected) Color(0xFF3F72AF) else Color.LightGray.copy(alpha = 0.8f),
            modifier = Modifier.size(32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    MainScreen(
        onNavigateToHelp = {},
        onNavigateToProfile = {}
    )
}
