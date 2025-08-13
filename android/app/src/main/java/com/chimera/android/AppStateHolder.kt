package com.chimera.android

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStateHolder @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()
    
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    companion object {
        private val DISCLAIMER_ACKNOWLEDGED = booleanPreferencesKey("disclaimer_acknowledged")
        private val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        private val IS_HIGH_CONTRAST = booleanPreferencesKey("is_high_contrast")
        private val TEXT_SCALE_FACTOR = floatPreferencesKey("text_scale_factor")
        private val HAS_SEEN_INTRO = booleanPreferencesKey("has_seen_intro")
        private val DATA_FRESHNESS = stringPreferencesKey("data_freshness")
        private val LAST_UPDATE_TIME = stringPreferencesKey("last_update_time")
    }

    init {
        loadAppState()
    }

    private fun loadAppState() {
        applicationScope.launch {
            try {
                val preferences = dataStore.data.first()
                
                val savedState = AppState(
                    isInitialized = true,
                    disclaimerAcknowledged = preferences[DISCLAIMER_ACKNOWLEDGED] ?: false,
                    isDarkTheme = preferences[IS_DARK_THEME] ?: false,
                    isHighContrastMode = preferences[IS_HIGH_CONTRAST] ?: false,
                    textScaleFactor = preferences[TEXT_SCALE_FACTOR] ?: 1.0f,
                    hasSeenIntro = preferences[HAS_SEEN_INTRO] ?: false,
                    dataFreshness = preferences[DATA_FRESHNESS] ?: "Unknown",
                    lastUpdateTime = preferences[LAST_UPDATE_TIME] ?: ""
                )
                
                _appState.value = savedState
                _isInitialized.value = true
                
                Timber.d("App state loaded: $savedState")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load app state")
                _isInitialized.value = true
            }
        }
    }

    fun updateAppState(newState: AppState) {
        _appState.value = newState
        saveAppState(newState)
    }

    fun acknowledgeDisclaimer() {
        updateAppState(_appState.value.copy(disclaimerAcknowledged = true))
    }

    fun setDarkTheme(isDark: Boolean) {
        updateAppState(_appState.value.copy(isDarkTheme = isDark))
    }

    fun setHighContrast(isHighContrast: Boolean) {
        updateAppState(_appState.value.copy(isHighContrastMode = isHighContrast))
    }

    fun setTextScaleFactor(scaleFactor: Float) {
        updateAppState(_appState.value.copy(textScaleFactor = scaleFactor))
    }

    fun markIntroSeen() {
        updateAppState(_appState.value.copy(hasSeenIntro = true))
    }

    fun updateDataFreshness(freshness: String, lastUpdate: String) {
        updateAppState(_appState.value.copy(
            dataFreshness = freshness,
            lastUpdateTime = lastUpdate
        ))
    }

    fun setOnlineStatus(isOnline: Boolean) {
        _appState.value = _appState.value.copy(isOnline = isOnline)
        // Don't persist online status as it's runtime state
    }

    private fun saveAppState(state: AppState) {
        applicationScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[DISCLAIMER_ACKNOWLEDGED] = state.disclaimerAcknowledged
                    preferences[IS_DARK_THEME] = state.isDarkTheme
                    preferences[IS_HIGH_CONTRAST] = state.isHighContrastMode
                    preferences[TEXT_SCALE_FACTOR] = state.textScaleFactor
                    preferences[HAS_SEEN_INTRO] = state.hasSeenIntro
                    preferences[DATA_FRESHNESS] = state.dataFreshness
                    preferences[LAST_UPDATE_TIME] = state.lastUpdateTime
                }
                Timber.d("App state saved")
            } catch (e: Exception) {
                Timber.e(e, "Failed to save app state")
            }
        }
    }
}