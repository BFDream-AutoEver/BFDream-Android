package com.example.bfdream_android.model

sealed class Routes(
    val route: String,
    val isRoot: Boolean = false,
) {
    data object Splash: Routes("Splash")
    data object Onboard: Routes("Onboard")
    data object Main: Routes("Main", true)

    companion object {
        fun getRoutes(route: String): Routes {
            return when {
                route == Splash.route -> Splash
                route == Onboard.route -> Onboard
                else -> Main
            }
        }
    }
}