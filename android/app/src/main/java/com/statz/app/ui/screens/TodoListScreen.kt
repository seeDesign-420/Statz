package com.statz.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.statz.app.ui.theme.StatzAnimation
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.statz.app.data.local.model.TaskItemEntity
import com.statz.app.domain.model.QueryUrgency
import com.statz.app.ui.navigation.Screen
import com.statz.app.ui.theme.*
import com.statz.app.ui.viewmodel.TasksViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    navController: NavController,
    viewModel: TasksViewModel = hiltViewModel()
) {
    val quickAddText by viewModel.quickAddText.collectAsStateWithLifecycle()
    val overdue by viewModel.overdueTasks.collectAsStateWithLifecycle()
    val today by viewModel.todayTasks.collectAsStateWithLifecycle()
    val upcoming by viewModel.upcomingTasks.collectAsStateWithLifecycle()
    val unscheduled by viewModel.unscheduledTasks.collectAsStateWithLifecycle()
    val completed by viewModel.completedTasks.collectAsStateWithLifecycle()
    var completedExpanded by remember { mutableStateOf(false) }

    Scaffold(
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Floating Header
            item {
                Text(
                    text = "To-Do",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
            }

            // Quick add input
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    com.statz.app.ui.components.StatzGlassTextField(
                        value = quickAddText,
                        onValueChange = { viewModel.updateQuickAddText(it) },
                        placeholder = "Add a task...",
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    val backdrop = com.statz.app.ui.components.LocalBackdrop.current
                    if (backdrop != null) {
                        com.statz.app.ui.components.StatzLiquidFab(
                            onClick = { viewModel.quickAdd() },
                            backdrop = backdrop,
                            icon = Icons.Default.Add,
                            contentDescription = "Add task",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            // Overdue
            if (overdue.isNotEmpty()) {
                item {
                    TaskSectionHeader("Overdue (${overdue.size})", Error)
                }
                items(overdue, key = { "overdue_${it.id}" }) { task ->
                    TaskCard(
                        task = task,
                        isOverdue = true,
                        onToggleDone = { viewModel.toggleDone(task.id) },
                        onClick = { navController.navigate(Screen.TaskDetail.createRoute(task.id)) }
                    )
                }
            }

            // Today
            if (today.isNotEmpty()) {
                item {
                    TaskSectionHeader("Today (${today.size})", Primary)
                }
                items(today, key = { "today_${it.id}" }) { task ->
                    TaskCard(
                        task = task,
                        isOverdue = false,
                        onToggleDone = { viewModel.toggleDone(task.id) },
                        onClick = { navController.navigate(Screen.TaskDetail.createRoute(task.id)) }
                    )
                }
            }

            // Upcoming
            if (upcoming.isNotEmpty()) {
                item {
                    TaskSectionHeader("Upcoming (${upcoming.size})", MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(upcoming, key = { "upcoming_${it.id}" }) { task ->
                    TaskCard(
                        task = task,
                        isOverdue = false,
                        onToggleDone = { viewModel.toggleDone(task.id) },
                        onClick = { navController.navigate(Screen.TaskDetail.createRoute(task.id)) },
                        dimmed = true
                    )
                }
            }

            // Unscheduled
            if (unscheduled.isNotEmpty()) {
                item {
                    TaskSectionHeader("Unscheduled (${unscheduled.size})", MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(unscheduled, key = { "unscheduled_${it.id}" }) { task ->
                    TaskCard(
                        task = task,
                        isOverdue = false,
                        onToggleDone = { viewModel.toggleDone(task.id) },
                        onClick = { navController.navigate(Screen.TaskDetail.createRoute(task.id)) },
                        dimmed = true
                    )
                }
            }

            // Completed history
            if (completed.isNotEmpty()) {
                item {
                    val chevronRotation by animateFloatAsState(
                        targetValue = if (completedExpanded) 180f else 0f,
                        animationSpec = StatzAnimation.microTween(),
                        label = "chevron"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { completedExpanded = !completedExpanded }
                            .padding(top = 16.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COMPLETED (${completed.size})".uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 2.sp
                        )
                        Icon(
                            Icons.Default.ExpandMore,
                            contentDescription = if (completedExpanded) "Collapse" else "Expand",
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(chevronRotation),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (completedExpanded) {
                    items(completed.take(10), key = { "completed_${it.id}" }) { task ->
                        TaskCard(
                            task = task,
                            isOverdue = false,
                            onToggleDone = { viewModel.toggleDone(task.id) },
                            onClick = { navController.navigate(Screen.TaskDetail.createRoute(task.id)) },
                            dimmed = true
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun TaskSectionHeader(
    text: String,
    color: androidx.compose.ui.graphics.Color
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = color,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun TaskCard(
    task: TaskItemEntity,
    isOverdue: Boolean,
    onToggleDone: () -> Unit,
    onClick: () -> Unit,
    dimmed: Boolean = false
) {
    val sdf = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    val tintColor = when {
        isOverdue -> Error
        task.urgency == QueryUrgency.CRITICAL -> com.statz.app.ui.theme.UrgencyCritical
        task.urgency == QueryUrgency.HIGH -> com.statz.app.ui.theme.UrgencyHigh
        else -> null
    }

    val glowColor = when {
        isOverdue -> com.statz.app.ui.theme.UrgencyCriticalGlow
        task.urgency == QueryUrgency.CRITICAL -> com.statz.app.ui.theme.UrgencyCriticalGlow
        task.urgency == QueryUrgency.HIGH -> com.statz.app.ui.theme.UrgencyHighGlow
        else -> null
    }

    val glowRadius = when {
        isOverdue -> 6.dp
        task.urgency == QueryUrgency.CRITICAL -> 8.dp
        task.urgency == QueryUrgency.HIGH -> 6.dp
        else -> 12.dp
    }

    // Status label & color for the right-aligned pill
    val (statusLabel, statusColor) = when {
        isOverdue -> "OVERDUE" to Error
        task.urgency == QueryUrgency.CRITICAL -> "CRITICAL" to com.statz.app.ui.theme.UrgencyCritical
        task.urgency == QueryUrgency.HIGH -> "HIGH" to com.statz.app.ui.theme.UrgencyHigh
        task.isDone -> "DONE" to com.statz.app.ui.theme.StatusClosed
        task.dueAt != null -> "DUE" to MaterialTheme.colorScheme.primary
        else -> null to null
    }

    com.statz.app.ui.components.StatzGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        tintColor = tintColor,
        glowColor = glowColor,
        glowRadius = glowRadius
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Top row: Title + Status Pill + Chevron
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
                    color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (statusLabel != null && statusColor != null) {
                        Row(
                            modifier = Modifier
                                .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View Details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Bottom row: Checkbox + metadata
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isDone,
                    onCheckedChange = { onToggleDone() },
                    colors = CheckboxDefaults.colors(
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
                )
                if (task.dueAt != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            null,
                            modifier = Modifier.size(12.dp),
                            tint = if (isOverdue) Error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isOverdue) "Overdue" else sdf.format(Date(task.dueAt)),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = if (isOverdue) Error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (task.urgency == QueryUrgency.HIGH || task.urgency == QueryUrgency.CRITICAL) {
                    val urgencyColor = if (task.urgency == QueryUrgency.CRITICAL) com.statz.app.ui.theme.UrgencyCritical else com.statz.app.ui.theme.UrgencyHigh
                    val urgencyLabel = if (task.urgency == QueryUrgency.CRITICAL) "Critical" else "High"
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Flag, null, modifier = Modifier.size(12.dp), tint = urgencyColor)
                        Text(urgencyLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = urgencyColor)
                    }
                }
            }
        }
    }
}
