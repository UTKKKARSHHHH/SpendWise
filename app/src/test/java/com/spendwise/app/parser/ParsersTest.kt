package com.spendwise.app.parser

import com.spendwise.app.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ParsersTest {

    private val smsParser = SmsTransactionParser()
    private val voiceParser = VoiceTransactionParser()

    @Test
    fun `test HDFC debit SMS parsing`() {
        val sms = "Rs 450.00 spent on your HDFC Bank Card ending 1234 at Starbucks on 12-Nov-26. Avl bal: INR 12,450.00"
        val draft = smsParser.parse("HDFCBK", sms)

        assertNotNull(draft)
        assertEquals(450.0, draft!!.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, draft.type)
        assertEquals("Food & Dining", draft.categoryName)
    }

    @Test
    fun `test UPI debit SMS parsing`() {
        val sms = "Your A/C 9876 debited by INR 1,200.00 on 02-Nov-26 towards Swiggy UPI Ref 32918239."
        val draft = smsParser.parse("SBIINB", sms)

        assertNotNull(draft)
        assertEquals(1200.0, draft!!.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, draft.type)
        assertEquals("Food & Dining", draft.categoryName)
        assertEquals("Digital Wallet / UPI", draft.walletName)
    }

    @Test
    fun `test voice parser for expense`() {
        val voiceInput = "Spent 150 rupees on dinner"
        val draft = voiceParser.parseSpokenText(voiceInput)

        assertNotNull(draft)
        assertEquals(150.0, draft!!.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, draft.type)
        assertEquals("Food & Dining", draft.categoryName)
    }

    @Test
    fun `test voice parser for allowance income`() {
        val voiceInput = "Got 8000 allowance from dad"
        val draft = voiceParser.parseSpokenText(voiceInput)

        assertNotNull(draft)
        assertEquals(8000.0, draft!!.amount, 0.01)
        assertEquals(TransactionType.INCOME, draft.type)
        assertEquals("Allowance / Pocket Money", draft.categoryName)
    }
}
