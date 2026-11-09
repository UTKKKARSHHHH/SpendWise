package com.spendwise.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendwise.app.domain.model.Category
import com.spendwise.app.domain.model.TransactionType
import com.spendwise.app.domain.model.Wallet
import com.spendwise.app.ui.theme.CardBackground
import com.spendwise.app.ui.theme.CardBorder
import com.spendwise.app.ui.theme.EmeraldLight
import com.spendwise.app.ui.theme.ExpenseRed
import com.spendwise.app.ui.theme.LocalVeltoColors
import com.spendwise.app.ui.theme.PrimaryEmerald
import com.spendwise.app.ui.theme.TextMuted
import com.spendwise.app.ui.theme.TextPrimary
import com.spendwise.app.ui.theme.TextSecondary
import com.spendwise.app.ui.theme.VeltoForestGreen
import com.spendwise.app.ui.theme.VeltoLime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickEntryBottomSheet(
    initialType: TransactionType,
    wallets: List<Wallet>,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirmNormal: (amount: Double, type: TransactionType, walletId: String, categoryId: String, note: String) -> Unit,
    onConfirmAllowanceTrigger: (amount: Double, walletId: String, categoryId: String, carryForward: Boolean, targetDays: Int) -> Unit
) {
    val colors = LocalVeltoColors.current
    var selectedType by remember { mutableStateOf(initialType) }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var selectedWalletId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: "") }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull { it.type == initialType }?.id ?: "") }

    // Allowance Lifecycle Trigger toggles
    var isAllowanceTrigger by remember { mutableStateOf(initialType == TransactionType.INCOME) }
    var carryForwardLeftover by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.card,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Type Selector Tab (Cash Out vs Cash In)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedType == TransactionType.EXPENSE) ExpenseRed else Color.Transparent)
                        .clickable {
                            selectedType = TransactionType.EXPENSE
                            isAllowanceTrigger = false
                            selectedCategoryId = categories.firstOrNull { it.type == TransactionType.EXPENSE }?.id ?: ""
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Cash Out (Expense)",
                        color = if (selectedType == TransactionType.EXPENSE) Color.White else TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedType == TransactionType.INCOME) PrimaryEmerald else Color.Transparent)
                        .clickable {
                            selectedType = TransactionType.INCOME
                            selectedCategoryId = categories.firstOrNull { it.type == TransactionType.INCOME }?.id ?: ""
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Cash In (Income)",
                        color = if (selectedType == TransactionType.INCOME) Color.White else TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Field (No prefilled amount)
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount (₹)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = if (selectedType == TransactionType.EXPENSE) ExpenseRed else PrimaryEmerald,
                    unfocusedBorderColor = CardBorder
                )
            )

            // Quick Add Chips
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(50, 100, 500, 2000).forEach { addAmount ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF334155))
                            .clickable {
                                val cur = amountText.toDoubleOrNull() ?: 0.0
                                amountText = "%.0f".format(cur + addAmount)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("+$addAmount", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wallet Selection (Displays full actual wallet names)
            Text("Select Wallet", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                wallets.forEach { wallet ->
                    val isSelected = wallet.id == selectedWalletId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) PrimaryEmerald.copy(alpha = 0.2f) else CardBackground)
                            .border(1.dp, if (isSelected) PrimaryEmerald else CardBorder, RoundedCornerShape(10.dp))
                            .clickable { selectedWalletId = wallet.id }
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = wallet.name,
                            color = if (isSelected) EmeraldLight else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Selection Chips
            Text("Category", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.filter { it.type == selectedType }.forEach { cat ->
                    val isSelected = cat.id == selectedCategoryId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFF3B82F6) else Color(0xFF334155))
                            .clickable { selectedCategoryId = cat.id }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat.name,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Optional Note
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = CardBorder,
                    unfocusedBorderColor = CardBorder
                )
            )

            // Allowance Lifecycle Switch if Income
            if (selectedType == TransactionType.INCOME) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F172A))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("New Allowance Lifecycle Trigger", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Starts a fresh cycle pool from this allowance amount", color = TextSecondary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = isAllowanceTrigger,
                                onCheckedChange = { isAllowanceTrigger = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryEmerald)
                            )
                        }

                        if (isAllowanceTrigger) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Carry-Forward Remaining Balance", color = EmeraldLight, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Switch(
                                    checked = carryForwardLeftover,
                                    onCheckedChange = { carryForwardLeftover = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = EmeraldLight)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Button
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt <= 0.0) return@Button

                    val chosenWallet = selectedWalletId.ifBlank { wallets.firstOrNull()?.id ?: "" }
                    val chosenCat = selectedCategoryId.ifBlank { categories.firstOrNull()?.id ?: "" }

                    if (selectedType == TransactionType.INCOME && isAllowanceTrigger) {
                        onConfirmAllowanceTrigger(amt, chosenWallet, chosenCat, carryForwardLeftover, 30)
                    } else {
                        onConfirmNormal(amt, selectedType, chosenWallet, chosenCat, noteText)
                    }
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == TransactionType.EXPENSE) ExpenseRed else PrimaryEmerald
                )
            ) {
                Text(
                    text = if (selectedType == TransactionType.EXPENSE) "Log Expense" else if (isAllowanceTrigger) "Set Allowance & Start Cycle" else "Log Income",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
