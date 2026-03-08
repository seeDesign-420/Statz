package com.statz.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.statz.app.data.repository.DailyEntry
import com.statz.app.domain.model.CategoryType
import com.statz.app.ui.viewmodel.SalesViewModel
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyEntryScreen(
    dateKey: String,
    navController: NavController,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val state by viewModel.dailyEntryState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val toastHost = com.statz.app.ui.components.LocalToastHost.current

    // Observe clipboard export events
    LaunchedEffect(Unit) {
        viewModel.clipboardText.collect { text ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Statz Daily Report", text))
            toastHost("Stats copied to clipboard")
        }
    }

    LaunchedEffect(dateKey) { viewModel.loadDailyEntry(dateKey) }

    // Local editable state
    val values = remember { mutableStateMapOf<String, String>() }
    var openOrdersNew by remember { mutableStateOf("0") }
    var openOrdersUpgrade by remember { mutableStateOf("0") }
    var declinedNew by remember { mutableStateOf("0") }
    var declinedUpgrade by remember { mutableStateOf("0") }

    // Populate from loaded entry
    LaunchedEffect(state.entry) {
        state.entry?.let { entry ->
            entry.categoryValues.forEach { (k, v) -> values[k] = v.toString() }
            openOrdersNew = entry.openOrdersNew.toString()
            openOrdersUpgrade = entry.openOrdersUpgrade.toString()
            declinedNew = entry.declinedNew.toString()
            declinedUpgrade = entry.declinedUpgrade.toString()
        }
    }

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Floating Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                    Text(
                        text = "Daily Entry",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Text(
                    text = state.dateDisplay,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            // Unit Sales
            val unitCats = state.categories.filter { it.type == CategoryType.UNIT }
            if (unitCats.isNotEmpty()) {
                SectionHeader("Unit Sales")
                unitCats.forEach { cat ->
                    StepperRow(
                        label = cat.name,
                        value = (values[cat.id] ?: "0").toLongOrNull() ?: 0L,
                        onValueChange = { values[cat.id] = it.toString() }
                    )
                }
            }

            // Revenue
            val moneyCats = state.categories.filter { it.type == CategoryType.MONEY }
            if (moneyCats.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                SectionHeader("Revenue")
                moneyCats.forEach { cat ->
                    StepperRow(
                        label = cat.name,
                        value = (values[cat.id] ?: "0").toLongOrNull() ?: 0L,
                        onValueChange = { values[cat.id] = it.toString() }
                    )
                }
            }

            // Open Orders
            Spacer(Modifier.height(4.dp))
            SectionHeader("Open Orders")
            StepperRow("New", openOrdersNew.toLongOrNull() ?: 0L) { openOrdersNew = it.toString() }
            StepperRow("Upgrade", openOrdersUpgrade.toLongOrNull() ?: 0L) { openOrdersUpgrade = it.toString() }

            // Declined
            Spacer(Modifier.height(4.dp))
            SectionHeader("Declined")
            StepperRow("New", declinedNew.toLongOrNull() ?: 0L) { declinedNew = it.toString() }
            StepperRow("Upgrade", declinedUpgrade.toLongOrNull() ?: 0L) { declinedUpgrade = it.toString() }

            Spacer(Modifier.height(20.dp))

            val backdrop = com.statz.app.ui.components.LocalBackdrop.current
            if (backdrop != null) {
                com.statz.app.ui.components.StatzLiquidButton(
                    onClick = {
                        val categoryValues = values.mapValues { (_, v) -> v.toLongOrNull() ?: 0L }
                        viewModel.saveDailyEntry(
                            DailyEntry(
                                dateKey = dateKey,
                                categoryValues = categoryValues,
                                openOrdersNew = openOrdersNew.toIntOrNull() ?: 0,
                                openOrdersUpgrade = openOrdersUpgrade.toIntOrNull() ?: 0,
                                declinedNew = declinedNew.toIntOrNull() ?: 0,
                                declinedUpgrade = declinedUpgrade.toIntOrNull() ?: 0
                            )
                        )
                        navController.popBackStack()
                    },
                    backdrop = backdrop,
                    modifier = Modifier.fillMaxWidth(),
                    tint = MaterialTheme.colorScheme.primary,
                    buttonHeight = 56.dp
                ) {
                    Text("Save Entry", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = androidx.compose.ui.graphics.Color.White)
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun StepperRow(
    label: String,
    value: Long,
    onValueChange: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        val backdrop = com.statz.app.ui.components.LocalBackdrop.current
        val primaryColor = MaterialTheme.colorScheme.primary
        val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
        Row(
            modifier = Modifier
                .then(
                    if (backdrop != null) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { RoundedCornerShape(50) },
                            effects = {
                                vibrancy()
                                blur(2f.dp.toPx())
                            },
                            onDrawSurface = {
                                drawRect(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.05f))
                            }
                        )
                    } else {
                        Modifier.background(
                            surfaceVariantColor.copy(alpha = 0.5f),
                            RoundedCornerShape(50)
                        )
                    }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Editable state — hoisted so step buttons can commit-then-step
            var isEditing by remember { mutableStateOf(false) }
            var hasFocusedOnce by remember { mutableStateOf(false) }
            val focusRequester = remember { FocusRequester() }
            val keyboardController = LocalSoftwareKeyboardController.current
            // Shared ref to the current text field value so step buttons can read it
            var currentEditText by remember { mutableStateOf(value.toString()) }

            val commitAndExit = {
                val parsed = currentEditText.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
                onValueChange(parsed)
                isEditing = false
                hasFocusedOnce = false
                keyboardController?.hide()
            }

            // Commit current text, apply step offset, exit edit mode
            val commitThenStep = { offset: Long ->
                if (isEditing) {
                    val parsed = currentEditText.toLongOrNull()?.coerceAtLeast(0L) ?: value
                    onValueChange((parsed + offset).coerceAtLeast(0L))
                    isEditing = false
                    hasFocusedOnce = false
                    keyboardController?.hide()
                } else {
                    onValueChange((value + offset).coerceAtLeast(0L))
                }
            }

            // Minus button — always enabled, commits current edit first if active
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { commitThenStep(-1L) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Remove,
                    "Decrease",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Animate width expansion when entering edit mode
            val targetWidth by animateDpAsState(
                targetValue = if (isEditing) 100.dp else 64.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "editWidthExpand"
            )

            Box(
                modifier = Modifier
                    .height(44.dp)
                    .width(targetWidth)
                    .clickable(enabled = !isEditing) { isEditing = true },
                contentAlignment = Alignment.Center
            ) {
                Crossfade(
                    targetState = isEditing,
                    animationSpec = tween(200),
                    label = "editCrossfade"
                ) { editing ->
                    if (editing) {
                        var tfv by remember(value) {
                            val s = value.toString()
                            mutableStateOf(TextFieldValue(s, TextRange(0, s.length)))
                        }

                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }

                        BasicTextField(
                            value = tfv,
                            onValueChange = { newVal ->
                                val filtered = newVal.text.filter { it.isDigit() }
                                tfv = newVal.copy(text = filtered)
                                currentEditText = filtered
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { commitAndExit() }
                            ),
                            modifier = Modifier
                                .widthIn(min = 48.dp)
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        hasFocusedOnce = true
                                    } else if (hasFocusedOnce) {
                                        commitAndExit()
                                    }
                                }
                        )
                    } else {
                        Box(
                            modifier = Modifier.height(44.dp).widthIn(min = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = value.toString(),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Plus button — always enabled, commits current edit first if active
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .then(
                        if (backdrop != null) {
                            Modifier.drawBackdrop(
                                backdrop = backdrop,
                                shape = { CircleShape },
                                effects = {
                                    vibrancy()
                                },
                                onDrawSurface = {
                                    drawRect(primaryColor.copy(alpha = 0.8f))
                                }
                            )
                        } else {
                            Modifier.background(primaryColor, CircleShape)
                        }
                    )
                    .clickable { commitThenStep(1L) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, "Increase", modifier = Modifier.size(20.dp), tint = androidx.compose.ui.graphics.Color.White)
            }
        }
    }
}


