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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.sp
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
    btViewModel: BTViewModel = viewModel(
        factory = BTViewModelFactory(LocalContext.current.applicationContext)
    )
) {
    val selectedBusId by btViewModel.selectedBusId.collectAsState()
    val connectionState by btViewModel.connectionState.collectAsState()
    val isSending by btViewModel.isSending.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // --- [수정] 확인 모달 상태 변수만 남깁니다. ---
    var showConfirmDialog by remember { mutableStateOf(false) }
    // var showFailureDialog by remember { mutableStateOf(false) } // [수정] 이 변수 제거

    // --- 선택된 버스 번호를 찾기 위한 로직 (유지) ---
    val selectedBusInfo: BusInfo? by remember(selectedBusId) {
        derivedStateOf {
            if (selectedBusId == null) {
                null
            } else {
                mockBusStops.firstOrNull()
                    ?.buses
                    ?.find { it.id == selectedBusId }
            }
        }
    }


    // --- [수정] State -> Snackbar/Modal 메시지 표시 로직 ---
    // LaunchedEffect는 이제 스낵바 메시지(성공/진행)만 처리합니다.
    LaunchedEffect(connectionState) {
        val currentState = connectionState
        val message = when (currentState) {
            is BTViewModel.BleConnectionState.Scanning -> "주변 버스 찾는 중..."
            is BTViewModel.BleConnectionState.Connecting -> "버스에 연결 중..."
            is BTViewModel.BleConnectionState.Connected -> "버스 연결됨, 알림 전송 시도..."
            is BTViewModel.BleConnectionState.Success -> "알림 전송 성공!"
            is BTViewModel.BleConnectionState.Error -> null // [수정] Error는 여기서 처리 안 함
            is BTViewModel.BleConnectionState.Idle -> null
        }

        message?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                // 성공 시 딜레이 후 상태 리셋
                if (currentState is BTViewModel.BleConnectionState.Success) {
                    kotlinx.coroutines.delay(2000)
                    btViewModel.resetState()
                }
            }
        }
    }


    // --- 전송 확인 다이얼로그 (유지) ---
    if (showConfirmDialog && selectedBusInfo != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(text = "알림 전송 확인") },
            text = { Text(text = "${selectedBusInfo!!.number}번 버스에 배려석 알림을 전송하시겠습니까?") },
            confirmButton = {
                Button(
                    onClick = {
                        btViewModel.sendCourtesySeatNotification()
                        showConfirmDialog = false
                    }
                ) { Text("확인") }
            },
            dismissButton = {
                Button(
                    onClick = { showConfirmDialog = false }
                ) { Text("취소") }
            }
        )
    }

    // --- [수정] 전송 실패 다이얼로그 ---
    // connectionState가 Error일 때 '직접' 다이얼로그를 표시합니다.
    val currentState = connectionState // 리컴포지션 시 현재 상태 읽기
    if (currentState is BTViewModel.BleConnectionState.Error) {
        AlertDialog(
            onDismissRequest = {
                btViewModel.resetState() // 밖을 클릭해도 상태 리셋
            },
            title = { Text(text = "버스 배려석 알림 전송에 실패하였습니다.") },
            // (선택사항) VM의 구체적인 에러 메시지를 표시할 수 있습니다.
            // text = { Text(text = currentState.message) },
            text = { Text(text = "다시 한 번 시도해주세요.") },
            confirmButton = {
                Button(
                    onClick = {
                        btViewModel.resetState() // 확인 클릭 시 상태 리셋
                    }
                ) { Text("확인") }
            }
        )
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.main_logo),
                            contentDescription = "맘편한 이동 로고",
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "맘편한 이동",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
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
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // 1. 그림자 레이어
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(y = 4.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .blur(radius = 10.dp)
                )

                // 2. 버튼 레이어
                Button(
                    onClick = {
                        if (selectedBusId != null && !isSending) {
                            showConfirmDialog = true // 확인 모달 띄우기 (유지)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4E71FF),
                        disabledContainerColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    ),
                    interactionSource = interactionSource,
                    enabled = selectedBusId != null
                            && !isSending
                            && (currentState !is BTViewModel.BleConnectionState.Error)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(100.dp))
                    } else {
                        val imageResource = if (selectedBusId != null) {
                            R.drawable.main_button_selected
                        } else {
                            R.drawable.main_button
                        }

                        val contentDesc = if (selectedBusId != null) {
                            "알림 발송 버튼 (활성화)"
                        } else {
                            "알림 발송 버튼 (비활성화 - 버스 선택 필요)"
                        }

                        Image(
                            painter = painterResource(id = imageResource),
                            contentDescription = contentDesc,
                            modifier = Modifier.size(280.dp)
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.height(34.dp))

            Text(
                text = if (selectedBusId == null) "알림을 보낼 버스를 선택해주세요!" else "선택 완료! 알림을 울려주세요",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(56.dp))

            BusStopCard(
                stop = mockBusStops.first(),
                selectedBusId = selectedBusId,
                onBusSelected = { busId ->
                    btViewModel.selectBus(busId)
                },
                onRefresh = { /* TODO: 새로고침 로직 */ }
            )
        }
    }
}

// --- 목업 데이터 및 실제 컴포저블 (이하 동일) ---

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
        shape = RoundedCornerShape(16.dp), // 모서리 둥글게
        colors = CardDefaults.cardColors(containerColor = Color.White) // 카드 배경 흰색
    ) {
        Column(modifier = Modifier.padding(top = 13.dp)) {
            // 헤더 (정류장 이름, 방향, 새로고침 버튼)
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 25.dp, end = 25.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stop.name,
                        style = MaterialTheme.typography.titleLarge, // 크기 키움
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black // 검은색
                    )
                    Text(
                        text = "사용자와 최근접의 버스정류장 정보가 표시됩니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 12.sp,
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
