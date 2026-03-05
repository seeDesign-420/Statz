package com.statz.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import com.statz.app.ui.theme.StatzAnimation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.statz.app.ui.components.GlassNavItem
import com.statz.app.ui.components.LocalBackdrop
import com.statz.app.ui.screens.DailyEntryScreen
import com.statz.app.ui.screens.EditTargetsScreen
import com.statz.app.ui.screens.NewQueryScreen
import com.statz.app.ui.screens.QueriesListScreen
import com.statz.app.ui.screens.QueryDetailScreen
import com.statz.app.ui.screens.SalesDashboardScreen
import com.statz.app.ui.screens.SettingsScreen
import com.statz.app.ui.screens.TaskDetailScreen
import com.statz.app.ui.screens.TodoListScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Sales Stats", Screen.SalesDashboard.route, Icons.Filled.BarChart, Icons.Outlined.BarChart),
    BottomNavItem("Queries", Screen.QueriesList.route, Icons.Filled.HeadsetMic, Icons.Outlined.HeadsetMic),
    BottomNavItem("To-Do", Screen.QueriesList.route.let { Screen.TodoList.route }, Icons.Filled.Checklist, Icons.Outlined.Checklist),
    BottomNavItem("Settings", Screen.Settings.route, Icons.Filled.Settings, Icons.Outlined.Settings)
)

private val tabRoutes = bottomNavItems.map { it.route }

// Spring constants are centralized in StatzAnimation

