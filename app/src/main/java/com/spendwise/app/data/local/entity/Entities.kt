package com.spendwise.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spendwise.app.domain.model.BudgetCycle
import com.spendwise.app.domain.model.Category
import com.spendwise.app.domain.model.DebtDirection
import com.spendwise.app.domain.model.DebtItem
import com.spendwise.app.domain.model.Transaction
import com.spendwise.app.domain.model.TransactionType
import com.spendwise.app.domain.model.Wallet
import com.spendwise.app.domain.model.WalletType

@Entity(tableName = "budget_cycles")
data class BudgetCycleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val startedAt: Long,
    val endedAt: Long?,
    val initialAllowance: Double,
    val carryForwardAmount: Double,
    val isActive: Boolean,
    val targetDays: Int
) {
    fun toDomain(): BudgetCycle = BudgetCycle(
        id = id,
        title = title,
        startedAt = startedAt,
        endedAt = endedAt,
        initialAllowance = initialAllowance,
        carryForwardAmount = carryForwardAmount,
        isActive = isActive,
        targetDays = targetDays
    )

    companion object {
        fun fromDomain(d: BudgetCycle): BudgetCycleEntity = BudgetCycleEntity(
            id = d.id,
            title = d.title,
            startedAt = d.startedAt,
            endedAt = d.endedAt,
            initialAllowance = d.initialAllowance,
            carryForwardAmount = d.carryForwardAmount,
            isActive = d.isActive,
            targetDays = d.targetDays
        )
    }
}

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val currentBalance: Double,
    val currency: String,
    val isExcludedFromTotal: Boolean,
    val colorHex: String
) {
    fun toDomain(): Wallet = Wallet(
        id = id,
        name = name,
        type = WalletType.valueOf(type),
        currentBalance = currentBalance,
        currency = currency,
        isExcludedFromTotal = isExcludedFromTotal,
        colorHex = colorHex
    )

    companion object {
        fun fromDomain(w: Wallet): WalletEntity = WalletEntity(
            id = w.id,
            name = w.name,
            type = w.type.name,
            currentBalance = w.currentBalance,
            currency = w.currency,
            isExcludedFromTotal = w.isExcludedFromTotal,
            colorHex = w.colorHex
        )
    }
}

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconKey: String,
    val colorHex: String,
    val type: String,
    val isCustom: Boolean
) {
    fun toDomain(): Category = Category(
        id = id,
        name = name,
        iconKey = iconKey,
        colorHex = colorHex,
        type = TransactionType.valueOf(type),
        isCustom = isCustom
    )

    companion object {
        fun fromDomain(c: Category): CategoryEntity = CategoryEntity(
            id = c.id,
            name = c.name,
            iconKey = c.iconKey,
            colorHex = c.colorHex,
            type = c.type.name,
            isCustom = c.isCustom
        )
    }
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val cycleId: String,
    val walletId: String,
    val categoryId: String,
    val amount: Double,
    val type: String,
    val timestamp: Long,
    val note: String,
    val receiptImagePath: String?,
    val source: String,
    val triggersNewCycle: Boolean,
    val carryForwardPreviousPool: Boolean
) {
    fun toDomain(): Transaction = Transaction(
        id = id,
        cycleId = cycleId,
        walletId = walletId,
        categoryId = categoryId,
        amount = amount,
        type = TransactionType.valueOf(type),
        timestamp = timestamp,
        note = note,
        receiptImagePath = receiptImagePath,
        source = source,
        triggersNewCycle = triggersNewCycle,
        carryForwardPreviousPool = carryForwardPreviousPool
    )

    companion object {
        fun fromDomain(t: Transaction): TransactionEntity = TransactionEntity(
            id = t.id,
            cycleId = t.cycleId,
            walletId = t.walletId,
            categoryId = t.categoryId,
            amount = t.amount,
            type = t.type.name,
            timestamp = t.timestamp,
            note = t.note,
            receiptImagePath = t.receiptImagePath,
            source = t.source,
            triggersNewCycle = t.triggersNewCycle,
            carryForwardPreviousPool = t.carryForwardPreviousPool
        )
    }
}

@Entity(tableName = "debt_ledger")
data class DebtItemEntity(
    @PrimaryKey val id: String,
    val contactName: String,
    val phoneNumber: String,
    val amount: Double,
    val direction: String,
    val note: String,
    val dueDate: Long?,
    val createdAt: Long,
    val isSettled: Boolean
) {
    fun toDomain(): DebtItem = DebtItem(
        id = id,
        contactName = contactName,
        phoneNumber = phoneNumber,
        amount = amount,
        direction = DebtDirection.valueOf(direction),
        note = note,
        dueDate = dueDate,
        createdAt = createdAt,
        isSettled = isSettled
    )

    companion object {
        fun fromDomain(d: DebtItem): DebtItemEntity = DebtItemEntity(
            id = d.id,
            contactName = d.contactName,
            phoneNumber = d.phoneNumber,
            amount = d.amount,
            direction = d.direction.name,
            note = d.note,
            dueDate = d.dueDate,
            createdAt = d.createdAt,
            isSettled = d.isSettled
        )
    }
}
