package com.example.bfdream_android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.bfdream_android.components.OnboardScreen
import com.example.bfdream_android.components.SplashScreen
import com.example.bfdream_android.model.Routes

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    // 실제 앱에서는 DataStore/ViewModel을 통해 첫 실행 여부를 주입받아야 합니다.
    // 여기서는 Splash에서 자체적으로 로직을 처리하도록 합니다.
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route,
        modifier = modifier
    ) {
        // 1. 스플래시 화면
        composable(Routes.Splash.route) {
            SplashScreen(
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
                    // 실제 앱에서는 여기서 DataStore에 온보딩 완료 저장
                    navController.navigate(Routes.MainRoot.route) {
                        popUpTo(Routes.Onboard.route) { inclusive = true }
                    }
                }
            )
        }

        // 3. 메인 내비게이션 그래프 (메인, 도움말, 내 정보)
        navigation(
            startDestination = Routes.Main.route,
            route = Routes.MainRoot.route
        ) {
            mainNavGraph(navController)
        }
    }
}
