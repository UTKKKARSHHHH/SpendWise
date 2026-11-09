package com.spendwise.app.domain.engine

import com.spendwise.app.domain.model.BudgetCycle
import com.spendwise.app.domain.model.Transaction
import com.spendwise.app.domain.model.TransactionType
import java.util.UUID

/**
 * The core architectural engine that replaces rigid calendar months with an event-driven
 * Income-Triggered Lifecycle.
 *
 * Prevents early pocket money/salary from corrupting the prior cycle or zeroing active funds.
 */
class IncomeTriggeredCycleEngine {

    /**
     * Calculates the cycle transition when a new transaction is recorded.
     * If [isMajorAllowance] is true, it archives the current active cycle (if any)
     * and initializes a fresh cycle with carry-forward support.
     */
    fun processCycleTransition(
        currentActiveCycle: BudgetCycle?,
        activeCycleExpenses: Double,
        activeCycleIncidentalIncome: Double,
        newIncomeAmount: Double,
        carryForwardLeftover: Boolean,
        customCycleTitle: String? = null,
        targetDurationDays: Int = 30,
        timestamp: Long = System.currentTimeMillis()
    ): CycleTransitionResult {
        var carryOverAmount = 0.0

        val archivedCycle = currentActiveCycle?.let { cycle ->
            val startingPool = cycle.totalStartingPool
            val remainingBalance = (startingPool + activeCycleIncidentalIncome - activeCycleExpenses)
            
            if (carryForwardLeftover && remainingBalance > 0.0) {
                carryOverAmount = remainingBalance
            }

            cycle.copy(
                endedAt = timestamp,
                isActive = false
            )
        }

        val cycleCount = (currentActiveCycle?.title?.filter { it.isDigit() }?.toIntOrNull() ?: 0) + 1
        val newTitle = customCycleTitle ?: "Allowance Cycle #$cycleCount"

        val newCycle = BudgetCycle(
            id = UUID.randomUUID().toString(),
            title = newTitle,
            startedAt = timestamp,
            endedAt = null,
            initialAllowance = newIncomeAmount,
            carryForwardAmount = carryOverAmount,
            isActive = true,
            targetDays = targetDurationDays
        )

        return CycleTransitionResult(
            cycleToArchive = archivedCycle,
            newActiveCycle = newCycle,
            rolledOverAmount = carryOverAmount
        )
    }

    /**
     * Computes the net active allowance pool taking into account starting pool,
     * incidental income (cash gifts, side gigs, refunds), and all expenses in this cycle.
     */
    fun computeCurrentPool(
        activeCycle: BudgetCycle?,
        cycleTransactions: List<Transaction>
    ): Double {
        if (activeCycle == null) return 0.0

        val totalExpenses = cycleTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        // Minor / incidental income inside the same cycle (excluding the trigger allowance itself)
        val incidentalIncome = cycleTransactions
            .filter { it.type == TransactionType.INCOME && !it.triggersNewCycle }
            .sumOf { it.amount }

        return (activeCycle.totalStartingPool + incidentalIncome - totalExpenses)
    }

    data class CycleTransitionResult(
        val cycleToArchive: BudgetCycle?,
        val newActiveCycle: BudgetCycle,
        val rolledOverAmount: Double
    )
}
