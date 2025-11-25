package com.example.bfdream_android.components

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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bfdream_android.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    onNavigateBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val inquiryUrl = "https://forms.gle/rnSD44sUEuy1nLaH6"
    val policyUrl = "https://important-hisser-903.notion.site/10-22-ver-29a65f12c44480b6b591e726c5c80f89?pvs=74"

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "앱 정보",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFA1ACF9) // 배경색
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFA1ACF9)) // 전체 배경색
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(screenHeight * 0.01f))

            // --- 앱 로고 ---
            Image(
                painter = painterResource(id = R.drawable.info_logo),
                contentDescription = "앱 로고",
                modifier = Modifier.size(screenHeight * 0.3f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // --- 정보 카드 ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(top = 26.dp, bottom = 26.dp, start = 8.dp, end = 8.dp)
                ) {
                    // --- 버전 ---
                    InfoRow(text = "버전", value = "v 1.0")

                    Divider(color = Color.LightGray.copy(alpha = 0.4f))

                    // --- 앱 문의 ---
                    ClickableInfoRow(
                        text = "앱 문의",
                        onClick = { uriHandler.openUri(inquiryUrl) }
                    )

                    Divider(color = Color.LightGray.copy(alpha = 0.4f))

                    // --- 개인정보 처리 방침 및 이용약관 ---
                    ClickableInfoRow(
                        text = "개인정보 처리 방침 및 이용약관",
                        onClick = { uriHandler.openUri(policyUrl) }
                    )
                }
            }
        }
    }
}

// --- 정보 행 (클릭 불가) ---
@Composable
fun InfoRow(text: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4E71FF)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}

// --- 정보 행 (클릭 가능) ---
@Composable
fun ClickableInfoRow(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4E71FF)
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "이동",
            tint = Color.Gray.copy(alpha = 0.7f),
            modifier = Modifier.size(26.dp)
        )
    }
}
