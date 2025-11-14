package com.example.bfdream_android.components

import android.content.Context
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bfdream_android.R
import com.example.bfdream_android.ui.theme.BFDreamAndroidTheme
import com.example.bfdream_android.viewmodel.BTViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToHelp: () -> Unit,
    onNavigateToProfile: () -> Unit,
    btViewModel: BTViewModel = viewModel( // 수정: ViewModel 타입 및 이름 변경
        factory = BTViewModelFactory(LocalContext.current.applicationContext)
    )
) {
    val selectedBusId by btViewModel.selectedBusId.collectAsState() // 수정: ViewModel 인스턴스 이름 변경
    val connectionState by btViewModel.connectionState.collectAsState() // 수정: ViewModel 인스턴스 이름 변경
    val isSending by btViewModel.isSending.collectAsState() // 수정: ViewModel 인스턴스 이름 변경

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // --- State -> Snackbar 메시지 표시 로직 ---
    LaunchedEffect(connectionState) {
        val currentState = connectionState // 스마트 캐스트를 위해 지역 변수에 저장
        val message = when (currentState) {
            is BTViewModel.BleConnectionState.Scanning -> "주변 버스 찾는 중..."
            is BTViewModel.BleConnectionState.Connecting -> "버스에 연결 중..."
            is BTViewModel.BleConnectionState.Connected -> "버스 연결됨, 알림 전송 시도..."
            is BTViewModel.BleConnectionState.Success -> "알림 전송 성공!"
            is BTViewModel.BleConnectionState.Error -> "오류: ${currentState.message}"
            is BTViewModel.BleConnectionState.Idle -> null // Idle 상태는 메시지 표시 안 함
        }
        message?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                // 성공 또는 에러 시 잠시 후 Idle 상태로 복귀
                if (currentState is BTViewModel.BleConnectionState.Success || currentState is BTViewModel.BleConnectionState.Error) {
                    kotlinx.coroutines.delay(2000) // 2초 대기
                    btViewModel.resetState()
                }
            }
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // 스낵바 추가
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) { // 로고와 텍스트 정렬
                        Image(
                            painter = painterResource(R.drawable.main_logo),
                            contentDescription = "맘편한 이동 로고",
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp)) // 로고와 텍스트 간격
                        Text(
                            text = "맘편한 이동",
                            fontWeight = FontWeight.Bold,
                            color = Color.White // 흰색 텍스트
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHelp) {
                        Image(
                            painter = painterResource(R.drawable.main_help),
                            contentDescription = "도움말",
                            modifier = Modifier.size(26.dp)
                            // tint 제거됨
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Image(
                            painter = painterResource(R.drawable.main_info),
                            contentDescription = "앱 정보",
                            modifier = Modifier.size(26.dp)
                            // tint 제거됨
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
            horizontalAlignment = Alignment.CenterHorizontally // 자식 요소 가운데 정렬
        ) {
            Spacer(modifier = Modifier.height(24.dp)) // 상단 여백

            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            // --- 버튼과 그림자를 겹치기 위한 Box ---
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // 1. 그림자 레이어
                if (!isPressed && !isSending && selectedBusId != null) { // 눌리지 않았고, 전송중이 아니고, 버스가 선택되었을때만 그림자 표시
                    Box(
                        modifier = Modifier
                            .matchParentSize() // 버튼 크기와 동일하게
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
                    onClick = {
                        if (selectedBusId != null && !isSending) {
                            btViewModel.sendCourtesySeatNotification() // 수정: ViewModel 호출
                        }
                    },
                    modifier = Modifier.fillMaxSize(), // Box 크기에 맞춤
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        // 비활성화 상태 색 추가 (버스 미선택 또는 전송 중)
                        disabledContainerColor = Color.White.copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp, // 기본 그림자 0 (수동 처리)
                        pressedElevation = 0.dp // 눌렀을 때 그림자 0
                    ),
                    interactionSource = interactionSource, // (중요) isPressed 상태 공유
                    enabled = selectedBusId != null && !isSending // 버스 선택 & 전송 중 아닐 때만 활성화
                ) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(100.dp)) // 전송 중일 때 로딩 표시
                    } else {
                        Image(
                            painter = painterResource(R.drawable.main_button),
                            contentDescription = "알림 발송 버튼",
                            modifier = Modifier.size(180.dp) // 버튼 크기(200dp)보다 작게 설정
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.height(24.dp))

            // 중앙 텍스트
            Text(
                text = if (selectedBusId == null) "알림을 보낼 버스를 선택해주세요!" else "선택 완료! 알림을 울려주세요",
                style = MaterialTheme.typography.titleMedium, // 크기 조정
                fontWeight = FontWeight.Bold,
                color = Color.White // 색상 변경
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 버스 목록 (실제 데이터 반영)
            BusStopCard( // BusStopList 대신 직접 Card 사용
                stop = mockBusStops.first(), // 일단 첫 번째 정류장만 표시 (추후 확장)
                selectedBusId = selectedBusId,
                onBusSelected = { busId ->
                    btViewModel.selectBus(busId) // 수정: ViewModel 호출
                },
                onRefresh = { /* TODO: 새로고침 로직 */ }
            )

            // LazyColumn은 Column 바로 아래에 직접 사용 권장되지 않음 (스크롤 문제)
            // 필요 시 BoxWithConstraints 등을 사용
        }
    }
}

