package com.example.bfdream_android.components

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bfdream_android.R
import com.example.bfdream_android.ui.theme.BFDreamAndroidTheme
import com.example.bfdream_android.viewmodel.OnboardingViewModel
import com.example.bfdream_android.viewmodel.SplashDestination

@Composable
fun SplashScreen(
    viewModel: OnboardingViewModel,
    onGoToOnboarding: () -> Unit,
    onGoToMain: () -> Unit,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    // 1. ViewModel의 목적지 상태를 구독
    val destination by viewModel.splashDestination.collectAsState()

    // 2. destination이 변경되면(Loading -> Onboarding/Main) 내비게이션 람다 호출
    LaunchedEffect(destination) {
        when (destination) {
            SplashDestination.Onboarding -> onGoToOnboarding()
            SplashDestination.Main -> onGoToMain()
            SplashDestination.Loading -> { /* 로딩 중... UI는 아래에서 표시 */ }
        }
    }

    // 3. 스플래시 UI
    Box(
        modifier = modifier.background(Color(0xFFA1ACF9)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash),
            contentDescription = "맘편한 이동, 예비 엄마의 마음 편한 이동",
            modifier = Modifier.size(280.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashPreview() {
    BFDreamAndroidTheme {
        SplashScreen(
            viewModel = OnboardingViewModel(Application()),
            onGoToOnboarding = {},
            onGoToMain = {},
        )
    }
}
