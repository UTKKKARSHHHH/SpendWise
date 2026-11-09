package com.spendwise.app.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spendwise.app.domain.engine.IncomeTriggeredCycleEngine
import com.spendwise.app.domain.engine.PredictiveAnalyticsEngine
import com.spendwise.app.domain.model.BudgetCycle
import com.spendwise.app.domain.model.DebtDirection
import com.spendwise.app.domain.model.DebtItem
import com.spendwise.app.domain.model.Transaction
import com.spendwise.app.domain.model.TransactionDraft
import com.spendwise.app.domain.model.TransactionType
import com.spendwise.app.domain.model.Wallet
import com.spendwise.app.domain.model.WalletType
import com.spendwise.app.domain.repository.SpendwiseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.util.UUID

class DashboardViewModel(
    private val repository: SpendwiseRepository,
    private val cycleEngine: IncomeTriggeredCycleEngine = IncomeTriggeredCycleEngine(),
    private val predictiveEngine: PredictiveAnalyticsEngine = PredictiveAnalyticsEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaults()
        }
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.getActiveCycle(),
                repository.getWallets(),
                repository.getCategories(),
                repository.getAllTransactions(),
                repository.getDebtItems()
            ) { activeCycle, wallets, categories, allTransactions, debts ->
                val currentPool = if (activeCycle != null && activeCycle.isActive) {
                    val cycleTxs = allTransactions.filter { it.cycleId == activeCycle.id }
                    cycleEngine.computeCurrentPool(activeCycle, cycleTxs)
                } else 0.0

                val safeSpend = if (activeCycle != null && activeCycle.isActive && currentPool > 0.0) {
                    activeCycle.calculateDailySafeToSpend(currentPool)
                } else 0.0

                val totalRunningBal = wallets.filter { !it.isExcludedFromTotal }.sumOf { it.currentBalance }

                val forecast = if (activeCycle != null && activeCycle.isActive && currentPool > 0.0) {
                    val cycleTxs = allTransactions.filter { it.cycleId == activeCycle.id }
                    predictiveEngine.forecastCashFlow(activeCycle, cycleTxs, currentPool)
                } else null

                val filtered = applyFilters(allTransactions, _uiState.value.searchQuery, _uiState.value.selectedWalletFilter, _uiState.value.selectedCategoryFilter)

                _uiState.update { current ->
                    current.copy(
                        activeCycle = activeCycle,
                        currentAllowancePool = currentPool,
                        dailySafeToSpend = safeSpend,
                        totalRunningBalance = totalRunningBal,
                        wallets = wallets,
                        categories = categories,
                        transactions = allTransactions,
                        filteredTransactions = filtered,
                        debtItems = debts,
                        forecast = forecast
                    )
                }
            }.collect {}
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                filteredTransactions = applyFilters(current.transactions, query, current.selectedWalletFilter, current.selectedCategoryFilter)
            )
        }
    }

    fun onFilterWalletSelected(walletId: String?) {
        _uiState.update { current ->
            current.copy(
                selectedWalletFilter = walletId,
                filteredTransactions = applyFilters(current.transactions, current.searchQuery, walletId, current.selectedCategoryFilter)
            )
        }
    }

    fun onFilterCategorySelected(categoryId: String?) {
        _uiState.update { current ->
            current.copy(
                selectedCategoryFilter = categoryId,
                filteredTransactions = applyFilters(current.transactions, current.searchQuery, current.selectedWalletFilter, categoryId)
            )
        }
    }

    private fun applyFilters(
        list: List<Transaction>,
        query: String,
        walletId: String?,
        categoryId: String?
    ): List<Transaction> {
        return list.filter { tx ->
            val matchesQuery = query.isBlank() || tx.note.contains(query, ignoreCase = true) ||
                    tx.amount.toString().contains(query)
            val matchesWallet = walletId == null || tx.walletId == walletId
            val matchesCategory = categoryId == null || tx.categoryId == categoryId
            matchesQuery && matchesWallet && matchesCategory
        }
    }

    /**
     * Fast single-click entry for "Cash In" and "Cash Out"
     */
    fun logQuickTransaction(
        amount: Double,
        type: TransactionType,
        walletId: String,
        categoryId: String,
        note: String,
        source: String = "MANUAL"
    ) {
        viewModelScope.launch {
            val activeCycleId = _uiState.value.activeCycle?.id ?: "manual_cycle"
            val tx = Transaction(
                id = UUID.randomUUID().toString(),
                cycleId = activeCycleId,
                walletId = walletId,
                categoryId = categoryId,
                amount = amount,
                type = type,
                timestamp = System.currentTimeMillis(),
                note = note,
                source = source
            )
            repository.insertTransaction(tx)
        }
    }

    /**
     * The Irregular Allowance Trigger:
     * Sets up or updates the dynamic allowance pool based on user input.
     */
    fun logAllowanceTrigger(
        amount: Double,
        walletId: String,
        categoryId: String,
        carryForward: Boolean,
        customTitle: String? = null,
        targetDays: Int = 30
    ) {
        viewModelScope.launch {
            repository.logAllowanceAndTriggerCycle(
                amount = amount,
                walletId = walletId,
                categoryId = categoryId,
                carryForward = carryForward,
                customTitle = customTitle,
                targetDays = targetDays
            )
        }
    }

    /**
     * Wallet Management
     */
    fun addNewWallet(name: String, type: WalletType, initialBalance: Double) {
        viewModelScope.launch {
            val newWallet = Wallet(
                id = "wallet_${UUID.randomUUID().toString().take(8)}",
                name = name,
                type = type,
                currentBalance = initialBalance,
                currency = "INR",
                isExcludedFromTotal = false,
                colorHex = when (type) {
                    WalletType.PHYSICAL_CASH -> "#10B981"
                    WalletType.BANK_ACCOUNT -> "#3B82F6"
                    WalletType.DIGITAL_WALLET -> "#8B5CF6"
                }
            )
            repository.insertWallet(newWallet)
        }
    }

    fun addNewCustomWallet(name: String, type: WalletType, initialBalance: Double) {
        addNewWallet(name, type, initialBalance)
    }

    fun updateWalletNameAndBalance(walletId: String, newName: String, newBalance: Double) {
        viewModelScope.launch {
            val existing = repository.getWalletById(walletId) ?: return@launch
            repository.updateWallet(existing.copy(name = newName, currentBalance = newBalance))
        }
    }

    fun deleteWallet(walletId: String) {
        viewModelScope.launch {
            repository.deleteWallet(walletId)
        }
    }

    /**
     * Commits a draft received from OCR Scanner or Voice Ingestion
     */
    fun commitDraft(draft: TransactionDraft) {
        val targetWallet = _uiState.value.wallets.find { it.name.contains(draft.walletName, ignoreCase = true) }
            ?: _uiState.value.wallets.firstOrNull() ?: return
        val targetCategory = _uiState.value.categories.find { it.name.contains(draft.categoryName, ignoreCase = true) }
            ?: _uiState.value.categories.firstOrNull() ?: return

        logQuickTransaction(
            amount = draft.amount,
            type = draft.type,
            walletId = targetWallet.id,
            categoryId = targetCategory.id,
            note = draft.note,
            source = draft.source
        )
    }

    /**
     * Debt & Credit Reminders
     */
    fun addDebtItem(contactName: String, phone: String, amount: Double, direction: DebtDirection, note: String) {
        viewModelScope.launch {
            val item = DebtItem(
                id = UUID.randomUUID().toString(),
                contactName = contactName,
                phoneNumber = phone,
                amount = amount,
                direction = direction,
                note = note
            )
            repository.insertDebtItem(item)
        }
    }

    fun deleteDebtItem(id: String) {
        viewModelScope.launch {
            repository.deleteDebtItem(id)
        }
    }

    fun settleDebtWithWallet(item: DebtItem, targetWalletId: String) {
        viewModelScope.launch {
            repository.updateDebtItem(item.copy(isSettled = true))
            // If we were owed money and now settled, cash is received (+).
            // If we owed money and now settled, cash was paid (-).
            val delta = if (item.direction == DebtDirection.YOU_ARE_OWED) item.amount else -item.amount
            repository.updateWalletBalance(targetWalletId, delta)
        }
    }

    /**
     * Direct WhatsApp payment reminder
     */
    fun sendWhatsAppReminder(context: Context, item: DebtItem) {
        val cleanPhone = item.phoneNumber.replace("[^0-9+]".toRegex(), "")
        val formattedAmount = "₹%.2f".format(item.amount)
        val message = "Hi ${item.contactName}, gentle reminder regarding the pending amount of $formattedAmount (${item.note}) via Spendwise. Thank you!"

        try {
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:$cleanPhone")
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(smsIntent)
        }
    }

    class Factory(private val repository: SpendwiseRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository) as T
        }
    }
}
