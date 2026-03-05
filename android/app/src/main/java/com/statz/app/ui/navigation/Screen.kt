package com.statz.app.ui.navigation

/**
 * All navigation routes in the app.
 */
sealed class Screen(val route: String) {
    // Tab roots
    data object SalesDashboard : Screen("sales_dashboard")
    data object QueriesList : Screen("queries_list")
    data object TodoList : Screen("todo_list")
    data object Settings : Screen("settings")

    // Sales sub-screens
    data object DailyEntry : Screen("daily_entry/{dateKey}") {
        fun createRoute(dateKey: String) = "daily_entry/$dateKey"
    }
    data object EditTargets : Screen("edit_targets/{monthKey}") {
        fun createRoute(monthKey: String) = "edit_targets/$monthKey"
    }

    // Query sub-screens
    data object NewQuery : Screen("new_query")
    data object QueryDetail : Screen("query_detail/{queryId}") {
        fun createRoute(queryId: String) = "query_detail/$queryId"
    }

    // Task sub-screens
    data object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
}
