package com.example.bfdream_android.ui.navigation

sealed class Routes(val route: String) {
    // 1. 최상위 경로
    data object Splash : Routes("splash")
    data object Onboard : Routes("onboard")
    data object MainRoot : Routes("main_root") // 메인 화면, 도움말, 내 정보를 포함하는 그래프

    // 2. MainRoot 그래프 내부 경로
    data object Main : Routes("main")
    data object Help : Routes("help")
    data object Info : Routes("info")
}
