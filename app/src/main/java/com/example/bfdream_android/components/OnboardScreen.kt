package com.example.bfdream_android.components

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
    val pages = listOf(
        OnboardPageData(
            R.drawable.onboard1,
            "안녕하세요 :)\n맘편한 이동입니다",
            "맘편한 이동",
            0xFF5409DA,
            "임산부 분들의 편안하고 안전한 버스 배려석 탑승을\n도와드릴게요!"
        ),
        OnboardPageData(
            R.drawable.onboard2,
            "부담감 제로",
            "제로",
            0xFF3F72AF,
            "탑승하려는 버스의 임산부 배려석에 알림을 줄 수 있어요!\n이로 인해 탑승객들의 자연스러운 배려석 양보가 \n가능합니다."
        ),
        OnboardPageData(
            R.drawable.onboard3,
            "쉽고 간편하게",
            "간편하게",
            0xFF4E71FF,
            "GPS & 실시간 버스 데이터 기반으로\n주변 정류장의 탑승할 버스 도착 정보를 확인하고,\n알림만 울리면 끝!"
        ),
        OnboardPageData(
            R.drawable.onboard4,
            "임산부들만 이용가능",
            "임산부",
            0xFFFFC0CB,
            "임산부 신고 후, 해당 서비스를 이용하실 수 있습니다.\n임산부 신고는 e보건소 혹은 직접 방문, 아이마중 어플 등을 통해 \n가능합니다."
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFA1ACF9)),
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

        Spacer(modifier = Modifier.weight(0.1f))
        // 버튼
        if (pagerState.currentPage == pages.size - 1) {
            Button(
                onClick = onComplete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5409DA),
                    contentColor = Color.White
                )
            ) {
                Text(text = "시작하기")
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
                )
            ) {
                Text(text = "시작하기")
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
    val description: String
)

// 개별 온보딩 페이지 UI
@Composable
fun OnboardPage(data: OnboardPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = data.image),
            contentDescription = data.title,
            modifier = Modifier.size(350.dp)
        )

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

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontSize = 32.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = data.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.White,
            fontSize = 13.sp
        )
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
