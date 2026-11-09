import 'package:flutter/foundation.dart';
import 'package:uuid/uuid.dart';
import '../models/models.dart';
import '../engine/cycle_engine.dart';
import '../services/parsers.dart';

class SpendwiseStore extends ChangeNotifier {
  final IncomeTriggeredCycleEngine cycleEngine = IncomeTriggeredCycleEngine();
  final PredictiveAnalyticsEngine predictiveEngine = PredictiveAnalyticsEngine();
  final SmsParserService smsParser = SmsParserService();
  final VoiceParserService voiceParser = VoiceParserService();

  BudgetCycle? activeCycle;
  List<Wallet> wallets = [];
  List<Category> categories = [];
  List<Transaction> transactions = [];
  List<DebtItem> debts = [];

  String searchQuery = '';
  String? selectedWalletFilter;
  String? selectedCategoryFilter;

  SpendwiseStore() {
    _initDefaults();
  }

  void _initDefaults() {
    // Zero-prefilled data: completely user-entered, no prefilled balances or specific bank names
    wallets = [
      Wallet(id: 'w_cash', name: 'Physical Cash', type: WalletType.physicalCash, currentBalance: 0.0),
      Wallet(id: 'w_bank', name: 'Bank Account', type: WalletType.bankAccount, currentBalance: 0.0),
      Wallet(id: 'w_upi', name: 'Digital Wallet / UPI', type: WalletType.digitalWallet, currentBalance: 0.0),
    ];

    categories = [
      Category(id: 'c_food', name: 'Food & Dining', iconKey: 'fastfood', colorHex: '#EF4444', type: TransactionType.expense),
      Category(id: 'c_grocery', name: 'Groceries', iconKey: 'shopping_cart', colorHex: '#F59E0B', type: TransactionType.expense),
      Category(id: 'c_rent', name: 'Rent & Bills', iconKey: 'home', colorHex: '#6366F1', type: TransactionType.expense),
      Category(id: 'c_transit', name: 'Transit & Fuel', iconKey: 'commute', colorHex: '#06B6D4', type: TransactionType.expense),
      Category(id: 'c_fun', name: 'Leisure & Fun', iconKey: 'movie', colorHex: '#EC4899', type: TransactionType.expense),
      Category(id: 'c_allowance', name: 'Allowance / Pocket Money', iconKey: 'wallet', colorHex: '#10B981', type: TransactionType.income),
      Category(id: 'c_salary', name: 'Salary / Freelance', iconKey: 'work', colorHex: '#3B82F6', type: TransactionType.income),
    ];

    // No prefilled allowance cycle: must be entered manually by the user
    activeCycle = null;

    // No dummy transactions
    transactions = [];

    // No dummy debts
    debts = [];
  }

  double get currentAllowancePool => cycleEngine.computeCurrentPool(activeCycle, transactions);

  double get totalNetBalance => wallets.fold(0.0, (sum, w) => sum + w.currentBalance);

  double get dailySafeToSpend => activeCycle?.calculateDailySafeToSpend(currentAllowancePool) ?? 0.0;

  CashFlowForecast? get forecast {
    if (activeCycle == null) return null;
    final cycleTxs = transactions.where((t) => t.cycleId == activeCycle!.id).toList();
    return predictiveEngine.forecastCashFlow(
      activeCycle: activeCycle!,
      cycleTransactions: cycleTxs,
      currentPoolBalance: currentAllowancePool,
    );
  }

  List<Transaction> get filteredTransactions {
    return transactions.where((t) {
      final matchesQuery = searchQuery.isEmpty ||
          t.note.toLowerCase().contains(searchQuery.toLowerCase()) ||
          t.amount.toString().contains(searchQuery);
      final matchesWallet = selectedWalletFilter == null || t.walletId == selectedWalletFilter;
      final matchesCategory = selectedCategoryFilter == null || t.categoryId == selectedCategoryFilter;
      return matchesQuery && matchesWallet && matchesCategory;
    }).toList();
  }

  void updateSearch(String q) {
    searchQuery = q;
    notifyListeners();
  }

  void filterByWallet(String? wid) {
    selectedWalletFilter = wid;
    notifyListeners();
  }

  void filterByCategory(String? cid) {
    selectedCategoryFilter = cid;
    notifyListeners();
  }

