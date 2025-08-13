package com.chimera.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.chimera.android.AppState
import com.chimera.android.ui.screens.chat.ChatScreen
import com.chimera.android.ui.screens.input.InputScreen
import com.chimera.android.ui.screens.results.ResultsScreen
import kotlinx.serialization.Serializable

/**
 * Navigation destinations for the 3-screen Chimera app
 */
@Serializable object InputRoute
@Serializable object ResultsRoute  
@Serializable object ChatRoute

@Composable
fun ChimeraNavigation(
    navController: NavHostController,
    appState: AppState,
    onAppStateChange: (AppState) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = InputRoute,
        modifier = modifier
    ) {
        composable<InputRoute> {
            InputScreen(
                appState = appState,
                onAppStateChange = onAppStateChange,
                onNavigateToResults = {
                    navController.navigate(ResultsRoute) {
                        // Don't clear input screen from back stack
                        launchSingleTop = true
                    }
                }
            )
        }
        
        composable<ResultsRoute> {
            ResultsScreen(
                appState = appState,
                onAppStateChange = onAppStateChange,
                onNavigateToChat = { assetId ->
                    navController.navigate(ChatRoute) {
                        launchSingleTop = true
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable<ChatRoute> {
            ChatScreen(
                appState = appState,
                onAppStateChange = onAppStateChange,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}