package com.schulte.grid.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.schulte.grid.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsStore by preferencesDataStore(name = "schulte_settings")

/**
 * 用户设置仓库 —— 持久化游戏设置
 */
class SettingsRepository(private val context: Context) {

    companion object {
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_SOUND = booleanPreferencesKey("sound")
        private val KEY_SHOW_TIMER = booleanPreferencesKey("show_timer")
        private val KEY_COUNTDOWN = booleanPreferencesKey("countdown")
        private val KEY_REVERSE = booleanPreferencesKey("reverse")
    }

    /** 以 Flow 形式暴露当前设置 */
    val settingsFlow: Flow<AppSettings> =
        context.settingsStore.data.map { prefs ->
            AppSettings(
                darkMode = prefs[KEY_DARK_MODE] ?: false,
                soundEnabled = prefs[KEY_SOUND] ?: true,
                showTimer = prefs[KEY_SHOW_TIMER] ?: true,
                showCountdown = prefs[KEY_COUNTDOWN] ?: false,
                reverseMode = prefs[KEY_REVERSE] ?: false,
            )
        }

    /** 一次性读取当前设置 */
    suspend fun getSettings(): AppSettings = settingsFlow.first()

    suspend fun updateSettings(settings: AppSettings) {
        context.settingsStore.edit { prefs ->
            prefs[KEY_DARK_MODE] = settings.darkMode
            prefs[KEY_SOUND] = settings.soundEnabled
            prefs[KEY_SHOW_TIMER] = settings.showTimer
            prefs[KEY_COUNTDOWN] = settings.showCountdown
            prefs[KEY_REVERSE] = settings.reverseMode
        }
    }
}
