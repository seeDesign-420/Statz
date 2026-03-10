package com.statz.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import com.statz.app.ui.theme.StatzAnimation
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import android.graphics.BitmapFactory
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.drawPlainBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.shadow.Shadow
import com.statz.app.ui.theme.DarkBackground
import com.statz.app.ui.theme.DarkSurfaceVariant
import com.statz.app.ui.theme.DarkOnSurfaceVariant
import com.statz.app.ui.theme.Primary
import com.statz.app.ui.theme.GlassTint
import com.statz.app.ui.theme.GlassTintDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

val LocalBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

/**
 * Cached noise texture bitmap for glass card overlays.
 * Loaded once from drawable resources and reused across recompositions.
 */
@Composable
fun rememberNoiseBitmap(): ImageBitmap {
    val context = LocalContext.current
    return remember {
        BitmapFactory.decodeResource(context.resources, com.statz.app.R.drawable.noise_texture)
            .asImageBitmap()
    }
}

@Composable
fun StatzGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(backdrop: LayerBackdrop) -> Unit
) {
    val backdrop = rememberLayerBackdrop()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Sibling 1: Background captured by layerBackdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .layerBackdrop(backdrop)
        )

        // Sibling 2: Content — inline glass components use drawBackdrop safely here
        CompositionLocalProvider(LocalBackdrop provides backdrop) {
            content(backdrop)
        }
    }
}

/**
 * Provides the nav-level backdrop (captured at StatzNavHost level).
 * Used by sub-screen overlays (date pickers, time pickers) to blur
 * content — same backdrop the nav bar and glass dialog use.
 */
val LocalNavBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

/**
 * Full-screen blurred background for sub-screens overlaying the pager.
 * Rendered as a SIBLING of layerBackdrop — uses navBackdrop for live blur.
 * Same approach as the dialog overlay.
 */
@Composable
fun GlassScreenBackground(
    backdrop: com.kyant.backdrop.Backdrop,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawPlainBackdrop(
                backdrop = backdrop,
                shape = { androidx.compose.ui.graphics.RectangleShape },
                effects = {
                    blur(24f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(Color(0xFF0A0A0A).copy(alpha = 0.85f))
                }
            ),
        content = content
    )
}

/**
 * Applies a subtle noise texture overlay to the component.
 * Used to give glassmorphic elements a premium, physical grain.
 */
fun Modifier.noiseOverlay(noiseBitmap: ImageBitmap?, alpha: Float = 0.06f): Modifier = this.drawWithContent {
    drawContent()
    if (noiseBitmap != null) {
        val tileW = noiseBitmap.width
        val tileH = noiseBitmap.height
        val cols = (size.width / tileW).toInt() + 1
        val rows = (size.height / tileH).toInt() + 1
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                drawImage(
                    image = noiseBitmap,
                    dstOffset = IntOffset(col * tileW, row * tileH),
                    dstSize = IntSize(tileW, tileH),
                    alpha = alpha,
                    blendMode = BlendMode.Overlay
                )
            }
        }
    }
}

@Composable
fun StatzGlassCard(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    tintColor: Color? = null,
    glowColor: Color? = null,
    glowRadius: Dp = 12.dp,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit
) {
    val backdrop = LocalBackdrop.current

    if (backdrop != null) {
        Box(
            modifier = modifier
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { shape },
                    effects = {
                        vibrancy()
                        blur(24f.dp.toPx())
                    },
                    shadow = if (glowColor != null) {
                        {
                            Shadow(
                                radius = glowRadius,
                                color = glowColor.copy(alpha = 0.9f)
                            )
                        }
                    } else null,
                    onDrawSurface = {
                        if (tintColor != null) {
                            drawRect(tintColor.copy(alpha = 0.35f))
                        } else {
                            drawRect(GlassTint)
                        }
                    }
                )
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = shape
                )
        ) {
            content()
        }
    } else {
        val fallbackColor = tintColor?.copy(alpha = 0.15f) ?: Color.White.copy(alpha = 0.12f)
        Box(
            modifier = modifier
                .background(fallbackColor, shape)
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = shape
                ),
            content = content
        )
    }
}

/**
 * Config for a hoisted glass dialog rendered at the StatzNavHost level.
 */
