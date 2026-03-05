package com.statz.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.statz.app.data.local.model.QueryItemEntity
import com.statz.app.domain.model.QueryStatus
import com.statz.app.domain.model.QueryUrgency
import com.statz.app.ui.components.StatusChip
import com.statz.app.ui.components.UrgencyBadge
import com.statz.app.ui.navigation.Screen
import com.statz.app.ui.theme.*
import com.statz.app.ui.viewmodel.QueriesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueriesListScreen(
    navController: NavController,
    viewModel: QueriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val queries by viewModel.queries.collectAsStateWithLifecycle()

    val filters = listOf(
        "All" to null,
        "Open" to QueryStatus.OPEN,
        "Follow-Up" to QueryStatus.FOLLOW_UP,
        "Escalated" to QueryStatus.ESCALATED,
        "Closed" to QueryStatus.CLOSED
    )

    Scaffold(
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        floatingActionButton = {
            val backdrop = com.statz.app.ui.components.LocalBackdrop.current
            if (backdrop != null) {
                com.statz.app.ui.components.StatzLiquidFab(
                    onClick = { navController.navigate(Screen.NewQuery.route) },
                    backdrop = backdrop,
                    icon = Icons.Default.Add,
                    contentDescription = "New Query",
                    modifier = Modifier.padding(bottom = 84.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Title
            Text(
                text = "Queries",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
            )

            // Search Bar
            com.statz.app.ui.components.StatzSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Pill Filter Tabs
            com.statz.app.ui.components.StatzPillTabRow {
                items(filters) { (label, status) ->
                    com.statz.app.ui.components.StatzPillTab(
                        selected = uiState.activeFilter == status,
                        onClick = { viewModel.setFilter(status) },
                        label = label
                    )
                }
            }

            // Query list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(queries, key = { it.id }) { query ->
                    QueryCard(
                        query = query,
                        onClick = { navController.navigate(Screen.QueryDetail.createRoute(query.id)) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun QueryCard(query: QueryItemEntity, onClick: () -> Unit) {
    val statusColor = when (query.status) {
        QueryStatus.OPEN -> StatusOpen
        QueryStatus.FOLLOW_UP -> StatusFollowUp
        QueryStatus.ESCALATED -> StatusEscalated
        QueryStatus.CLOSED -> StatusClosed
    }
    
    val urgencyColor = when (query.urgency) {
        QueryUrgency.LOW -> UrgencyLow
        QueryUrgency.MEDIUM -> UrgencyMedium
        QueryUrgency.HIGH -> UrgencyHigh
        QueryUrgency.CRITICAL -> UrgencyCritical
        QueryUrgency.CUSTOM -> UrgencyCustom
    }

    val glowColor = when (query.urgency) {
        QueryUrgency.LOW -> UrgencyLowGlow
        QueryUrgency.MEDIUM -> UrgencyMediumGlow
        QueryUrgency.HIGH -> UrgencyHighGlow
        QueryUrgency.CRITICAL -> UrgencyCriticalGlow
        QueryUrgency.CUSTOM -> UrgencyCustomGlow
    }

    val glowRadius = when (query.urgency) {
        QueryUrgency.LOW -> 3.dp
        QueryUrgency.MEDIUM -> 5.dp
        QueryUrgency.HIGH -> 6.dp
        QueryUrgency.CRITICAL -> 8.dp
        QueryUrgency.CUSTOM -> 5.dp
    }

    com.statz.app.ui.components.StatzGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        tintColor = urgencyColor,
        glowColor = glowColor,
        glowRadius = glowRadius
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = query.ticketNumber.ifEmpty { "#${query.id.take(8)}" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                    Text(
                        text = query.customerName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (query.status != QueryStatus.CLOSED) {
                        val sdf = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
                        val isOverdue = query.nextFollowUpAt <= System.currentTimeMillis()
                        Text(
                            text = if (isOverdue) "Overdue" else "Next: ${sdf.format(Date(query.nextFollowUpAt))}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = if (isOverdue) Error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Right aligned status pill
                    Row(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = query.status.name.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View Details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
