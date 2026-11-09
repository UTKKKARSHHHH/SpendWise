import 'package:uuid/uuid.dart';
import '../models/models.dart';

class CycleTransitionResult {
  final BudgetCycle? cycleToArchive;
  final BudgetCycle newActiveCycle;
  final double rolledOverAmount;

  CycleTransitionResult({
    required this.cycleToArchive,
    required this.newActiveCycle,
    required this.rolledOverAmount,
  });
}

class IncomeTriggeredCycleEngine {
  CycleTransitionResult processCycleTransition({
    required BudgetCycle? currentActiveCycle,
    required double activeExpenses,
    required double incidentalIncome,
    required double newAllowanceAmount,
    required bool carryForwardLeftover,
    String? customTitle,
    int targetDays = 30,
    DateTime? timestamp,
  }) {
    final now = timestamp ?? DateTime.now();
    double carryOver = 0.0;

    BudgetCycle? archived;
    if (currentActiveCycle != null) {
      final pool = currentActiveCycle.totalStartingPool;
      final remaining = pool + incidentalIncome - activeExpenses;
      if (carryForwardLeftover && remaining > 0) {
        carryOver = remaining;
      }
      archived = currentActiveCycle.copyWith(
        endedAt: now,
        isActive: false,
      );
    }

    final newCycle = BudgetCycle(
      id = const Uuid().v4(),
      title = customTitle ?? 'Allowance Pool',
      startedAt = now,
      initialAllowance = newAllowanceAmount,
      carryForwardAmount = carryOver,
      isActive = true,
      targetDays = targetDays,
    );

    return CycleTransitionResult(
      cycleToArchive = archived,
      newActiveCycle = newCycle,
      rolledOverAmount = carryOver,
    );
  }

  double computeCurrentPool(
    BudgetCycle? activeCycle,
    List<Transaction> transactions,
  ) {
    if (activeCycle == null) return 0.0;

    final cycleTxs = transactions.where((t) => t.cycleId == activeCycle.id).toList();

    final expenses = cycleTxs
        .where((t) => t.type == TransactionType.expense)
        .fold(0.0, (sum, t) => sum + t.amount);

    final minorIncome = cycleTxs
        .where((t) => t.type == TransactionType.income && !t.triggersNewCycle)
        .fold(0.0, (sum, t) => sum + t.amount);

    return activeCycle.totalStartingPool + minorIncome - expenses;
  }
}

class CashFlowForecast {
  final double currentDailyBurnRate;
  final double recommendedDailySpend;
  final int projectedDaysRemaining;
  final int targetDaysRemaining;
  final bool isShortagePredicted;
  final int? estimatedShortageDay;
  final double projectedDeficitAmount;

  CashFlowForecast({
    required this.currentDailyBurnRate,
    required this.recommendedDailySpend,
    required this.projectedDaysRemaining,
    required this.targetDaysRemaining,
    required this.isShortagePredicted,
    this.estimatedShortageDay,
    required this.projectedDeficitAmount,
  });
}

class PredictiveAnalyticsEngine {
  CashFlowForecast forecastCashFlow({
    required BudgetCycle activeCycle,
    required List<Transaction> cycleTransactions,
    required double currentPoolBalance,
  }) {
    final elapsedDays = activeCycle.elapsedDays.clamp(1, 365);
    final targetDaysRemaining = (activeCycle.targetDays - elapsedDays).clamp(1, 365);

    final totalExpenses = cycleTransactions
        .where((t) => t.type == TransactionType.expense)
        .fold(0.0, (sum, t) => sum + t.amount);

    final currentDailyBurnRate = totalExpenses / elapsedDays;
    final recommendedDailySpend = (currentPoolBalance / targetDaysRemaining).clamp(0.0, 1000000.0);

    final projectedDaysRemaining = currentDailyBurnRate > 0
        ? (currentPoolBalance / currentDailyBurnRate).toInt()
        : targetDaysRemaining;

    final isShortage = projectedDaysRemaining < targetDaysRemaining && currentPoolBalance > 0;
    final estimatedDay = isShortage ? elapsedDays + projectedDaysRemaining : null;
    final deficit = isShortage
        ? (currentDailyBurnRate * targetDaysRemaining) - currentPoolBalance
        : 0.0;

    return CashFlowForecast(
      currentDailyBurnRate: currentDailyBurnRate,
      recommendedDailySpend: recommendedDailySpend,
      projectedDaysRemaining: projectedDaysRemaining,
      targetDaysRemaining: targetDaysRemaining,
      isShortagePredicted: isShortage,
      estimatedShortageDay: estimatedDay,
      projectedDeficitAmount: deficit.clamp(0.0, 1000000.0),
    );
  }
}
