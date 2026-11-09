import 'package:flutter/material.dart';
import '../../models/models.dart';
import '../../state/spendwise_store.dart';

class AnalyticsScreen extends StatelessWidget {
  final SpendwiseStore store;

  const AnalyticsScreen({Key? key, required this.store}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final forecast = store.forecast;
    final expenses = store.transactions.where((t) => t.type == TransactionType.expense).toList();
    final totalExpense = expenses.fold(0.0, (sum, t) => sum + t.amount);

    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const SizedBox(height: 12),
              const Text('Predictive Analytics', style: TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.bold)),
              const Text('Velocity modeling & cash-flow shortage prediction', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 13)),
              const SizedBox(height: 18),
              if (forecast != null) ...[
                Container(
                  padding: const EdgeInsets.all(18),
                  decoration: BoxDecoration(
                    color: const Color(0xFF1E293B),
                    borderRadius: BorderRadius.circular(20),
                    border: Border.all(
                      color: forecast.isShortagePredicted ? const Color(0xFFF59E0B) : const Color(0xFF10B981),
                    ),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Icon(
                            forecast.isShortagePredicted ? Icons.warning : Icons.check_circle,
                            color: forecast.isShortagePredicted ? const Color(0xFFF59E0B) : const Color(0xFF34D399),
                            size: 20,
                          ),
                          const SizedBox(width: 8),
                          Text(
                            forecast.isShortagePredicted ? 'Shortage Predicted!' : 'Healthy Burn Velocity',
                            style: TextStyle(
                              color: forecast.isShortagePredicted ? const Color(0xFFF59E0B) : const Color(0xFF34D399),
                              fontSize: 15,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 12),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              const Text('Current Burn Rate', style: TextStyle(color: Color(0xFF64748B), fontSize: 11)),
                              Text('₹${forecast.currentDailyBurnRate.toStringAsFixed(0)} / day', style: const TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
                            ],
                          ),
                          Column(
                            crossAxisAlignment: CrossAxisAlignment.end,
                            children: [
                              const Text('Recommended Pace', style: TextStyle(color: Color(0xFF64748B), fontSize: 11)),
                              Text('₹${forecast.recommendedDailySpend.toStringAsFixed(0)} / day', style: const TextStyle(color: Color(0xFF10B981), fontSize: 16, fontWeight: FontWeight.bold)),
                            ],
                          ),
                        ],
                      ),
                      const SizedBox(height: 10),
                      Text(
                        forecast.isShortagePredicted
                            ? 'Funds run dry in ${forecast.projectedDaysRemaining} days (Day ${forecast.estimatedShortageDay}), leaving a ₹${forecast.projectedDeficitAmount.toStringAsFixed(0)} shortage.'
                            : 'Spending is sustainable across remaining ${forecast.targetDaysRemaining} days without shortage.',
                        style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12),
                      ),
                    ],
                  ),
                ),
              ] else ...[
                Container(
                  padding: const EdgeInsets.all(18),
                  decoration: BoxDecoration(
                    color: const Color(0xFF1E293B),
                    borderRadius: BorderRadius.circular(20),
                    border: Border.all(color: const Color(0xFF334155)),
                  ),
                  child: const Row(
                    children: [
                      Icon(Icons.info_outline, color: Color(0xFF38BDF8), size: 24),
                      SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text('No Active Allowance Pool', style: TextStyle(color: Colors.white, fontSize: 14, fontWeight: FontWeight.w600)),
                            SizedBox(height: 2),
                            Text('Set your allowance pool from the Home screen to track velocity modeling and cash-flow shortage forecasts.', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ],
              const SizedBox(height: 20),
              const Text('Category Breakdown', style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.w600)),
              const SizedBox(height: 10),
              Expanded(
                child: ListView(
                  children: store.categories.where((c) => c.type == TransactionType.expense).map((cat) {
                    final sum = expenses.where((t) => t.categoryId == cat.id).fold(0.0, (s, t) => s + t.amount);
                    final pct = totalExpense > 0 ? (sum / totalExpense) : 0.0;
                    return Container(
                      margin: const EdgeInsets.only(bottom: 10),
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: const Color(0xFF1E293B),
                        borderRadius: BorderRadius.circular(14),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text(cat.name, style: const TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.w500)),
                              Text('₹${sum.toStringAsFixed(0)} (${(pct * 100).toInt()}%)', style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
                            ],
                          ),
                          const SizedBox(height: 6),
                          ClipRRect(
                            borderRadius: BorderRadius.circular(4),
                            child: LinearProgressIndicator(
                              value: pct,
                              minHeight: 6,
                              backgroundColor: const Color(0xFF334155),
                              valueColor: const AlwaysStoppedAnimation<Color>(Color(0xFF3B82F6)),
                            ),
                          ),
                        ],
                      ),
                    );
                  }).toList(),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
