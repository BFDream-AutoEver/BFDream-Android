package com.example.bfdream_android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bfdream_android.data.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface SplashDestination {
    object Loading : SplashDestination
    object Onboarding : SplashDestination
    object Main : SplashDestination
}

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    // 1. Repository 인스턴스 생성
    private val settingsRepository = SettingsRepository(application)

    // 2. 스플래시 목적지 상태 관리
    private val _splashDestination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val splashDestination = _splashDestination.asStateFlow()

    init {
        viewModelScope.launch {
            // (선택) 스플래시 화면을 최소 1~2초간 보여주기 위한 딜레이
            delay(1500)

            // 3. DataStore에서 첫 실행 여부 값을 '한번만' 가져옴
            val isFirstRun = settingsRepository.isFirstRunFlow.first()

            // 4. 값에 따라 목적지 상태 변경
            _splashDestination.value = if (isFirstRun) {
                SplashDestination.Onboarding
            } else {
                SplashDestination.Main
            }
        }
    }

    // 5. 온보딩 완료 시 호출될 함수
    fun setOnboardingCompleted() {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted()
        }
    }
}