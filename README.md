# 🌾 Kissaan Sync (Kisan-Sethu)

A modern, digital public-service Android application designed to provide farmers with a calm, trustworthy, and minimal interface for procurement slot bookings, live queue tracking, and direct payment tracking. 

Built with **Kotlin**, **Jetpack Compose**, and **Firebase**.

---

## 📖 About

Kissaan Sync bridges the gap between digital infrastructure and the agricultural sector, offering farmers a seamless, government-service-oriented platform. We intentionally stepped away from traditional "agricultural clichés" to focus on a hyper-clean, minimal, and modern digital identity.

**Recent Update:** The app now features a highly custom **"Liquid Glass" split-pill bottom navigation bar**. Inspired by premium OS aesthetics, it uses the `Haze` library to achieve real-time frosted glass background blur natively in Jetpack Compose. The nav bar includes interactive swipe gestures and animated "pop-out" icons for a highly engaging, fluid user experience.

---

## ✨ Current Features

* **Liquid Glass Navigation:** Floating, split-pill navigation bar with true background blur, swipe gesture support, and springing pop-out icons.
* **Modern Digital Identity:** Clean, minimal UI utilizing Material 3 components and full-screen interactive overlays.
* **Multi-Language Support:** Remembers localized language preferences securely.
* **Robust Registration:** 4-step secure onboarding storing Farmer details safely via Firebase Realtime Database.
* **Smart Authentication:** Log in seamlessly via a unique 6-character Farmer ID or 10-digit mobile number + secure 6-digit PIN.
* **Dynamic Dashboard:** Conditional UI displaying live queues and active token status only when a procurement slot is booked.

## 📥 Download

You can download the latest compiled APK directly from the **[release folder](release/KisanSethu-debug.apk)** in this repository.

## 🛠️ Tech Stack

* **UI Toolkit:** Jetpack Compose (Material 3)
* **Visual Effects:** dev.chrisbanes.haze (Real-time blur rendering)
* **Language:** Kotlin
* **Backend:** Firebase Realtime Database
* **Architecture:** MVVM, Navigation Component
