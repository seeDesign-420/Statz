package com.statz.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.statz.app.domain.model.CategoryType
import com.statz.app.ui.components.CategoryProgressRow
import com.statz.app.ui.components.KpiCard
import com.statz.app.ui.navigation.Screen
import com.statz.app.ui.viewmodel.SalesViewModel
import androidx.compose.ui.graphics.Color
import com.statz.app.domain.model.MoneyUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import com.statz.app.ui.theme.DarkSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesDashboardScreen(
    navController: NavController,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val state by viewModel.salesState.collectAsStateWithLifecycle()

    Scaffold(
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val dashboard = state.dashboard ?: return@Scaffold
        val backdrop = com.statz.app.ui.components.LocalBackdrop.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Floating Top Bar (Month Navigation)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Column {
                        Text("Current Month", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(state.monthDisplay.ifEmpty { "Loading..." }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                }
                if (backdrop != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        com.statz.app.ui.components.StatzLiquidButton(
                            onClick = { viewModel.previousMonth() },
                            backdrop = backdrop,
                            modifier = Modifier.size(44.dp),
                            buttonHeight = 44.dp,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, "Previous month", modifier = Modifier.size(24.dp), tint = Color.White)
                        }
                        com.statz.app.ui.components.StatzLiquidButton(
                            onClick = { viewModel.nextMonth() },
                            backdrop = backdrop,
                            modifier = Modifier.size(44.dp),
                            buttonHeight = 44.dp,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, "Next month", modifier = Modifier.size(24.dp), tint = Color.White)
                        }
                    }
                }
            }

            // Total Revenue KPI
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "TOTAL REVENUE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp
                )
                Text(
                    text = MoneyUtils.centsToDisplay(dashboard.totalRevenue),
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Main Action Buttons
            if (backdrop != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    com.statz.app.ui.components.StatzLiquidButton(
                        onClick = { navController.navigate(Screen.DailyEntry.createRoute(viewModel.todayDateKey())) },
                        backdrop = backdrop,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                        Text("New entry", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    com.statz.app.ui.components.StatzLiquidButton(
                        onClick = { navController.navigate(Screen.EditTargets.createRoute(state.monthKey)) },
                        backdrop = backdrop,
                        tint = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Text("Targets", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // New Lines vs Upgrades Summary
            val newLineIds = setOf("new", "sme_new", "ec_new")
            val upgradeIds = setOf("upgrade", "sme_up", "ec_upgd")
            val totalNewLines = dashboard.categories
                .filter { it.category.id in newLineIds }
                .sumOf { it.actual }
            val totalNewLinesTarget = dashboard.categories
                .filter { it.category.id in newLineIds }
                .sumOf { it.target }
            val totalUpgrades = dashboard.categories
                .filter { it.category.id in upgradeIds }
                .sumOf { it.actual }
            val totalUpgradesTarget = dashboard.categories
                .filter { it.category.id in upgradeIds }
                .sumOf { it.target }

            com.statz.app.ui.components.StatzGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                com.statz.app.ui.components.UnitSplitDonut(
                    newLines = totalNewLines,
                    upgrades = totalUpgrades,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Unit Categories Performance Section
            com.statz.app.ui.components.CategoryBarChart(
                dashboard = dashboard,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(80.dp))
        }
    }
}
