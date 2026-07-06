package com.example.stoku.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stoku.ui.auth.LoginScreen
import com.example.stoku.ui.brand.BrandListScreen
import com.example.stoku.ui.category.CategoryListScreen
import com.example.stoku.ui.dashboard.DashboardScreen
import com.example.stoku.ui.history.HistoryDetailScreen
import com.example.stoku.ui.history.HistoryScreen
import com.example.stoku.ui.kasir.KasirScreen
import com.example.stoku.ui.main.MainScaffold
import com.example.stoku.ui.manual.ManualInputScreen
import com.example.stoku.ui.manual.ManualKeluarScreen
import com.example.stoku.ui.manual.ManualMasukScreen
import com.example.stoku.ui.scan.ScanMasukScreen
import com.example.stoku.ui.settings.SettingsScreen
import com.example.stoku.ui.statistics.StatisticsScreen
import com.example.stoku.ui.stock.StockDetailScreen
import com.example.stoku.ui.stock.StockListScreen

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    val navGraphViewModel: NavGraphViewModel = hiltViewModel()
    val sessionState by navGraphViewModel.sessionState.collectAsStateWithLifecycle()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    LaunchedEffect(sessionState, currentRoute) {
        when (val state = sessionState) {
            is SessionState.Loading -> Unit
            is SessionState.LoggedOut -> {
                if (currentRoute != null && currentRoute != Routes.LOGIN) {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is SessionState.LoggedIn -> {
                if (currentRoute == null || currentRoute == Routes.LOGIN) {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                } else if (!RouteAccess.isAllowed(currentRoute, state.user.role)) {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(currentRoute) { inclusive = true }
                    }
                }
            }
        }
    }

    if (sessionState is SessionState.Loading) return

    val loggedInRole = (sessionState as? SessionState.LoggedIn)?.user?.role

    // Kasir screen is full-screen (no bottom nav) — treat like scan/manual flows
    val isFullScreenRoute = currentRoute == Routes.KASIR ||
        currentRoute == Routes.SCAN_IN ||
        currentRoute == Routes.MANUAL_INPUT ||
        currentRoute == Routes.MANUAL_IN ||
        currentRoute == Routes.MANUAL_OUT ||
        currentRoute?.startsWith(Routes.STOCK_DETAIL) == true ||
        currentRoute?.startsWith(Routes.HISTORY_DETAIL) == true

    if (loggedInRole != null && currentRoute != null && currentRoute != Routes.LOGIN && !isFullScreenRoute) {
        MainScaffold(navController = navController, currentRoute = currentRoute, role = loggedInRole) { contentModifier ->
            StokuNavHost(navController = navController, modifier = contentModifier)
        }
    } else {
        StokuNavHost(navController = navController, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun StokuNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(navController = navController, startDestination = Routes.LOGIN, modifier = modifier) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.DASHBOARD) { DashboardScreen(navController = navController) }
        composable(Routes.SCAN_IN) { ScanMasukScreen(navController = navController) }
        composable(Routes.MANUAL_INPUT) { ManualInputScreen(navController = navController) }
        composable(Routes.MANUAL_IN) { ManualMasukScreen(navController = navController) }
        composable(Routes.MANUAL_OUT) { ManualKeluarScreen(navController = navController) }
        composable(Routes.STOCK_LIST) { StockListScreen(navController = navController) }
        composable(
            route = Routes.STOCK_DETAIL_PATTERN,
            arguments = listOf(navArgument(Routes.STOCK_DETAIL_ARG_SKU) { type = NavType.StringType }),
        ) { StockDetailScreen(navController = navController) }
        composable(Routes.HISTORY) { HistoryScreen(navController = navController) }
        composable(
            route = Routes.HISTORY_DETAIL_PATTERN,
            arguments = listOf(navArgument(Routes.HISTORY_DETAIL_ARG_ID) { type = NavType.LongType }),
        ) { HistoryDetailScreen(navController = navController) }
        composable(Routes.STATISTICS) { StatisticsScreen() }
        composable(Routes.SETTINGS) { SettingsScreen(navController = navController) }
        composable(Routes.KASIR) { KasirScreen(navController = navController) }
        composable(Routes.BRAND_LIST) { BrandListScreen(navController = navController) }
        composable(Routes.CATEGORY_LIST) { CategoryListScreen(navController = navController) }
    }
}
