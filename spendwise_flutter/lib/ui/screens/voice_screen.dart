import 'package:flutter/material.dart';
import '../../models/models.dart';
import '../../services/parsers.dart';
import '../../state/spendwise_store.dart';

class VoiceInputScreen extends StatefulWidget {
  final SpendwiseStore store;

  const VoiceInputScreen({Key? key, required this.store}) : super(key: key);

  @override
  State<VoiceInputScreen> createState() => _VoiceInputScreenState();
}

class _VoiceInputScreenState extends State<VoiceInputScreen> {
  final TextEditingController _controller = TextEditingController();
  final VoiceParserService _parser = VoiceParserService();
  TransactionDraft? _draft;
  bool _isListening = false;

  void _reparse() {
    final text = _controller.text.trim();
    if (text.isEmpty) {
      setState(() => _draft = null);
      return;
    }
    setState(() {
      _draft = _parser.parseSpokenText(text);
    });
  }

  void _simulateVoiceRecording() async {
    setState(() => _isListening = true);
    // Visual feedback for listening
    await Future.delayed(const Duration(milliseconds: 1200));
    if (mounted) {
      setState(() {
        _isListening = false;
      });
      // Show prompt or focus input
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Microphone active. Type or speak your transaction details.'),
          duration: Duration(seconds: 2),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        title: const Text('Voice-Activated Entry'),
        backgroundColor: const Color(0xFF0F172A),
        foregroundColor: Colors.white,
        elevation: 0,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: GestureDetector(
                onTap: _simulateVoiceRecording,
                child: Container(
                  width: 90,
                  height: 90,
                  decoration: BoxDecoration(
                    color: _isListening
                        ? const Color(0xFFA855F7)
                        : const Color(0xFFA855F7).withOpacity(0.2),
                    shape: BoxShape.circle,
                    border: Border.all(color: const Color(0xFFA855F7), width: 2),
                  ),
                  child: Icon(
                    Icons.mic,
                    color: _isListening ? Colors.white : const Color(0xFFA855F7),
                    size: 44,
                  ),
                ),
              ),
            ),
            const SizedBox(height: 12),
            Center(
              child: Text(
                _isListening ? 'Listening... Speak now' : 'Tap microphone to speak or type phrase',
                style: TextStyle(
                  color: _isListening ? const Color(0xFFA855F7) : const Color(0xFF94A3B8),
                  fontSize: 13,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
            const SizedBox(height: 20),
            TextField(
              controller: _controller,
              onChanged: (_) => _reparse(),
              style: const TextStyle(color: Colors.white),
              decoration: InputDecoration(
                labelText: 'Spoken Transcript',
                labelStyle: const TextStyle(color: Color(0xFF94A3B8)),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: const BorderSide(color: Color(0xFF334155)),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: const BorderSide(color: Color(0xFFA855F7)),
                ),
              ),
            ),
            const SizedBox(height: 20),
            if (_draft != null) ...[
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: const Color(0xFF1E293B),
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: const Color(0xFF10B981)),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('NLP Parsed Draft', style: TextStyle(color: Color(0xFF34D399), fontSize: 12, fontWeight: FontWeight.w600)),
                    const SizedBox(height: 8),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text('Category: ${_draft!.categoryName}', style: const TextStyle(color: Colors.white, fontSize: 14)),
                            Text('Wallet: ${_draft!.walletName}', style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
                            Text('Type: ${_draft!.type.name.toUpperCase()}', style: const TextStyle(color: Color(0xFF64748B), fontSize: 11)),
                          ],
                        ),
                        Text(
                          '₹${_draft!.amount.toStringAsFixed(2)}',
                          style: const TextStyle(color: Color(0xFF10B981), fontSize: 22, fontWeight: FontWeight.bold),
                        ),
                      ],
                    ),
                    const SizedBox(height: 14),
                    SizedBox(
                      width: double.infinity,
                      child: ElevatedButton.icon(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: const Color(0xFF10B981),
                          padding: const EdgeInsets.symmetric(vertical: 12),
                          shape: RoundedCornerShape(12),
                        ),
                        onPressed: () {
                          widget.store.commitDraft(_draft!);
                          Navigator.pop(context);
                        },
                        icon: const Icon(Icons.send, color: Colors.white, size: 16),
                        label: const Text('Log Transaction Now', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
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
