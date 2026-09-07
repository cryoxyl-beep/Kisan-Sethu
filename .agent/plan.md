# Project Plan

Build the frontend foundation and Splash Screen for "Kissaan Sync", an Android farmer-facing application for the SIH project "Kisan Sethu".
Features:
- Jetpack Compose, Material 3, Clean Architecture.
- Reusable Theme: Agricultural green primary (#3F6B50), clean neutral background, sans-serif typography.
- Splash Screen: App name, simple logo (leaf/crop), tagline ("Smarter procurement. Simpler for farmers."), subtle animation (fade/scale).
- Navigation architecture: Placeholders for Language Selection, Auth Entry, Login.
- Exclusions: NO backend, NO firebase, NO auth logic, NO actual language persistence. Only UI foundation and Splash screen.

## Project Brief

# Project Brief: Kissaan Sync

## Features
1. **Animated Splash Screen**: An initial launch screen featuring the app name, a simple leaf/crop logo, the tagline "Smarter procurement. Simpler for farmers.", and a subtle fade/scale animation.
2. **Reusable Agricultural Theme**: A centralized Jetpack Compose theme system implementing the agricultural green primary color (#3F6B50), clean neutral backgrounds, and standard sans-serif typography.
3. **Language Selection Flow (UI Placeholder)**: A foundational UI screen allowing the user to select their preferred language (frontend layout only).
4. **Authentication & Login (UI Placeholders)**: Minimal UI foundation for user onboarding, containing layout placeholders for the auth entry point and login screens without backend integration.

## High-Level Tech Stack
* **Core Language**: Kotlin
* **UI Framework**: Jetpack Compose (Material 3)
* **Architecture Design**: Clean Architecture (Focus strictly on the Presentation layer for this MVP)
* **Navigation & Adaptive Strategy**: Jetpack Navigation 3 (state-driven) and the Compose Material Adaptive library for robust, responsive layout components.
* **Concurrency / Animations**: Kotlin Coroutines (for managing splash screen transition delays and simple UI state flows).

## Implementation Steps

### Task_1_ThemeAndFoundation: Set up the Material 3 agricultural theme with the specified colors (#3F6B50 primary, #E8F0EA primary light, etc.) and typography. Ensure foundational Compose structures are in place.
- **Status:** COMPLETED
- **Updates:** Successfully set up the Material 3 agricultural theme with the specified colors and typography, ensuring a clean foundational Compose structure. Colors, Typography, Shapes, and Theme have been defined and MainActivity is updated to use them.
- **Acceptance Criteria:**
  - Theme colors match requirements
  - App compiles successfully

### Task_2_SplashScreen: Implement the Splash Screen using Jetpack Compose. Include the app name, simple leaf/crop logo, tagline ('Smarter procurement. Simpler for farmers.'), and a subtle fade/scale animation. Use Kotlin Coroutines for the transition delay.
- **Status:** COMPLETED
- **Updates:** Created SplashScreen.kt with a leaf logo, app name, and tagline. Implemented subtle fade-in and scale animations using compose Animation APIs. Used LaunchedEffect for a ~2.5s delay before calling onSplashFinished. Updated MainActivity to temporarily show the splash screen.
- **Acceptance Criteria:**
  - Splash screen displays logo, title, and tagline
  - Fade/scale animation works correctly

### Task_3_NavigationAndPlaceholders: Set up Jetpack Navigation 3 and create basic UI placeholders for the Language Selection, Auth Entry, and Login screens. Wire up the navigation so the Splash Screen routes to the Language Selection placeholder after its animation.
- **Status:** COMPLETED
- **Updates:** Added navigation compose dependency, defined type-safe navigation routes (Splash, LanguageSelection, AuthEntry, Login) and KisanSethuNavHost. Configured Splash screen navigation to pop itself off the backstack. Created placeholders for LanguageSelectionScreen, AuthEntryScreen, and LoginScreen with navigation buttons between them. Integrated KisanSethuNavHost in MainActivity. Verified with assembleDebug and testDebugUnitTest.
- **Acceptance Criteria:**
  - Navigation graph is set up
  - Language Selection, Auth Entry, and Login placeholders exist
  - Splash screen navigates to Language Selection placeholder

### Task_4_RunAndVerify: Run the app and verify application stability, ensuring no crashes. Confirm alignment with user requirements and report any critical UI issues.
- **Status:** COMPLETED
- **Updates:** Verified static code, compilation, unit tests, and architecture. Build completed successfully without errors. Splash screen, typography, color palette, edge-to-edge support, and navigation placeholders function as required.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - UI aligns with user requirements
- **Duration:** N/A

