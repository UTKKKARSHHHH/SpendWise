import 'package:flutter/material.dart';
import 'state/spendwise_store.dart';
import 'ui/screens/dashboard_screen.dart';
import 'ui/screens/wallets_screen.dart';
import 'ui/screens/analytics_screen.dart';
import 'ui/screens/credit_book_screen.dart';
import 'ui/screens/voice_screen.dart';
import 'ui/screens/ocr_screen.dart';

void main() {
  runApp(const SpendwiseApp());
}

class SpendwiseApp extends StatefulWidget {
  const SpendwiseApp({Key? key}) : super(key: key);

  @override
  State<SpendwiseApp> createState() => _SpendwiseAppState();
}

class _SpendwiseAppState extends State<SpendwiseApp> {
  final SpendwiseStore _store = SpendwiseStore();
  int _currentIndex = 0;

  @override
  void initState() {
    super.initState();
    _store.addListener(() {
      setState(() {});
    });
  }

  @override
  Widget build(BuildContext context) {
    final screens = [
      DashboardScreen(
        store: _store,
        onNavigateVoice: () => Navigator.push(context, MaterialPageRoute(builder: (_) => VoiceInputScreen(store: _store))),
        onNavigateOcr: () => Navigator.push(context, MaterialPageRoute(builder: (_) => OcrScannerScreen(store: _store))),
      ),
      WalletsScreen(store: _store),
      AnalyticsScreen(store: _store),
      CreditBookScreen(store: _store),
    ];

    return MaterialApp(
      title: 'Spendwise',
      debugShowCheckedModeBanner: false,
      theme: ThemeData.dark().copyWith(
        scaffoldBackgroundColor: const Color(0xFF0F172A),
        colorScheme: const ColorScheme.dark(
          primary: Color(0xFF10B981),
          secondary: Color(0xFF38BDF8),
          surface: Color(0xFF1E293B),
        ),
      ),
      home: Builder(
        builder: (ctx) => Scaffold(
          body: screens[_currentIndex],
          bottomNavigationBar: NavigationBar(
            selectedIndex: _currentIndex,
            onDestinationSelected: (idx) => setState(() => _currentIndex = idx),
            backgroundColor: const Color(0xFF1E293B),
            indicatorColor: const Color(0xFF0F172A),
            destinations: const [
              NavigationDestination(
                icon: Icon(Icons.dashboard_outlined, color: Color(0xFF94A3B8)),
                selectedIcon: Icon(Icons.dashboard, color: Color(0xFF10B981)),
                label: 'Home',
              ),
              NavigationDestination(
                icon: Icon(Icons.account_balance_wallet_outlined, color: Color(0xFF94A3B8)),
                selectedIcon: Icon(Icons.account_balance_wallet, color: Color(0xFF10B981)),
                label: 'Wallets',
              ),
              NavigationDestination(
                icon: Icon(Icons.analytics_outlined, color: Color(0xFF94A3B8)),
                selectedIcon: Icon(Icons.analytics, color: Color(0xFF10B981)),
                label: 'Analytics',
              ),
              NavigationDestination(
                icon: Icon(Icons.menu_book_outlined, color: Color(0xFF94A3B8)),
                selectedIcon: Icon(Icons.menu_book, color: Color(0xFF10B981)),
                label: 'Credit Book',
              ),
            ],
          ),
        ),
      ),
    );
  }
}
