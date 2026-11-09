package com.spendwise.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendwise.app.domain.model.DebtDirection
import com.spendwise.app.domain.model.DebtItem
import com.spendwise.app.domain.model.Wallet
import com.spendwise.app.ui.theme.ExpenseRed
import com.spendwise.app.ui.theme.LocalVeltoColors
import com.spendwise.app.ui.theme.PrimaryEmerald
import com.spendwise.app.ui.theme.VeltoForestGreen
import com.spendwise.app.ui.theme.VeltoLime
import com.spendwise.app.ui.viewmodel.DashboardViewModel

@Composable
fun CreditBookScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalVeltoColors.current
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedDebtDetail by remember { mutableStateOf<DebtItem?>(null) }
    var itemToSettle by remember { mutableStateOf<DebtItem?>(null) }
    var selectedFilterTab by remember { mutableStateOf(DebtDirection.YOU_ARE_OWED) }

    val owedToYou = state.debtItems.filter { it.direction == DebtDirection.YOU_ARE_OWED && !it.isSettled }
    val youOwe = state.debtItems.filter { it.direction == DebtDirection.YOU_OWE && !it.isSettled }
    val totalOwedToYou = owedToYou.sumOf { it.amount }
    val totalYouOwe = youOwe.sumOf { it.amount }

    val filteredList = state.debtItems.filter { it.direction == selectedFilterTab }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas)
            .padding(horizontal = 20.dp)
    ) {
        // --- 3-Step Progress Line (Replicated from Image 3) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (colors.isDark) VeltoLime else VeltoForestGreen)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.border)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.border)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Editorial Section Header (Replicated from Image 3)
        Text(
            text = "01 / THE RIGHT PERSON",
            color = colors.textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Who's it for?",
            color = colors.textPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1.0).sp
        )
        Text(
            text = "Record money lent or borrowed, settle balances, and send reminders.",
            color = colors.textSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Summary Overview Banners (Owed vs Owe) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Owed to you card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.card)
                    .border(1.dp, if (selectedFilterTab == DebtDirection.YOU_ARE_OWED) (if (colors.isDark) VeltoLime else VeltoForestGreen) else colors.border, RoundedCornerShape(18.dp))
                    .clickable { selectedFilterTab = DebtDirection.YOU_ARE_OWED }
                    .padding(14.dp)
            ) {
                Column {
                    Text("You Are Owed", color = colors.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹${"%,.2f".format(totalOwedToYou)}",
                        color = PrimaryEmerald,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("${owedToYou.size} Pending", color = colors.textMuted, fontSize = 10.sp)
                }
            }

            // You owe card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.card)
                    .border(1.dp, if (selectedFilterTab == DebtDirection.YOU_OWE) ExpenseRed else colors.border, RoundedCornerShape(18.dp))
                    .clickable { selectedFilterTab = DebtDirection.YOU_OWE }
                    .padding(14.dp)
            ) {
                Column {
                    Text("You Owe", color = colors.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹${"%,.2f".format(totalYouOwe)}",
                        color = ExpenseRed,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("${youOwe.size} To Pay", color = colors.textMuted, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Prominent Add Action Button
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (colors.isDark) VeltoLime else VeltoForestGreen,
                contentColor = if (colors.isDark) Color.Black else Color.White
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Add Khata / Credit Entry",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Recipient Cards (Replicating Image 3 Maya Chen / Leo Martin style) ---
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(colors.card)
                            .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (selectedFilterTab == DebtDirection.YOU_ARE_OWED) "No receivables recorded" else "No payables recorded",
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Tap 'Add Khata Entry' above to track someone.",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { item ->
                    VeltoRecipientCard(
                        item = item,
                        onClick = { selectedDebtDetail = item }
                    )
                }
            }

            // Info Notice (Matching Image 3)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (colors.isDark) Color(0xFF14241C) else Color(0xFFE8EFE5))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = if (colors.isDark) VeltoLime else VeltoForestGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Tap on any contact row to settle payment directly with any wallet, or send automatic WhatsApp / SMS payment reminders.",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // --- Add Debt Entry Dialog (Completely Responsive & Validated) ---
    if (showAddDialog) {
        AddDebtItemDialog(
            initialDirection = selectedFilterTab,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, amount, direction, note ->
                viewModel.addDebtItem(name, phone, amount, direction, note)
                showAddDialog = false
                Toast.makeText(context, "Added khata entry for $name", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // --- Action Sheet / Detail Dialog on Tapping Recipient ---
    selectedDebtDetail?.let { item ->
        RecipientActionDialog(
            item = item,
            onDismiss = { selectedDebtDetail = null },
            onSettle = {
                itemToSettle = item
                selectedDebtDetail = null
            },
            onWhatsApp = {
                viewModel.sendWhatsAppReminder(context, item)
            },
            onSms = {
                sendDirectSms(context, item)
            },
            onDelete = {
                viewModel.deleteDebtItem(item.id)
                selectedDebtDetail = null
                Toast.makeText(context, "Entry removed", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // --- Settle with Wallet Dialog ---
    itemToSettle?.let { debt ->
        SettleDebtDialog(
            debtItem = debt,
            wallets = state.wallets,
            onDismiss = { itemToSettle = null },
            onConfirm = { selectedWalletId ->
                viewModel.settleDebtWithWallet(debt, selectedWalletId)
                itemToSettle = null
                Toast.makeText(context, "Settled successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// --- Velto Recipient Card (Replicated from Image 3 Maya Chen / Leo Martin style) ---
@Composable
private fun VeltoRecipientCard(
    item: DebtItem,
    onClick: () -> Unit
) {
    val colors = LocalVeltoColors.current
    val isOwed = item.direction == DebtDirection.YOU_ARE_OWED

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
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
                // Circle Avatar (as in Image 3)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (colors.isDark) Color(0xFF1D2E25) else Color(0xFFE7ECE4)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.contactName.take(2).uppercase(),
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.contactName,
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (item.phoneNumber.isNotBlank()) item.phoneNumber else (if (item.note.isNotBlank()) item.note else "Contact"),
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${"%,.2f".format(item.amount)}",
                        color = if (isOwed) PrimaryEmerald else ExpenseRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (item.isSettled) "Settled" else (if (isOwed) "You are owed" else "You owe"),
                        color = colors.textMuted,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// --- Recipient Detail & Actions Dialog ---
@Composable
private fun RecipientActionDialog(
    item: DebtItem,
    onDismiss: () -> Unit,
    onSettle: () -> Unit,
    onWhatsApp: () -> Unit,
    onSms: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalVeltoColors.current
    val isOwed = item.direction == DebtDirection.YOU_ARE_OWED

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = item.contactName,
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${if (isOwed) "Owes you" else "You owe"}: ₹${"%,.2f".format(item.amount)}",
                    color = if (isOwed) PrimaryEmerald else ExpenseRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        containerColor = colors.card,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (item.phoneNumber.isNotBlank()) {
                    Text("Phone: ${item.phoneNumber}", color = colors.textSecondary, fontSize = 13.sp)
                }
                if (item.note.isNotBlank()) {
                    Text("Reason: ${item.note}", color = colors.textSecondary, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons
                Button(
                    onClick = onSettle,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (colors.isDark) VeltoLime else VeltoForestGreen,
                        contentColor = if (colors.isDark) Color.Black else Color.White
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mark as Settled with Wallet", fontWeight = FontWeight.Bold)
                }

                if (isOwed) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onWhatsApp,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onSms,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                        ) {
                            Icon(Icons.Default.Message, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SMS", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = colors.textSecondary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete", color = ExpenseRed)
            }
        }
    )
}

// --- Add Debt Item Dialog (Zero Placeholders, Clean Validation) ---
@Composable
private fun AddDebtItemDialog(
    initialDirection: DebtDirection,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, amount: Double, direction: DebtDirection, note: String) -> Unit
) {
    val colors = LocalVeltoColors.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf(initialDirection) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Khata / Credit Entry", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
        containerColor = colors.card,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Direction Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (colors.isDark) colors.cardElevated else Color(0xFFEAEFE7))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (direction == DebtDirection.YOU_ARE_OWED) (if (colors.isDark) VeltoLime else VeltoForestGreen) else Color.Transparent)
                            .clickable { direction = DebtDirection.YOU_ARE_OWED }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "I Am Owed",
                            color = if (direction == DebtDirection.YOU_ARE_OWED) (if (colors.isDark) Color.Black else Color.White) else colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (direction == DebtDirection.YOU_OWE) ExpenseRed else Color.Transparent)
                            .clickable { direction = DebtDirection.YOU_OWE }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "I Owe",
                            color = if (direction == DebtDirection.YOU_OWE) Color.White else colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Reason") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                if (errorMsg != null) {
                    Text(errorMsg!!, color = ExpenseRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (name.isBlank()) {
                        errorMsg = "Please enter contact name"
                        return@Button
                    }
                    if (amt <= 0.0) {
                        errorMsg = "Please enter a valid amount"
                        return@Button
                    }
                    onConfirm(name.trim(), phone.trim(), amt, direction, note.trim())
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (colors.isDark) VeltoLime else VeltoForestGreen,
                    contentColor = if (colors.isDark) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textSecondary)
            }
        }
    )
}

// --- Settle Debt Dialog ---
@Composable
private fun SettleDebtDialog(
    debtItem: DebtItem,
    wallets: List<Wallet>,
    onDismiss: () -> Unit,
    onConfirm: (selectedWalletId: String) -> Unit
) {
    val colors = LocalVeltoColors.current
    var selectedWalletId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Settle Payment", color = colors.textPrimary, fontWeight = FontWeight.Bold)
        },
        containerColor = colors.card,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Select wallet to adjust for ₹${"%,.2f".format(debtItem.amount)} (${debtItem.contactName}):",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )

                if (wallets.isEmpty()) {
                    Text("No wallets available. Please create a wallet first.", color = ExpenseRed, fontSize = 12.sp)
                } else {
                    wallets.forEach { w ->
                        val isSelected = w.id == selectedWalletId
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) (if (colors.isDark) VeltoLime.copy(alpha = 0.2f) else VeltoForestGreen.copy(alpha = 0.1f)) else colors.card)
                                .border(1.dp, if (isSelected) (if (colors.isDark) VeltoLime else VeltoForestGreen) else colors.border, RoundedCornerShape(12.dp))
                                .clickable { selectedWalletId = w.id }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(w.name, color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("₹${"%,.2f".format(w.currentBalance)}", color = colors.textSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedWalletId.isNotBlank()) {
                        onConfirm(selectedWalletId)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (colors.isDark) VeltoLime else VeltoForestGreen,
                    contentColor = if (colors.isDark) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = selectedWalletId.isNotBlank()
            ) {
                Text("Confirm Settlement")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textSecondary)
            }
        }
    )
}

private fun sendDirectSms(context: Context, item: DebtItem) {
    val cleanPhone = item.phoneNumber.replace("[^0-9+]".toRegex(), "")
    val msg = "Hi ${item.contactName}, gentle reminder regarding pending amount of ₹${"%.2f".format(item.amount)} (${item.note}) via Spendwise."
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("sms:$cleanPhone")
            putExtra("sms_body", msg)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not launch SMS app", Toast.LENGTH_SHORT).show()
    }
}
