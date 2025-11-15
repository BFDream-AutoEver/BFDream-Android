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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bfdream_android.R
import com.example.bfdream_android.data.BusDataRepository
import com.example.bfdream_android.data.BusInfo
import com.example.bfdream_android.data.BusStop
import com.example.bfdream_android.data.getBusColorByRouteType
import com.example.bfdream_android.network.BusApiService
import com.example.bfdream_android.ui.theme.BFDreamAndroidTheme
import com.example.bfdream_android.viewmodel.BTViewModel
import com.example.bfdream_android.viewmodel.BTViewModelFactory
import com.example.bfdream_android.viewmodel.BusApiState
import com.example.bfdream_android.viewmodel.BusViewModel
import com.example.bfdream_android.viewmodel.BusViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToHelp: () -> Unit,
    onNavigateToProfile: () -> Unit,
    // [수정] 2개의 ViewModel 주입
    btViewModel: BTViewModel = viewModel(
        factory = BTViewModelFactory(LocalContext.current.applicationContext)
    ),
) {
    // [수정] BusViewModel을 composable 본문 안에서 초기화합니다.
    val appContext = LocalContext.current.applicationContext
    val busViewModel: BusViewModel = viewModel(
        factory = BusViewModelFactory(
            appContext,
            BusApiService.instance, // 1. busApi 전달
            remember(appContext) { BusDataRepository(appContext) } // 2. busRepo 생성 및 전달
        )
    )

    // --- BTViewModel 상태 ---
    val selectedBusId by btViewModel.selectedBusId.collectAsState()
    val btConnectionState by btViewModel.connectionState.collectAsState()
    val isSending by btViewModel.isSending.collectAsState()

    // --- BusViewModel 상태 ---
    val busApiState by busViewModel.busApiState.collectAsState()

    // --- UI 상태 ---
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showConfirmDialog by remember { mutableStateOf(false) }

    // --- [수정] 선택된 버스 정보를 API 결과(busApiState)에서 찾기 ---
    val selectedBusInfo: BusInfo? by remember(selectedBusId, busApiState) {
        derivedStateOf {
            if (selectedBusId == null || busApiState !is BusApiState.Success) {
                null
            } else {
                // 모든 정류장의 모든 버스를 뒤져서 ID가 일치하는 버스 찾기
                (busApiState as BusApiState.Success).busStops // [수정] busStops
                    .flatMap { it.buses }
                    .find { it.id == selectedBusId }
            }
        }
    }

    // --- Bluetooth 상태 처리 Effect (기존과 동일) ---
    LaunchedEffect(btConnectionState) {
        val currentState = btConnectionState
        val message = when (currentState) {
            is BTViewModel.BleConnectionState.Scanning -> "주변 버스 찾는 중..."
            is BTViewModel.BleConnectionState.Connecting -> "버스에 연결 중..."
            is BTViewModel.BleConnectionState.Connected -> "버스 연결됨, 알림 전송 시도..."
            is BTViewModel.BleConnectionState.Success -> "알림 전송 성공!"
            is BTViewModel.BleConnectionState.Error -> null
            is BTViewModel.BleConnectionState.Idle -> null
        }

        message?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                if (currentState is BTViewModel.BleConnectionState.Success) {
                    kotlinx.coroutines.delay(2000)
                    btViewModel.resetState()
                }
            }
        }
    }

    // --- 확인 다이얼로그 (기존과 동일) ---
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

    // --- [수정] BT 실패 다이얼로그 (기존과 동일, 변수명만 변경) ---
    val currentBtState = btConnectionState
    if (currentBtState is BTViewModel.BleConnectionState.Error) {
        AlertDialog(
            onDismissRequest = { btViewModel.resetState() },
            title = { Text(text = "전송 실패") },
            text = { Text(text = "버스 배려석 알림 전송에 실패하였습니다. (${currentBtState.message})") }, // [수정] 에러 메시지 포함
            confirmButton = {
                Button(onClick = { btViewModel.resetState() }) { Text("확인") }
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
                .padding(horizontal = 16.dp), // [수정] 상하 패딩은 LazyColumn이 처리하도록 수평만
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            Box(
                modifier = Modifier.size(260.dp),
                contentAlignment = Alignment.Center
            ) {
                // 1. 그림자
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

                // 2. 버튼
                Button(
                    onClick = {
                        if (selectedBusId != null && !isSending) {
                            showConfirmDialog = true
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
                    // [수정] BT 에러 시에도 비활성화
                    enabled = selectedBusId != null && !isSending && (currentBtState !is BTViewModel.BleConnectionState.Error)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(100.dp))
                    } else {
                        // [수정] selectedBusInfo (실제 데이터) 기반으로 이미지 변경
                        val imageResource = if (selectedBusInfo != null) {
                            R.drawable.main_button_selected
                        } else {
                            R.drawable.main_button
                        }
                        val contentDesc = if (selectedBusInfo != null) {
                            "알림 발송 버튼 (활성화)"
                        } else {
                            "알림 발송 버튼 (비활성화 - 버스 선택 필요)"
                        }

                        Image(
                            painter = painterResource(id = imageResource),
                            contentDescription = contentDesc,
                            modifier = Modifier.size(260.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(34.dp))

            // [수정] selectedBusInfo 기반으로 텍스트 변경
            Text(
                text = if (selectedBusInfo == null) "버스 선택 후, 알림을 울려주세요!" else "선택 완료! 알림을 울려주세요",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(56.dp))

            // --- [수정] 버스 목록 API 상태에 따라 분기 처리 ---
            when (val state = busApiState) {
                is BusApiState.Loading -> {
                    // 로딩 중
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 32.dp),
                        color = Color.White
                    )
                }
                is BusApiState.Success -> {
                    // [수정] LazyColumn으로 정류장 목록 표시
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp) // 하단 여백
                    ) {
                        items(state.busStops, key = { it.arsId }) { stop -> // [수정] busStops
                            BusStopCard(
                                stop = stop,
                                selectedBusId = selectedBusId,
                                onBusSelected = { busId ->
                                    // [수정] 버스 선택 시 BTViewModel의 selectBus 호출
                                    btViewModel.selectBus(busId)
                                },
                                onRefresh = {
                                    // [수정] 함수 이름 변경
                                    busViewModel.loadBusDataFromCurrentLocation()
                                }
                            )
                        }
                    }
                }
                is BusApiState.Error -> {
                    // [수정] 에러 메시지 내용에 따라 UI를 분기합니다.
                    if (state.message.contains("주변")) {
                        // "주변 정류장 없음" 또는 "주변 관심 버스 없음" 등
                        // 사용자가 요청한 대로 Card 스타일에 메시지를 표시합니다.
                        EmptyBusStopCard(
                            message = state.message,
                            onRefresh = { busViewModel.loadBusDataFromCurrentLocation() }
                        )
                    } else {
                        // "위치 권한" 또는 "네트워크 실패" 등
                        // 기존의 일반 에러 UI를 표시합니다.
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 32.dp)
                        ) {
                            Text(
                                text = "버스 정보를 불러오는 데 실패했습니다.\n(${state.message})",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { busViewModel.loadBusDataFromCurrentLocation() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                            ) {
                                Text("새로고침")
                            }
                        }
                    }
                }
                is BusApiState.Idle -> {
                    // 초기 상태 (보통 Loading으로 바로 전환됨)
                    Text(
                        text = "버스 정보를 불러오는 중입니다...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 32.dp)
                    )
                }
            }
        }
    }
}


