package com.spendwise.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendwise.app.domain.model.Transaction
import com.spendwise.app.domain.model.TransactionType
import com.spendwise.app.ui.components.QuickEntryBottomSheet
import com.spendwise.app.ui.theme.ExpenseRed
import com.spendwise.app.ui.theme.LocalVeltoColors
import com.spendwise.app.ui.theme.PrimaryEmerald
import com.spendwise.app.ui.theme.VeltoForestGreen
import com.spendwise.app.ui.theme.VeltoLime
import com.spendwise.app.ui.theme.VeltoSage
import com.spendwise.app.ui.theme.VeltoSageDark
import com.spendwise.app.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToVoice: () -> Unit,
    onNavigateToOcr: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToCards: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalVeltoColors.current
    val state by viewModel.uiState.collectAsState()

    var showQuickEntry by remember { mutableStateOf(false) }
    var entryInitialType by remember { mutableStateOf(TransactionType.EXPENSE) }

    val hasAllowance = state.activeCycle != null && state.activeCycle!!.isActive && state.activeCycle!!.initialAllowance > 0
    val totalStarting = if (hasAllowance) state.activeCycle!!.totalStartingPool else 1.0
    val progress = if (hasAllowance && totalStarting > 0) {
        (state.currentAllowancePool / totalStarting).coerceIn(0.0, 1.0).toFloat()
    } else 0f

    val dateFormatted = remember {
        val sdf = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
        sdf.format(Date())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Date & Editorial Header ---
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateFormatted,
                color = colors.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Save For Your Future Plans !!",
                color = colors.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.8).sp
            )
        }

        // --- Signature Forest Green Card ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(VeltoForestGreen)
                    .padding(22.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Available balance",
                            color = Color(0xFFBDC7C1),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        )

                        // Currency Account Pill Dropdown
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1E372E))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "INR account ⌵",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Huge Amount Display
                    Text(
                        text = "₹${"%,.2f".format(state.totalRunningBalance)}",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.2).sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Buttons (Send removed, Lime palette shifted to Save)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 🌐 Accounts (Glass Pill Button -> Navigates to Wallets)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xFF1A332A))
                                .clickable(onClick = onNavigateToCards)
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Accounts",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // + Save (Shifted Yellow/Lime Palette as requested)
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .clip(RoundedCornerShape(22.dp))
                                .background(VeltoLime)
                                .clickable {
                                    entryInitialType = TransactionType.INCOME
                                    showQuickEntry = true
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Save",
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Fast AI Shortcuts (Voice & OCR) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Voice Logging Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.card)
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .clickable(onClick = onNavigateToVoice)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = VeltoLime, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Voice Log", color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // OCR Bill Scanner Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.card)
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .clickable(onClick = onNavigateToOcr)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = VeltoLime, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan Receipt", color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- Goals Section: "Your next chapter" [GOAL] (Replicated from Image 4) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your next chapter",
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (colors.isDark) Color(0xFF1E3227) else Color(0xFFE2EBE0))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "GOAL",
                        color = colors.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Soft Sage Green Goal Card (Matching Image 4)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (colors.isDark) VeltoSageDark else VeltoSage)
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (hasAllowance) state.activeCycle!!.title else "Active Allowance Cycle",
                            color = if (colors.isDark) Color.White else Color(0xFF192A20),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowOutward,
                            contentDescription = null,
                            tint = if (colors.isDark) VeltoLime else VeltoForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (hasAllowance) {
                            "₹${"%,.0f".format(state.currentAllowancePool)} of ₹${"%,.0f".format(totalStarting)}"
                        } else {
                            "Tap to set allowance pool"
                        },
                        color = if (colors.isDark) Color(0xFFBDC7BF) else Color(0xFF4A554E),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Minimalist Progress Bar
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = if (colors.isDark) VeltoLime else VeltoForestGreen,
                        trackColor = if (colors.isDark) Color(0xFF17251D) else Color(0xFFCBD6C2)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = if (colors.isDark) VeltoLime else VeltoForestGreen, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (hasAllowance) "Safe: ₹${"%.0f".format(state.dailySafeToSpend)}/day" else "Not started",
                                color = if (colors.isDark) Color.White else Color(0xFF192A20),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (hasAllowance) {
                            val elapsed = state.activeCycle!!.getElapsedDays()
                            val target = state.activeCycle!!.targetDays
                            Text(
                                text = "Day $elapsed of $target",
                                color = if (colors.isDark) Color(0xFFBDC7BF) else Color(0xFF4A554E),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // --- Recent Activity Section (Replicated from Image 4) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent activity",
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "See all",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (state.transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.card)
                        .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions logged yet.", color = colors.textMuted, fontSize = 13.sp)
                }
            }
        } else {
            items(state.transactions.take(8), key = { it.id }) { tx ->
                VeltoActivityItem(tx = tx)
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    // Modal Sheet for quick entry
    if (showQuickEntry) {
        QuickEntryBottomSheet(
            initialType = entryInitialType,
            wallets = state.wallets,
            categories = state.categories,
            onDismiss = { showQuickEntry = false },
            onConfirmNormal = { amount, type, walletId, categoryId, note ->
                viewModel.logQuickTransaction(amount, type, walletId, categoryId, note)
            },
            onConfirmAllowanceTrigger = { amount, walletId, categoryId, carryForward, targetDays ->
                viewModel.logAllowanceTrigger(amount, walletId, categoryId, carryForward, targetDays = targetDays)
            }
        )
    }
}

// --- Activity Row Item (Matching Fika Coffee / Studio Payroll from Image 4) ---
@Composable
private fun VeltoActivityItem(tx: Transaction) {
    val colors = LocalVeltoColors.current
    val isExpense = tx.type == TransactionType.EXPENSE

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Rounded Square Icon Container (as in Image 4)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (colors.isDark) Color(0xFF1C2C24) else Color(0xFFE8ECE5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpense) Icons.Default.ShoppingBag else Icons.Default.Add,
                        contentDescription = null,
                        tint = colors.textPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (tx.note.isNotBlank()) tx.note else "Transaction",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(tx.timestamp))} • ${tx.source}",
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = "${if (isExpense) "-" else "+"}₹${"%,.2f".format(tx.amount)}",
                color = if (isExpense) colors.textPrimary else PrimaryEmerald,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
