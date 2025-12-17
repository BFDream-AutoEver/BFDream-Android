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

    // Preference Keys
    private val IS_FIRST_RUN_KEY = booleanPreferencesKey("is_first_run")
    private val IS_SOUND_ON_KEY = booleanPreferencesKey("is_sound_on") // [추가] 사운드 설정 키

    // --- 첫 실행 여부 ---
    val isFirstRunFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_FIRST_RUN_KEY] ?: true
        }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { settings ->
            settings[IS_FIRST_RUN_KEY] = false
        }
    }

    // --- [추가] 사운드 설정 관리 ---
    // 기본값은 true (소리 켜짐)
    val isSoundOnFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_SOUND_ON_KEY] ?: true
        }

    suspend fun setSoundOn(isEnabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[IS_SOUND_ON_KEY] = isEnabled
        }
    }
}