package com.spendwise.app.ui.viewmodel

import com.spendwise.app.domain.engine.PredictiveAnalyticsEngine
import com.spendwise.app.domain.model.BudgetCycle
import com.spendwise.app.domain.model.Category
import com.spendwise.app.domain.model.DebtItem
import com.spendwise.app.domain.model.Transaction
import com.spendwise.app.domain.model.Wallet

data class DashboardUiState(
    val activeCycle: BudgetCycle? = null,
    val totalRunningBalance: Double = 0.0,
    val currentAllowancePool: Double = 0.0,
    val dailySafeToSpend: Double = 0.0,
    val wallets: List<Wallet> = emptyList(),
    val categories: List<Category> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val debtItems: List<DebtItem> = emptyList(),
    val forecast: PredictiveAnalyticsEngine.CashFlowForecast? = null,
    val searchQuery: String = "",
    val selectedWalletFilter: String? = null,
    val selectedCategoryFilter: String? = null,
    val isLoading: Boolean = false,
    val statusMessage: String? = null
)
