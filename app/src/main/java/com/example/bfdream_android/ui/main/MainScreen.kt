package com.example.bfdream_android.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bfdream_android.R
import com.example.bfdream_android.data.BusDataRepository
import com.example.bfdream_android.data.BusInfo
import com.example.bfdream_android.data.BusStop
import com.example.bfdream_android.network.BusApiService
import com.example.bfdream_android.ui.main.components.BusStopCard
import com.example.bfdream_android.ui.main.components.EmptyBusStopCard
import com.example.bfdream_android.ui.theme.BFDreamAndroidTheme
import com.example.bfdream_android.ui.theme.pr_LavenderPurple
import com.example.bfdream_android.ui.theme.pr_PeriwinkleBlue
import com.example.bfdream_android.ui.theme.pr_White
import com.example.bfdream_android.ui.theme.tx_Black
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
    var showSuccessDialog by remember { mutableStateOf(false) }

    // --- 화면 크기 가져오기 및 버튼 크기 계산 ---
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val buttonSize = screenWidth * 0.55f

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
        if (currentState is BTViewModel.BleConnectionState.Success) {
            showSuccessDialog = true
        }
    }

    // --- 전송 성공 Dialog ---
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                btViewModel.resetState()
            },
            title = { Text(stringResource(R.string.dialog_send_success_title)) },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    btViewModel.resetState()
                }) {
                    Text(stringResource(R.string.btn_confirm))
                }
            }
        )
    }

    // --- Confirm Dialog ---
    if (showConfirmDialog && selectedBusInfo != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(
                text = stringResource(
                    id = R.string.dialog_send_confirm_title,
                    selectedBusInfo!!.number,
                )
            ) },
            confirmButton = {
                Button(onClick = {
                    btViewModel.sendCourtesySeatNotification(selectedBusInfo!!.number)
                    showConfirmDialog = false
                }) {
                    Text(stringResource(R.string.btn_confirm))
                }
            },
            dismissButton = {
                Button(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    // --- BT Error Dialog ---
    val currentBtState = btConnectionState
    if (currentBtState is BTViewModel.BleConnectionState.Error) {
        AlertDialog(
            onDismissRequest = { btViewModel.resetState() },
            title = { Text(text = stringResource(R.string.dialog_send_error_title)) },
            text = { Text(
                text = stringResource(
                    id = R.string.dialog_send_error_desc,
                    currentBtState.message,
                )
            ) },
            confirmButton = {
                Button(onClick = { btViewModel.resetState() }) {
                    Text(stringResource(R.string.btn_confirm))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.main_title_logo),
                            contentDescription = "맘편한 이동 로고",
                            modifier = Modifier.size(126.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHelp) {
                        Image(
                            painter = painterResource(R.drawable.main_help),
                            contentDescription = stringResource(R.string.title_help),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Image(
                            painter = painterResource(R.drawable.main_settings),
                            contentDescription = stringResource(R.string.title_info),
                            modifier = Modifier.size(26.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = pr_LavenderPurple)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(pr_LavenderPurple)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // --- 1. 상단 고정 영역 (버튼 + 텍스트) ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier.size(buttonSize),
                    contentAlignment = Alignment.Center
                ) {
                    // 그림자
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(y = 4.dp)
                            .background(tx_Black.copy(alpha = 0.2f), CircleShape)
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
                            containerColor = pr_PeriwinkleBlue,
                            disabledContainerColor = pr_White,
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                        interactionSource = interactionSource,
                        enabled =
                            selectedBusId != null &&
                            !isSending &&
                            (currentBtState !is BTViewModel.BleConnectionState.Error)
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(modifier = Modifier.size(100.dp))
                        } else {
                            val imageResource =
                                if (selectedBusInfo != null) R.drawable.main_button_enabled
                                else R.drawable.main_button_default

                            val contentDesc =
                                if (selectedBusInfo != null) stringResource(R.string.main_btn_send_enabled)
                                else stringResource(R.string.main_btn_send_disabled)

                            Image(
                                painter = painterResource(id = imageResource),
                                contentDescription = contentDesc,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                Text(
                    text =
                        if (selectedBusInfo == null) stringResource(R.string.main_guide_default)
                        else stringResource(R.string.main_guide_selected),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = pr_White,
                    modifier = Modifier.semantics { heading() },
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.03f))
            }

            // --- 2. 스크롤 가능한 버스 목록 영역 ---
            when (val state = busApiState) {
                is BusApiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = pr_White)
                    }
                }
                is BusApiState.Success -> {
                    if (state.busStops.isEmpty()) {
                        EmptyBusStopCard(
                            message = stringResource(R.string.bus_empty_nearby),
                            isRefreshing = isRefreshing,
                            onRefresh = { busViewModel.loadBusDataFromCurrentLocation() }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
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
                }
                is BusApiState.Error -> {
                    EmptyBusStopCard(
                        message = stringResource(R.string.bus_load_error),
                        isRefreshing = isRefreshing,
                        onRefresh = { busViewModel.loadBusDataFromCurrentLocation() }
                    )
                }
                is BusApiState.Idle -> {
                    EmptyBusStopCard(
                        message = stringResource(R.string.bus_loading),
                        isRefreshing = isRefreshing,
                        onRefresh = { busViewModel.loadBusDataFromCurrentLocation() }
                    )
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
//                    BusInfo("100100199", "2222", getBusColorByRouteType("5"), "3분", "5"),
//                    BusInfo("100100211", "2415", getBusColorByRouteType("3"), "곧 도착", "3")
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