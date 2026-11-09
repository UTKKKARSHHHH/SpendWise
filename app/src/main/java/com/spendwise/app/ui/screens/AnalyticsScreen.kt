package com.spendwise.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendwise.app.domain.model.TransactionType
import com.spendwise.app.ui.theme.LocalVeltoColors
import com.spendwise.app.ui.theme.PrimaryEmerald
import com.spendwise.app.ui.theme.VeltoForestGreen
import com.spendwise.app.ui.theme.VeltoLime
import com.spendwise.app.ui.theme.WarningAmber
import com.spendwise.app.ui.viewmodel.DashboardViewModel

@Composable
fun AnalyticsScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalVeltoColors.current
    val state by viewModel.uiState.collectAsState()
    val forecast = state.forecast
    val hasAllowance = state.activeCycle != null && state.activeCycle!!.isActive && state.activeCycle!!.initialAllowance > 0

    val expenses = state.transactions.filter { it.type == TransactionType.EXPENSE }
    val totalExpenseSum = expenses.sumOf { it.amount }

    var selectedCycleTab by remember { mutableStateOf(0) }

    val categoryBreakdown = state.categories
        .filter { it.type == TransactionType.EXPENSE }
        .map { cat ->
            val sum = expenses.filter { it.categoryId == cat.id }.sumOf { it.amount }
            val pct = if (totalExpenseSum > 0) (sum / totalExpenseSum).toFloat() else 0f
            Triple(cat, sum, pct)
        }
        .filter { it.second > 0 }
        .sortedByDescending { it.second }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Section (Replicated from Image 1) ---
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "MAKE SENSE OF YOUR MONEY",
                color = colors.textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "A clearer picture.",
                color = colors.textPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1.0).sp
            )
        }

        // --- Tab Switcher (September / October Pills from Image 1) ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (colors.isDark) colors.cardElevated else Color(0xFFE8ECE5))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab 1: Current Cycle
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedCycleTab == 0) colors.card else Color.Transparent)
                        .clickable { selectedCycleTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Current Cycle",
                        color = if (selectedCycleTab == 0) colors.textPrimary else colors.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (selectedCycleTab == 0) FontWeight.Bold else FontWeight.Medium
                    )
                }

                // Tab 2: All Time
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedCycleTab == 1) colors.card else Color.Transparent)
                        .clickable { selectedCycleTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "All Time",
                        color = if (selectedCycleTab == 1) colors.textPrimary else colors.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (selectedCycleTab == 1) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // --- Total Spent & Weekly Bar Chart Card (Replicated from Image 1) ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.card)
                    .border(1.dp, colors.border, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total spent • ${if (selectedCycleTab == 0) "Current cycle" else "All time"}",
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Currency Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (colors.isDark) Color(0xFF1E3227) else Color(0xFFE4EDE1))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "INR",
                                color = colors.textPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "₹${"%,.2f".format(totalExpenseSum)}",
                        color = colors.textPrimary,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.0).sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- Vertical Rounded Bar Chart (Replicating Image 1: 1-6, 7-13, 14-20, 21-27, 28-30) ---
                    val intervals = listOf(
                        "1–6" to 0.45f,
                        "7–13" to 0.75f,
                        "14–20" to 0.40f,
                        "21–27" to 0.90f,
                        "28–30" to 0.55f
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        intervals.forEachIndexed { index, (label, heightFraction) ->
                            val isHighlighted = index == 4 // Highlights the current interval in olive/lime
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(38.dp)
                                        .height((100 * heightFraction).dp)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                        .background(
                                            if (isHighlighted) {
                                                if (colors.isDark) VeltoLime else Color(0xFF758D38)
                                            } else {
                                                if (colors.isDark) Color(0xFF172921) else VeltoForestGreen
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = label,
                                    color = colors.textSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Predictive Velocity Modeling Card ---
        item {
            if (!hasAllowance) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.card)
                        .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = VeltoLime, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("No Active Allowance Pool", color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Set an allowance pool from the Home screen to track velocity modeling and predictive burn rate.",
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            } else if (forecast != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.card)
                        .border(1.dp, if (forecast.isShortagePredicted) WarningAmber else PrimaryEmerald, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (forecast.isShortagePredicted) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (forecast.isShortagePredicted) WarningAmber else PrimaryEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (forecast.isShortagePredicted) "Shortage Pace Detected" else "Healthy Burn Velocity",
                                color = if (forecast.isShortagePredicted) WarningAmber else PrimaryEmerald,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Current Burn Rate", color = colors.textMuted, fontSize = 11.sp)
                                Text("₹${"%.0f".format(forecast.currentDailyBurnRate)} / day", color = colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Recommended Pace", color = colors.textMuted, fontSize = 11.sp)
                                Text("₹${"%.0f".format(forecast.recommendedDailySpend)} / day", color = PrimaryEmerald, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- Where It Went (Category Breakdown from Image 1) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Where it went",
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "${categoryBreakdown.size} categories",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (categoryBreakdown.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.card)
                        .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No expenses logged in this cycle yet.", color = colors.textMuted, fontSize = 13.sp)
                }
            }
        } else {
            items(categoryBreakdown.size) { idx ->
                val (cat, sum, pct) = categoryBreakdown[idx]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.card)
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cat.name, color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("₹${"%,.2f".format(sum)} (${(pct * 100).toInt()}%)", color = colors.textSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = pct,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape),
                            color = if (colors.isDark) VeltoLime else VeltoForestGreen,
                            trackColor = if (colors.isDark) Color(0xFF1E2F26) else Color(0xFFE4EDE1)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
