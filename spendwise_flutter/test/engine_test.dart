import 'package:flutter_test/flutter_test.dart';
import 'package:spendwise/engine/cycle_engine.dart';
import 'package:spendwise/models/models.dart';
import 'package:spendwise/services/parsers.dart';

void main() {
  group('IncomeTriggeredCycleEngine', () {
    final engine = IncomeTriggeredCycleEngine();

    test('Early allowance archives previous cycle and rolls forward remaining pool', () {
      final now = DateTime.now();
      final oldCycle = BudgetCycle(
        id: 'cycle_1',
        title: 'Allowance Pool #1',
        startedAt: now.subtract(const Duration(days: 26)),
        initialAllowance: 10000.0,
        carryForwardAmount: 0.0,
        isActive: true,
        targetDays: 30,
      );

      final result = engine.processCycleTransition(
        currentActiveCycle: oldCycle,
        activeExpenses: 6500.0,
        incidentalIncome: 500.0,
        newAllowanceAmount: 12000.0,
        carryForwardLeftover: true,
        timestamp: now,
      );

      expect(result.cycleToArchive, isNotNull);
      expect(result.cycleToArchive!.isActive, isFalse);
      expect(result.rolledOverAmount, 4000.0); // 10000 + 500 - 6500 = 4000
      expect(result.newActiveCycle.totalStartingPool, 16000.0); // 12000 + 4000
    });
  });

  group('Parsers', () {
    final sms = SmsParserService();
    final voice = VoiceParserService();

    test('SMS debit parser extracts amount and category', () {
      final draft = sms.parse('HDFCBK', 'Rs 450.00 spent on your card at Starbucks');
      expect(draft, isNotNull);
      expect(draft!.amount, 450.0);
      expect(draft.type, TransactionType.expense);
      expect(draft.categoryName, 'Food & Dining');
    });

    test('Voice parser extracts amount and category', () {
      final draft = voice.parseSpokenText('Spent 150 rupees on dinner');
      expect(draft, isNotNull);
      expect(draft!.amount, 150.0);
      expect(draft.categoryName, 'Food & Dining');
    });
  });
}
