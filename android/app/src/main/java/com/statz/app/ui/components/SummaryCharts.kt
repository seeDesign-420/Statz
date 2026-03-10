package com.statz.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import com.statz.app.ui.theme.StatzAnimation
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.statz.app.data.repository.MonthDashboard
import com.statz.app.domain.model.CategoryType
import com.statz.app.domain.model.MoneyUtils
import com.statz.app.ui.theme.Primary
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.Pie

// ── Custom Rounded Donut ────────────────────────────────────────

/**
 * Premium donut chart with rounded segment caps, drawn directly
 * on a Compose Canvas for maximum fidelity.
 */
@Composable
fun RoundedDonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 28.dp,
    gapDegrees: Float = 8f
) {
    val total = segments.sumOf { it.value }
    if (total <= 0.0) return

    Canvas(modifier = modifier) {
        val diameter = size.minDimension
        val strokePx = strokeWidth.toPx()
        val topLeft = Offset(
            (size.width - diameter) / 2f + strokePx / 2f,
            (size.height - diameter) / 2f + strokePx / 2f
        )
        val arcSize = Size(diameter - strokePx, diameter - strokePx)

        val totalGap = gapDegrees * segments.size
        val availableDegrees = 360f - totalGap
        var startAngle = -90f // Start from top

        segments.forEach { segment ->
            val sweep = (segment.value / total).toFloat() * availableDegrees
            drawArc(
                color = segment.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            startAngle += sweep + gapDegrees
        }
    }
}

data class DonutSegment(
    val label: String,
    val value: Double,
    val color: Color
)

// ── Revenue Wave Chart ──────────────────────────────────────────

/**
 * A smooth, curved line chart with gradient fill showing
 * daily revenue trends within a month. Uses a 3-layer setup
 * to perfectly trace a volumetric neon glow over a deep area fill.
 */
@Composable
fun RevenueWaveChart(
    values: List<Double>,
    modifier: Modifier = Modifier
) {
    if (values.size < 2) return

    Box(modifier = modifier) {
        // Layer 1: Area Fill Only
        // Strong gradient falling from the curve to transparency
        LineChart(
            modifier = Modifier.fillMaxSize(),
            data = remember(values) {
                listOf(
                    Line(
                        label = "Revenue_Fill",
                        values = values,
                        color = SolidColor(Color.Transparent),
                        firstGradientFillColor = Primary.copy(alpha = 0.5f), // Stronger start
                        secondGradientFillColor = Color.Transparent,
                        drawStyle = DrawStyle.Stroke(width = 0.dp),
                        curvedEdges = true
                    )
                )
            },
            curvedEdges = true,
            indicatorProperties = HorizontalIndicatorProperties(enabled = false),
            gridProperties = GridProperties(enabled = false),
            labelProperties = LabelProperties(enabled = false),
            labelHelperProperties = LabelHelperProperties(enabled = false),
            animationDelay = 0,
            maxValue = values.max() * 1.1,
            minValue = 0.0
        )

        // Layer 2: Outer Volumetric Glow
        // Uses true Canvas blur modifier for neon bloom dispersion
        LineChart(
            modifier = Modifier
                .fillMaxSize()
                .blur(16.dp), // TRUE volumetric blur applied only to the glow layer
            data = remember(values) {
                listOf(
                    Line(
                        label = "Revenue_Glow",
                        values = values,
                        color = SolidColor(Primary.copy(alpha = 0.8f)), // High opacity, dispersed by blur
                        firstGradientFillColor = Color.Transparent,
                        secondGradientFillColor = Color.Transparent,
                        drawStyle = DrawStyle.Stroke(width = 8.dp),
                        curvedEdges = true
                    )
                )
            },
            curvedEdges = true,
            indicatorProperties = HorizontalIndicatorProperties(enabled = false),
            gridProperties = GridProperties(enabled = false),
            labelProperties = LabelProperties(enabled = false),
            labelHelperProperties = LabelHelperProperties(enabled = false),
            animationDelay = 0,
            maxValue = values.max() * 1.1,
            minValue = 0.0
        )

        // Layer 3: Inner Solid Core
        // Sharp, bright, solid orange wire 
        LineChart(
            modifier = Modifier.fillMaxSize(),
            data = remember(values) {
                listOf(
                    Line(
                        label = "Revenue_Core",
                        values = values,
                        color = SolidColor(Primary), // Solid distinct orange
                        firstGradientFillColor = Color.Transparent,
                        secondGradientFillColor = Color.Transparent,
                        drawStyle = DrawStyle.Stroke(width = 2.dp),
                        curvedEdges = true
                    )
                )
            },
            curvedEdges = true,
            indicatorProperties = HorizontalIndicatorProperties(enabled = false),
            gridProperties = GridProperties(enabled = false),
            labelProperties = LabelProperties(enabled = false),
            labelHelperProperties = LabelHelperProperties(enabled = false),
            animationDelay = 0,
            maxValue = values.max() * 1.1,
            minValue = 0.0
        )

        // Layer 4: Peak Glow Highlight (native Canvas)
        val maxVal = values.maxOrNull() ?: 0.0
        val maxIndex = values.indexOf(maxVal)
        if (maxIndex >= 0) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val chartCeiling = maxVal * 1.1
                val xPct = maxIndex.toFloat() / (values.size - 1).coerceAtLeast(1)
                val yPct = if (chartCeiling > 0) (1f - (maxVal / chartCeiling).toFloat()) else 1f

                val peakX = size.width * xPct
                val peakY = size.height * yPct

                // Outer radial glow (large, soft, dense orange bloom)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Primary.copy(alpha = 0.8f),
                            Primary.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        center = Offset(peakX, peakY),
                        radius = 80f // Increased bloom
                    ),
                    radius = 80f,
                    center = Offset(peakX, peakY)
                )

                // Inner bright core dot
                drawCircle(
                    color = Color.White,
                    radius = 4f, // Slightly larger
                    center = Offset(peakX, peakY)
                )
            }
        }
    }
}

