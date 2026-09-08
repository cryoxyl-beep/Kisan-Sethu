# 🌾 Kissaan Sync (Kisan-Sethu)

> A modern, digital public-service Android application designed to provide farmers with a calm, trustworthy, and minimal interface for procurement slot bookings, live queue tracking, and direct payment tracking.

Built natively for Android using **Kotlin**, **Jetpack Compose**, and **Firebase**.

---

## 📖 About This Project

Kissaan Sync bridges the gap between complex digital infrastructure and the agricultural sector. It offers farmers a seamless, government-service-oriented platform that is easy to understand and use. 

**Design Philosophy:** We intentionally stepped away from traditional "agricultural clichés" (like cluttered green backgrounds and tractor vectors). Instead, the app focuses on a hyper-clean, minimal, and modern digital identity that instills trust and clarity. 

**Recent Enhancements:** The application features premium UX details, such as an iOS-inspired **"Liquid Glass" split-pill navigation bar** that utilizes real-time frosted glass rendering, and a robust **Persistent Session** system that remembers your login state so you can get straight to your dashboard.

---

## ✨ Features In Detail

### 🧭 Liquid Glass Navigation
* **True Background Blur:** Built using the `Haze` library, the navigation bar leverages Android's `RenderNode` APIs to dynamically blur the scrolling content behind it in real-time.
* **Dual-Theme Adaptive:** The frosted glass intelligently adapts. In Light Mode, it renders a bright, airy frost. In Dark Mode, it shifts to a sleek, dim translucent glass. 
* **Swipe-to-Navigate:** Simply swipe left or right across the bottom bar to smoothly transition between tabs.
* **Minimalist UI:** Icon-only interface with a subtle, gray pill-shaped highlight for the active state.

### 🔐 Smart Authentication & Sessions
* **Persistent Login:** Log in once and stay logged in. The app securely persists session tokens using Android `DataStore`, bypassing the login screen on subsequent launches.
* **Flexible Sign-In:** Authenticate using a unique 6-character **Farmer ID** or a 10-digit **Mobile Number**, combined with a secure 6-digit PIN.
* **Secure Registration:** A seamless 4-step onboarding process safely stores farmer credentials and demographic data via Firebase Realtime Database.

### 🌍 Accessibility & Dashboard
* **Multi-Language Support:** Remembers localized language preferences (English, Hindi, Telugu, etc.) securely, applying them instantly across the app.
* **Dynamic Dashboard:** A context-aware UI that conditionally displays live queues and active token status only when a procurement slot is actively booked.

---

## 📥 Download & Install (Published Package)

You can download and install the latest compiled APK package directly from this repository:

**👉 [Download KisanSethu-debug.apk](release/KisanSethu-debug.apk)**

*Note: You may need to enable "Install from Unknown Sources" on your Android device to install the APK.*

---

## 🛠️ Tech Stack & Architecture

* **UI Toolkit:** Jetpack Compose (Material Design 3)
* **Language:** Kotlin
* **Visual Effects:** `dev.chrisbanes.haze` (for real-time composable blur)
* **Backend / DB:** Firebase Realtime Database
* **Local Storage:** Jetpack DataStore (Preferences)
* **Architecture:** MVVM (Model-View-ViewModel) & Compose Navigation Component
### ?? Procurement Slot Bookings (Phase 2.2)
* **Smart Booking Flow:** A seamless, 5-step wizard to book procurement slots (Date -> Centre -> Time -> Produce -> Review).
* **Firebase Firestore Integration:** All bookings are securely persisted to Firestore with a highly optimized data structure.
* **Auto-generated Tracking IDs:** Creates clean, unique, non-sequential tracking codes (e.g., KS26XXXXXX) that protect personal demographic data.
* **Integrated QR Generation:** Generates a robust QR code encoding the tracking ID natively on the device using ZXing, ready to be scanned at the procurement centre.
* **Intelligent Unit Normalization:** Automatically handles agricultural conversions (e.g., Quintal to KG) on the fly.
