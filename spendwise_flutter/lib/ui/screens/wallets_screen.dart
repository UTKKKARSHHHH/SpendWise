import 'package:flutter/material.dart';
import '../../models/models.dart';
import '../../state/spendwise_store.dart';

class WalletsScreen extends StatefulWidget {
  final SpendwiseStore store;

  const WalletsScreen({Key? key, required this.store}) : super(key: key);

  @override
  State<WalletsScreen> createState() => _WalletsScreenState();
}

class _WalletsScreenState extends State<WalletsScreen> {
  void _openEditDialog(Wallet wallet) {
    final nameController = TextEditingController(text: wallet.name);
    final balanceController = TextEditingController(text: wallet.currentBalance.toStringAsFixed(2));

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: const Color(0xFF1E293B),
        title: const Text('Edit Wallet', style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: nameController,
              style: const TextStyle(color: Colors.white),
              decoration: const InputDecoration(
                labelText: 'Wallet Name',
                labelStyle: TextStyle(color: Color(0xFF94A3B8)),
                enabledBorder: UnderlineInputBorder(borderSide: BorderSide(color: Color(0xFF334155))),
                focusedBorder: UnderlineInputBorder(borderSide: BorderSide(color: Color(0xFF10B981))),
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: balanceController,
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
              style: const TextStyle(color: Colors.white),
              decoration: const InputDecoration(
                labelText: 'Wallet Balance (₹)',
                labelStyle: TextStyle(color: Color(0xFF94A3B8)),
                enabledBorder: UnderlineInputBorder(borderSide: BorderSide(color: Color(0xFF334155))),
                focusedBorder: UnderlineInputBorder(borderSide: BorderSide(color: Color(0xFF10B981))),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () {
              widget.store.deleteWallet(wallet.id);
              Navigator.pop(ctx);
              setState(() {});
            },
            child: const Text('Delete', style: TextStyle(color: Color(0xFFF43F5E))),
          ),
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel', style: TextStyle(color: Color(0xFF94A3B8))),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF10B981)),
            onPressed: () {
              final newBal = double.tryParse(balanceController.text) ?? wallet.currentBalance;
              final newName = nameController.text.trim();
              if (newName.isNotEmpty) {
                widget.store.updateWallet(wallet.id, newName, newBal);
              }
              Navigator.pop(ctx);
              setState(() {});
            },
            child: const Text('Save', style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }

  void _openAddWalletDialog() {
    final nameController = TextEditingController();
    final balanceController = TextEditingController(text: '0');
    WalletType selectedType = WalletType.bankAccount;

    showDialog(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          backgroundColor: const Color(0xFF1E293B),
          title: const Text('Add New Wallet', style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold)),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              TextField(
                controller: nameController,
                style: const TextStyle(color: Colors.white),
                decoration: const InputDecoration(
                  labelText: 'Wallet Name',
                  labelStyle: TextStyle(color: Color(0xFF94A3B8)),
                  enabledBorder: UnderlineInputBorder(borderSide: BorderSide(color: Color(0xFF334155))),
                  focusedBorder: UnderlineInputBorder(borderSide: BorderSide(color: Color(0xFF10B981))),
                ),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: balanceController,
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                style: const TextStyle(color: Colors.white),
                decoration: const InputDecoration(
                  labelText: 'Initial Balance (₹)',
                  labelStyle: TextStyle(color: Color(0xFF94A3B8)),
                  enabledBorder: UnderlineInputBorder(borderSide: BorderSide(color: Color(0xFF334155))),
                  focusedBorder: UnderlineInputBorder(borderSide: BorderSide(color: Color(0xFF10B981))),
                ),
              ),
              const SizedBox(height: 16),
              const Text('Wallet Type', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
              const SizedBox(height: 8),
              Row(
                children: [
                  _typeChip('Cash', WalletType.physicalCash, selectedType, () {
                    setDialogState(() => selectedType = WalletType.physicalCash);
                  }),
                  const SizedBox(width: 6),
                  _typeChip('Bank', WalletType.bankAccount, selectedType, () {
                    setDialogState(() => selectedType = WalletType.bankAccount);
                  }),
                  const SizedBox(width: 6),
                  _typeChip('UPI', WalletType.digitalWallet, selectedType, () {
                    setDialogState(() => selectedType = WalletType.digitalWallet);
                  }),
                ],
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(ctx),
              child: const Text('Cancel', style: TextStyle(color: Color(0xFF94A3B8))),
            ),
            ElevatedButton(
              style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF10B981)),
              onPressed: () {
                final newBal = double.tryParse(balanceController.text) ?? 0.0;
                final newName = nameController.text.trim();
                if (newName.isNotEmpty) {
                  widget.store.addCustomWallet(newName, selectedType, newBal);
                }
                Navigator.pop(ctx);
                setState(() {});
              },
              child: const Text('Create Wallet', style: TextStyle(color: Colors.white)),
            ),
          ],
        ),
      ),
    );
  }

  Widget _typeChip(String label, WalletType type, WalletType selected, VoidCallback onTap) {
    final isSelected = type == selected;
    return Expanded(
      child: GestureDetector(
        onTap: onTap,
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 8),
          decoration: BoxDecoration(
            color: isSelected ? const Color(0xFF10B981) : const Color(0xFF334155),
            borderRadius: BorderRadius.circular(8),
          ),
          alignment: Alignment.Center,
          child: Text(
            label,
            style: TextStyle(
              color: Colors.white,
              fontSize: 12,
              fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
            ),
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      floatingActionButton: FloatingActionButton(
        backgroundColor: const Color(0xFF10B981),
        onPressed: _openAddWalletDialog,
        child: const Icon(Icons.add, color: Colors.white),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const SizedBox(height: 12),
              const Text('Multi-Wallet Ledgers', style: TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.bold)),
              const Text('Tap any wallet to view or manually set its balance', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 13)),
              const SizedBox(height: 20),
              Expanded(
                child: ListView.builder(
                  itemCount: widget.store.wallets.length,
                  itemBuilder: (context, idx) {
                    final wallet = widget.store.wallets[idx];
                    final txCount = widget.store.transactions.where((t) => t.walletId == wallet.id).length;
                    IconData icon = Icons.account_balance;
                    if (wallet.type == WalletType.physicalCash) icon = Icons.money;
                    if (wallet.type == WalletType.digitalWallet) icon = Icons.qr_code;

                    return GestureDetector(
                      onTap: () => _openEditDialog(wallet),
                      child: Container(
                        margin: const EdgeInsets.only(bottom: 12),
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: const Color(0xFF1E293B),
                          borderRadius: BorderRadius.circular(18),
                          border: Border.all(color: const Color(0xFF334155)),
                        ),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Row(
                              children: [
                                CircleAvatar(
                                  radius: 22,
                                  backgroundColor: const Color(0xFF10B981).withOpacity(0.15),
                                  child: Icon(icon, color: const Color(0xFF10B981), size: 22),
                                ),
                                const SizedBox(width: 12),
                                Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(wallet.name, style: const TextStyle(color: Colors.white, fontSize: 15, fontWeight: FontWeight.w600)),
                                    const SizedBox(height: 2),
                                    Text('$txCount entries recorded', style: const TextStyle(color: Color(0xFF64748B), fontSize: 12)),
                                  ],
                                ),
                              ],
                            ),
                            Column(
                              crossAxisAlignment: CrossAxisAlignment.end,
                              children: [
                                Text(
                                  '₹${wallet.currentBalance.toStringAsFixed(2)}',
                                  style: const TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold),
                                ),
                                // NOTE: "Available" is REMOVED completely as requested
                              ],
                            ),
                          ],
                        ),
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
