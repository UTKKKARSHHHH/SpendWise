# SpendWise

"Save For Your Future Plans !!"  
Designed and maintained by Utkarsh Raj.

SpendWise is a personal finance and expense tracking Android app built entirely with Jetpack Compose and Kotlin. It focuses on cycle-based budget forecasting, multi-wallet ledger tracking, voice logging, and quick credit bookkeeping (Khata) with minimal manual overhead.

---

## Key Features

- **Predictive Allowance & Burn Rate**: Calculates your daily safe-to-spend allowance and cash velocity based on your active cycle balance.
- **Multi-Wallet Ledgers**: Set up and manage balances across physical cash, bank accounts, and digital wallets/UPI without hardcoded defaults.
- **Credit Book (Khata)**: Keep track of money lent or borrowed, settle dues, and send reminders directly through WhatsApp or SMS.
- **Voice-Activated Logging**: Speak an expense naturally (e.g. "200 for groceries") and have it parsed into an entry automatically.
- **Receipt Scanner (OCR)**: Snap photos of physical bills to extract amounts and log them on the spot.
- **Shortage Alerts**: Keeps notifications silent and unobtrusive, alerting only when remaining pool drops to 25% or lower.
- **Light & Dark Theme**: Built-in support for both light and dark mode with high-contrast palette.
- **Cloud Backup & Session Management**: Save snapshots of local data and restore them when needed.

---

## Tech Stack

- **Framework**: Android SDK (minSdk 26, targetSdk 34)
- **UI**: Jetpack Compose with Material 3
- **Language**: Kotlin
- **Architecture**: MVVM with Kotlin Coroutines and StateFlow
- **Database**: Room (SQLite)
- **OCR**: Google ML Kit Text Recognition
- **Audio**: Android SpeechRecognizer
- **Build Tool**: Gradle (Kotlin DSL)

---

## Setup and Installation

### Requirements
- Android Studio (Ladybug, Iguana, or later)
- JDK 17
- Android device or emulator running Android 8.0 (API 26) or higher

### Building the Project
Clone the repository:
```bash
git clone https://github.com/UTKKKARSHHHH/SpendWise.git
cd SpendWise
```

Build the debug APK:
```bash
./gradlew assembleDebug
```
The output file is generated at `app/build/outputs/apk/debug/app-debug.apk`.

You can also download prebuilt APK binaries directly from the Releases tab on GitHub.

---

## Author

**Utkarsh Raj**  
Designer and Maintainer
