package com.example.bfdream_android.ui.onboarding

import android.Manifest
import android.os.Build
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bfdream_android.R
import com.example.bfdream_android.ui.onboarding.components.OnboardPage
import com.example.bfdream_android.ui.onboarding.components.OnboardPageData
import com.example.bfdream_android.ui.onboarding.components.PageIndicator
import com.example.bfdream_android.ui.theme.BFDreamAndroidTheme
import com.example.bfdream_android.ui.theme.bt_RoyalViolet
import com.example.bfdream_android.ui.theme.pr_LavenderPurple
import com.example.bfdream_android.ui.theme.pr_White
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardScreen(
    onComplete: () -> Unit
) {
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val buttonSize = screenHeight * 0.2f

    val pages = listOf(
        OnboardPageData(
            1,
            R.drawable.onboard1,
            stringResource(R.string.onboard_1_title),
            stringResource(R.string.onboard_1_highlight),
            0xFF5409DA, // bt_RoyalViolet
            stringResource(R.string.onboard_1_desc),
        ),
        OnboardPageData(
            2,
            R.drawable.onboard2,
            stringResource(R.string.onboard_2_title),
            stringResource(R.string.onboard_2_highlight),
            0xFF3F72AF, // se_OceanBlue
            stringResource(R.string.onboard_2_desc),
        ),
        OnboardPageData(
            3,
            R.drawable.onboard3,
            stringResource(R.string.onboard_3_title),
            stringResource(R.string.onboard_3_highlight),
            0xFF4E71FF, // pr_PeriwinkleBlue
            stringResource(R.string.onboard_3_desc),
        ),
        OnboardPageData(
            4,
            R.drawable.onboard4,
            stringResource(R.string.onboard_4_title),
            stringResource(R.string.onboard_4_highlight),
            0xFFFFC0CB,
            stringResource(R.string.onboard_4_desc),
        ),
        OnboardPageData(
            5,
            R.drawable.info_logo,
            stringResource(R.string.onboard_permission_title),
            stringResource(R.string.onboard_permission_highlight),
            0xFF5409DA, // bt_RoyalViolet
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
                Toast.makeText(
                    context,
                    context.getString(R.string.msg_permission_granted),
                    Toast.LENGTH_SHORT).show()
                onComplete()
            } else {
                // 권한이 하나라도 거부되었을 때
                // 사용자에게 권한이 꼭 필요하다고 알려주는 것이 좋습니다. (예: Toast, Dialog)
                Toast.makeText(
                    context,
                    context.getString(R.string.msg_permission_denied),
                    Toast.LENGTH_LONG).show()
                // (참고) 여기서 바로 onComplete()를 호출하면 권한 없이 메인으로 넘어갑니다.
                // onComplete()
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pr_LavenderPurple)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            OnboardPage(
                data = pages[pageIndex],
                isSelected = (pagerState.currentPage == pageIndex)
            )
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
                onClick = {
                    view.performHapticFeedback(
                        HapticFeedbackConstants.CLOCK_TICK,
                        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                    )
                    permissionLauncher.launch(permissionsToRequest)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = bt_RoyalViolet,
                    contentColor = pr_White,
                ),
                modifier = Modifier.width(buttonSize).height(60.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_start),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        } else {
            Button(
                onClick = {
                    view.performHapticFeedback(
                        HapticFeedbackConstants.CLOCK_TICK,
                        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                    )
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = bt_RoyalViolet.copy(alpha = 0.6f),
                    contentColor = pr_White,
                ),
                modifier = Modifier.width(buttonSize).height(60.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_next),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.1f))
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
