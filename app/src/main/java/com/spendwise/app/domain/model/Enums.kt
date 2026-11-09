package com.spendwise.app.domain.model

enum class TransactionType {
    INCOME,
    EXPENSE
}

enum class WalletType {
    PHYSICAL_CASH,
    BANK_ACCOUNT,
    DIGITAL_WALLET
}

enum class DebtDirection {
    YOU_ARE_OWED, // Lent money, to receive
    YOU_OWE       // Borrowed money, to pay back
}
