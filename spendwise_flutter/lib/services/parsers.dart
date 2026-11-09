import '../models/models.dart';

class SmsParserService {
  final RegExp amountRegex = RegExp(
    r'(?:(?:rs|inr|inr\.)\s*([0-9,]+(?:\.[0-9]{1,2})?))|(?:([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:rs|inr))',
    caseSensitive: false,
  );

  final List<String> debitKeywords = ['debited', 'spent', 'paid', 'withdrawn', 'sent', 'deducted'];
  final List<String> creditKeywords = ['credited', 'received', 'deposited', 'added', 'refunded'];

  TransactionDraft? parse(String sender, String messageBody) {
    final lower = messageBody.toLowerCase();
    final isDebit = debitKeywords.any((k) => lower.contains(k));
    final isCredit = creditKeywords.any((k) => lower.contains(k));

    if (!isDebit && !isCredit) return null;

    final match = amountRegex.firstMatch(messageBody);
    if (match == null) return null;

    final raw = (match.group(1) ?? match.group(2))?.replaceAll(',', '');
    final amount = double.tryParse(raw ?? '') ?? 0.0;
    if (amount <= 0) return null;

    String category = 'Rent & Bills';
    if (lower.contains('swiggy') || lower.contains('zomato') || lower.contains('food')) {
      category = 'Food & Dining';
    } else if (lower.contains('uber') || lower.contains('ola') || lower.contains('fuel')) {
      category = 'Transit & Fuel';
    } else if (lower.contains('amazon') || lower.contains('mart')) {
      category = 'Groceries';
    } else if (lower.contains('allowance') || lower.contains('pocket')) {
      category = 'Allowance / Pocket Money';
    }

    String wallet = 'Bank Account';
    if (lower.contains('upi') || lower.contains('paytm') || lower.contains('gpay')) {
      wallet = 'Digital Wallet / UPI';
    } else if (lower.contains('cash') || lower.contains('atm')) {
      wallet = 'Physical Cash';
    }

    return TransactionDraft(
      amount: amount,
      type: isDebit ? TransactionType.expense : TransactionType.income,
      categoryName: category,
      walletName: wallet,
      note: 'SMS Alert ($sender)',
      confidenceScore: 0.92,
      source: 'SMS',
    );
  }
}

class VoiceParserService {
  final RegExp numberRegex = RegExp(r'([0-9]+(?:\.[0-9]{1,2})?)');

  TransactionDraft? parseSpokenText(String text) {
    final clean = text.toLowerCase().trim();
    if (clean.isEmpty) return null;

    final isIncome = clean.startsWith('got') || clean.startsWith('received') ||
        clean.contains('allowance') || clean.contains('pocket money') || clean.contains('salary');

    final match = numberRegex.firstMatch(clean);
    if (match == null) return null;
    final amount = double.tryParse(match.group(1) ?? '') ?? 0.0;
    if (amount <= 0) return null;

    String category = 'Food & Dining';
    if (clean.contains('dinner') || clean.contains('lunch') || clean.contains('food') || clean.contains('tea')) {
      category = 'Food & Dining';
    } else if (clean.contains('grocery') || clean.contains('mart') || clean.contains('milk')) {
      category = 'Groceries';
    } else if (clean.contains('rent') || clean.contains('recharge') || clean.contains('bill')) {
      category = 'Rent & Bills';
    } else if (clean.contains('cab') || clean.contains('uber') || clean.contains('petrol')) {
      category = 'Transit & Fuel';
    } else if (clean.contains('allowance') || clean.contains('pocket money')) {
      category = 'Allowance / Pocket Money';
    }

    String wallet = 'Bank Account';
    if (clean.contains('cash')) {
      wallet = 'Physical Cash';
    } else if (clean.contains('upi') || clean.contains('gpay') || clean.contains('paytm')) {
      wallet = 'Digital Wallet / UPI';
    }

    return TransactionDraft(
      amount: amount,
      type: isIncome ? TransactionType.income : TransactionType.expense,
      categoryName: category,
      walletName: wallet,
      note: text,
      confidenceScore: 0.95,
      source: 'VOICE',
    );
  }
}

class ReceiptOcrService {
  Future<TransactionDraft> simulateScanReceipt(String merchant, double amount, String category) async {
    await Future.delayed(const Duration(milliseconds: 600));
    return TransactionDraft(
      amount: amount,
      type: TransactionType.expense,
      categoryName: category,
      walletName: 'Physical Cash',
      note: 'Receipt: $merchant',
      confidenceScore: 0.94,
      source: 'OCR',
    );
  }
}