  void logQuickTransaction({
    required double amount,
    required TransactionType type,
    required String walletId,
    required String categoryId,
    required String note,
    String source = 'MANUAL',
  }) {
    final tx = Transaction(
      id: const Uuid().v4(),
      cycleId: activeCycle?.id ?? 'default',
      walletId: walletId,
      categoryId: categoryId,
      amount: amount,
      type: type,
      timestamp: DateTime.now(),
      note: note,
      source: source,
    );
    transactions.insert(0, tx);

    final wallet = wallets.firstWhere((w) => w.id == walletId, orElse: () => wallets.first);
    if (type == TransactionType.income) {
      wallet.currentBalance += amount;
    } else {
      wallet.currentBalance -= amount;
    }
    notifyListeners();
  }

  void logAllowanceTrigger({
    required double amount,
    required String walletId,
    required String categoryId,
    required bool carryForward,
    int targetDays = 30,
  }) {
    final activeExpenses = transactions
        .where((t) => t.cycleId == activeCycle?.id && t.type == TransactionType.expense)
        .fold(0.0, (sum, t) => sum + t.amount);

    final incidental = transactions
        .where((t) => t.cycleId == activeCycle?.id && t.type == TransactionType.income && !t.triggersNewCycle)
        .fold(0.0, (sum, t) => sum + t.amount);

    final result = cycleEngine.processCycleTransition(
      currentActiveCycle: activeCycle,
      activeExpenses: activeExpenses,
      incidentalIncome: incidental,
      newAllowanceAmount: amount,
      carryForwardLeftover: carryForward,
      targetDays: targetDays,
    );

    activeCycle = result.newActiveCycle;

    final tx = Transaction(
      id: const Uuid().v4(),
      cycleId: activeCycle!.id,
      walletId: walletId,
      categoryId: categoryId,
      amount: amount,
      type: TransactionType.income,
      timestamp: DateTime.now(),
      note = result.rolledOverAmount > 0
          ? 'Allowance Cycle Influx (Rolled ₹${result.rolledOverAmount.toStringAsFixed(0)} forward)'
          : 'Fresh Allowance Cycle Trigger',
      source = 'CYCLE_TRIGGER',
      triggersNewCycle = true,
    );
    transactions.insert(0, tx);

    final wallet = wallets.firstWhere((w) => w.id == walletId, orElse: () => wallets.first);
    wallet.currentBalance += amount;
    notifyListeners();
  }

  void commitDraft(TransactionDraft draft) {
    final targetWallet = wallets.firstWhere(
      (w) => w.name.toLowerCase().contains(draft.walletName.toLowerCase()),
      orElse: () => wallets.first,
    );
    final targetCat = categories.firstWhere(
      (c) => c.name.toLowerCase().contains(draft.categoryName.toLowerCase()),
      orElse: () => categories.first,
    );

    logQuickTransaction(
      amount: draft.amount,
      type: draft.type,
      walletId: targetWallet.id,
      categoryId: targetCat.id,
      note: draft.note,
      source: draft.source,
    );
  }

  void updateWallet(String id, String newName, double newBalance) {
    final idx = wallets.indexWhere((w) => w.id == id);
    if (idx != -1) {
      wallets[idx] = Wallet(
        id: id,
        name: newName,
        type: wallets[idx].type,
        currentBalance: newBalance,
      );
      notifyListeners();
    }
  }

  void addCustomWallet(String name, WalletType type, double initialBalance) {
    wallets.add(Wallet(
      id: const Uuid().v4(),
      name: name,
      type: type,
      currentBalance: initialBalance,
    ));
    notifyListeners();
  }

  void deleteWallet(String id) {
    wallets.removeWhere((w) => w.id == id);
    notifyListeners();
  }

  void addDebt({
    required String name,
    required String phone,
    required double amount,
    required DebtDirection direction,
    required String note,
  }) {
    debts.insert(
      0,
      DebtItem(
        id: const Uuid().v4(),
        contactName: name,
        phoneNumber: phone,
        amount: amount,
        direction: direction,
        note: note,
        createdAt: DateTime.now(),
      ),
    );
    notifyListeners();
  }

  void settleDebt(DebtItem item) {
    item.isSettled = true;
    notifyListeners();
  }
}
