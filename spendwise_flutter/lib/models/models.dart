import 'package:uuid/uuid.dart';

enum TransactionType { income, expense }

enum WalletType { physicalCash, bankAccount, digitalWallet }

enum DebtDirection { youAreOwed, youOwe }

class BudgetCycle {
  final String id;
  final String title;
  final DateTime startedAt;
  final DateTime? endedAt;
  final double initialAllowance;
  final double carryForwardAmount;
  final bool isActive;
  final int targetDays;

  BudgetCycle({
    required this.id,
    required this.title,
    required this.startedAt,
    this.endedAt,
    required this.initialAllowance,
    this.carryForwardAmount = 0.0,
    this.isActive = true,
    this.targetDays = 30,
  });

  double get totalStartingPool => initialAllowance + carryForwardAmount;

  int get elapsedDays {
    return DateTime.now().difference(startedAt).inDays.clamp(0, 365);
  }

  double calculateDailySafeToSpend(double currentPool) {
    if (!isActive || currentPool <= 0) return 0.0;
    final remainingDays = (targetDays - elapsedDays).clamp(1, 365);
    return currentPool / remainingDays;
  }

  BudgetCycle copyWith({
    DateTime? endedAt,
    bool? isActive,
    double? carryForwardAmount,
  }) {
    return BudgetCycle(
      id: id,
      title: title,
      startedAt: startedAt,
      endedAt: endedAt ?? this.endedAt,
      initialAllowance: initialAllowance,
      carryForwardAmount: carryForwardAmount ?? this.carryForwardAmount,
      isActive: isActive ?? this.isActive,
      targetDays: targetDays,
    );
  }
}

class Wallet {
  final String id;
  final String name;
  final WalletType type;
  double currentBalance;
  final String currency;

  Wallet({
    required this.id,
    required this.name,
    required this.type,
    required this.currentBalance,
    this.currency = 'INR',
  });
}

class Category {
  final String id;
  final String name;
  final String iconKey;
  final String colorHex;
  final TransactionType type;

  Category({
    required this.id,
    required this.name,
    required this.iconKey,
    required this.colorHex,
    required this.type,
  });
}

class Transaction {
  final String id;
  final String cycleId;
  final String walletId;
  final String categoryId;
  final double amount;
  final TransactionType type;
  final DateTime timestamp;
  final String note;
  final String source; // MANUAL, VOICE, OCR, SMS, CYCLE_TRIGGER
  final bool triggersNewCycle;

  Transaction({
    required this.id,
    required this.cycleId,
    required this.walletId,
    required this.categoryId,
    required this.amount,
    required this.type,
    required this.timestamp,
    required this.note,
    this.source = 'MANUAL',
    this.triggersNewCycle = false,
  });
}

class DebtItem {
  final String id;
  final String contactName;
  final String phoneNumber;
  final double amount;
  final DebtDirection direction;
  final String note;
  final DateTime createdAt;
  bool isSettled;

  DebtItem({
    required this.id,
    required this.contactName,
    required this.phoneNumber,
    required this.amount,
    required this.direction,
    required this.note,
    required this.createdAt,
    this.isSettled = false,
  });
}

class TransactionDraft {
  final double amount;
  final TransactionType type;
  final String categoryName;
  final String walletName;
  final String note;
  final double confidenceScore;
  final String source;

  TransactionDraft({
    required this.amount,
    required this.type,
    required this.categoryName,
    required this.walletName,
    required this.note,
    required this.confidenceScore,
    required this.source,
  });
}
