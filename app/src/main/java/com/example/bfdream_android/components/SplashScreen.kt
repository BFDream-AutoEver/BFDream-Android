package com.example.bfdream_android.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bfdream_android.R
import com.example.bfdream_android.ui.theme.BFDreamAndroidTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onGoToOnboarding: () -> Unit,
    onGoToMain: () -> Unit,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    LaunchedEffect(key1 = Unit) {
        delay(2000) // 2초간 스플래시 노출

        // --- 첫 실행 여부 판단 로직 ---
        // 실제 앱에서는 DataStore 또는 ViewModel에서
        // 비동기적으로 이 값을 가져와야 합니다.
        val isFirstLaunch = true // (임시) 항상 true로 설정하여 온보딩으로 이동

        if (isFirstLaunch) {
            onGoToOnboarding()
        } else {
            onGoToMain()
        }
    }

    Box(
        modifier = modifier.background(Color(0xFFA1ACF9)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash),
            contentDescription = "맘편한 이동, 예비 엄마의 마음 편한 이동",
            modifier = Modifier.size(250.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashPreview() {
    BFDreamAndroidTheme {
        SplashScreen(
            onGoToOnboarding = {},
            onGoToMain = {}
        )
    }
}
