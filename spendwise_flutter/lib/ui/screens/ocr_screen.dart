import 'package:flutter/material.dart';
import '../../models/models.dart';
import '../../state/spendwise_store.dart';

class OcrScannerScreen extends StatefulWidget {
  final SpendwiseStore store;

  const OcrScannerScreen({Key? key, required this.store}) : super(key: key);

  @override
  State<OcrScannerScreen> createState() => _OcrScannerScreenState();
}

class _OcrScannerScreenState extends State<OcrScannerScreen> {
  TransactionDraft? _detectedDraft;
  bool _isScanning = false;
  final TextEditingController _merchantCtrl = TextEditingController();
  final TextEditingController _amountCtrl = TextEditingController();

  void _triggerScan() {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: const Color(0xFF1E293B),
        title: const Text('Enter Bill / Receipt Details', style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: _merchantCtrl,
              style: const TextStyle(color: Colors.white),
              decoration: const InputDecoration(
                labelText: 'Merchant / Store Name',
                labelStyle: TextStyle(color: Color(0xFF94A3B8)),
                enabledBorder: UnderlineInputBorder(borderSide: BorderSide(color: Color(0xFF334155))),
                focusedBorder: UnderlineInputBorder(borderSide: BorderSide(color: Color(0xFF38BDF8))),
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _amountCtrl,
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
              style: const TextStyle(color: Colors.white),
              decoration: const InputDecoration(
                labelText: 'Bill Total Amount (₹)',
                labelStyle: TextStyle(color: Color(0xFF94A3B8)),
                enabledBorder: UnderlineInputBorder(borderSide: BorderSide(color: Color(0xFF334155))),
                focusedBorder: UnderlineInputBorder(borderSide: BorderSide(color: Color(0xFF38BDF8))),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel', style: TextStyle(color: Color(0xFF94A3B8))),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF38BDF8)),
            onPressed: () {
              final amt = double.tryParse(_amountCtrl.text) ?? 0.0;
              final name = _merchantCtrl.text.trim();
              if (amt > 0) {
                setState(() {
                  _detectedDraft = TransactionDraft(
                    amount: amt,
                    type: TransactionType.expense,
                    categoryName: 'Food & Dining',
                    walletName: widget.store.wallets.isNotEmpty ? widget.store.wallets.first.name : 'Physical Cash',
                    note: name.isNotEmpty ? 'Receipt: $name' : 'Scanned Receipt',
                    confidenceScore: 0.98,
                    source: 'OCR',
                  );
                });
              }
              Navigator.pop(ctx);
            },
            child: const Text('Extract', style: TextStyle(color: Colors.black, fontWeight: FontWeight.bold)),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        title: const Text('AI Smart Bill Scanner'),
        backgroundColor: const Color(0xFF0F172A),
        foregroundColor: Colors.white,
        elevation: 0,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            GestureDetector(
              onTap: _triggerScan,
              child: Container(
                height: 200,
                width: double.infinity,
                decoration: BoxDecoration(
                  color: const Color(0xFF1E293B),
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: const Color(0xFF38BDF8), width: 2),
                ),
                child: const Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(Icons.camera_alt, color: Color(0xFF38BDF8), size: 48),
                    SizedBox(height: 10),
                    Text('Tap to scan receipt with camera', style: TextStyle(color: Colors.white, fontSize: 14, fontWeight: FontWeight.w600)),
                    SizedBox(height: 4),
                    Text('ML Kit OCR extracts Total & Merchant from receipt', style: TextStyle(color: Color(0xFF64748B), fontSize: 11)),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 20),
            if (_detectedDraft != null) ...[
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: const Color(0xFF1E293B),
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: const Color(0xFF334155)),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text('Extracted Key Figures', style: TextStyle(color: Color(0xFF34D399), fontSize: 12, fontWeight: FontWeight.w600)),
                        Text('Confidence: ${(_detectedDraft!.confidenceScore * 100).toInt()}%', style: const TextStyle(color: Color(0xFF64748B), fontSize: 11)),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(_detectedDraft!.note, style: const TextStyle(color: Colors.white, fontSize: 15, fontWeight: FontWeight.w500)),
                            Text('Category: ${_detectedDraft!.categoryName}', style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
                          ],
                        ),
                        Text(
                          '₹${_detectedDraft!.amount.toStringAsFixed(2)}',
                          style: const TextStyle(color: Color(0xFF10B981), fontSize: 22, fontWeight: FontWeight.bold),
                        ),
                      ],
                    ),
                    const SizedBox(height: 14),
                    SizedBox(
                      width: double.infinity,
                      child: ElevatedButton(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: const Color(0xFF10B981),
                          padding: const EdgeInsets.symmetric(vertical: 12),
                          shape: RoundedCornerShape(12),
                        ),
                        onPressed: () {
                          widget.store.commitDraft(_detectedDraft!);
                          Navigator.pop(context);
                        },
                        child: const Text('Confirm & Log to Active Pool', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
