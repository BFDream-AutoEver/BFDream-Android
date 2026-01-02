package com.example.bfdream_android.ui.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bfdream_android.R
import com.example.bfdream_android.viewmodel.BTViewModel
import com.example.bfdream_android.viewmodel.BTViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    onNavigateBack: () -> Unit,
    btViewModel: BTViewModel = viewModel(
        factory = BTViewModelFactory(LocalContext.current.applicationContext)
    )
) {
    val uriHandler = LocalUriHandler.current
    val inquiryUrl = "https://forms.gle/rnSD44sUEuy1nLaH6"
    val policyUrl = "https://important-hisser-903.notion.site/10-22-ver-29a65f12c44480b6b591e726c5c80f89?pvs=74"

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val isSoundOn by btViewModel.isSoundOn.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("앱 정보", color = Color.White, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFA1ACF9))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFA1ACF9))
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(screenHeight * 0.01f))

            Image(
                painter = painterResource(id = R.drawable.info_logo),
                contentDescription = "앱 로고",
                modifier = Modifier.size(screenHeight * 0.24f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(top = 26.dp, bottom = 26.dp, start = 8.dp, end = 8.dp)
                ) {
                    SwitchInfoRow(
                        text = "배려석 알림음 on / off",
                        subText = "알림음을 꺼도 불빛과 전광판 알림은 유지됩니다.",
                        checked = isSoundOn,
                        onCheckedChange = { btViewModel.toggleSound(it) }
                    )
                    Divider(color = Color.LightGray.copy(alpha = 0.4f))

                    InfoRow(text = "버전", value = "v 1.0.0")
                    Divider(color = Color.LightGray.copy(alpha = 0.4f))

                    ClickableInfoRow(
                        text = "앱 문의",
                        onClick = { uriHandler.openUri(inquiryUrl) }
                    )
                    Divider(color = Color.LightGray.copy(alpha = 0.4f))

                    ClickableInfoRow(
                        text = "개인정보 처리 방침 및 이용약관",
                        onClick = { uriHandler.openUri(policyUrl) }
                    )
                }
            }
        }
    }
}

// [추가] 토글 스위치가 있는 행 컴포저블
@Composable
fun SwitchInfoRow(
    text: String,
    subText: String = "", // 기본값은 빈 문자열
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 텍스트 영역 (제목 + 부연설명)
        Column(
            modifier = Modifier.weight(1f), // 남은 공간을 차지하여 스위치를 오른쪽으로 밀어냄
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4E71FF)
            )
            // 부연 설명이 있을 때만 표시
            if (subText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodySmall, // 작은 글씨 크기
                    color = Color.Gray, // 회색 처리
                    lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.2 // 줄간격 살짝 조정
                )
            }
        }

        // Column에 weight(1f)를 주었으므로 Spacer는 필요 없음

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4E71FF),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}

// 기존 InfoRow, ClickableInfoRow는 그대로 유지
@Composable
fun InfoRow(text: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFF4E71FF))
        Spacer(modifier = Modifier.weight(1f))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
    }
}

@Composable
fun ClickableInfoRow(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFF4E71FF))
        Spacer(modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "이동", tint = Color.Gray.copy(alpha = 0.7f), modifier = Modifier.size(26.dp))
    }
}