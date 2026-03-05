package com.statz.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.CrisisAlert
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.statz.app.domain.model.QueryUrgency
import com.statz.app.ui.theme.*
import com.statz.app.ui.viewmodel.TasksViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    navController: NavController,
    viewModel: TasksViewModel = hiltViewModel()
) {
    val state by viewModel.detailState.collectAsStateWithLifecycle()

    LaunchedEffect(taskId) { viewModel.loadTaskDetail(taskId) }

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

        val task = state.task ?: return@Scaffold
        var editedTitle by remember(task.id) { mutableStateOf(task.title) }
        var editedNotes by remember(task.id) { mutableStateOf(task.notes ?: "") }
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Floating header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                        Text(
                            "Task Detail",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    IconButton(onClick = {
                        viewModel.deleteTask(taskId)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Delete, "Delete", tint = Error)
                    }
                }
                // Editable title
                com.statz.app.ui.components.StatzGlassTextField(
                    value = editedTitle,
                    onValueChange = { editedTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    singleLine = false,
                    maxLines = 3
                )

                // Due Date Card
                com.statz.app.ui.components.StatzGlassCard(
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .background(Primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CalendarMonth, null, tint = Primary, modifier = Modifier.size(20.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("DUE DATE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp)
                            Text(
                                if (task.dueAt != null) sdf.format(Date(task.dueAt)) else "No due date",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Reminder Card
                com.statz.app.ui.components.StatzGlassCard(
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("REMINDER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp)
                            Text(if (task.reminderEnabled) "At time of event" else "Off", style = MaterialTheme.typography.bodyMedium)
                        }
                        Switch(
                            checked = task.reminderEnabled,
                            onCheckedChange = { viewModel.updateTaskFields(taskId, reminderEnabled = it) }
                        )
                    }
                }

                // Urgency Selector
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Urgency", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(QueryUrgency.entries.toList()) { urgency ->
                            val isSelected = task.urgency == urgency
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
                                QueryUrgency.LOW -> "LOW"
                                QueryUrgency.MEDIUM -> "MED"
                                QueryUrgency.HIGH -> "HIGH"
                                QueryUrgency.CRITICAL -> "CRIT"
                                QueryUrgency.CUSTOM -> "CUSTOM"
                            }
                            com.statz.app.ui.components.SelectionChipButton(
                                icon = icon,
                                label = label,
                                color = color,
                                isSelected = isSelected,
                                onClick = { viewModel.updateTaskFields(taskId, urgency = urgency) }
                            )
                        }
                    }

                    // Urgency interval hint
                    Spacer(Modifier.height(4.dp))
                    val dotColor = when (task.urgency) {
                        QueryUrgency.CRITICAL -> UrgencyCritical
                        QueryUrgency.HIGH -> UrgencyHigh
                        QueryUrgency.MEDIUM -> UrgencyMedium
                        QueryUrgency.LOW -> UrgencyLow
                        QueryUrgency.CUSTOM -> UrgencyCustom
                    }
                    val intervalText = when (task.urgency) {
                        QueryUrgency.CRITICAL -> "Every 6 hours"
                        QueryUrgency.HIGH -> "Every 12 hours"
                        QueryUrgency.MEDIUM -> "Every 24 hours"
                        QueryUrgency.LOW -> "Every 48 hours"
                        QueryUrgency.CUSTOM -> "Every ${task.customFollowUpHours ?: "?"} hours"
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
                    if (task.urgency == QueryUrgency.CUSTOM) {
                        com.statz.app.ui.components.StatzGlassTextField(
                            value = (task.customFollowUpHours ?: "").toString().let { if (it == "null") "" else it },
                            onValueChange = { value ->
                                if (value.all { it.isDigit() }) {
                                    viewModel.updateTaskFields(
                                        taskId,
                                        urgency = QueryUrgency.CUSTOM,
                                        customFollowUpHours = value.toIntOrNull()
                                    )
                                }
                            },
                            label = "Follow-up interval (hours)",
                            placeholder = "e.g. 8",
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // Notes
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Notes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    com.statz.app.ui.components.StatzGlassTextField(
                        value = editedNotes,
                        onValueChange = { editedNotes = it },
                        placeholder = "Add additional details here...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(128.dp),
                        singleLine = false,
                        maxLines = 6
                    )
                }

                Spacer(Modifier.height(16.dp))
            }

            // Bottom action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Save changes
                val backdrop = com.statz.app.ui.components.LocalBackdrop.current
                if (backdrop != null) {
                    com.statz.app.ui.components.StatzLiquidButton(
                        onClick = {
                            viewModel.updateTaskFields(
                                taskId = taskId,
                                title = editedTitle,
                                notes = editedNotes.ifBlank { null }
                            )
                        },
                        backdrop = backdrop,
                        modifier = Modifier.weight(1f),
                        tint = MaterialTheme.colorScheme.primary,
                        buttonHeight = 56.dp
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Mark done
                    com.statz.app.ui.components.StatzLiquidButton(
                        onClick = {
                            viewModel.markDone(taskId)
                            navController.popBackStack()
                        },
                        backdrop = backdrop,
                        modifier = Modifier.weight(1f),
                        buttonHeight = 56.dp
                    ) {
                        Text(if (task.isDone) "Mark Undone" else "Mark Done", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
