package com.statz.app.ui.screens

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
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
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import com.statz.app.domain.model.CategoryType
import com.statz.app.ui.viewmodel.SalesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTargetsScreen(
    monthKey: String,
    navController: NavController,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val state by viewModel.targetsState.collectAsStateWithLifecycle()

    LaunchedEffect(monthKey) { viewModel.loadTargets(monthKey) }

    val editedTargets = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(state.targets) {
        state.targets.forEach { (k, v) -> editedTargets[k] = v.toString() }
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Floating Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                    Text(
                        text = "Edit Targets",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Text(
                    text = state.monthDisplay,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Unit Targets
            val unitCats = state.categories.filter { it.type == CategoryType.UNIT }
            if (unitCats.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                SectionHeader("Unit Targets")
                unitCats.forEach { cat ->
                    StepperRow(
                        label = cat.name,
                        value = (editedTargets[cat.id] ?: "0").toLongOrNull() ?: 0L,
                        onValueChange = { editedTargets[cat.id] = it.toString() }
                    )
                }
            }

            // Revenue Targets
            val moneyCats = state.categories.filter { it.type == CategoryType.MONEY }
            if (moneyCats.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                SectionHeader("Revenue Targets")
                moneyCats.forEach { cat ->
                    StepperRow(
                        label = cat.name,
                        value = (editedTargets[cat.id] ?: "0").toLongOrNull() ?: 0L,
                        onValueChange = { editedTargets[cat.id] = it.toString() }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            val backdrop = com.statz.app.ui.components.LocalBackdrop.current
            if (backdrop != null) {
                com.statz.app.ui.components.StatzLiquidButton(
                    onClick = {
                        val targets = editedTargets.mapValues { (_, v) -> v.toLongOrNull() ?: 0L }
                        viewModel.saveAllTargets(monthKey, targets)
                        navController.popBackStack()
                    },
                    backdrop = backdrop,
                    modifier = Modifier.fillMaxWidth(),
                    tint = MaterialTheme.colorScheme.primary,
                    buttonHeight = 56.dp
                ) {
                    Text("Save All Targets", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
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

/**
 * Full-width stepper row: Label on left, [− value +] in a single dark pill on right.
 */
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
                                drawRect(Color.White.copy(alpha = 0.05f))
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
            // Minus button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onValueChange((value - 1).coerceAtLeast(0)) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Remove,
                    "Decrease",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Tappable value display / inline text input
            var isEditing by remember { mutableStateOf(false) }
            var hasFocusedOnce by remember { mutableStateOf(false) }
            val focusRequester = remember { FocusRequester() }
            val keyboardController = LocalSoftwareKeyboardController.current

            val commitEdit = { text: String ->
                if (isEditing) {
                    val parsed = text.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
                    onValueChange(parsed)
                    isEditing = false
                    hasFocusedOnce = false
                    keyboardController?.hide()
                }
            }

            Box(
                modifier = Modifier
                    .height(44.dp)
                    .widthIn(min = 64.dp)
                    .clickable(enabled = !isEditing) { isEditing = true },
                contentAlignment = Alignment.Center
            ) {
                if (isEditing) {
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
                            onDone = { commitEdit(tfv.text) }
                        ),
                        modifier = Modifier
                            .widthIn(min = 48.dp)
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    hasFocusedOnce = true
                                } else if (hasFocusedOnce) {
                                    commitEdit(tfv.text)
                                }
                            }
                    )
                } else {
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

            // Plus button
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
                                    blur(2f.dp.toPx())
                                },
                                onDrawSurface = {
                                    drawRect(primaryColor.copy(alpha = 0.8f))
                                }
                            )
                        } else {
                            Modifier.background(primaryColor, CircleShape)
                        }
                    )
                    .clickable { onValueChange(value + 1) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, "Increase", modifier = Modifier.size(20.dp), tint = Color.White)
            }
        }
    }
}
