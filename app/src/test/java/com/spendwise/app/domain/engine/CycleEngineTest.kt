package com.spendwise.app.domain.engine

import com.spendwise.app.domain.model.BudgetCycle
import com.spendwise.app.domain.model.Transaction
import com.spendwise.app.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class IncomeTriggeredCycleEngineTest {

    private val engine = IncomeTriggeredCycleEngine()

    @Test
    fun `test early allowance archives previous cycle and rolls forward balance`() {
        val now = System.currentTimeMillis()
        val oldCycle = BudgetCycle(
            id = "cycle_1",
            title = "Allowance Cycle #1",
            startedAt = now - (25 * 86400000L), // started 25 days ago
            endedAt = null,
            initialAllowance = 10000.0,
            carryForwardAmount = 0.0,
            isActive = true,
            targetDays = 30
        )

        // User spent 6,000, leaving 4,000 remaining
        val expenses = 6000.0
        val incidentalIncome = 500.0 // e.g. sold an old item

        // New allowance arrives 5 days early!
        val newAllowance = 12000.0

        val result = engine.processCycleTransition(
            currentActiveCycle = oldCycle,
            activeCycleExpenses = expenses,
            activeCycleIncidentalIncome = incidentalIncome,
            newIncomeAmount = newAllowance,
            carryForwardLeftover = true,
            customCycleTitle = null,
            targetDurationDays = 30,
            timestamp = now
        )

        // Verify old cycle is archived
        assertNotNull(result.cycleToArchive)
        assertFalse(result.cycleToArchive!!.isActive)
        assertEquals(now, result.cycleToArchive!!.endedAt)

        // Verify carry-forward math: 10,000 + 500 - 6,000 = 4,500
        assertEquals(4500.0, result.rolledOverAmount, 0.01)

        // Verify new cycle is created with the new allowance + rolled over amount
        assertTrue(result.newActiveCycle.isActive)
        assertEquals(newAllowance, result.newActiveCycle.initialAllowance, 0.01)
        assertEquals(4500.0, result.newActiveCycle.carryForwardAmount, 0.01)
        assertEquals(16500.0, result.newActiveCycle.totalStartingPool, 0.01)
    }

    @Test
    fun `test carry forward toggle off does not roll leftover balance`() {
        val now = System.currentTimeMillis()
        val oldCycle = BudgetCycle(
            id = "cycle_1",
            title = "Allowance Cycle #1",
            startedAt = now - (28 * 86400000L),
            endedAt = null,
            initialAllowance = 10000.0,
            carryForwardAmount = 0.0,
            isActive = true,
            targetDays = 30
        )

        val result = engine.processCycleTransition(
            currentActiveCycle = oldCycle,
            activeCycleExpenses = 5000.0,
            activeCycleIncidentalIncome = 0.0,
            newIncomeAmount = 10000.0,
            carryForwardLeftover = false,
            timestamp = now
        )

        assertEquals(0.0, result.rolledOverAmount, 0.01)
        assertEquals(10000.0, result.newActiveCycle.totalStartingPool, 0.01)
    }

    @Test
    fun `test daily safe to spend calculation`() {
        val now = System.currentTimeMillis()
        val cycle = BudgetCycle(
            id = "cycle_1",
            title = "Cycle #1",
            startedAt = now - (10 * 86400000L), // 10 days elapsed
            endedAt = null,
            initialAllowance = 10000.0,
            carryForwardAmount = 0.0,
            isActive = true,
            targetDays = 30 // 20 days remaining
        )

        val currentPool = 6000.0
        val safeSpend = cycle.calculateDailySafeToSpend(currentPool, now)

        // 6000 / 20 days = 300 / day
        assertEquals(300.0, safeSpend, 1.0)
    }
}