// --- 목업 데이터 및 실제 컴포저블 ---

// 데이터 클래스 (실제 앱에서는 API 등에서 가져와야 함)
data class BusInfo(val id: String, val number: String, val color: Color, val arrivalTime: String? = null)
data class BusStop(val name: String, val direction: String, val buses: List<BusInfo>)

// 임시 목업 데이터
val mockBusStops = listOf(
    BusStop(
        "한아름공원",
        "건대입구역사거리 건대병원 방면",
        listOf(
            BusInfo("ID_721", "721", Color(0xFF3F72AF), "3분"),
            BusInfo("ID_147", "147", Color(0xFF3F72AF), "5분"),
            BusInfo("ID_2222", "2222", Color(0xFF5DB075), "8분 도착예정") // 색상 예시
        )
    )
    // 필요 시 다른 정류장 추가
)


@Composable
fun BusStopCard(
    stop: BusStop,
    selectedBusId: String?,
    onBusSelected: (String?) -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // 그림자 살짝 추가
        shape = RoundedCornerShape(16.dp), // 모서리 둥글게
        colors = CardDefaults.cardColors(containerColor = Color.White) // 카드 배경 흰색
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 헤더 (정류장 이름, 방향, 새로고침 버튼)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stop.name,
                        style = MaterialTheme.typography.titleLarge, // 크기 키움
                        fontWeight = FontWeight.Bold,
                        color = Color.Black // 검은색
                    )
                    Text(
                        text = stop.direction,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray // 회색
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "새로고침",
                        tint = Color.Gray // 아이콘 색상
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f)) // 구분선 추가
            Spacer(modifier = Modifier.height(16.dp))

            // 버스 목록
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { // 버스 간 간격
                stop.buses.forEach { bus ->
                    BusRow(
                        bus = bus,
                        isSelected = bus.id == selectedBusId,
                        onClick = {
                            // 이미 선택된 것을 다시 누르면 선택 해제, 아니면 새로 선택
                            val newSelection = if (bus.id == selectedBusId) null else bus.id
                            onBusSelected(newSelection)
                        }
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
            .clip(RoundedCornerShape(8.dp)) // 클릭 영역 시각화 (선택사항)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp), // 상하 패딩 추가
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = bus.number,
            style = MaterialTheme.typography.headlineSmall, // 버스 번호 크게
            fontWeight = FontWeight.Bold,
            color = bus.color // 지정된 색상 사용
        )
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = if (isSelected) "선택됨" else "선택하기",
            tint = if (isSelected) Color(0xFF3F72AF) else Color.LightGray, // 선택 시 파란색, 아니면 회색
            modifier = Modifier.size(32.dp) // 아이콘 크기
        )
    }
}


// --- Preview ---
@Preview(showBackground = true)
@Composable
fun MainPreview() {
    BFDreamAndroidTheme { // Preview에도 Theme 적용
        MainScreen(
            onNavigateToHelp = {},
            onNavigateToProfile = {}
        )
    }
}

// ViewModel Factory (Context 주입 위해 필요)
class BTViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BTViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BTViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
