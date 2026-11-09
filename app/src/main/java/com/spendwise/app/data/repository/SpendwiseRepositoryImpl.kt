package com.spendwise.app.data.repository

import com.spendwise.app.data.local.dao.SpendwiseDao
import com.spendwise.app.data.local.entity.BudgetCycleEntity
import com.spendwise.app.data.local.entity.CategoryEntity
import com.spendwise.app.data.local.entity.DebtItemEntity
import com.spendwise.app.data.local.entity.TransactionEntity
import com.spendwise.app.data.local.entity.WalletEntity
import com.spendwise.app.domain.engine.IncomeTriggeredCycleEngine
import com.spendwise.app.domain.model.BudgetCycle
import com.spendwise.app.domain.model.Category
import com.spendwise.app.domain.model.DebtItem
import com.spendwise.app.domain.model.Transaction
import com.spendwise.app.domain.model.TransactionType
import com.spendwise.app.domain.model.Wallet
import com.spendwise.app.domain.repository.SpendwiseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class SpendwiseRepositoryImpl(
    private val dao: SpendwiseDao,
    private val cycleEngine: IncomeTriggeredCycleEngine = IncomeTriggeredCycleEngine()
) : SpendwiseRepository {

    override fun getActiveCycle(): Flow<BudgetCycle?> {
        return dao.getActiveCycle().map { it?.toDomain() }
    }

    override fun getAllCycles(): Flow<List<BudgetCycle>> {
        return dao.getAllCycles().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertCycle(cycle: BudgetCycle) {
        dao.insertCycle(BudgetCycleEntity.fromDomain(cycle))
    }

    override suspend fun updateCycle(cycle: BudgetCycle) {
        dao.updateCycle(BudgetCycleEntity.fromDomain(cycle))
    }

    override suspend fun logAllowanceAndTriggerCycle(
        amount: Double,
        walletId: String,
        categoryId: String,
        carryForward: Boolean,
        customTitle: String?,
        targetDays: Int
    ) {
        val now = System.currentTimeMillis()
        val currentCycleEntity = dao.getActiveCycleSync()
        val currentDomainCycle = currentCycleEntity?.toDomain()

        val activeExpenses = if (currentCycleEntity != null) {
            val txs = dao.getTransactionsForCycleSync(currentCycleEntity.id)
            txs.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
        } else 0.0

        val incidentalIncome = if (currentCycleEntity != null) {
            val txs = dao.getTransactionsForCycleSync(currentCycleEntity.id)
            txs.filter { it.type == TransactionType.INCOME.name && !it.triggersNewCycle }.sumOf { it.amount }
        } else 0.0

        val transition = cycleEngine.processCycleTransition(
            currentActiveCycle = currentDomainCycle,
            activeCycleExpenses = activeExpenses,
            activeCycleIncidentalIncome = incidentalIncome,
            newIncomeAmount = amount,
            carryForwardLeftover = carryForward,
            customCycleTitle = customTitle,
            targetDurationDays = targetDays,
            timestamp = now
        )

        // 1. Archive old active cycle if exists
        transition.cycleToArchive?.let { old ->
            dao.updateCycle(BudgetCycleEntity.fromDomain(old))
        }

        // 2. Insert brand new cycle
        dao.insertCycle(BudgetCycleEntity.fromDomain(transition.newActiveCycle))

        // 3. Record the allowance transaction tied to the new cycle
        val tx = TransactionEntity(
            id = UUID.randomUUID().toString(),
            cycleId = transition.newActiveCycle.id,
            walletId = walletId,
            categoryId = categoryId,
            amount = amount,
            type = TransactionType.INCOME.name,
            timestamp = now,
            note = if (transition.rolledOverAmount > 0) {
                "Allowance Cycle Influx (Rolled ₹${"%.0f".format(transition.rolledOverAmount)} forward)"
            } else {
                "Allowance Influx"
            },
            receiptImagePath = null,
            source = "CYCLE_TRIGGER",
            triggersNewCycle = true,
            carryForwardPreviousPool = carryForward
        )
        dao.insertTransaction(tx)

        // 4. Credit the recipient wallet
        dao.adjustWalletBalance(walletId, amount)
    }

    override fun getWallets(): Flow<List<Wallet>> {
        return dao.getWallets().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getWalletById(id: String): Wallet? {
        return dao.getWalletById(id)?.toDomain()
    }

    override suspend fun insertWallet(wallet: Wallet) {
        dao.insertWallet(WalletEntity.fromDomain(wallet))
    }

    override suspend fun updateWallet(wallet: Wallet) {
        dao.updateWallet(WalletEntity.fromDomain(wallet))
    }

    override suspend fun setWalletBalance(walletId: String, balance: Double) {
        dao.setWalletBalance(walletId, balance)
    }

    override suspend fun updateWalletBalance(walletId: String, delta: Double) {
        dao.adjustWalletBalance(walletId, delta)
    }

    override suspend fun deleteWallet(walletId: String) {
        dao.deleteWallet(walletId)
    }

    override fun getTransactionsForWallet(walletId: String): Flow<List<Transaction>> {
        return dao.getTransactionsForWallet(walletId).map { list -> list.map { it.toDomain() } }
    }

    override fun getCategories(): Flow<List<Category>> {
        return dao.getCategories().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertCategory(category: Category) {
        dao.insertCategory(CategoryEntity.fromDomain(category))
    }

    override fun getTransactionsForCycle(cycleId: String): Flow<List<Transaction>> {
        return dao.getTransactionsForCycle(cycleId).map { list -> list.map { it.toDomain() } }
    }

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return dao.getAllTransactions().map { list -> list.map { it.toDomain() } }
    }

    override fun getRecentTransactions(limit: Int): Flow<List<Transaction>> {
        return dao.getRecentTransactions(limit).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        dao.insertTransaction(TransactionEntity.fromDomain(transaction))
        val delta = if (transaction.type == TransactionType.INCOME) transaction.amount else -transaction.amount
        dao.adjustWalletBalance(transaction.walletId, delta)
    }

    override suspend fun deleteTransaction(transactionId: String) {
        dao.deleteTransaction(transactionId)
    }

    override fun getDebtItems(): Flow<List<DebtItem>> {
        return dao.getDebtItems().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertDebtItem(item: DebtItem) {
        dao.insertDebtItem(DebtItemEntity.fromDomain(item))
    }

    override suspend fun updateDebtItem(item: DebtItem) {
        dao.updateDebtItem(DebtItemEntity.fromDomain(item))
    }

    override suspend fun deleteDebtItem(id: String) {
        dao.deleteDebtItem(id)
    }

    override suspend fun ensureDefaults() {
        val currentWallets = dao.getWalletsSync()
        if (currentWallets.isEmpty()) {
            dao.insertWallets(
                listOf(
                    WalletEntity("wallet_cash", "Physical Cash", "PHYSICAL_CASH", 0.0, "INR", false, "#10B981"),
                    WalletEntity("wallet_bank", "Bank Account", "BANK_ACCOUNT", 0.0, "INR", false, "#3B82F6"),
                    WalletEntity("wallet_upi", "Digital Wallet / UPI", "DIGITAL_WALLET", 0.0, "INR", false, "#8B5CF6")
                )
            )
        }
        val currentCats = dao.getCategoriesSync()
        if (currentCats.isEmpty()) {
            dao.insertCategories(
                listOf(
                    CategoryEntity("cat_food", "Food & Dining", "fastfood", "#EF4444", TransactionType.EXPENSE.name, false),
                    CategoryEntity("cat_groceries", "Groceries", "shopping_cart", "#F59E0B", TransactionType.EXPENSE.name, false),
                    CategoryEntity("cat_rent", "Rent & Bills", "home", "#6366F1", TransactionType.EXPENSE.name, false),
                    CategoryEntity("cat_transport", "Transit & Fuel", "commute", "#06B6D4", TransactionType.EXPENSE.name, false),
                    CategoryEntity("cat_entertainment", "Leisure & Subscriptions", "movie", "#EC4899", TransactionType.EXPENSE.name, false),
                    CategoryEntity("cat_allowance", "Allowance / Pocket Money", "account_balance_wallet", "#10B981", TransactionType.INCOME.name, false),
                    CategoryEntity("cat_salary", "Salary / Freelance", "work", "#3B82F6", TransactionType.INCOME.name, false),
                    CategoryEntity("cat_gift", "Gift / Refund", "card_giftcard", "#8B5CF6", TransactionType.INCOME.name, false)
                )
            )
        }
    }
}
