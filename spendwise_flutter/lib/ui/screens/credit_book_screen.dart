import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';
import '../../models/models.dart';
import '../../state/spendwise_store.dart';

class CreditBookScreen extends StatefulWidget {
  final SpendwiseStore store;

  const CreditBookScreen({Key? key, required this.store}) : super(key: key);

  @override
  State<CreditBookScreen> createState() => _CreditBookScreenState();
}

class _CreditBookScreenState extends State<CreditBookScreen> {
  void _sendWhatsAppReminder(DebtItem item) async {
    final cleanPhone = item.phoneNumber.replaceAll(RegExp(r'[^0-9+]'), '');
    final msg = Uri.encodeComponent(
      'Hi ${item.contactName}, gentle reminder regarding the pending amount of ₹${item.amount.toStringAsFixed(2)} (${item.note}) via Spendwise. Thank you!',
    );
    final url = Uri.parse('https://api.whatsapp.com/send?phone=$cleanPhone&text=$msg');
    if (await canLaunchUrl(url)) {
      await launchUrl(url, mode: LaunchMode.externalApplication);
    }
  }

  void _showAddDialog() {
    final nameCtrl = TextEditingController();
    final phoneCtrl = TextEditingController();
    final amountCtrl = TextEditingController();
    final noteCtrl = TextEditingController();

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: const Color(0xFF1E293B),
        title: const Text('Add Credit / Ledger Entry', style: TextStyle(color: Colors.white)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: nameCtrl,
              style: const TextStyle(color: Colors.white),
              decoration: const InputDecoration(labelText: 'Name', labelStyle: TextStyle(color: Color(0xFF94A3B8))),
            ),
            TextField(
              controller: phoneCtrl,
              style: const TextStyle(color: Colors.white),
              decoration: const InputDecoration(labelText: 'Phone', labelStyle: TextStyle(color: Color(0xFF94A3B8))),
            ),
            TextField(
              controller: amountCtrl,
              keyboardType: TextInputType.number,
              style: const TextStyle(color: Colors.white),
              decoration: const InputDecoration(labelText: 'Amount (₹)', labelStyle: TextStyle(color: Color(0xFF94A3B8))),
            ),
            TextField(
              controller: noteCtrl,
              style: const TextStyle(color: Colors.white),
              decoration: const InputDecoration(labelText: 'Reason', labelStyle: TextStyle(color: Color(0xFF94A3B8))),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel', style: TextStyle(color: Color(0xFF94A3B8))),
          ),
          ElevatedButton(
            onPressed: () {
              final amt = double.tryParse(amountCtrl.text) ?? 0.0;
              if (nameCtrl.text.isNotEmpty && amt > 0) {
                widget.store.addDebt(
                  name: nameCtrl.text,
                  phone: phoneCtrl.text,
                  amount: amt,
                  direction: DebtDirection.youAreOwed,
                  note: noteCtrl.text,
                );
              }
              Navigator.pop(ctx);
            },
            style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF10B981)),
            child: const Text('Add', style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final owed = widget.store.debts.where((d) => d.direction == DebtDirection.youAreOwed && !d.isSettled).toList();
    final totalOwed = owed.fold(0.0, (s, d) => s + d.amount);

    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      floatingActionButton: FloatingActionButton(
        onPressed: _showAddDialog,
        backgroundColor: const Color(0xFF10B981),
        child: const Icon(Icons.add, color: Colors.white),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const SizedBox(height: 12),
              const Text('Credit & Khata Book', style: TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.bold)),
              const Text('Automated payment reminders via WhatsApp & SMS', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 13)),
              const SizedBox(height: 18),
              Container(
                padding: const EdgeInsets.all(18),
                decoration: BoxDecoration(
                  color: const Color(0xFF1E293B),
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: const Color(0xFF334155)),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('Total You Are Owed', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
                        Text('₹${totalOwed.toStringAsFixed(2)}', style: const TextStyle(color: Color(0xFF34D399), fontSize: 24, fontWeight: FontWeight.bold)),
                      ],
                    ),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                      decoration: BoxDecoration(
                        color: const Color(0xFF10B981).withOpacity(0.2),
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: Text('${owed.length} Pending', style: const TextStyle(color: Color(0xFF34D399), fontSize: 12, fontWeight: FontWeight.w600)),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 18),
              Expanded(
                child: widget.store.debts.isEmpty
                    ? const Center(child: Text('No credit records logged.', style: TextStyle(color: Color(0xFF64748B))))
                    : ListView.builder(
                        itemCount: widget.store.debts.length,
                        itemBuilder: (context, idx) {
                          final item = widget.store.debts[idx];
                          return Container(
                            margin: const EdgeInsets.only(bottom: 10),
                            padding: const EdgeInsets.all(14),
                            decoration: BoxDecoration(
                              color: const Color(0xFF1E293B),
                              borderRadius: BorderRadius.circular(16),
                              border: Border.all(color: const Color(0xFF334155)),
                            ),
                            child: Column(
                              children: [
                                Row(
                                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                  children: [
                                    Column(
                                      crossAxisAlignment: CrossAxisAlignment.start,
                                      children: [
                                        Text(item.contactName, style: const TextStyle(color: Colors.white, fontSize: 15, fontWeight: FontWeight.w600)),
                                        Text(item.phoneNumber, style: const TextStyle(color: Color(0xFF64748B), fontSize: 12)),
                                        if (item.note.isNotEmpty) Text(item.note, style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
                                      ],
                                    ),
                                    Column(
                                      crossAxisAlignment: CrossAxisAlignment.end,
                                      children: [
                                        Text('₹${item.amount.toStringAsFixed(2)}', style: const TextStyle(color: Color(0xFF34D399), fontSize: 16, fontWeight: FontWeight.bold)),
                                        Text(item.isSettled ? 'Settled' : 'Pending', style: TextStyle(color: item.isSettled ? const Color(0xFF64748B) : const Color(0xFF34D399), fontSize: 11)),
                                      ],
                                    ),
                                  ],
                                ),
                                if (!item.isSettled) ...[
                                  const SizedBox(height: 10),
                                  Row(
                                    mainAxisAlignment: MainAxisAlignment.end,
                                    children: [
                                      TextButton(
                                        onPressed: () => widget.store.settleDebt(item),
                                        child: const Text('Settle', style: TextStyle(color: Color(0xFF10B981))),
                                      ),
                                      const SizedBox(width: 8),
                                      ElevatedButton.icon(
                                        onPressed: () => _sendWhatsAppReminder(item),
                                        icon: const Icon(Icons.send, size: 14, color: Colors.white),
                                        label: const Text('WhatsApp Reminder', style: TextStyle(fontSize: 11, color: Colors.white)),
                                        style: ElevatedButton.styleFrom(
                                          backgroundColor: const Color(0xFF25D366),
                                          shape: RoundedCornerShape(10),
                                        ),
                                      ),
                                    ],
                                  ),
                                ],
                              ],
                            ),
                          );
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
