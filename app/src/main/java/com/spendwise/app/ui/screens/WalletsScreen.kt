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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.QrCode
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendwise.app.domain.model.Wallet
import com.spendwise.app.domain.model.WalletType
import com.spendwise.app.ui.theme.ExpenseRed
import com.spendwise.app.ui.theme.LocalVeltoColors
import com.spendwise.app.ui.theme.PrimaryEmerald
import com.spendwise.app.ui.theme.VeltoForestGreen
import com.spendwise.app.ui.theme.VeltoLime
import com.spendwise.app.ui.viewmodel.DashboardViewModel

@Composable
fun WalletsScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalVeltoColors.current
    val state by viewModel.uiState.collectAsState()

    var selectedWalletToEdit by remember { mutableStateOf<Wallet?>(null) }
    var showAddWalletDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Section ---
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "MULTI-WALLET LEDGERS",
                color = colors.textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Accounts & Ledgers",
                color = colors.textPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1.0).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Manage your physical cash, bank accounts, and digital wallets.",
                color = colors.textSecondary,
                fontSize = 13.sp
            )
        }

        // --- Action Button: Add New Wallet ---
        item {
            Button(
                onClick = { showAddWalletDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (colors.isDark) VeltoLime else VeltoForestGreen,
                    contentColor = if (colors.isDark) Color.Black else Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Custom Wallet",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap any wallet below to view, rename, or manually adjust its balance.",
                color = colors.textSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }

        // --- Controls Section Header ---
        item {
            Text(
                text = "Your Wallets",
                color = colors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
        }

        // --- Wallet Ledger Cards ---
        if (state.wallets.isEmpty()) {
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No wallets found", color = colors.textSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAddWalletDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (colors.isDark) VeltoLime else VeltoForestGreen,
                                contentColor = if (colors.isDark) Color.Black else Color.White
                            )
                        ) {
                            Text("Create Wallet")
                        }
                    }
                }
            }
        } else {
            items(state.wallets, key = { it.id }) { wallet ->
                VeltoWalletLedgerRow(
                    wallet = wallet,
                    txCount = state.transactions.count { it.walletId == wallet.id },
                    onClick = { selectedWalletToEdit = wallet }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    // --- Edit Wallet Dialog ---
    selectedWalletToEdit?.let { wallet ->
        EditWalletDialog(
            wallet = wallet,
            onDismiss = { selectedWalletToEdit = null },
            onSave = { newName, newBalance ->
                viewModel.updateWalletNameAndBalance(wallet.id, newName, newBalance)
                selectedWalletToEdit = null
            },
            onDelete = {
                viewModel.deleteWallet(wallet.id)
                selectedWalletToEdit = null
            }
        )
    }

    // --- Add Wallet Dialog ---
    if (showAddWalletDialog) {
        AddWalletDialog(
            onDismiss = { showAddWalletDialog = false },
            onConfirm = { name, type, balance ->
                viewModel.addNewCustomWallet(name, type, balance)
                showAddWalletDialog = false
            }
        )
    }
}

// --- Velto Wallet Row Item (Zero "Available" text) ---
@Composable
private fun VeltoWalletLedgerRow(
    wallet: Wallet,
    txCount: Int,
    onClick: () -> Unit
) {
    val colors = LocalVeltoColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
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
                // Circle Icon Container
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (colors.isDark) Color(0xFF1B2E24) else Color(0xFFE4ECE2)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (wallet.type) {
                        WalletType.PHYSICAL_CASH -> Icons.Default.Money
                        WalletType.BANK_ACCOUNT -> Icons.Default.AccountBalance
                        WalletType.DIGITAL_WALLET -> Icons.Default.QrCode
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (colors.isDark) VeltoLime else VeltoForestGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = wallet.name,
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${wallet.type.name.replace("_", " ")} • $txCount logs",
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${"%,.2f".format(wallet.currentBalance)}",
                    color = colors.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- Edit Wallet Dialog ---
@Composable
private fun EditWalletDialog(
    wallet: Wallet,
    onDismiss: () -> Unit,
    onSave: (newName: String, newBalance: Double) -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalVeltoColors.current
    var name by remember { mutableStateOf(wallet.name) }
    var balanceText by remember { mutableStateOf("%.2f".format(wallet.currentBalance)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Wallet", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
        containerColor = colors.card,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Wallet Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Wallet Balance (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bal = balanceText.toDoubleOrNull() ?: wallet.currentBalance
                    if (name.isNotBlank()) {
                        onSave(name.trim(), bal)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (colors.isDark) VeltoLime else VeltoForestGreen,
                    contentColor = if (colors.isDark) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", color = ExpenseRed)
                }
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        }
    )
}

// --- Add Wallet Dialog ---
@Composable
private fun AddWalletDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: WalletType, initialBalance: Double) -> Unit
) {
    val colors = LocalVeltoColors.current
    var name by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("0") }
    var selectedType by remember { mutableStateOf(WalletType.BANK_ACCOUNT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Wallet", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
        containerColor = colors.card,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Wallet Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Initial Balance (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                Text("Wallet Type", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    WalletType.values().forEach { type ->
                        val isSelected = type == selectedType
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) (if (colors.isDark) VeltoLime else VeltoForestGreen) else (if (colors.isDark) colors.cardElevated else Color(0xFFE8ECE4)))
                                .clickable { selectedType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.name.split("_").first(),
                                color = if (isSelected) (if (colors.isDark) Color.Black else Color.White) else colors.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bal = balanceText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), selectedType, bal)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (colors.isDark) VeltoLime else VeltoForestGreen,
                    contentColor = if (colors.isDark) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Create Wallet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textSecondary)
            }
        }
    )
}
