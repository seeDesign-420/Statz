package com.statz.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CrisisAlert
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.statz.app.domain.model.QueryUrgency
import com.statz.app.ui.theme.*
import com.statz.app.ui.viewmodel.QueriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewQueryScreen(
    navController: NavController,
    viewModel: QueriesViewModel = hiltViewModel()
) {
    val state by viewModel.newQueryState.collectAsStateWithLifecycle()

    LaunchedEffect(state.createdId) {
        if (state.createdId != null) {
            viewModel.resetNewQuery()
            navController.popBackStack()
        }
    }

    Scaffold(
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Floating header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    viewModel.resetNewQuery()
                    navController.popBackStack()
                }) {
                    Icon(Icons.Default.Close, "Close")
                }
                Text(
                    "New Query",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            // Ticket Number
            com.statz.app.ui.components.StatzGlassTextField(
                value = state.ticketNumber,
                onValueChange = { viewModel.updateNewQuery(ticketNumber = it) },
                label = "Ticket Number",
                placeholder = "e.g. QRY-4521",
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Customer ID
            com.statz.app.ui.components.StatzGlassTextField(
                value = state.customerId,
                onValueChange = { viewModel.updateNewQuery(customerId = it) },
                label = "Customer ID (Optional)",
                placeholder = "e.g. CUST-1234",
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Customer Name
            com.statz.app.ui.components.StatzGlassTextField(
                value = state.customerName,
                onValueChange = { viewModel.updateNewQuery(customerName = it) },
                label = "Customer Name *",
                placeholder = "John Doe",
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Urgency Selector
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Urgency",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(QueryUrgency.entries.toList()) { urgency ->
                        val color = when (urgency) {
                            QueryUrgency.LOW -> UrgencyLow
                            QueryUrgency.MEDIUM -> UrgencyMedium
                            QueryUrgency.HIGH -> UrgencyHigh
                            QueryUrgency.CRITICAL -> UrgencyCritical
                            QueryUrgency.CUSTOM -> UrgencyCustom
                        }
                        val icon = when (urgency) {
                            QueryUrgency.LOW -> Icons.Outlined.Spa
                            QueryUrgency.MEDIUM -> Icons.Outlined.Schedule
                            QueryUrgency.HIGH -> Icons.Outlined.LocalFireDepartment
                            QueryUrgency.CRITICAL -> Icons.Outlined.CrisisAlert
                            QueryUrgency.CUSTOM -> Icons.Outlined.Tune
                        }
                        val label = when (urgency) {
                            QueryUrgency.LOW -> "Low"
                            QueryUrgency.MEDIUM -> "Med"
                            QueryUrgency.HIGH -> "High"
                            QueryUrgency.CRITICAL -> "Crit"
                            QueryUrgency.CUSTOM -> "Custom"
                        }
                        com.statz.app.ui.components.SelectionChipButton(
                            icon = icon,
                            label = label,
                            color = color,
                            isSelected = state.urgency == urgency,
                            onClick = { viewModel.updateNewQuery(urgency = urgency) }
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Show interval hint with colored dot
                val dotColor = when (state.urgency) {
                    QueryUrgency.CRITICAL -> UrgencyCritical
                    QueryUrgency.HIGH -> UrgencyHigh
                    QueryUrgency.MEDIUM -> UrgencyMedium
                    QueryUrgency.LOW -> UrgencyLow
                    QueryUrgency.CUSTOM -> UrgencyCustom
                }
                val intervalText = when (state.urgency) {
                    QueryUrgency.CRITICAL -> "Every 6 hours"
                    QueryUrgency.HIGH -> "Every 12 hours"
                    QueryUrgency.MEDIUM -> "Every 24 hours"
                    QueryUrgency.LOW -> "Every 48 hours"
                    QueryUrgency.CUSTOM -> "Every ${state.customFollowUpHours.ifEmpty { "?" }} hours"
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.size(8.dp)
                    ) {
                        drawCircle(color = dotColor)
                    }
                    Spacer(Modifier.size(8.dp))
                    Text(
                        intervalText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Custom hours input
                if (state.urgency == QueryUrgency.CUSTOM) {
                    com.statz.app.ui.components.StatzGlassTextField(
                        value = state.customFollowUpHours,
                        onValueChange = { value ->
                            if (value.all { it.isDigit() }) {
                                viewModel.updateNewQuery(customFollowUpHours = value)
                            }
                        },
                        label = "Follow-up interval (hours)",
                        placeholder = "e.g. 8",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Initial Note
            com.statz.app.ui.components.StatzGlassTextField(
                value = state.initialNote,
                onValueChange = { viewModel.updateNewQuery(initialNote = it) },
                label = "Initial Note",
                placeholder = "Describe the issue...",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp),
                singleLine = false,
                maxLines = 5
            )

            Spacer(Modifier.height(16.dp))

            val backdrop = com.statz.app.ui.components.LocalBackdrop.current
            if (backdrop != null) {
                com.statz.app.ui.components.StatzLiquidButton(
                    onClick = { viewModel.createQuery() },
                    backdrop = backdrop,
                    modifier = Modifier
                        .fillMaxWidth(),
                    tint = MaterialTheme.colorScheme.primary,
                    buttonHeight = 56.dp
                ) {
                    Text("Create Query", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

