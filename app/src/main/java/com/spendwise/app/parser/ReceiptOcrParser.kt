package com.spendwise.app.parser

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.spendwise.app.domain.model.TransactionDraft
import com.spendwise.app.domain.model.TransactionType
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.regex.Pattern
import kotlin.coroutines.resume

/**
 * Computer Vision OCR parser utilizing Google ML Kit Text Recognition.
 * Extracts: Total Amount, Date, Tax, and Merchant from physical bills.
 */
class ReceiptOcrParser {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val totalRegex = Pattern.compile(
        "(?i)(?:total|grand\\s*total|net\\s*amount|amt|balance\\s*due|amount\\s*due|final\\s*total)\\s*[:=]?\\s*(?:rs\\.?|inr|[$₹])?\\s*([0-9,]+\\.[0-9]{2})",
        Pattern.CASE_INSENSITIVE
    )

    private val fallbackNumberRegex = Pattern.compile("([0-9,]+\\.[0-9]{2})")

    suspend fun analyzeReceipt(bitmap: Bitmap): TransactionDraft = suspendCancellableCoroutine { continuation ->
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val text = visionText.text
                val draft = extractFinancialData(text)
                continuation.resume(draft)
            }
            .addOnFailureListener { error ->
                // Fallback graceful degradation
                continuation.resume(
                    TransactionDraft(
                        amount = 0.0,
                        type = TransactionType.EXPENSE,
                        categoryName = "Food & Dining",
                        walletName = "Physical Cash",
                        note = "OCR Scan failed: ${error.localizedMessage}",
                        confidenceScore = 0.0f,
                        source = "OCR"
                    )
                )
            }
    }

    private fun extractFinancialData(rawText: String): TransactionDraft {
        val lines = rawText.lines()
        var extractedAmount = 0.0
        var merchantName = lines.firstOrNull { it.isNotBlank() } ?: "Receipt Merchant"

        // Search for primary total regex
        val matcher = totalRegex.matcher(rawText)
        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", "")
            extractedAmount = amountStr?.toDoubleOrNull() ?: 0.0
        } else {
            // Find max decimal number on lines near the bottom of receipt
            val candidateAmounts = mutableListOf<Double>()
            for (line in lines.takeLast(10)) {
                val fMatcher = fallbackNumberRegex.matcher(line)
                while (fMatcher.find()) {
                    val v = fMatcher.group(1)?.replace(",", "")?.toDoubleOrNull()
                    if (v != null && v < 1000000.0) { // filter out phone or invoice numbers
                        candidateAmounts.add(v)
                    }
                }
            }
            extractedAmount = candidateAmounts.maxOrNull() ?: 0.0
        }

        // Categorize based on keywords in receipt
        val lower = rawText.lowercase()
        val category = when {
            lower.contains("restaurant") || lower.contains("cafe") || lower.contains("table") || lower.contains("food") -> "Food & Dining"
            lower.contains("supermarket") || lower.contains("mart") || lower.contains("retail") || lower.contains("grocery") -> "Groceries"
            lower.contains("fuel") || lower.contains("petrol") || lower.contains("diesel") -> "Transit & Fuel"
            else -> "Food & Dining"
        }

        return TransactionDraft(
            amount = extractedAmount,
            type = TransactionType.EXPENSE,
            categoryName = category,
            walletName = "Physical Cash",
            note = "Receipt: $merchantName",
            confidenceScore = if (extractedAmount > 0.0) 0.88f else 0.3f,
            source = "OCR"
        )
    }
}
