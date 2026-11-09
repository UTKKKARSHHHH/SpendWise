package com.spendwise.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.spendwise.app.data.local.entity.BudgetCycleEntity
import com.spendwise.app.data.local.entity.CategoryEntity
import com.spendwise.app.data.local.entity.DebtItemEntity
import com.spendwise.app.data.local.entity.TransactionEntity
import com.spendwise.app.data.local.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpendwiseDao {

    // --- Cycles ---
    @Query("SELECT * FROM budget_cycles WHERE isActive = 1 LIMIT 1")
    fun getActiveCycle(): Flow<BudgetCycleEntity?>

    @Query("SELECT * FROM budget_cycles WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveCycleSync(): BudgetCycleEntity?

    @Query("SELECT * FROM budget_cycles ORDER BY startedAt DESC")
    fun getAllCycles(): Flow<List<BudgetCycleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(cycle: BudgetCycleEntity)

    @Update
    suspend fun updateCycle(cycle: BudgetCycleEntity)

    @Query("UPDATE budget_cycles SET isActive = 0, endedAt = :endedAt WHERE isActive = 1")
    suspend fun deactivateActiveCycles(endedAt: Long)

    // --- Wallets ---
    @Query("SELECT * FROM wallets ORDER BY id ASC")
    fun getWallets(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets ORDER BY id ASC")
    suspend fun getWalletsSync(): List<WalletEntity>

    @Query("SELECT * FROM wallets WHERE id = :id LIMIT 1")
    suspend fun getWalletById(id: String): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWallets(wallets: List<WalletEntity>)

    @Update
    suspend fun updateWallet(wallet: WalletEntity)

    @Query("UPDATE wallets SET currentBalance = currentBalance + :delta WHERE id = :walletId")
    suspend fun adjustWalletBalance(walletId: String, delta: Double)

    @Query("UPDATE wallets SET currentBalance = :balance WHERE id = :walletId")
    suspend fun setWalletBalance(walletId: String, balance: Double)

    @Query("DELETE FROM wallets WHERE id = :id")
    suspend fun deleteWallet(id: String)

    // --- Categories ---
    @Query("SELECT * FROM categories")
    fun getCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories")
    suspend fun getCategoriesSync(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    // --- Transactions ---
    @Query("SELECT * FROM transactions WHERE cycleId = :cycleId ORDER BY timestamp DESC")
    fun getTransactionsForCycle(cycleId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE cycleId = :cycleId")
    suspend fun getTransactionsForCycleSync(cycleId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE walletId = :walletId ORDER BY timestamp DESC")
    fun getTransactionsForWallet(walletId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)

    // --- Debts & Reminders ---
    @Query("SELECT * FROM debt_ledger ORDER BY isSettled ASC, createdAt DESC")
    fun getDebtItems(): Flow<List<DebtItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtItem(item: DebtItemEntity)

    @Update
    suspend fun updateDebtItem(item: DebtItemEntity)

    @Query("DELETE FROM debt_ledger WHERE id = :id")
    suspend fun deleteDebtItem(id: String)
}
