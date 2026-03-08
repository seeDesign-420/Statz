package com.statz.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.statz.app.data.repository.MonthDashboard
import com.statz.app.domain.model.CategoryType
import com.statz.app.domain.model.MoneyUtils
import com.statz.app.ui.theme.Primary
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie

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

/**
 * Unit Performance section showing per-category progress rows.
 */
@Composable
fun CategoryBarChart(
    dashboard: MonthDashboard,
    modifier: Modifier = Modifier
) {
    val unitCategories = dashboard.categories.filter { it.category.type == CategoryType.UNIT }
    if (unitCategories.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "UNIT PERFORMANCE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 2.sp
        )

        unitCategories.forEach { cat ->
            CategoryProgressRow(
                name = cat.category.name,
                actual = cat.actual,
                target = cat.target,
                isMoney = false,
                progressPercent = cat.progressPercent
            )
        }
    }
}

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
/**
 * Donut chart showing the split between two unit categories (e.g., New Lines vs Upgrades).
 */
@Composable
fun UnitSplitDonut(
    newLines: Long,
    upgrades: Long,
    modifier: Modifier = Modifier
) {
    if (newLines == 0L && upgrades == 0L) return

    val colors = listOf(
        Primary,
        Color(0xFFD9F522), // Neon Lime
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PieChart(
            modifier = Modifier.size(120.dp),
            data = listOf(
                Pie(label = "New Lines", data = newLines.toDouble(), color = colors[0], selectedColor = colors[0]),
                Pie(label = "Upgrades", data = upgrades.toDouble(), color = colors[1], selectedColor = colors[1])
            ),
            style = Pie.Style.Stroke(width = 18.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(10.dp).background(colors[0], RoundedCornerShape(2.dp)))
                Column {
                    Text("NEW LINES", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f), letterSpacing = 1.sp)
                    Text("$newLines", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(10.dp).background(colors[1], RoundedCornerShape(2.dp)))
                Column {
                    Text("UPGRADES", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f), letterSpacing = 1.sp)
                    Text("$upgrades", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
