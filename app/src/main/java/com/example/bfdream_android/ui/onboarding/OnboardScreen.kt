package com.example.bfdream_android.ui.onboarding

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bfdream_android.R
import com.example.bfdream_android.ui.theme.BFDreamAndroidTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardScreen(
    onComplete: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val buttonSize = screenHeight * 0.2f

    val pages = listOf(
        OnboardPageData(
            R.drawable.onboard1,
            "안녕하세요 :)\n맘편한 이동입니다",
            "맘편한 이동",
            0xFF5409DA,
            "임산부 분들의 편안하고 안전한 버스 배려석 탑승을 도와드릴게요!"
        ),
        OnboardPageData(
            R.drawable.onboard2,
            "부담감 제로",
            "제로",
            0xFF3F72AF,
            "탑승하려는 버스의 임산부 배려석에 알림을 줄 수 있어요! 이로 인해 탑승객들의 자연스러운 배려석 양보가 가능합니다."
        ),
        OnboardPageData(
            R.drawable.onboard3,
            "쉽고 간편하게",
            "간편하게",
            0xFF4E71FF,
            "GPS와 실시간 버스 데이터 기반으로 주변 정류장의 탑승할 버스 도착 정보를 확인하고, 알림만 울리면 끝!"
        ),
        OnboardPageData(
            R.drawable.onboard4,
            "임산부들만 이용가능",
            "임산부",
            0xFFFFC0CB,
            "임산부 신고 후, 해당 서비스를 이용하실 수 있습니다. 임산부 신고는 e보건소 혹은 직접 방문, 아이마중 어플 등을 통해 가능합니다."
        ),
        OnboardPageData(
            R.drawable.info_logo,
            "맘편한 이동을 위한\n필수 접근권한 안내",
            "맘편한 이동",
            0xFF5409DA,
            isLast = true,
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    // 1. 요청할 권한 목록 정의
    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // 안드로이드 12 (API 31) 이상
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    } else {
        // 안드로이드 11 (API 30) 이하
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
            // BLUETOOTH, BLUETOOTH_ADMIN은 install-time 권한 (보통)
        )
    }

    // 2. 권한 요청 런처 생성
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            // 3. 권한 요청 결과 처리
            val allPermissionsGranted = permissions.values.all { it }
            if (allPermissionsGranted) {
                // 모든 권한이 허용되었으면 onComplete (메인 화면 이동)
                Toast.makeText(context, "권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
                onComplete()
            } else {
                // 권한이 하나라도 거부되었을 때
                // 사용자에게 권한이 꼭 필요하다고 알려주는 것이 좋습니다. (예: Toast, Dialog)
                Toast.makeText(context, "앱 기능 사용을 위해 권한 허용이 필요합니다.", Toast.LENGTH_LONG).show()
                // (참고) 여기서 바로 onComplete()를 호출하면 권한 없이 메인으로 넘어갑니다.
                // onComplete()
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFA1ACF9))
            .systemBarsPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            OnboardPage(data = pages[pageIndex])
        }

        // 페이지 인디케이터
        PageIndicator(
            pageCount = pages.size,
            currentPage = pagerState.currentPage
        )

        Spacer(modifier = Modifier.weight(0.06f))
        // 버튼
        if (pagerState.currentPage == pages.size - 1) {
            Button(
                onClick = { permissionLauncher.launch(permissionsToRequest) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5409DA),
                    contentColor = Color.White
                ),
                modifier = Modifier.width(buttonSize).height(60.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "시작하기",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        } else {
            Button(
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5409DA).copy(alpha = 0.6f),
                    contentColor = Color.White,
                ),
                modifier = Modifier.width(buttonSize).height(60.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "다음",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.1f))
    }
}

// 온보딩 페이지 데이터 클래스
data class OnboardPageData(
    val image: Int,
    val title: String,
    val titleHighlight: String,
    val highlightColor: Long,
    val description: String = "",
    val isLast: Boolean = false,
)

// 개별 온보딩 페이지 UI
@Composable
fun OnboardPage(data: OnboardPageData) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val spacerSize = screenHeight * 0.01f
    val imageSize = screenHeight * 0.35f
    val imageSize2 = screenHeight * 0.22f
    val screenWidth = configuration.screenWidthDp
    val textScale = (screenWidth / 360f).coerceIn(0.4f, 1.3f)

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val annotatedString = buildAnnotatedString {
            append(data.title)
            // 1. 전체 텍스트를 흰색으로 설정
            addStyle(
                style = SpanStyle(color = Color.White),
                start = 0,
                end = data.title.length
            )

            // 2. 강조할 텍스트가 있다면, 해당 부분만 primary 색상으로 덧칠
            val startIndex = data.title.indexOf(data.titleHighlight)
            if (startIndex != -1) {
                addStyle(
                    style = SpanStyle(color = Color(data.highlightColor)),
                    start = startIndex,
                    end = startIndex + data.titleHighlight.length
                )
            }
        }

        if (data.isLast) {
            Image(
                painter = painterResource(id = data.image),
                contentDescription = data.title,
                modifier = Modifier.size(imageSize2)
            )

            Spacer(modifier = Modifier.height(spacerSize*3))
            Text(
                text = annotatedString,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 36.sp * textScale,
                lineHeight = (36.sp * textScale) * 1.3f
            )

            Spacer(modifier = Modifier.height(spacerSize*4))
            Row(
                modifier = Modifier.width(250.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.location),
                        contentDescription = "위치 아이콘",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "위치",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp * textScale,
                        color = Color.White,
                    )
                    Text(
                        text = "현재 버스정류장 및\n탑승할 버스 안내",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp * textScale,
                        color = Color.White,
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.bluetooth),
                        contentDescription = "블루투스 아이콘",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "근처 기기",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp * textScale,
                        color = Color.White,
                    )
                    Text(
                        text = "버스 내부 배려석\n알림 기기 통신",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp * textScale,
                        color = Color.White,
                    )
                }
            }
        } else {
            Image(
                painter = painterResource(id = data.image),
                contentDescription = data.title,
                modifier = Modifier.size(imageSize)
            )

            Spacer(modifier = Modifier.height(spacerSize))
            Text(
                text = annotatedString,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 36.sp * textScale,
                lineHeight = (36.sp * textScale) * 1.3f
            )

            Spacer(modifier = Modifier.height(spacerSize))
            Text(
                text = data.description,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 16.sp
            )
        }
    }
}

// 페이지 인디케이터 (점)
@Composable
fun PageIndicator(pageCount: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val color = if (currentPage == index) Color(0xFF5409DA) else Color(0xFFDBE2EF)
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardPreview() {
    BFDreamAndroidTheme {
        OnboardScreen (
            onComplete = {},
        )
    }
}
