package com.example.bfdream_android.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToHelp: () -> Unit,
    onNavigateToProfile: () -> Unit,
    btViewModel: BTViewModel = viewModel(
        factory = BTViewModelFactory(LocalContext.current.applicationContext)
    ),
) {
    val appContext = LocalContext.current.applicationContext
    val busViewModel: BusViewModel = viewModel(
        factory = BusViewModelFactory(
            appContext,
            BusApiService.instance,
            remember(appContext) { BusDataRepository(appContext) }
        )
    )

    // --- State 수집 ---
    val selectedBusId by btViewModel.selectedBusId.collectAsState()
    val btConnectionState by btViewModel.connectionState.collectAsState()
    val isSending by btViewModel.isSending.collectAsState()
    val busApiState by busViewModel.busApiState.collectAsState()
    val isRefreshing by busViewModel.isRefreshing.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }

    // [추가] 성공 다이얼로그 표시 여부를 관리하는 상태 변수
    var showSuccessDialog by remember { mutableStateOf(false) }

    // --- 화면 크기 가져오기 및 버튼 크기 계산 [추가됨] ---
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    // 화면 너비의 65%를 버튼 크기로 설정 (너무 작거나 크지 않게 조절 가능)
    val buttonSize = screenWidth * 0.65f

    // --- 선택된 버스 정보 찾기 ---
    val selectedBusInfo: BusInfo? by remember(selectedBusId, busApiState) {
        derivedStateOf {
            if (selectedBusId == null || busApiState !is BusApiState.Success) {
                null
            } else {
                (busApiState as BusApiState.Success).busStops
                    .flatMap { it.buses }
                    .find { it.id == selectedBusId }
            }
        }
    }

    // --- Bluetooth 상태 Effect ---
    LaunchedEffect(btConnectionState) {
        val currentState = btConnectionState

        // 1. 성공 상태 감지 시 다이얼로그 띄우기
        if (currentState is BTViewModel.BleConnectionState.Success) {
            showSuccessDialog = true
        }
    }

    // --- [추가] 전송 성공 Dialog ---
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                // 다이얼로그 바깥을 터치해서 닫을 때도 상태 초기화
                showSuccessDialog = false
                btViewModel.resetState()
            },
            title = { Text(text = "알림 전송 성공") },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    btViewModel.resetState() // 확인 버튼 누르면 초기화
                }) {
                    Text("확인")
                }
            }
        )
    }

    // --- Confirm Dialog ---
    if (showConfirmDialog && selectedBusInfo != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(text = "알림 전송 확인") },
            text = { Text(text = "${selectedBusInfo!!.number}번 버스에 배려석 알림을 전송하시겠습니까?") },
            confirmButton = {
                Button(onClick = {
                    btViewModel.sendCourtesySeatNotification()
                    showConfirmDialog = false
                }) { Text("확인") }
            },
            dismissButton = {
                Button(onClick = { showConfirmDialog = false }) { Text("취소") }
            }
        )
    }

    // --- BT Error Dialog ---
    val currentBtState = btConnectionState
    if (currentBtState is BTViewModel.BleConnectionState.Error) {
        AlertDialog(
            onDismissRequest = { btViewModel.resetState() },
            title = { Text(text = "전송 실패") },
            text = { Text(text = "버스 배려석 알림 전송에 실패하였습니다. (${currentBtState.message})") },
            confirmButton = {
                Button(onClick = { btViewModel.resetState() }) { Text("확인") }
            }
        )
    }

    Scaffold(
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
                        Text(text = "맘편한 이동", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHelp) {
                        Image(painter = painterResource(R.drawable.main_help), contentDescription = "도움말", modifier = Modifier.size(26.dp))
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Image(painter = painterResource(R.drawable.main_info), contentDescription = "앱 정보", modifier = Modifier.size(26.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFA1ACF9))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFA1ACF9))
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // --- 1. 상단 헤더 영역 ---
                item {
                    Spacer(modifier = Modifier.height(24.dp))

                    val interactionSource = remember { MutableInteractionSource() }

                    // [수정] 동적 크기 적용
                    Box(
                        modifier = Modifier.size(buttonSize), // buttonSize 사용
                        contentAlignment = Alignment.Center
                    ) {
                        // 그림자
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(y = 4.dp)
                                .background(Color.Black.copy(alpha = 0.2f), CircleShape)
                                .blur(radius = 10.dp)
                        )
                        // 버튼
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
                            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                            interactionSource = interactionSource,
                            enabled = selectedBusId != null && !isSending && (currentBtState !is BTViewModel.BleConnectionState.Error)
                        ) {
                            if (isSending) {
                                CircularProgressIndicator(modifier = Modifier.size(100.dp))
                            } else {
                                val imageResource = if (selectedBusInfo != null) R.drawable.main_button_selected else R.drawable.main_button
                                val contentDesc = if (selectedBusInfo != null) "알림 발송 버튼 (활성화)" else "알림 발송 버튼 (비활성화)"

                                // [수정] 이미지가 부모 박스(버튼) 크기를 가득 채우도록 변경
                                Image(
                                    painter = painterResource(id = imageResource),
                                    contentDescription = contentDesc,
                                    modifier = Modifier.fillMaxSize() // fillMaxSize 사용
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                    Text(
                        text = if (selectedBusInfo == null) "버스 선택 후, 알림을 울려주세요!" else "선택 완료! 알림을 울려주세요",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(screenHeight * 0.02f))
                }

                // --- 2. 버스 목록 데이터 영역 ---
                when (val state = busApiState) {
                    is BusApiState.Loading -> {
                        item {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.padding(top = 32.dp)
                            )
                        }
                    }
                    is BusApiState.Success -> {
                        if (state.busStops.isEmpty()) {
                            item {
                                EmptyBusStopCard(
                                    message = "주변에 관심 버스가 없습니다.",
                                    isRefreshing = isRefreshing,
                                    onRefresh = { busViewModel.loadBusDataFromCurrentLocation() }
                                )
                            }
                        } else {
                            items(state.busStops, key = { it.arsId }) { stop ->
                                BusStopCard(
                                    stop = stop,
                                    selectedBusId = selectedBusId,
                                    isRefreshing = isRefreshing,
                                    onBusSelected = { btViewModel.selectBus(it) },
                                    onRefresh = { busViewModel.loadBusDataFromCurrentLocation() }
                                )
                            }
                        }
                    }
                    is BusApiState.Error -> {
                        item {
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
                        item {
                            Text(
                                text = "버스 정보를 불러오는 중입니다...",
                                color = Color.White,
                                modifier = Modifier.padding(top = 32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- 하위 Composable 함수들 (기존과 동일) ---
@Composable
fun BusStopCard(
    stop: BusStop,
    selectedBusId: String?,
    isRefreshing: Boolean,
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stop.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "사용자와 최근접의 버스정류장 정보가 표시됩니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        maxLines = 1,
                        fontSize = 12.sp,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                    if (isRefreshing) {
                        val transition = rememberInfiniteTransition(label = "refresh_transition")
                        val rotation by transition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "refresh_rotation"
                        )
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "새로고침 중",
                            tint = Color.Gray,
                            modifier = Modifier.rotate(rotation)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "새로고침",
                            tint = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Column {
                stop.buses.forEachIndexed { index, bus ->
                    BusRow(
                        bus = bus,
                        isSelected = bus.id == selectedBusId,
                        onClick = {
                            val newSelection = if (bus.id == selectedBusId) null else bus.id
                            onBusSelected(newSelection)
                        }
                    )
                    if (index < stop.buses.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
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
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.bus),
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
fun EmptyBusStopCard(
    message: String,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
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
                IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                    if (isRefreshing) {
                        val transition = rememberInfiniteTransition(label = "refresh_transition")
                        val rotation by transition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "refresh_rotation"
                        )
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "새로고침 중",
                            tint = Color.Gray,
                            modifier = Modifier.rotate(rotation)
                        )
                    } else {
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
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    BFDreamAndroidTheme {
        val previewBusStops = listOf(
            BusStop(
                "05189", "한아름공원", "건대입구역 방면",
                listOf(
                    BusInfo("100100199", "2222", getBusColorByRouteType("5"), "3분", "5"),
                    BusInfo("100100211", "2415", getBusColorByRouteType("3"), "곧 도착", "3")
                )
            )
        )
        Scaffold { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFA1ACF9))
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Preview용 간단한 Column (LazyColumn 대신)
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    previewBusStops.forEach { stop ->
                        BusStopCard(
                            stop = stop,
                            selectedBusId = "100100199",
                            isRefreshing = false,
                            onBusSelected = {},
                            onRefresh = {}
                        )
                    }
                }
            }
        }
    }
}