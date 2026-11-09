package com.spendwise.app.parser

import com.spendwise.app.domain.model.TransactionDraft
import com.spendwise.app.domain.model.TransactionType
import java.util.regex.Pattern

/**
 * Intelligent regex engine designed to parse transaction alerts from standard banking
 * and UPI SMS headers (e.g., HDFC, SBI, ICICI, AXIS, PAYTM, GPAY, PHONEPE).
 */
class SmsTransactionParser {

    private val amountRegex = Pattern.compile(
        "(?:(?:rs|inr|inr\\.)\\s*([0-9,]+(?:\\.[0-9]{1,2})?))|(?:([0-9,]+(?:\\.[0-9]{1,2})?)\\s*(?:rs|inr))",
        Pattern.CASE_INSENSITIVE
    )

    private val debitKeywords = listOf("debited", "spent", "paid", "withdrawn", "sent", "deducted", "purchase")
    private val creditKeywords = listOf("credited", "received", "deposited", "added", "refunded")

    private val merchantRegex = Pattern.compile(
        "(?:at|to|towards|vpa|info)\\s+([A-Za-z0-9&\\.\\s]{2,25})(?:\\s+on|\\s+ref|\\s+bal|\\s+avl|\\.)",
        Pattern.CASE_INSENSITIVE
    )

    fun parse(sender: String, messageBody: String): TransactionDraft? {
        val lower = messageBody.lowercase()

        // 1. Determine Transaction Type
        val isDebit = debitKeywords.any { lower.contains(it) }
        val isCredit = creditKeywords.any { lower.contains(it) }

        if (!isDebit && !isCredit) return null

        val txType = if (isDebit) TransactionType.EXPENSE else TransactionType.INCOME

        // 2. Extract Amount
        val matcher = amountRegex.matcher(messageBody)
        var parsedAmount = 0.0
        if (matcher.find()) {
            val group1 = matcher.group(1)
            val group2 = matcher.group(2)
            val rawStr = (group1 ?: group2)?.replace(",", "")
            parsedAmount = rawStr?.toDoubleOrNull() ?: 0.0
        }

        if (parsedAmount <= 0.0) return null

        // 3. Extract Merchant / Note
        var note = "SMS Alert from $sender"
        val mMatcher = merchantRegex.matcher(messageBody)
        if (mMatcher.find()) {
            val merchant = mMatcher.group(1)?.trim()
            if (!merchant.isNullOrBlank()) {
                note = merchant
            }
        }

        // 4. Determine Recommended Category
        val categoryName = when {
            lower.contains("swiggy") || lower.contains("zomato") || lower.contains("food") || lower.contains("cafe") -> "Food & Dining"
            lower.contains("uber") || lower.contains("ola") || lower.contains("fuel") || lower.contains("petrol") -> "Transit & Fuel"
            lower.contains("amazon") || lower.contains("flipkart") || lower.contains("mart") -> "Groceries"
            lower.contains("allowance") || lower.contains("pocket") -> "Allowance / Pocket Money"
            lower.contains("salary") -> "Salary / Freelance"
            else -> if (isDebit) "Rent & Bills" else "Gift / Refund"
        }

        val walletName = when {
            lower.contains("upi") || lower.contains("paytm") || lower.contains("gpay") || lower.contains("phonepe") -> "Digital Wallet / UPI"
            lower.contains("cash") || lower.contains("atm") -> "Physical Cash"
            else -> "Bank Account"
        }

        return TransactionDraft(
            amount = parsedAmount,
            type = txType,
            categoryName = categoryName,
            walletName = walletName,
            note = note,
            confidenceScore = 0.92f,
            source = "SMS"
        )
    }
}
