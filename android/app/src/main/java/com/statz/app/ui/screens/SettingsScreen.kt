package com.statz.app.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.statz.app.ui.components.SettingsRow
import com.statz.app.ui.navigation.Screen
import com.statz.app.ui.theme.*
import com.statz.app.ui.viewmodel.SalesViewModel
import com.statz.app.ui.viewmodel.SettingsViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    salesViewModel: SalesViewModel = hiltViewModel()
) {
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val backupState by settingsViewModel.backupState.collectAsStateWithLifecycle()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { settingsViewModel.exportBackup(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { settingsViewModel.importBackup(it) }
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Inline title
            Text(
                text = "Settings",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )

            // ── Profile ──────────────────────────────────────────
            SectionTitle("Profile")
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1C1C1E),
                tonalElevation = 0.dp
            ) {
                var nameText by remember(settings.displayName) { mutableStateOf(settings.displayName) }
                val commitName = {
                    val trimmed = nameText.trim()
                    if (trimmed.isNotEmpty() && trimmed != settings.displayName) {
                        settingsViewModel.setDisplayName(trimmed)
                    }
                }
                SettingsRow(
                    icon = {
                        Icon(
                            Icons.Default.Edit,
                            null,
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = "Display Name",
                    rightElement = {
                        BasicTextField(
                            value = nameText,
                            onValueChange = { nameText = it },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { commitName() }),
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .onFocusChanged { if (!it.isFocused) commitName() }
                        )
                    }
                )
            }
            // ── Sales ───────────────────────────────────────────
            SectionTitle("Sales")
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1C1C1E),
                tonalElevation = 0.dp
            ) {
                SettingsRow(
                    icon = {
                        Icon(
                            Icons.Default.Edit,
                            null,
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = "Edit Monthly Targets",
                    onClick = {
                        navController.navigate(
                            Screen.EditTargets.createRoute(salesViewModel.currentMonthKey())
                        )
                    }
                )
            }

            // ── Notifications ───────────────────────────────────
            SectionTitle("Notifications")
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1C1C1E),
                tonalElevation = 0.dp
            ) {
                Column {
                    SettingsRow(
                        icon = {
                            Icon(
                                Icons.Default.Notifications,
                                null,
                                tint = Color(0xFF8E8E93),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = "Query Reminders",
                        rightElement = {
                            com.statz.app.ui.components.StatzGlassToggle(
                                checked = settings.queryRemindersEnabled,
                                onCheckedChange = { settingsViewModel.setQueryReminders(it) }
                            )
                        }
                    )
                    HorizontalDivider(color = Color(0xFF38383A))
                    SettingsRow(
                        icon = {
                            Icon(
                                Icons.Default.Notifications,
                                null,
                                tint = Color(0xFF8E8E93),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = "To-Do Reminders",
                        rightElement = {
                            com.statz.app.ui.components.StatzGlassToggle(
                                checked = settings.todoRemindersEnabled,
                                onCheckedChange = { settingsViewModel.setTodoReminders(it) }
                            )
                        }
                    )
                }
            }

            // ── Backup ──────────────────────────────────────────
            SectionTitle("Backup & Restore")
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1C1C1E),
                tonalElevation = 0.dp
            ) {
                Column {
                    SettingsRow(
                        icon = {
                            Icon(
                                Icons.Default.Backup,
                                null,
                                tint = Color(0xFF8E8E93),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = "Export Backup",
                        onClick = { exportLauncher.launch("statz_backup.json") }
                    )
                    HorizontalDivider(color = Color(0xFF38383A))
                    SettingsRow(
                        icon = {
                            Icon(
                                Icons.Default.Restore,
                                null,
                                tint = Color(0xFF8E8E93),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = "Import Backup",
                        onClick = { importLauncher.launch(arrayOf("application/json")) }
                    )
                }
            }

            // Backup status message
            backupState.lastResult?.let { result ->
                Text(
                    text = result,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (result.startsWith("Export successful") || result.startsWith("Import successful"))
                        Success else Error,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // ── Help ────────────────────────────────────────────
            SectionTitle("System")
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1C1C1E),
                tonalElevation = 0.dp
            ) {
                SettingsRow(
                    icon = {
                        Icon(
                            Icons.Default.HelpOutline,
                            null,
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = "Help & Support"
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF8E8E93),
        letterSpacing = 2.sp
    )
}
