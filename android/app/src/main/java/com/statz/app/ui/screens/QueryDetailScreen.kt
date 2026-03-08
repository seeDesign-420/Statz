package com.statz.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.statz.app.domain.model.QueryStatus
import com.statz.app.domain.model.QueryUrgency
import com.statz.app.ui.components.StatusChip
import com.statz.app.ui.components.UrgencyBadge
import com.statz.app.ui.theme.*
import com.statz.app.ui.viewmodel.QueriesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueryDetailScreen(
    queryId: String,
    navController: NavController,
    viewModel: QueriesViewModel = hiltViewModel()
) {
    val state by viewModel.detailState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf(false) }

    LaunchedEffect(queryId) { viewModel.loadQueryDetail(queryId) }

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

        val query = state.query ?: return@Scaffold

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Floating header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }

                        // Inline-editable ticket number — always a real TextField
                        val displayTicket = state.query?.ticketNumber?.ifEmpty { "#${queryId.take(8)}" } ?: ""
                        var editedTicket by remember(displayTicket) { mutableStateOf(displayTicket) }
                        var isFocused by remember { mutableStateOf(false) }
                        val keyboardController = LocalSoftwareKeyboardController.current

                        BasicTextField(
                            value = editedTicket,
                            onValueChange = { editedTicket = it },
                            textStyle = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(Primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (editedTicket.isNotBlank()) {
                                    viewModel.updateTicketNumber(queryId, editedTicket.trim())
                                }
                                keyboardController?.hide()
                            }),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                                .onFocusChanged { focusState ->
                                    isFocused = focusState.isFocused
                                    if (!focusState.isFocused && editedTicket.isNotBlank()) {
                                        viewModel.updateTicketNumber(queryId, editedTicket.trim())
                                    }
                                },
                            decorationBox = { innerTextField ->
                                Column {
                                    innerTextField()
                                    if (isFocused) {
                                        Spacer(Modifier.height(4.dp))
                                        Box(
                                            Modifier
                                                .fillMaxWidth()
                                                .height(2.dp)
                                                .background(Primary)
                                        )
                                    }
                                }
                            }
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete query",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
        val urgencyGlowColor = remember(query.urgency) {
            when (query.urgency) {
                QueryUrgency.LOW -> UrgencyLowGlow
                QueryUrgency.MEDIUM -> UrgencyMediumGlow
                QueryUrgency.HIGH -> UrgencyHighGlow
                QueryUrgency.CRITICAL -> UrgencyCriticalGlow
                QueryUrgency.CUSTOM -> UrgencyCustomGlow
            }
        }
        val glowRadius = remember(query.urgency) {
            when (query.urgency) {
                QueryUrgency.LOW -> 3.dp
                QueryUrgency.MEDIUM -> 5.dp
                QueryUrgency.HIGH -> 6.dp
                QueryUrgency.CRITICAL -> 8.dp
                QueryUrgency.CUSTOM -> 5.dp
            }
        }
        val pulseDuration = remember(query.urgency) {
            when (query.urgency) {
                QueryUrgency.CRITICAL -> 1200
                QueryUrgency.HIGH -> 1800
                else -> 2400
            }
        }

        // Breathing glow animation
        val infiniteTransition = rememberInfiniteTransition(label = "urgencyGlow")
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.15f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(pulseDuration, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowAlpha"
        )
        val radiusMultiplier by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(pulseDuration, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowRadius"
        )
        val animatedGlowColor = urgencyGlowColor.copy(alpha = glowAlpha)
        val animatedGlowRadius = glowRadius * radiusMultiplier

                // Info Card
                com.statz.app.ui.components.StatzGlassCard(
                    shape = RoundedCornerShape(16.dp),
                    tintColor = urgencyColor,
                    glowColor = animatedGlowColor,
                    glowRadius = animatedGlowRadius
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("CUSTOMER NAME", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp, fontSize = 10.sp)
                                Text(query.customerName, fontWeight = FontWeight.Medium)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("CUSTOMER ID", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp, fontSize = 10.sp)
                                Text(query.customerId.ifEmpty { "—" }, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("STATUS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp, fontSize = 10.sp)
                            StatusChip(query.status.name.replace("_", " "), statusColor)
                            Spacer(Modifier.weight(1f))
                            UrgencyBadge("${query.urgency.name} urgency", urgencyColor)
                        }
                    }
                }

                // Actions
                Text("ACTIONS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp, fontSize = 10.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val backdrop = com.statz.app.ui.components.LocalBackdrop.current
                    if (backdrop != null) {
                        com.statz.app.ui.components.StatzLiquidButton(
                            onClick = { viewModel.markFollowUp(queryId) },
                            backdrop = backdrop,
                            modifier = Modifier.weight(1f),
                            tint = StatusFollowUp
                        ) { Text("Follow-Up", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White) }
                        com.statz.app.ui.components.StatzLiquidButton(
                            onClick = { viewModel.escalate(queryId) },
                            backdrop = backdrop,
                            modifier = Modifier.weight(1f),
                            tint = StatusEscalated
                        ) { Text("Escalate", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White) }
                        com.statz.app.ui.components.StatzLiquidButton(
                            onClick = { viewModel.closeQuery(queryId) },
                            backdrop = backdrop,
                            modifier = Modifier.weight(1f),
                            tint = Success
                        ) { Text("Close", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White) }
                    }
                }

                // Snooze
                Text("SNOOZE FOLLOW-UP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp, fontSize = 10.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val snoozeBackdrop = com.statz.app.ui.components.LocalBackdrop.current
                    if (snoozeBackdrop != null) {
                        com.statz.app.ui.components.StatzLiquidButton(onClick = { viewModel.snooze1Hour(queryId) }, backdrop = snoozeBackdrop) {
                            Text("+1h", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color.White)
                        }
                        com.statz.app.ui.components.StatzLiquidButton(onClick = { viewModel.snooze4Hours(queryId) }, backdrop = snoozeBackdrop) {
                            Text("+4h", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color.White)
                        }
                        com.statz.app.ui.components.StatzLiquidButton(onClick = { viewModel.snoozeTomorrow9am(queryId) }, backdrop = snoozeBackdrop) {
                            Text("Tomorrow 09:00", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color.White)
                        }
                    }
                }

                // Log Timeline
                Text("LOG TIMELINE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp, fontSize = 10.sp)

                val sdf = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
                val lineColor = MaterialTheme.colorScheme.outlineVariant

                Column(
                    modifier = Modifier
                        .drawBehind {
                            val lineX = 6.dp.toPx()
                            drawLine(
                                color = lineColor,
                                start = Offset(lineX, 0f),
                                end = Offset(lineX, size.height),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                        .padding(start = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    state.logs.forEachIndexed { index, log ->
                        Box {
                            // Timeline dot — centered on the line
                            Box(
                                modifier = Modifier
                                    .offset(x = (-24).dp, y = 6.dp)
                                    .size(12.dp)
                                    .background(
                                        if (index == 0) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    sdf.format(Date(log.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (log.statusAfter != null) {
                                    Text(
                                        log.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        log.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // Bottom add-note bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.statz.app.ui.components.StatzGlassTextField(
                    value = state.noteText,
                    onValueChange = { viewModel.updateNoteText(it) },
                    placeholder = "Add a note...",
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                val sendBackdrop = com.statz.app.ui.components.LocalBackdrop.current
                if (sendBackdrop != null) {
                    com.statz.app.ui.components.StatzLiquidFab(
                        onClick = { viewModel.addNote(queryId) },
                        backdrop = sendBackdrop,
                        icon = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send"
                    )
                }
            }
        }
    }

    // Delete confirmation dialog — onConfirm flags pending delete,
    // navigation happens in onDismiss so dialog stays over detail screen.
    if (showDeleteDialog) {
        val dialogHost = com.statz.app.ui.components.LocalDialogHost.current
        androidx.compose.runtime.LaunchedEffect(Unit) {
            dialogHost(
                com.statz.app.ui.components.DialogConfig(
                    title = "Delete Query?",
                    message = "This will permanently remove this query and all its log entries. This cannot be undone.",
                    confirmText = "Delete",
                    onConfirm = {
                        viewModel.deleteQuery(queryId)
                        pendingDelete = true
                    },
                    onDismiss = {
                        showDeleteDialog = false
                        if (pendingDelete) {
                            navController.popBackStack()
                        }
                    }
                )
            )
        }
    }
}
