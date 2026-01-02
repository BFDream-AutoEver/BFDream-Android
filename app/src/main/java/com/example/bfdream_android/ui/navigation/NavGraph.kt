package com.example.bfdream_android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.bfdream_android.ui.onboarding.OnboardScreen
import com.example.bfdream_android.ui.onboarding.SplashScreen
import com.example.bfdream_android.viewmodel.OnboardingViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    // NavGraph 레벨에서 ViewModel을 생성하여 하위 컴포저블에 전달
    onboardingViewModel: OnboardingViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route,
        modifier = modifier
    ) {
        // 1. 스플래시 화면
        composable(Routes.Splash.route) {
            SplashScreen(
                // ViewModel을 SplashScreen에 전달
                viewModel = onboardingViewModel,
                onGoToOnboarding = {
                    navController.navigate(Routes.Onboard.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                },
                onGoToMain = {
                    navController.navigate(Routes.MainRoot.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. 온보딩 화면
        composable(Routes.Onboard.route) {
            OnboardScreen(
                onComplete = {
                    // 1. ViewModel을 통해 "온보딩 완료" 플래그 저장
                    onboardingViewModel.setOnboardingCompleted()

                    // 2. 메인으로 이동
                    navController.navigate(Routes.MainRoot.route) {
                        popUpTo(Routes.Onboard.route) { inclusive = true }
                    }
                }
            )
        }

        // 3. 메인 내비게이션 그래프
        navigation(
            startDestination = Routes.Main.route,
            route = Routes.MainRoot.route
        ) {
            mainNavGraph(navController)
        }
    }
}