data class DialogConfig(
    val title: String,
    val message: String,
    val confirmText: String,
    val dismissText: String = "Cancel",
    val confirmColor: Color = Color(0xFFEF5350),
    val onConfirm: () -> Unit,
    val onDismiss: () -> Unit
)

/**
 * Provides a callback to show a dialog. The dialog is rendered at the
 * StatzNavHost level as a sibling of layerBackdrop, enabling live blur.
 */
val LocalDialogHost = staticCompositionLocalOf<(DialogConfig) -> Unit> { {} }

/**
 * A glass-styled dialog rendered as an in-tree overlay (NOT a Dialog window).
 * Must be rendered as a SIBLING of layerBackdrop to enable live content blur.
 */
@Composable
fun StatzGlassDialogOverlay(
    config: DialogConfig,
    backdrop: com.kyant.backdrop.Backdrop,
    onDismiss: () -> Unit
) {
    androidx.activity.compose.BackHandler(onBack = onDismiss)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawPlainBackdrop(
                backdrop = backdrop,
                shape = { androidx.compose.ui.graphics.RectangleShape },
                effects = {},
                onDrawSurface = {
                    drawRect(Color.Black.copy(alpha = 0.5f))
                }
            )
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { androidx.compose.foundation.shape.RoundedCornerShape(24.dp) },
                    effects = {
                        vibrancy()
                        blur(24f.dp.toPx())
                    },
                    onDrawSurface = {
                        drawRect(Color(0xFF0A0A0A).copy(alpha = 0.55f))
                    }
                )
                .clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = {}
                )
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                androidx.compose.material3.Text(
                    config.title,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                androidx.compose.material3.Text(
                    config.message,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                            .drawBackdrop(
                                backdrop = backdrop,
                                shape = { androidx.compose.foundation.shape.RoundedCornerShape(50) },
                                effects = { vibrancy() },
                                onDrawSurface = { drawRect(DarkSurfaceVariant) }
                            )
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = onDismiss
                            )
                            .height(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            config.dismissText,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .drawBackdrop(
                                backdrop = backdrop,
                                shape = { androidx.compose.foundation.shape.RoundedCornerShape(50) },
                                effects = { vibrancy() },
                                onDrawSurface = { drawRect(config.confirmColor) }
                            )
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = {
                                    config.onConfirm()
                                    onDismiss()
                                }
                            )
                            .height(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            config.confirmText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ── Toast System ────────────────────────────────────────────────────────

/**
 * Provides a callback to show a glass toast. The toast is rendered at the
 * StatzNavHost level as a sibling of layerBackdrop, enabling live blur.
 */
val LocalToastHost = staticCompositionLocalOf<(String) -> Unit> { {} }

/**
 * A glass-styled toast notification rendered as an in-tree overlay.
 * Auto-dismisses after [durationMs]. Uses backdrop blur for frosted glass.
 */
@Composable
fun StatzGlassToastHost(
    backdrop: Backdrop,
    message: String?,
    onDismiss: () -> Unit
) {
    // Auto-dismiss timer
    LaunchedEffect(message) {
        if (message != null) {
            delay(2500L)
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(bottom = 88.dp), // Above nav bar
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = message != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = StatzAnimation.overshootSpring()
            ) + fadeIn(animationSpec = StatzAnimation.standardSpring()),
            exit = slideOutVertically(
                targetOffsetY = { it / 2 },
                animationSpec = StatzAnimation.standardSpring()
            ) + fadeOut(animationSpec = StatzAnimation.standardSpring())
        ) {
            if (message != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { RoundedCornerShape(50) },
                            effects = {
                                vibrancy()
                                blur(24f.dp.toPx())
                            },
                            onDrawSurface = {
                                drawRect(GlassTint)
                            }
                        )
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ── Wheel Picker Primitives ─────────────────────────────────────────────

private val MONTH_NAMES = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

private fun daysInMonth(month: Int, year: Int): Int {
    val cal = java.util.Calendar.getInstance()
    cal.set(year, month, 1)
    return cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
}

/**
 * A single scroll-wheel column with snap-to-center fling behavior.
 * Items fade out toward the edges. The centered item is selected.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelColumn(
    items: List<String>,
    initialIndex: Int,
    onSelectedChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleCount: Int = 5,
    itemHeightDp: Dp = 44.dp
) {
    val halfVisible = visibleCount / 2
    val totalHeight = itemHeightDp * visibleCount
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Emit selected index changes
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = layoutInfo.viewportStartOffset +
                    (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
            layoutInfo.visibleItemsInfo.minByOrNull {
                kotlin.math.abs((it.offset + it.size / 2) - viewportCenter)
            }?.index ?: 0
        }
            .distinctUntilChanged()
            .collect { index ->
                onSelectedChanged(index.coerceIn(0, items.lastIndex))
            }
    }

    Box(
        modifier = modifier
            .height(totalHeight)
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                // Top fade
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black, Color.Transparent),
                        startY = 0f,
                        endY = size.height * 0.35f
                    ),
                    blendMode = BlendMode.DstIn
                )
                // Bottom fade
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black),
                        startY = size.height * 0.65f,
                        endY = size.height
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = itemHeightDp * halfVisible),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items.size) { index ->
                val isSelected by remember {
                    derivedStateOf {
                        val layoutInfo = listState.layoutInfo
                        val viewportCenter = layoutInfo.viewportStartOffset +
                                (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
                        val closest = layoutInfo.visibleItemsInfo.minByOrNull {
                            kotlin.math.abs((it.offset + it.size / 2) - viewportCenter)
                        }
                        closest?.index == index
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeightDp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[index],
                        fontSize = if (isSelected) 22.sp else 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else DarkOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Selection highlight strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeightDp)
                .align(Alignment.Center)
                .background(
                    Primary.copy(alpha = 0.1f),
                    RoundedCornerShape(8.dp)
                )
        )
    }
}

// ── Date Picker Overlay ─────────────────────────────────────────────────

/**
 * Full-screen glass date picker overlay with scroll-wheel columns.
 * Day | Month | Year wheels with snap-to-center fling.
 */
@Composable
fun StatzGlassDatePickerOverlay(
    backdrop: Backdrop,
    initialDateMillis: Long?,
    onDismiss: () -> Unit,
    onDateSelected: (Long?) -> Unit
) {
    val initialCal = remember(initialDateMillis) {
        java.util.Calendar.getInstance().apply {
            timeInMillis = initialDateMillis ?: System.currentTimeMillis()
        }
    }

    var selectedDay by remember { mutableStateOf(initialCal.get(java.util.Calendar.DAY_OF_MONTH)) }
    var selectedMonth by remember { mutableStateOf(initialCal.get(java.util.Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(initialCal.get(java.util.Calendar.YEAR)) }

    val currentYear = remember { java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) }
    val yearRange = remember { (currentYear - 5)..(currentYear + 10) }
    val years = remember { yearRange.map { it.toString() } }
    val months = remember { MONTH_NAMES }
    val days by remember(selectedMonth, selectedYear) {
        derivedStateOf {
            val maxDay = daysInMonth(selectedMonth, selectedYear)
            (1..maxDay).map { it.toString() }
        }
    }

    // Entrance animation trigger — brief delay lets wheels settle before animating in
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(50); visible = true }

    androidx.activity.compose.BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(interactionSource = null, indication = null, onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(StatzAnimation.standardSpring()) + scaleIn(
                initialScale = 0.92f,
                animationSpec = StatzAnimation.overshootSpring()
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedCornerShape(24.dp) },
                        effects = {
                            vibrancy()
                            blur(24f.dp.toPx())
                        },
                        onDrawSurface = { drawRect(GlassTintDark) }
                    )
                    .clickable(interactionSource = null, indication = null, onClick = {})
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Select date",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Three-column wheel: Day | Month | Year
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WheelColumn(
                            items = days,
                            initialIndex = (selectedDay - 1).coerceAtLeast(0),
                            onSelectedChanged = { idx -> selectedDay = idx + 1 },
                            modifier = Modifier.weight(1f)
                        )
                        WheelColumn(
                            items = months,
                            initialIndex = selectedMonth,
                            onSelectedChanged = { idx -> selectedMonth = idx },
                            modifier = Modifier.weight(1.2f)
                        )
                        WheelColumn(
                            items = years,
                            initialIndex = (selectedYear - yearRange.first).coerceIn(0, years.lastIndex),
                            onSelectedChanged = { idx -> selectedYear = yearRange.first + idx },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Glass Cancel button
                        Box(
                            Modifier
                                .weight(1f)
                                .drawBackdrop(
                                    backdrop = backdrop,
                                    shape = { RoundedCornerShape(50) },
                                    effects = { vibrancy(); blur(2f.dp.toPx()) },
                                    onDrawSurface = { drawRect(DarkSurfaceVariant) }
                                )
                                .clickable(interactionSource = null, indication = null, onClick = onDismiss)
                                .height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Cancel", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                        // Glass Next button
                        Box(
                            Modifier
                                .weight(1f)
                                .drawBackdrop(
                                    backdrop = backdrop,
                                    shape = { RoundedCornerShape(50) },
                                    effects = { vibrancy() },
                                    onDrawSurface = { drawRect(Primary.copy(alpha = 0.8f)) }
                                )
                                .clickable(
                                    interactionSource = null, indication = null,
                                    onClick = {
                                        val cal = java.util.Calendar.getInstance().apply {
                                            set(java.util.Calendar.YEAR, selectedYear)
                                            set(java.util.Calendar.MONTH, selectedMonth)
                                            set(java.util.Calendar.DAY_OF_MONTH, selectedDay)
                                        }
                                        onDateSelected(cal.timeInMillis)
                                    }
                                )
                                .height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Next", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

// ── Time Picker Overlay ─────────────────────────────────────────────────

/**
 * Full-screen glass time picker overlay with scroll-wheel style.
 * Uses ComposeDatePicker library for elegant wheel picker.
 */
@Composable
fun StatzGlassTimePickerOverlay(
    backdrop: Backdrop,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }

    // Entrance animation trigger
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    androidx.activity.compose.BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(StatzAnimation.standardSpring()) + scaleIn(
                initialScale = 0.92f,
                animationSpec = StatzAnimation.overshootSpring()
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedCornerShape(24.dp) },
                        effects = {
                            vibrancy()
                            blur(24f.dp.toPx())
                        },
                        onDrawSurface = { drawRect(GlassTintDark) }
                    )
                    .clickable(interactionSource = null, indication = null, onClick = {})
                    .padding(24.dp)
            ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Select time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Scroll-wheel time picker
                com.vsnappy1.timepicker.TimePicker(
                    modifier = Modifier.fillMaxWidth(),
                    is24Hour = true,
                    time = com.vsnappy1.timepicker.data.model.TimePickerTime(
                        hour = initialHour,
                        minute = initialMinute
                    ),
                    onTimeSelected = { hour, minute ->
                        selectedHour = hour
                        selectedMinute = minute
                    },
                    configuration = com.vsnappy1.timepicker.ui.model.TimePickerConfiguration.Builder()
                        .height(220.dp)
                        .timeTextStyle(
                            androidx.compose.ui.text.TextStyle(
                                color = DarkOnSurfaceVariant,
                                fontSize = 16.sp
                            )
                        )
                        .selectedTimeTextStyle(
                            androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        .selectedTimeScaleFactor(1.3f)
                        .numberOfTimeRowsDisplayed(5)
                        .build()
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Glass Cancel button
                    Box(
                        Modifier
                            .weight(1f)
                            .drawBackdrop(
                                backdrop = backdrop,
                                shape = { RoundedCornerShape(50) },
                                effects = {
                                    vibrancy()
                                    blur(2f.dp.toPx())
                                },
                                onDrawSurface = { drawRect(DarkSurfaceVariant) }
                            )
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = onDismiss
                            )
                            .height(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Cancel",
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                    // Glass Confirm button
                    Box(
                        Modifier
                            .weight(1f)
                            .drawBackdrop(
                                backdrop = backdrop,
                                shape = { RoundedCornerShape(50) },
                                effects = {
                                    vibrancy()
                                },
                                onDrawSurface = {
                                    drawRect(Primary.copy(alpha = 0.8f))
                                }
                            )
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = {
                                    onTimeSelected(selectedHour, selectedMinute)
                                }
                            )
                            .height(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Confirm",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                }
            }
        }
    }
}
