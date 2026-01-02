package com.example.bfdream_android.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.bfdream_android.ui.help.HelpScreen
import com.example.bfdream_android.ui.info.InfoScreen
import com.example.bfdream_android.ui.main.MainScreen

fun NavGraphBuilder.mainNavGraph(navController: NavController) {
    // 메인 화면
    composable(Routes.Main.route) {
        MainScreen(
            onNavigateToHelp = { navController.navigate(Routes.Help.route) },
            onNavigateToProfile = { navController.navigate(Routes.Info.route) }
        )
    }

    // 도움말 화면 (추후 개발)
    composable(Routes.Help.route) {
        HelpScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // 내 정보 화면 (추후 개발)
    composable(Routes.Info.route) {
        InfoScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
