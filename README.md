# SpendWise 💸

> **"Save For Your Future Plans !!"**  
> *Designed and Maintained by Utkarsh Raj*

SpendWise is a next-generation Android financial tracking application built with **Jetpack Compose**, **Kotlin Coroutines & Flow**, **Room Database**, and native AI-powered tools. It provides velocity modeling, predictive burn-rate analytics, multi-wallet ledger management, credit books (Khata), voice-activated logging, and OCR receipt scanning.

---

## ✨ Features

- 🎯 **Predictive Analytics & Burn-Rate Forecasting**: Real-time cash velocity calculation, recommended daily spending pace, and remaining cycle runway.
- 💼 **Multi-Wallet Ledgers**: Manage cash, bank accounts, UPI, and digital wallets without forced presets.
- 📖 **Credit Book (Khata Management)**: Track lent and borrowed funds with single-tap WhatsApp / SMS reminders and balance settlements.
- 🎙️ **Voice-Activated Transaction Logging**: Speech-to-Text NLP parsing for hands-free expense entry.
- 📸 **Receipt OCR Scanner**: Camera-based receipt scanner that detects amounts, dates, and categories.
- 🔔 **Smart Shortage Alerts**: Silent & distraction-free notifications that only alert when remaining allowance drops below 25%.
- 🌓 **Dynamic Theme Engine**: Velto-inspired Light and Dark themes with signature forest green and electric lime accents.
- ☁️ **Cloud Sync & User Management**: User profile, cloud snapshot backups, and restore workflows.

---

## 🛠️ Tech Stack & Architecture

- **UI**: 100% Jetpack Compose (Material3)
- **Language**: Kotlin 1.9+
- **Architecture**: Clean Architecture + MVVM + Unidirectional Data Flow (StateFlow)
- **Local Storage**: Room Persistence Library (SQLite)
- **Camera & OCR**: CameraX + Google ML Kit Text Recognition
- **Audio / Voice**: Android SpeechRecognizer API
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`)

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug / Iguana or newer
- JDK 17
- Android SDK 34 (Android 14) minimum SDK 26 (Android 8.0)

### Build APK
```bash
./gradlew assembleDebug
```
The compiled APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 👨‍💻 Author

**Utkarsh Raj**  
*Designer & Maintainer*
