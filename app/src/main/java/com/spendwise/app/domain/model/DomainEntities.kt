package com.spendwise.app.domain.model

data class Wallet(
    val id: String,
    val name: String,
    val type: WalletType,
    val currentBalance: Double,
    val currency: String = "INR",
    val isExcludedFromTotal: Boolean = false,
    val colorHex: String = "#3B82F6"
)

data class Category(
    val id: String,
    val name: String,
    val iconKey: String,
    val colorHex: String,
    val type: TransactionType,
    val isCustom: Boolean = false
)

data class Transaction(
    val id: String,
    val cycleId: String,
    val walletId: String,
    val categoryId: String,
    val amount: Double,
    val type: TransactionType,
    val timestamp: Long,
    val note: String = "",
    val receiptImagePath: String? = null,
    val source: String = "MANUAL", // MANUAL, SMS, OCR, VOICE
    val triggersNewCycle: Boolean = false,
    val carryForwardPreviousPool: Boolean = true
)

data class DebtItem(
    val id: String,
    val contactName: String,
    val phoneNumber: String,
    val amount: Double,
    val direction: DebtDirection,
    val note: String = "",
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isSettled: Boolean = false
)

data class TransactionDraft(
    val amount: Double,
    val type: TransactionType,
    val categoryName: String,
    val walletName: String,
    val note: String,
    val confidenceScore: Float = 1.0f,
    val source: String
)
