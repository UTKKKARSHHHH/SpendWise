package com.spendwise.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.spendwise.app.SpendwiseApplication
import com.spendwise.app.domain.model.Transaction
import com.spendwise.app.parser.SmsTransactionParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

class FinancialSmsReceiver : BroadcastReceiver() {

    private val parser = SmsTransactionParser()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val app = context.applicationContext as? SpendwiseApplication ?: return
        val repository = app.repository

        for (msg in messages) {
            val sender = msg.originatingAddress ?: continue
            val body = msg.messageBody ?: continue

            val draft = parser.parse(sender, body)
            if (draft != null) {
                Log.d("SpendwiseSMS", "Parsed financial alert: ₹${draft.amount} from $sender")

                CoroutineScope(Dispatchers.IO).launch {
                    val activeCycle = repository.getActiveCycle().firstOrNull() ?: return@launch
                    val wallets = repository.getWallets().firstOrNull() ?: emptyList()
                    val categories = repository.getCategories().firstOrNull() ?: emptyList()

                    val targetWallet = wallets.find { it.name.contains("UPI", ignoreCase = true) }
                        ?: wallets.firstOrNull() ?: return@launch

                    val targetCategory = categories.find { it.name.equals(draft.categoryName, ignoreCase = true) }
                        ?: categories.firstOrNull() ?: return@launch

                    val tx = Transaction(
                        id = UUID.randomUUID().toString(),
                        cycleId = activeCycle.id,
                        walletId = targetWallet.id,
                        categoryId = targetCategory.id,
                        amount = draft.amount,
                        type = draft.type,
                        timestamp = System.currentTimeMillis(),
                        note = draft.note,
                        receiptImagePath = null,
                        source = "SMS_AUTO",
                        triggersNewCycle = false
                    )

                    repository.insertTransaction(tx)
                }
            }
        }
    }
}
