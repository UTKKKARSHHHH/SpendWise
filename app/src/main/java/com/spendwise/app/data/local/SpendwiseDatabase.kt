package com.spendwise.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.spendwise.app.data.local.dao.SpendwiseDao
import com.spendwise.app.data.local.entity.BudgetCycleEntity
import com.spendwise.app.data.local.entity.CategoryEntity
import com.spendwise.app.data.local.entity.DebtItemEntity
import com.spendwise.app.data.local.entity.TransactionEntity
import com.spendwise.app.data.local.entity.WalletEntity
import com.spendwise.app.domain.model.TransactionType
import com.spendwise.app.domain.model.WalletType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BudgetCycleEntity::class,
        WalletEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        DebtItemEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class SpendwiseDatabase : RoomDatabase() {

    abstract fun spendwiseDao(): SpendwiseDao

    companion object {
        @Volatile
        private var INSTANCE: SpendwiseDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SpendwiseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpendwiseDatabase::class.java,
                    "spendwise_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDefaults(database.spendwiseDao())
                    }
                }
            }

            private suspend fun populateDefaults(dao: SpendwiseDao) {
                // Multi-Wallets initialized with ZERO balance (No prefilled money, no specific bank names)
                val defaultWallets = listOf(
                    WalletEntity(
                        id = "wallet_cash",
                        name = "Physical Cash",
                        type = WalletType.PHYSICAL_CASH.name,
                        currentBalance = 0.0,
                        currency = "INR",
                        isExcludedFromTotal = false,
                        colorHex = "#10B981"
                    ),
                    WalletEntity(
                        id = "wallet_bank",
                        name = "Bank Account",
                        type = WalletType.BANK_ACCOUNT.name,
                        currentBalance = 0.0,
                        currency = "INR",
                        isExcludedFromTotal = false,
                        colorHex = "#3B82F6"
                    ),
                    WalletEntity(
                        id = "wallet_upi",
                        name = "Digital Wallet / UPI",
                        type = WalletType.DIGITAL_WALLET.name,
                        currentBalance = 0.0,
                        currency = "INR",
                        isExcludedFromTotal = false,
                        colorHex = "#8B5CF6"
                    )
                )
                dao.insertWallets(defaultWallets)

                // Default Categories
                val defaultCategories = listOf(
                    CategoryEntity("cat_food", "Food & Dining", "fastfood", "#EF4444", TransactionType.EXPENSE.name, false),
                    CategoryEntity("cat_groceries", "Groceries", "shopping_cart", "#F59E0B", TransactionType.EXPENSE.name, false),
                    CategoryEntity("cat_rent", "Rent & Bills", "home", "#6366F1", TransactionType.EXPENSE.name, false),
                    CategoryEntity("cat_transport", "Transit & Fuel", "commute", "#06B6D4", TransactionType.EXPENSE.name, false),
                    CategoryEntity("cat_entertainment", "Leisure & Subscriptions", "movie", "#EC4899", TransactionType.EXPENSE.name, false),
                    CategoryEntity("cat_allowance", "Allowance / Pocket Money", "account_balance_wallet", "#10B981", TransactionType.INCOME.name, false),
                    CategoryEntity("cat_salary", "Salary / Freelance", "work", "#3B82F6", TransactionType.INCOME.name, false),
                    CategoryEntity("cat_gift", "Gift / Refund", "card_giftcard", "#8B5CF6", TransactionType.INCOME.name, false)
                )
                dao.insertCategories(defaultCategories)

                // NOTE: No initial budget cycle, transactions, or debts are prefilled!
                // All cycles, balances, and entries must be created manually by the user.
            }
        }
    }
}