// ── Unit Target Donut ───────────────────────────────────────────

/**
 * Hero Donut Chart showing total units progress against targets.
 */
@Composable
fun UnitTargetDonut(
    dashboard: MonthDashboard,
    modifier: Modifier = Modifier
) {
    val unitCategories = dashboard.categories.filter { it.category.type == CategoryType.UNIT }
    val totalTarget = unitCategories.sumOf { it.target }
    val currentActual = dashboard.totalUnits

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        PieChart(
            modifier = Modifier.size(220.dp),
            data = listOf(
                Pie(
                    label = "Actual",
                    data = currentActual.toDouble(),
                    color = Primary,
                    selectedColor = Primary
                ),
                Pie(
                    label = "Remaining",
                    data = if (totalTarget > currentActual) (totalTarget - currentActual).toDouble() else 0.1,
                    color = Color.White.copy(alpha = 0.05f),
                    selectedColor = Color.White.copy(alpha = 0.1f)
                )
            ),
            style = Pie.Style.Stroke(width = 32.dp)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${currentActual} / ${totalTarget}",
                style = MaterialTheme.typography.headlineLarge.copy(fontFamily = FontFamily.Monospace),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "UNITS SOLD",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 2.sp
            )
        }
    }
}

// ── Category Metric Grid ────────────────────────────────────────

/**
 * 2×2 grid of compact metric cards showing per-category unit performance.
 * Each card contains: category name, actual/target, and a thin progress bar.
 */
