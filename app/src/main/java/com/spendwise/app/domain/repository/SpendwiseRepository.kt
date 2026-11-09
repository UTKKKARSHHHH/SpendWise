package com.spendwise.app.domain.repository

import com.spendwise.app.domain.model.BudgetCycle
import com.spendwise.app.domain.model.Category
import com.spendwise.app.domain.model.DebtItem
import com.spendwise.app.domain.model.Transaction
import com.spendwise.app.domain.model.Wallet
import kotlinx.coroutines.flow.Flow

interface SpendwiseRepository {
    // Cycles
    fun getActiveCycle(): Flow<BudgetCycle?>
    fun getAllCycles(): Flow<List<BudgetCycle>>
    suspend fun insertCycle(cycle: BudgetCycle)
    suspend fun updateCycle(cycle: BudgetCycle)
    suspend fun logAllowanceAndTriggerCycle(
        amount: Double,
        walletId: String,
        categoryId: String,
        carryForward: Boolean,
        customTitle: String? = null,
        targetDays: Int = 30
    )

    // Wallets
    fun getWallets(): Flow<List<Wallet>>
    suspend fun getWalletById(id: String): Wallet?
    suspend fun insertWallet(wallet: Wallet)
    suspend fun updateWallet(wallet: Wallet)
    suspend fun setWalletBalance(walletId: String, balance: Double)
    suspend fun updateWalletBalance(walletId: String, delta: Double)
    suspend fun deleteWallet(walletId: String)
    fun getTransactionsForWallet(walletId: String): Flow<List<Transaction>>

    // Categories
    fun getCategories(): Flow<List<Category>>
    suspend fun insertCategory(category: Category)

    // Transactions
    fun getTransactionsForCycle(cycleId: String): Flow<List<Transaction>>
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getRecentTransactions(limit: Int = 20): Flow<List<Transaction>>
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transactionId: String)
    suspend fun ensureDefaults()

    // Debts & Reminders
    fun getDebtItems(): Flow<List<DebtItem>>
    suspend fun insertDebtItem(item: DebtItem)
    suspend fun updateDebtItem(item: DebtItem)
    suspend fun deleteDebtItem(id: String)
}
