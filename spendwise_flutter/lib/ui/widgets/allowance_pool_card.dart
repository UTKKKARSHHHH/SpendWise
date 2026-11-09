import 'package:flutter/material.dart';
import '../../state/spendwise_store.dart';

class AllowancePoolCard extends StatelessWidget {
  final SpendwiseStore store;
  final VoidCallback onNewAllowance;

  const AllowancePoolCard({
    Key? key,
    required this.store,
    required this.onNewAllowance,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final pool = store.currentAllowancePool;
    final total = store.activeCycle?.totalStartingPool ?? 1.0;
    final progress = (pool / total).clamp(0.0, 1.0);
    final forecast = store.forecast;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [Color(0xFF1E293B), Color(0xFF0F172A)],
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
        ),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0xFF334155)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    store.activeCycle?.title ?? 'Current Allowance Pool',
                    style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 13, fontWeight: FontWeight.w500),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '₹${pool.toStringAsFixed(2)}',
                    style: const TextStyle(color: Colors.white, fontSize: 28, fontWeight: FontWeight.bold),
                  ),
                ],
              ),
              ElevatedButton.icon(
                onPressed: onNewAllowance,
                icon: const Icon(Icons.add, size: 16),
                label: const Text('Allowance', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600)),
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF10B981).withOpacity(0.2),
                  foregroundColor: const Color(0xFF34D399),
                  elevation: 0,
                  shape: RoundedCornerShape(12),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          ClipRRect(
            borderRadius: BorderRadius.circular(8),
            child: LinearProgressIndicator(
              value: progress,
              minHeight: 8,
              backgroundColor: const Color(0xFF334155),
              valueColor: AlwaysStoppedAnimation<Color>(
                progress > 0.25 ? const Color(0xFF10B981) : const Color(0xFFF43F5E),
              ),
            ),
          ),
          const SizedBox(height: 14),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  const Icon(Icons.speed, color: Color(0xFF34D399), size: 18),
                  const SizedBox(width: 6),
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text('Daily Safe-to-Spend', style: TextStyle(color: Color(0xFF64748B), fontSize: 11)),
                      Text(
                        '₹${store.dailySafeToSpend.toStringAsFixed(0)} / day',
                        style: const TextStyle(color: Colors.white, fontSize: 14, fontWeight: FontWeight.w600),
                      ),
                    ],
                  ),
                ],
              ),
              if (store.activeCycle != null)
                Text(
                  'Day ${store.activeCycle!.elapsedDays} of ${store.activeCycle!.targetDays}',
                  style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12, fontWeight: FontWeight.w500),
                )
              else
                const Text(
                  'Not set yet',
                  style: TextStyle(color: Color(0xFF64748B), fontSize: 12),
                ),
            ],
          ),
        ],
      ),
    );
  }
}
