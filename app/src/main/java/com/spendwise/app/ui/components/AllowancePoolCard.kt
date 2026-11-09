package com.spendwise.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendwise.app.domain.engine.PredictiveAnalyticsEngine
import com.spendwise.app.domain.model.BudgetCycle
import com.spendwise.app.ui.theme.CardBorder
import com.spendwise.app.ui.theme.EmeraldLight
import com.spendwise.app.ui.theme.ExpenseRed
import com.spendwise.app.ui.theme.PrimaryEmerald
import com.spendwise.app.ui.theme.TextMuted
import com.spendwise.app.ui.theme.TextPrimary
import com.spendwise.app.ui.theme.TextSecondary
import com.spendwise.app.ui.theme.WarningAmber

@Composable
fun AllowancePoolCard(
    activeCycle: BudgetCycle?,
    currentPool: Double,
    dailySafeSpend: Double,
    forecast: PredictiveAnalyticsEngine.CashFlowForecast?,
    onNewAllowanceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasActiveAllowance = activeCycle != null && activeCycle.isActive && activeCycle.initialAllowance > 0
    val totalStarting = if (hasActiveAllowance) activeCycle!!.totalStartingPool else 1.0
    val progress = if (hasActiveAllowance && totalStarting > 0) {
        (currentPool / totalStarting).coerceIn(0.0, 1.0).toFloat()
    } else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (hasActiveAllowance) activeCycle!!.title else "Allowance Pool",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (hasActiveAllowance) "₹${"%,.2f".format(currentPool)}" else "₹0.00",
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onNewAllowanceClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryEmerald.copy(alpha = 0.2f),
                        contentColor = EmeraldLight
                    ),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Cycle",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (hasActiveAllowance) "New Allowance" else "Set Allowance",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar of remaining allowance pool
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (progress > 0.25f) PrimaryEmerald else ExpenseRed,
                trackColor = Color(0xFF334155)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Daily Safe Spend Metric
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Burn Rate",
                        tint = EmeraldLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Daily Safe-to-Spend", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = if (hasActiveAllowance) "₹${"%.0f".format(dailySafeSpend)} / day" else "₹0 / day",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (hasActiveAllowance) {
                    val daysElapsed = activeCycle!!.getElapsedDays()
                    val targetDays = activeCycle.targetDays
                    Text(
                        text = "Day $daysElapsed of $targetDays",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "Not set yet",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