// --- 목업 데이터 삭제 ---
// data class BusInfo(...), data class BusStop(...), val mockBusStops ...
// [수정] 이 부분의 목업 데이터는 BusData.kt로 이동했으므로 삭제합니다.


@Composable
fun BusStopCard(
    stop: BusStop, // [수정] 목업 클래스 대신 실제 data 클래스 사용
    selectedBusId: String?,
    onBusSelected: (String?) -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) { // [수정] 텍스트가 길어질 경우 대비
                    Text(
                        text = stop.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Text(
//                        text = stop.direction,
                        text = "사용자와 최근접의 버스정류장 정보가 표시됩니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        maxLines = 1, // [수정] 방향 텍스트 한 줄 처리
                        fontSize = 12.sp,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "새로고침",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                stop.buses.forEach { bus ->
                    BusRow(
                        bus = bus,
                        isSelected = bus.id == selectedBusId,
                        onClick = {
                            val newSelection = if (bus.id == selectedBusId) null else bus.id
                            onBusSelected(newSelection)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun BusRow(
    bus: BusInfo, // [수정] 목업 클래스 대신 실제 data 클래스 사용
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
//            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = "버스",
            tint = bus.color,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = bus.number,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = bus.color,
                fontSize = 18.sp
            )
            Text(
                text = bus.arrivalTime,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontSize = 12.sp
            )
            Text(
                text = "${bus.adirection} 방면",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = if (isSelected) "선택됨" else "선택하기",
            tint = if (isSelected) bus.color else Color.LightGray,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun EmptyBusStopCard(message: String, onRefresh: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "새로고침",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

// --- Preview (수정) ---
@Preview(showBackground = true)
@Composable
fun MainPreview() {
    BFDreamAndroidTheme {
        // Mock 데이터로 BusStopCard 프리뷰
        val previewBusStops = listOf(
            BusStop(
                "05189", "한아름공원", "건대입구역 방면",
                listOf(
                    BusInfo("100100199", "2222", getBusColorByRouteType("5"), "3분", "5"),
                    BusInfo("100100211", "2415", getBusColorByRouteType("3"), "곧 도착", "3")
                )
            )
        )

        // LazyColumn 대신 Column을 사용한 프리뷰
        Scaffold { paddingValues -> // [수정] Scaffold로 감싸기
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFA1ACF9))
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                previewBusStops.forEach { stop ->
                    BusStopCard(
                        stop = stop,
                        selectedBusId = "100100199", // 2222번이 선택된 상태로
                        onBusSelected = {},
                        onRefresh = {}
                    )
                }
            }
        }
    }
}

// --- BTViewModelFactory (기존과 동일) ---
class BTViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BTViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BTViewModel(context.applicationContext) as T // [수정] ApplicationContext
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}