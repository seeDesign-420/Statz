package com.statz.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import com.statz.app.ui.theme.StatzAnimation
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy

private val CapsuleShape = RoundedCornerShape(50)

/**
 * A Liquid Glass button that uses the Kyant Backdrop for frosted glass blur.
 * Replaces Material You Button/OutlinedButton/FloatingActionButton.
 */
@Composable
fun StatzLiquidButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    buttonHeight: Dp = 48.dp,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { CapsuleShape },
                effects = {
                    vibrancy()
                    blur(2f.dp.toPx())
                },
                onDrawSurface = {
                    if (tint.isSpecified) {
                        drawRect(tint, blendMode = BlendMode.Hue)
                        drawRect(tint.copy(alpha = 0.75f))
                    }
                    if (surfaceColor.isSpecified) {
                        drawRect(surfaceColor)
                    }
                }
            )
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .height(buttonHeight)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/**
 * A Liquid Glass Floating Action Button.
 */
@Composable
fun StatzLiquidFab(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    icon: ImageVector,
    contentDescription: String = ""
) {
    StatzLiquidButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier.size(56.dp),
        tint = tint,
        buttonHeight = 56.dp
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Liquid Glass bottom navigation bar, replacing Material NavigationBar.
 */
@Composable
fun GlassNavBar(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { CapsuleShape },
                effects = {
                    vibrancy()
                    blur(8f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(Color(0xFF121212).copy(alpha = 0.4f))
                }
            )
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/**
 * A single tab item for the GlassNavBar.
 */
@Composable
fun RowScope.GlassNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val contentColor = if (selected) accentColor else Color.White.copy(alpha = 0.5f)

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
/**
 * A row of pill-shaped tabs, replacing Material FilterChips.
 */
@Composable
fun StatzPillTabRow(
    modifier: Modifier = Modifier,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

/**
 * A single pill tab with liquid glass effect when selected.
 */
@Composable
fun StatzPillTab(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val backdrop = LocalBackdrop.current
    val contentColor = if (selected) Color.White else Color.White.copy(alpha = 0.7f)

    if (backdrop != null && selected) {
        // Active pill — liquid glass with accent hue tint
        Box(
            modifier = modifier
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { CapsuleShape },
                    effects = {
                        vibrancy()
                        blur(2f.dp.toPx())
                    },
                    onDrawSurface = {
                        drawRect(accentColor, blendMode = BlendMode.Hue)
                        drawRect(accentColor.copy(alpha = 0.75f))
                    }
                )
                .clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    } else {
        // Inactive pill — subtle ghost background
        Box(
            modifier = modifier
                .background(Color.White.copy(alpha = 0.05f), CapsuleShape)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

/**
 * A persistent search bar styled as a dark rounded pill.
 */
@Composable
fun StatzSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search..."
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { 
            Text(
                placeholder, 
                color = Color.White.copy(alpha = 0.4f), 
                fontSize = 14.sp 
            ) 
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = CapsuleShape,
        colors = statzGlassTextFieldColors()
    )
}

/** Shared glass-styled text field colors */
@Composable
fun statzGlassTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF1A1A1A),
    unfocusedContainerColor = Color(0xFF1A1A1A),
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = Color.White.copy(alpha = 0.6f),
    unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
    focusedPlaceholderColor = Color.White.copy(alpha = 0.4f),
    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.3f),
    cursorColor = Color.White
)

/**
 * A glass-styled text field matching the dark filled aesthetic.
 * Use this instead of OutlinedTextField for consistent UI.
 */
@Composable
fun StatzGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    textStyle: androidx.compose.ui.text.TextStyle? = null,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = if (label != null) {
            { Text(label) }
        } else null,
        placeholder = if (placeholder != null) {
            { Text(placeholder, color = Color.White.copy(alpha = 0.4f)) }
        } else null,
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        maxLines = maxLines,
        textStyle = textStyle ?: androidx.compose.material3.LocalTextStyle.current,
        shape = if (singleLine) CapsuleShape else RoundedCornerShape(24.dp),
        colors = statzGlassTextFieldColors(),
        keyboardOptions = keyboardOptions
    )
}

/**
 * A glass-styled toggle switch matching the liquid glass aesthetic.
 * Replaces Material3 Switch with a capsule-shaped animated toggle.
 */
@Composable
fun StatzGlassToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val trackWidth = 52.dp
    val trackHeight = 30.dp
    val thumbSize = 22.dp
    val thumbPadding = 4.dp

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - thumbPadding * 2 else 0.dp,
        animationSpec = StatzAnimation.microTween(),
        label = "thumbOffset"
    )

    val backdrop = LocalBackdrop.current

    if (backdrop != null) {
        Box(
            modifier = modifier
                .width(trackWidth)
                .height(trackHeight)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { CapsuleShape },
                    effects = {
                        vibrancy()
                        blur(2f.dp.toPx())
                    },
                    onDrawSurface = {
                        if (checked) {
                            drawRect(accentColor, blendMode = BlendMode.Hue)
                            drawRect(accentColor.copy(alpha = 0.75f))
                        } else {
                            drawRect(Color.White.copy(alpha = 0.08f))
                        }
                    }
                )
                .clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = { onCheckedChange(!checked) }
                )
                .padding(thumbPadding),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(thumbSize)
                    .background(
                        if (checked) Color.White else Color.White.copy(alpha = 0.5f),
                        CircleShape
                    )
            )
        }
    } else {
        // Fallback without backdrop
        val trackColor = if (checked) accentColor.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f)
        Box(
            modifier = modifier
                .width(trackWidth)
                .height(trackHeight)
                .background(trackColor, CapsuleShape)
                .clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = { onCheckedChange(!checked) }
                )
                .padding(thumbPadding),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(thumbSize)
                    .background(
                        if (checked) Color.White else Color.White.copy(alpha = 0.5f),
                        CircleShape
                    )
            )
        }
    }
}
