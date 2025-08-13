package com.chimera.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.chimera.android.ui.navigation.ChimeraNavigation
import com.chimera.android.ui.theme.ChimeraTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appStateHolder: AppStateHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        // Keep splash screen on screen while app initializes
        splashScreen.setKeepOnScreenCondition {
            !appStateHolder.isInitialized.value
        }

        setContent {
            val appState by appStateHolder.appState.collectAsStateWithLifecycle()
            
            ChimeraTheme {
                ChimeraApp(
                    appState = appState,
                    onAppStateChange = appStateHolder::updateAppState
                )
            }
        }
        
        Timber.d("MainActivity created")
    }
}

@Composable
fun ChimeraApp(
    appState: AppState,
    onAppStateChange: (AppState) -> Unit
) {
    val navController = rememberNavController()
    
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        ChimeraNavigation(
            navController = navController,
            appState = appState,
            onAppStateChange = onAppStateChange,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChimeraAppPreview() {
    ChimeraTheme {
        ChimeraApp(
            appState = AppState(),
            onAppStateChange = { }
        )
    }
}