@Composable
fun CategoryMetricGrid(
    dashboard: MonthDashboard,
    modifier: Modifier = Modifier
) {
    val unitCategories = dashboard.categories.filter { it.category.type == CategoryType.UNIT }
    if (unitCategories.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "UNIT PERFORMANCE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 2.sp
        )

        // Render categories in rows of 2
        unitCategories.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { cat ->
                    MetricCell(
                        name = cat.category.name,
                        actual = cat.actual,
                        target = cat.target,
                        progressPercent = cat.progressPercent,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty space if odd number of categories
                if (rowItems.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * A single compact metric card for the 2×2 grid.
 * Shows category name, actual/target fraction, and a thin progress bar.
 */
@Composable
private fun MetricCell(
    name: String,
    actual: Long,
    target: Long,
    progressPercent: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (target == 0L) 0f else (actual.toFloat() / target.toFloat()).coerceIn(0f, 1f),
        animationSpec = StatzAnimation.easeSpring(),
        label = "metricProgress"
    )
    val isComplete = actual >= target && target > 0

    Column(
        modifier = modifier
            .background(
                Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$actual / $target",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        // Thin progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    Color.White.copy(alpha = 0.08f),
                    RoundedCornerShape(2.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(
                        if (isComplete) Color(0xFF22C55E) else Primary,
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

// ── Revenue Donut ───────────────────────────────────────────────

/**
 * Donut chart showing revenue breakdown between categories.
 */
@Composable
fun RevenueSplitDonut(
    dashboard: MonthDashboard,
    modifier: Modifier = Modifier
) {
    val revenueCategories = dashboard.categories.filter { it.category.type == CategoryType.MONEY && it.actual > 0 }
    if (revenueCategories.isEmpty()) return

    val colors = listOf(
        Primary,
        Color(0xFFD9F522), // Neon Lime
        Color(0xFF3B82F6), // Blue
        Color(0xFFA855F7), // Purple
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        PieChart(
            modifier = Modifier.size(140.dp),
            data = revenueCategories.mapIndexed { index, cat ->
                Pie(
                    label = cat.category.name,
                    data = cat.actual.toDouble(),
                    color = colors[index % colors.size],
                    selectedColor = colors[index % colors.size]
                )
            },
            style = Pie.Style.Stroke(width = 20.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            revenueCategories.forEachIndexed { index, cat ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(10.dp).background(colors[index % colors.size], RoundedCornerShape(2.dp)))
                    Column {
                        Text(cat.category.name, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                        Text(MoneyUtils.centsToDisplay(cat.actual), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ── Multi-Ring Progress Chart ────────────────────────────────────

/**
 * Data for a single ring in the multi-ring donut chart.
 */
data class RingData(
    val label: String,
    val actual: Long,
    val target: Long,
    val color: Color,
    val isMoney: Boolean = false
) {
    val progress: Float
        get() = if (target > 0) (actual.toFloat() / target).coerceIn(0f, 1f) else 0f
    val percent: Int
        get() = (progress * 100).toInt()
}

/**
 * Premium multi-ring concentric donut chart with 3D depth effects.
 * Each ring shows actual/target progress for a category group.
 * Features: drop shadow, sweep gradient stroke, specular highlight.
 */
@Composable
fun MultiRingProgressChart(
    rings: List<RingData>,
    modifier: Modifier = Modifier,
    ringWidth: Dp = 14.dp,
    ringGap: Dp = 8.dp
) {
    if (rings.isEmpty()) return

    // Animate each ring's progress
    val animatedProgresses = rings.map { ring ->
        animateFloatAsState(
            targetValue = ring.progress,
            animationSpec = StatzAnimation.easeSpring(),
            label = "ring_${ring.label}"
        ).value
    }

    Canvas(modifier = modifier) {
        val strokePx = ringWidth.toPx()
        val gapPx = ringGap.toPx()
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val shadowOffset = strokePx * 0.15f

        // Outermost ring gets the largest radius
        val maxRadius = (size.minDimension / 2f) - (strokePx / 2f)

        rings.forEachIndexed { index, ring ->
            val radius = maxRadius - (index * (strokePx + gapPx))
            if (radius <= 0f) return@forEachIndexed

            val topLeft = Offset(centerX - radius, centerY - radius)
            val arcSize = Size(radius * 2, radius * 2)

            // Background track
            drawArc(
                color = ring.color.copy(alpha = 0.20f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress arc with 3D effects
            val sweep = animatedProgresses[index] * 360f
            if (sweep > 0f) {
                val clampedSweep = sweep.coerceAtMost(359.9f)

                // 1) Drop shadow — slightly offset and more transparent
                drawArc(
                    color = ring.color.copy(alpha = 0.25f),
                    startAngle = -90f,
                    sweepAngle = clampedSweep,
                    useCenter = false,
                    topLeft = Offset(topLeft.x + shadowOffset, topLeft.y + shadowOffset),
                    size = arcSize,
                    style = Stroke(width = strokePx * 1.1f, cap = StrokeCap.Round)
                )

                // 2) Main progress arc with sweep gradient for dimensionality
                val darkVariant = ring.color.copy(
                    red = ring.color.red * 0.6f,
                    green = ring.color.green * 0.6f,
                    blue = ring.color.blue * 0.6f
                )
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to darkVariant,
                        0.5f to ring.color,
                        1f to darkVariant,
                        center = Offset(centerX, centerY)
                    ),
                    startAngle = -90f,
                    sweepAngle = clampedSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // 3) Specular highlight — thin bright arc at the top for light source effect
                val highlightSweep = clampedSweep.coerceAtMost(90f)
                drawArc(
                    color = Color.White.copy(alpha = 0.15f),
                    startAngle = -90f,
                    sweepAngle = highlightSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx * 0.4f, cap = StrokeCap.Round)
                )
            }
        }
    }
}

/**
 * Sales Breakdown card content with multi-ring donut chart.
 * Groups unit categories into New Lines / Upgrades / Other and shows
 * progress against targets with a premium concentric ring design.
 */
@Composable
fun SalesBreakdownChart(
    dashboard: MonthDashboard,
    modifier: Modifier = Modifier
) {
    val newLineIds = setOf("new", "sme_new", "ec_new")
    val upgradeIds = setOf("upgrade", "sme_up", "ec_upgd")

    val unitCategories = dashboard.categories.filter { it.category.type == CategoryType.UNIT }
    if (unitCategories.isEmpty()) return

    val newLinesActual = unitCategories.filter { it.category.id in newLineIds }.sumOf { it.actual }
    val newLinesTarget = unitCategories.filter { it.category.id in newLineIds }.sumOf { it.target }
    val upgradesActual = unitCategories.filter { it.category.id in upgradeIds }.sumOf { it.actual }
    val upgradesTarget = unitCategories.filter { it.category.id in upgradeIds }.sumOf { it.target }

    // Accessories (MONEY category) — show revenue progress as a ring
    val accessoriesCat = dashboard.categories.find { it.category.id == "accessories" }
    val accessoriesActual = accessoriesCat?.actual ?: 0L
    val accessoriesTarget = accessoriesCat?.target ?: 0L

    val totalActual = dashboard.totalUnits

    val ringColors = listOf(
        Color(0xFF7C4DFF), // Purple - outermost
        Primary,            // Primary Orange
        Color(0xFF00E5CC)   // Teal - innermost
    )

    val rings = listOf(
        RingData("New Lines", newLinesActual, newLinesTarget, ringColors[0]),
        RingData("Upgrades", upgradesActual, upgradesTarget, ringColors[1]),
        RingData("Accessories", accessoriesActual, accessoriesTarget, ringColors[2], isMoney = true)
    ).filter { it.target > 0 || it.actual > 0 }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "SALES BREAKDOWN",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.6f)
        )

        // Chart + center text
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            MultiRingProgressChart(
                rings = rings,
                modifier = Modifier.size(200.dp),
                ringWidth = 14.dp,
                ringGap = 8.dp
            )

            // Center text — total units sold
            Text(
                text = "$totalActual",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = FontFamily.Monospace
                ),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Enriched legend with actual/target and percentage
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            rings.forEach { ring ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(ring.color, CircleShape)
                        )
                        Text(
                            text = ring.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (ring.isMoney) {
                                "${MoneyUtils.centsToDisplay(ring.actual)} / ${MoneyUtils.centsToDisplay(ring.target)}"
                            } else {
                                "${ring.actual} / ${ring.target}"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "${ring.percent}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = ring.color
                        )
                    }
                }
            }
        }
    }
}
