package com.example.bfdream_android.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1. DataStore 인스턴스 생성 (Context의 확장 프로퍼티)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    // 2. Preference Key 정의 (이 키로 값을 저장/로드)
    private val IS_FIRST_RUN_KEY = booleanPreferencesKey("is_first_run")

    // 3. 첫 실행 여부 Flow (기본값 true)
    // 앱이 처음 실행되면 저장된 값이 없으므로 기본값(true)을 반환합니다.
    val isFirstRunFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_FIRST_RUN_KEY] ?: true
        }

    // 4. 온보딩 완료 시 호출할 함수 (false로 저장)
    // 이 함수가 호출되면, 다음부터 isFirstRunFlow는 false를 방출합니다.
    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { settings ->
            settings[IS_FIRST_RUN_KEY] = false
        }
    }
}