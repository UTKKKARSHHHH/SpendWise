package com.spendwise.app.domain.engine

import com.spendwise.app.domain.model.BudgetCycle
import com.spendwise.app.domain.model.Transaction
import com.spendwise.app.domain.model.TransactionType
import kotlin.math.max

/**
 * Predictive model analyzing cycle spending patterns to forecast cash-flow shortages
 * before they occur.
 */
class PredictiveAnalyticsEngine {

    data class CashFlowForecast(
        val currentDailyBurnRate: Double,
        val recommendedDailySpend: Double,
        val projectedDaysRemaining: Int,
        val targetDaysRemaining: Int,
        val isShortagePredicted: Boolean,
        val estimatedShortageDay: Int?,
        val projectedDeficitAmount: Double
    )

    fun forecastCashFlow(
        activeCycle: BudgetCycle,
        cycleTransactions: List<Transaction>,
        currentPoolBalance: Double,
        currentTimeMs: Long = System.currentTimeMillis()
    ): CashFlowForecast {
        val elapsedDays = max(1, activeCycle.getElapsedDays(currentTimeMs))
        val targetDays = activeCycle.targetDays
        val targetDaysRemaining = max(1, targetDays - elapsedDays)

        val totalExpenses = cycleTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val currentDailyBurnRate = totalExpenses / elapsedDays
        val recommendedDailySpend = max(0.0, currentPoolBalance / targetDaysRemaining)

        val projectedDaysRemaining = if (currentDailyBurnRate > 0.0) {
            (currentPoolBalance / currentDailyBurnRate).toInt()
        } else {
            targetDaysRemaining
        }

        val isShortagePredicted = projectedDaysRemaining < targetDaysRemaining && currentPoolBalance > 0.0
        val estimatedShortageDay = if (isShortagePredicted) elapsedDays + projectedDaysRemaining else null
        val projectedDeficit = if (isShortagePredicted) {
            (currentDailyBurnRate * targetDaysRemaining) - currentPoolBalance
        } else {
            0.0
        }

        return CashFlowForecast(
            currentDailyBurnRate = currentDailyBurnRate,
            recommendedDailySpend = recommendedDailySpend,
            projectedDaysRemaining = projectedDaysRemaining,
            targetDaysRemaining = targetDaysRemaining,
            isShortagePredicted = isShortagePredicted,
            estimatedShortageDay = estimatedShortageDay,
            projectedDeficitAmount = max(0.0, projectedDeficit)
        )
    }
}
