package jp.co.kirokuai.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import jp.co.kirokuai.app.ui.history.HistoryScreen
import jp.co.kirokuai.app.ui.home.HomeScreen
import jp.co.kirokuai.app.ui.settings.SettingsScreen

private const val HOME_ROUTE = "home"
private const val HISTORY_ROUTE = "history"
private const val SETTINGS_ROUTE = "settings"

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE,
    ) {
        composable(HOME_ROUTE) {
            HomeScreen(
                onHistoryClick = { navController.navigate(HISTORY_ROUTE) },
                onSettingsClick = { navController.navigate(SETTINGS_ROUTE) },
            )
        }
        composable(HISTORY_ROUTE) {
            HistoryScreen()
        }
        composable(SETTINGS_ROUTE) {
            SettingsScreen()
        }
    }
}