@Composable
fun StatzNavHost() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val backgroundColor = com.statz.app.ui.theme.DarkBackground

    // ── Pager state for tab-root swiping ─────────────────────────────
    val pagerState = rememberPagerState(initialPage = 0) { bottomNavItems.size }

    // Track whether we're on a sub-screen (to disable pager swiping)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isOnTabRoot = currentRoute in tabRoutes

    // Sync pager → NavHost: when user swipes, navigate NavHost to match
    LaunchedEffect(Unit) {
        snapshotFlow { pagerState.currentPage }.collectLatest { page ->
            val targetRoute = bottomNavItems[page].route
            if (navController.currentDestination?.route != targetRoute) {
                navController.navigate(targetRoute) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    // Outer backdrop: captures EVERYTHING (background + screen content) for nav bar live blur.
    val navBackdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    val dialogConfigState = remember { mutableStateOf<com.statz.app.ui.components.DialogConfig?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Sibling 1: Pager content — captured by navBackdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(navBackdrop)
        ) {
            com.statz.app.ui.components.StatzGlassBackground {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1,
                    userScrollEnabled = isOnTabRoot,
                    flingBehavior = PagerDefaults.flingBehavior(
                        state = pagerState,
                        snapAnimationSpec = spring(
                            dampingRatio = 0.85f,
                            stiffness = 400f
                        ),
                        pagerSnapDistance = PagerSnapDistance.atMost(1)
                    ),
                    key = { it }
                ) { page ->
                    when (page) {
                        0 -> SalesDashboardScreen(navController = navController)
                        1 -> QueriesListScreen(navController = navController)
                        2 -> TodoListScreen(navController = navController)
                        3 -> SettingsScreen(navController = navController)
                    }
                }
            }
        }

        // Sibling 2: NavHost for sub-screen push navigation
        // Sits OUTSIDE layerBackdrop so sub-screens can use navBackdrop for live blur (same as dialog).
        CompositionLocalProvider(
            com.statz.app.ui.components.LocalDialogHost provides { config ->
                dialogConfigState.value = config
            }
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.SalesDashboard.route,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        spring(
                            dampingRatio = 0.85f,
                            stiffness = 400f
                        )
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        spring(
                            dampingRatio = 0.85f,
                            stiffness = 400f
                        )
                    )
                },
                popEnterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        spring(
                            dampingRatio = 0.85f,
                            stiffness = 400f
                        )
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        spring(
                            dampingRatio = 0.85f,
                            stiffness = 400f
                        )
                    )
                }
            ) {
                // Tab roots: empty — rendered by HorizontalPager in sibling 1
                composable(Screen.SalesDashboard.route) { }
                composable(Screen.QueriesList.route) { }
                composable(Screen.TodoList.route) { }
                composable(Screen.Settings.route) { }

                // ── Sales sub-screens ────────────────────────
                composable(
                    Screen.DailyEntry.route,
                    arguments = listOf(navArgument("dateKey") { type = NavType.StringType })
                ) { backStackEntry ->
                    com.statz.app.ui.components.GlassScreenBackground(backdrop = navBackdrop) {
                        val dateKey = backStackEntry.arguments?.getString("dateKey") ?: ""
                        DailyEntryScreen(dateKey = dateKey, navController = navController)
                    }
                }
                composable(
                    Screen.EditTargets.route,
                    arguments = listOf(navArgument("monthKey") { type = NavType.StringType })
                ) { backStackEntry ->
                    com.statz.app.ui.components.GlassScreenBackground(backdrop = navBackdrop) {
                        val monthKey = backStackEntry.arguments?.getString("monthKey") ?: ""
                        EditTargetsScreen(monthKey = monthKey, navController = navController)
                    }
                }

                // ── Query sub-screens ────────────────────────
                composable(Screen.NewQuery.route) {
                    com.statz.app.ui.components.GlassScreenBackground(backdrop = navBackdrop) {
                        NewQueryScreen(navController = navController)
                    }
                }
                composable(
                    Screen.QueryDetail.route,
                    arguments = listOf(navArgument("queryId") { type = NavType.StringType })
                ) { backStackEntry ->
                    com.statz.app.ui.components.GlassScreenBackground(backdrop = navBackdrop) {
                        val queryId = backStackEntry.arguments?.getString("queryId") ?: ""
                        QueryDetailScreen(queryId = queryId, navController = navController)
                    }
                }

                // ── Task sub-screens ─────────────────────────
                composable(
                    Screen.TaskDetail.route,
                    arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                ) { backStackEntry ->
                    com.statz.app.ui.components.GlassScreenBackground(backdrop = navBackdrop) {
                        val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                        TaskDetailScreen(taskId = taskId, navController = navController)
                    }
                }
            }
        }

        // Sibling 2: Glass nav bar — uses navBackdrop to blur live screen content
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            StatzBottomNav(
                navController = navController,
                backdrop = navBackdrop,
                pagerCurrentPage = pagerState.currentPage,
                onTabSelected = { index ->
                    scope.launch {
                        pagerState.animateScrollToPage(
                            page = index,
                            animationSpec = spring(
                                dampingRatio = 0.85f,
                                stiffness = 400f
                            )
                        )
                    }
                }
            )
        }

        // Sibling 3: Glass dialog overlay — uses navBackdrop to blur live content
        dialogConfigState.value?.let { config ->
            com.statz.app.ui.components.StatzGlassDialogOverlay(
                config = config,
                backdrop = navBackdrop,
                onDismiss = {
                    config.onDismiss()
                    dialogConfigState.value = null
                }
            )
        }
    }
}

@Composable
private fun StatzBottomNav(
    navController: NavHostController,
    backdrop: LayerBackdrop,
    pagerCurrentPage: Int,
    onTabSelected: (Int) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Only show bottom nav on tab root screens
    val showBottomBar = currentDestination?.route in tabRoutes

    if (!showBottomBar) return

    com.statz.app.ui.components.GlassNavBar(
        backdrop = backdrop,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        bottomNavItems.forEachIndexed { index, item ->
            val selected = index == pagerCurrentPage
            GlassNavItem(
                selected = selected,
                onClick = { onTabSelected(index) },
                icon = if (selected) item.selectedIcon else item.unselectedIcon,
                label = item.label
            )
        }
    }
}
