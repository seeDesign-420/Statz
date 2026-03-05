package com.statz.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.statz.app.ui.theme.Primary

val LocalBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

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
                .background(
                    Brush.radialGradient(
                        colors = listOf(Primary.copy(alpha = 0.15f), Color.Transparent),
                        radius = 1500f
                    )
                )
                .layerBackdrop(backdrop)
        )

        // Sibling 2: Content — inline glass components use drawBackdrop safely here
        CompositionLocalProvider(LocalBackdrop provides backdrop) {
            content(backdrop)
        }
    }
}

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
                            drawRect(Color.White.copy(alpha = 0.12f))
                        }
                    }
                )
        ) {
            content()
        }
    } else {
        val fallbackColor = tintColor?.copy(alpha = 0.15f) ?: Color.White.copy(alpha = 0.12f)
        Box(
            modifier = modifier.background(fallbackColor, shape),
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
                        drawRect(Color(0xFF121212).copy(alpha = 0.4f))
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
                    Row(
                        Modifier
                            .weight(1f)
                            .drawBackdrop(
                                backdrop = backdrop,
                                shape = { androidx.compose.foundation.shape.RoundedCornerShape(50) },
                                effects = {
                                    vibrancy()
                                    blur(2f.dp.toPx())
                                },
                                onDrawSurface = {}
                            )
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = onDismiss
                            )
                            .height(44.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Text(
                            config.dismissText,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Row(
                        Modifier
                            .weight(1f)
                            .drawBackdrop(
                                backdrop = backdrop,
                                shape = { androidx.compose.foundation.shape.RoundedCornerShape(50) },
                                effects = {
                                    vibrancy()
                                    blur(2f.dp.toPx())
                                },
                                onDrawSurface = {
                                    drawRect(config.confirmColor, blendMode = androidx.compose.ui.graphics.BlendMode.Hue)
                                    drawRect(config.confirmColor.copy(alpha = 0.75f))
                                }
                            )
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = {
                                    config.onConfirm()
                                    onDismiss()
                                }
                            )
                            .height(44.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
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
