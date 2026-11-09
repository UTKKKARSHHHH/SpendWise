package com.spendwise.app.parser

import com.spendwise.app.domain.model.TransactionDraft
import com.spendwise.app.domain.model.TransactionType
import java.util.regex.Pattern

/**
 * Natural Language Processing (NLP) tokenizer that extracts amount, category,
 * transaction type, and payment channel from spoken sentences.
 * Example: "Spent 150 rupees on dinner with UPI", "Got 5000 allowance from dad"
 */
class VoiceTransactionParser {

    private val numberRegex = Pattern.compile("([0-9]+(?:\\.[0-9]{1,2})?)")

    fun parseSpokenText(spokenText: String): TransactionDraft? {
        val clean = spokenText.lowercase().trim()
        if (clean.isBlank()) return null

        // 1. Determine Type
        val isIncome = clean.startsWith("got") || clean.startsWith("received") ||
                clean.contains("allowance") || clean.contains("salary") || clean.contains("pocket money")
        val txType = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE

        // 2. Parse Numeric Amount
        var amount = 0.0
        val matcher = numberRegex.matcher(clean)
        if (matcher.find()) {
            amount = matcher.group(1)?.toDoubleOrNull() ?: 0.0
        }

        if (amount <= 0.0) return null

        // 3. Category Categorization
        val categoryName = when {
            clean.contains("dinner") || clean.contains("lunch") || clean.contains("food") ||
                    clean.contains("tea") || clean.contains("coffee") || clean.contains("burger") || clean.contains("pizza") -> "Food & Dining"
            clean.contains("grocery") || clean.contains("groceries") || clean.contains("milk") || clean.contains("veggies") -> "Groceries"
            clean.contains("rent") || clean.contains("electricity") || clean.contains("bill") || clean.contains("recharge") -> "Rent & Bills"
            clean.contains("cab") || clean.contains("auto") || clean.contains("uber") || clean.contains("ola") ||
                    clean.contains("fuel") || clean.contains("petrol") || clean.contains("bus") -> "Transit & Fuel"
            clean.contains("movie") || clean.contains("netflix") || clean.contains("game") -> "Leisure & Subscriptions"
            clean.contains("allowance") || clean.contains("pocket money") -> "Allowance / Pocket Money"
            clean.contains("salary") || clean.contains("freelance") || clean.contains("client") -> "Salary / Freelance"
            else -> if (isIncome) "Gift / Refund" else "Food & Dining"
        }

        // 4. Wallet Channel
        val walletName = when {
            clean.contains("cash") -> "Physical Cash"
            clean.contains("upi") || clean.contains("gpay") || clean.contains("paytm") || clean.contains("phonepe") -> "Digital Wallet / UPI"
            else -> "Bank Account"
        }

        return TransactionDraft(
            amount = amount,
            type = txType,
            categoryName = categoryName,
            walletName = walletName,
            note = spokenText,
            confidenceScore = 0.95f,
            source = "VOICE"
        )
    }
}
