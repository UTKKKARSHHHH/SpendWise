import 'package:flutter/material.dart';
import '../../models/models.dart';
import '../../state/spendwise_store.dart';

class QuickEntrySheet extends StatefulWidget {
  final SpendwiseStore store;
  final TransactionType initialType;

  const QuickEntrySheet({
    Key? key,
    required this.store,
    required this.initialType,
  }) : super(key: key);

  @override
  State<QuickEntrySheet> createState() => _QuickEntrySheetState();
}

class _QuickEntrySheetState extends State<QuickEntrySheet> {
  late TransactionType _selectedType;
  final TextEditingController _amountController = TextEditingController();
  final TextEditingController _noteController = TextEditingController();
  late String _selectedWalletId;
  late String _selectedCategoryId;

  bool _isAllowanceTrigger = false;
  bool _carryForward = true;

  @override
  void initState() {
    super.initState();
    _selectedType = widget.initialType;
    _selectedWalletId = widget.store.wallets.first.id;
    _selectedCategoryId = widget.store.categories
        .firstWhere((c) => c.type == _selectedType, orElse: () => widget.store.categories.first)
        .id;
    _isAllowanceTrigger = widget.initialType == TransactionType.income;
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.only(
        left: 20,
        right: 20,
        top: 20,
        bottom: MediaQuery.of(context).viewInsets.bottom + 20,
      ),
      decoration: const BoxDecoration(
        color: Color(0xFF1E293B),
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      child: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Tab Toggle
            Container(
              padding: const EdgeInsets.all(4),
              decoration: BoxDecoration(
                color: const Color(0xFF0F172A),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Row(
                children: [
                  Expanded(
                    child: GestureDetector(
                      onTap: () {
                        setState(() {
                          _selectedType = TransactionType.expense;
                          _isAllowanceTrigger = false;
                        });
                      },
                      child: Container(
                        padding: const EdgeInsets.symmetric(vertical: 10),
                        decoration: BoxDecoration(
                          color: _selectedType == TransactionType.expense
                              ? const Color(0xFFF43F5E)
                              : Colors.transparent,
                          borderRadius: BorderRadius.circular(10),
                        ),
                        alignment: Alignment.Center,
                        child: Text(
                          'Cash Out (Expense)',
                          style: TextStyle(
                            color: _selectedType == TransactionType.expense ? Colors.white : const Color(0xFF94A3B8),
                            fontWeight: FontWeight.w600,
                            fontSize: 13,
                          ),
                        ),
                      ),
                    ),
                  ),
                  Expanded(
                    child: GestureDetector(
                      onTap: () {
                        setState(() {
                          _selectedType = TransactionType.income;
                        });
                      },
                      child: Container(
                        padding: const EdgeInsets.symmetric(vertical: 10),
                        decoration: BoxDecoration(
                          color: _selectedType == TransactionType.income
                              ? const Color(0xFF10B981)
                              : Colors.transparent,
                          borderRadius: BorderRadius.circular(10),
                        ),
                        alignment: Alignment.Center,
                        child: Text(
                          'Cash In (Income)',
                          style: TextStyle(
                            color: _selectedType == TransactionType.income ? Colors.white : const Color(0xFF94A3B8),
                            fontWeight: FontWeight.w600,
                            fontSize: 13,
                          ),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
            // Amount Input
            TextField(
              controller: _amountController,
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
              style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold),
              decoration: InputDecoration(
                labelText: 'Amount (₹)',
                labelStyle: const TextStyle(color: Color(0xFF94A3B8)),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: const BorderSide(color: Color(0xFF334155)),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: BorderSide(
                    color: _selectedType == TransactionType.expense ? const Color(0xFFF43F5E) : const Color(0xFF10B981),
                  ),
                ),
              ),
            ),
            const SizedBox(height: 8),
            // Quick increment chips
            Row(
              children: [50, 100, 500, 2000].map((amt) {
                return Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: ActionChip(
                    label: Text('+$amt', style: const TextStyle(color: Colors.white, fontSize: 12)),
                    backgroundColor: const Color(0xFF334155),
                    onPressed: () {
                      final cur = double.tryParse(_amountController.text) ?? 0.0;
                      _amountController.text = (cur + amt).toStringAsFixed(0);
                    },
                  ),
                );
              }).toList(),
            ),
            const SizedBox(height: 14),
            // Wallet selection
            const Text('Wallet', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 12, fontWeight: FontWeight.w500)),
            const SizedBox(height: 6),
            Row(
              children: widget.store.wallets.map((w) {
                final isSelected = w.id == _selectedWalletId;
                return Expanded(
                  child: GestureDetector(
                    onTap: () => setState(() => _selectedWalletId = w.id),
                    child: Container(
                      margin: const EdgeInsets.symmetric(horizontal: 4),
                      padding: const EdgeInsets.symmetric(vertical: 8),
                      decoration: BoxDecoration(
                        color: isSelected ? const Color(0xFF10B981).withOpacity(0.2) : const Color(0xFF0F172A),
                        border: Border.all(color: isSelected ? const Color(0xFF10B981) : const Color(0xFF334155)),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      alignment: Alignment.Center,
                      child: Text(
                        w.name.split(' ').first,
                        style: TextStyle(
                          color: isSelected ? const Color(0xFF34D399) : const Color(0xFF94A3B8),
                          fontSize: 12,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ),
                  ),
                );
              }).toList(),
            ),
            const SizedBox(height: 14),
            // Note
            TextField(
              controller: _noteController,
              style: const TextStyle(color: Colors.white, fontSize: 14),
              decoration: InputDecoration(
                labelText: 'Note (Optional)',
                labelStyle: const TextStyle(color: Color(0xFF94A3B8)),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: const BorderSide(color: Color(0xFF334155)),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: const BorderSide(color: Color(0xFF3B82F6)),
                ),
              ),
            ),
            // Allowance Lifecycle trigger if Income
            if (_selectedType == TransactionType.income) ...[
              const SizedBox(height: 14),
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: const Color(0xFF0F172A),
                  borderRadius: BorderRadius.circular(14),
                ),
                child: Column(
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text('Allowance Lifecycle Trigger', style: TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.w600)),
                            Text('Archives old cycle & spins up new pool', style: TextStyle(color: Color(0xFF64748B), fontSize: 11)),
                          ],
                        ),
                        Switch(
                          value: _isAllowanceTrigger,
                          activeColor: const Color(0xFF10B981),
                          onChanged: (val) => setState(() => _isAllowanceTrigger = val),
                        ),
                      ],
                    ),
                    if (_isAllowanceTrigger) ...[
                      const Divider(color: Color(0xFF334155)),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          const Text('Carry-Forward Balance', style: TextStyle(color: Color(0xFF34D399), fontSize: 12, fontWeight: FontWeight.w500)),
                          Switch(
                            value: _carryForward,
                            activeColor: const Color(0xFF34D399),
                            onChanged: (val) => setState(() => _carryForward = val),
                          ),
                        ],
                      ),
                    ],
                  ],
                ),
              ),
            ],
            const SizedBox(height: 20),
            SizedBox(
              width: double.infinity,
              height: 48,
              child: ElevatedButton(
                onPressed: () {
                  final amt = double.tryParse(_amountController.text) ?? 0.0;
                  if (amt <= 0) return;

                  if (_selectedType == TransactionType.income && _isAllowanceTrigger) {
                    widget.store.logAllowanceTrigger(
                      amount: amt,
                      walletId: _selectedWalletId,
                      categoryId: _selectedCategoryId,
                      carryForward: _carryForward,
                    );
                  } else {
                    widget.store.logQuickTransaction(
                      amount: amt,
                      type: _selectedType,
                      walletId: _selectedWalletId,
                      categoryId: _selectedCategoryId,
                      note: _noteController.text,
                    );
                  }
                  Navigator.pop(context);
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: _selectedType == TransactionType.expense
                      ? const Color(0xFFF43F5E)
                      : const Color(0xFF10B981),
                  shape: RoundedCornerShape(12),
                ),
                child: Text(
                  _selectedType == TransactionType.expense
                      ? 'Log Expense'
                      : _isAllowanceTrigger
                          ? 'Start New Allowance Cycle'
                          : 'Log Income',
                  style: const TextStyle(color: Colors.white, fontSize: 15, fontWeight: FontWeight.bold),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
