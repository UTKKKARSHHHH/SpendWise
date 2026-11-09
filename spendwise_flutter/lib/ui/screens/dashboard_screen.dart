import 'package:flutter/material.dart';
import '../../models/models.dart';
import '../../state/spendwise_store.dart';
import '../widgets/allowance_pool_card.dart';
import '../widgets/quick_entry_sheet.dart';
import '../widgets/transaction_tile.dart';

class DashboardScreen extends StatelessWidget {
  final SpendwiseStore store;
  final VoidCallback onNavigateVoice;
  final VoidCallback onNavigateOcr;

  const DashboardScreen({
    Key? key,
    required this.store,
    required this.onNavigateVoice,
    required this.onNavigateOcr,
  }) : super(key: key);

  void _openSheet(BuildContext context, TransactionType type) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => QuickEntrySheet(store: store, initialType: type),
    );
  }

  @override
  Widget build(BuildContext context) {
    final filtered = store.filteredTransactions;

    final isLowAllowance = store.activeCycle != null &&
        store.activeCycle!.totalStartingPool > 0 &&
        store.currentAllowancePool <= (0.25 * store.activeCycle!.totalStartingPool);

    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const SizedBox(height: 12),
              // Header Running Balance & Notification Section
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text('Total Net Balance', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
                      Text(
                        '₹${store.totalNetBalance.toStringAsFixed(2)}',
                        style: const TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.bold),
                      ),
                    ],
                  ),
                  Row(
                    children: [
                      // Notification Bell for 25% shortage alerts
                      GestureDetector(
                        onTap: () {
                          showDialog(
                            context: context,
                            builder: (ctx) => AlertDialog(
                              backgroundColor: const Color(0xFF1E293B),
                              title: Row(
                                children: [
                                  Icon(
                                    Icons.notifications,
                                    color: isLowAllowance ? const Color(0xFFF59E0B) : const Color(0xFF10B981),
                                    size: 20,
                                  ),
                                  const SizedBox(width: 8),
                                  const Text('Notifications & Alerts', style: TextStyle(color: Colors.white, fontSize: 16)),
                                ],
                              ),
                              content: isLowAllowance
                                  ? Container(
                                      padding: const EdgeInsets.all(12),
                                      decoration: BoxDecoration(
                                        color: const Color(0xFFF59E0B).withOpacity(0.12),
                                        borderRadius: BorderRadius.circular(12),
                                        border: Border.all(color: const Color(0xFFF59E0B).withOpacity(0.4)),
                                      ),
                                      child: Column(
                                        mainAxisSize: MainAxisSize.min,
                                        crossAxisAlignment: CrossAxisAlignment.start,
                                        children: [
                                          const Row(
                                            children: [
                                              Icon(Icons.warning, color: Color(0xFFF59E0B), size: 16),
                                              SizedBox(width: 6),
                                              Text('Low Allowance Alert (≤ 25%)', style: TextStyle(color: Color(0xFFF59E0B), fontWeight: FontWeight.bold, fontSize: 13)),
                                            ],
                                          ),
                                          const SizedBox(height: 6),
                                          Text(
                                            'Only ₹${store.currentAllowancePool.toStringAsFixed(2)} left in your active allowance pool (≤ 25% remaining). Adjust daily spend to ₹${store.dailySafeToSpend.toStringAsFixed(0)}/day.',
                                            style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12),
                                          ),
                                        ],
                                      ),
                                    )
                                  : Container(
                                      padding: const EdgeInsets.all(12),
                                      decoration: BoxDecoration(
                                        color: const Color(0xFF0F172A),
                                        borderRadius: BorderRadius.circular(12),
                                      ),
                                      child: const Text(
                                        'No active shortage alerts. Your allowance pool is healthy (> 25%).',
                                        style: TextStyle(color: Color(0xFF64748B), fontSize: 12),
                                      ),
                                    ),
                              actions: [
                                TextButton(
                                  onPressed: () => Navigator.pop(ctx),
                                  child: const Text('Close', style: TextStyle(color: Color(0xFF10B981))),
                                ),
                              ],
                            ),
                          );
                        },
                        child: Stack(
                          children: [
                            Container(
                              padding: const EdgeInsets.all(8),
                              decoration: const BoxDecoration(
                                color: Color(0xFF1E293B),
                                shape: BoxShape.circle,
                              ),
                              child: Icon(
                                Icons.notifications,
                                color: isLowAllowance ? const Color(0xFFF59E0B) : const Color(0xFF94A3B8),
                                size: 18,
                              ),
                            ),
                            if (isLowAllowance)
                              Positioned(
                                top: 0,
                                right: 0,
                                child: Container(
                                  width: 8,
                                  height: 8,
                                  decoration: const BoxDecoration(
                                    color: Color(0xFFF43F5E),
                                    shape: BoxShape.circle,
                                  ),
                                ),
                              ),
                          ],
                        ),
                      ),
                      const SizedBox(width: 8),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                        decoration: BoxDecoration(
                          color: const Color(0xFF1E293B),
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: Text(
                          '${store.wallets.length} Wallets',
                          style: const TextStyle(color: Color(0xFF10B981), fontSize: 11, fontWeight: FontWeight.w600),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
              const SizedBox(height: 14),
              // Allowance Pool Card
              AllowancePoolCard(
                store: store,
                onNewAllowance: () => _openSheet(context, TransactionType.income),
              ),
              const SizedBox(height: 14),
              // Action Buttons Row
              Row(
                children: [
                  _ActionButton(
                    label: 'Cash Out',
                    icon: Icons.remove,
                    color: const Color(0xFFF43F5E),
                    onTap: () => _openSheet(context, TransactionType.expense),
                  ),
                  const SizedBox(width: 8),
                  _ActionButton(
                    label: 'Cash In',
                    icon: Icons.add,
                    color: const Color(0xFF10B981),
                    onTap: () => _openSheet(context, TransactionType.income),
                  ),
                  const SizedBox(width: 8),
                  _ActionButton(
                    label: 'Voice',
                    icon: Icons.mic,
                    color: const Color(0xFFA855F7),
                    onTap: onNavigateVoice,
                  ),
                  const SizedBox(width: 8),
                  _ActionButton(
                    label: 'Scan',
                    icon: Icons.camera_alt,
                    color: const Color(0xFF38BDF8),
                    onTap: onNavigateOcr,
                  ),
                ],
              ),
              const SizedBox(height: 14),
              // Search Field
              TextField(
                onChanged: store.updateSearch,
                style: const TextStyle(color: Colors.white, fontSize: 14),
                decoration: InputDecoration(
                  hintText: 'Search logs by note, amount...',
                  hintStyle: const TextStyle(color: Color(0xFF64748B), fontSize: 13),
                  prefixIcon: const Icon(Icons.search, color: Color(0xFF64748B), size: 18),
                  filled: true,
                  fillColor: const Color(0xFF1E293B),
                  contentPadding: const EdgeInsets.symmetric(vertical: 0),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(14),
                    borderSide: const BorderSide(color: Color(0xFF334155)),
                  ),
                  enabledBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(14),
                    borderSide: const BorderSide(color: Color(0xFF334155)),
                  ),
                ),
              ),
              const SizedBox(height: 14),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text('Recent Transactions', style: TextStyle(color: Colors.white, fontSize: 15, fontWeight: FontWeight.w600)),
                  Text('${filtered.length} entries', style: const TextStyle(color: Color(0xFF64748B), fontSize: 12)),
                ],
              ),
              const SizedBox(height: 8),
              Expanded(
                child: filtered.isEmpty
                    ? const Center(
                        child: Text('No transactions found.', style: TextStyle(color: Color(0xFF64748B))),
                      )
                    : ListView.builder(
                        itemCount: filtered.length,
                        itemBuilder: (context, idx) {
                          final tx = filtered[idx];
                          final w = store.wallets.firstWhere((w) => w.id == tx.walletId, orElse: () => store.wallets.first);
                          final c = store.categories.firstWhere((c) => c.id == tx.categoryId, orElse: () => store.categories.first);
                          return TransactionTile(transaction: tx, wallet: w, category: c);
                        },
                      ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _ActionButton extends StatelessWidget {
  final String label;
  final IconData icon;
  final Color color;
  final VoidCallback onTap;

  const _ActionButton({
    Key? key,
    required this.label,
    required this.icon,
    required this.color,
    required this.onTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: GestureDetector(
        onTap: onTap,
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 10),
          decoration: BoxDecoration(
            color: color.withOpacity(0.15),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Column(
            children: [
              Icon(icon, color: color, size: 18),
              const SizedBox(height: 4),
              Text(label, style: TextStyle(color: color, fontSize: 11, fontWeight: FontWeight.w600)),
            ],
          ),
        ),
      ),
    );
  }
}
