package com.spendwise.app.domain.model

import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * Represents a dynamic spending lifecycle triggered by major cash inflows (allowance/salary)
 * rather than arbitrary calendar months.
 */
data class BudgetCycle(
    val id: String,
    val title: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val initialAllowance: Double,
    val carryForwardAmount: Double = 0.0,
    val isActive: Boolean = true,
    val targetDays: Int = 30
) {
    val totalStartingPool: Double
        get() = initialAllowance + carryForwardAmount

    /**
     * Computes the dynamic daily safe-to-spend velocity.
     * Prevents burn-rate distortion when money arrives early or late.
     */
    fun calculateDailySafeToSpend(currentPool: Double, currentTimeMs: Long = System.currentTimeMillis()): Double {
        if (!isActive || currentPool <= 0.0) return 0.0

        val elapsedMs = max(0L, currentTimeMs - startedAt)
        val elapsedDays = TimeUnit.MILLISECONDS.toDays(elapsedMs).toInt()
        val remainingDays = max(1, targetDays - elapsedDays)

        return currentPool / remainingDays
    }

    fun getElapsedDays(currentTimeMs: Long = System.currentTimeMillis()): Int {
        val elapsedMs = max(0L, currentTimeMs - startedAt)
        return TimeUnit.MILLISECONDS.toDays(elapsedMs).toInt()
    }
}
