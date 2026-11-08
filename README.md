# 🐍 Snake Evolution: Neon Survival (الحنش المتطور)

A premium offline **Cyberpunk Neon Arcade** 2D Snake game for Android built with **Kotlin**, **Jetpack Compose**, **Canvas 2D**, **Jetpack DataStore**, and **Clean MVVM Architecture**.

---

## ✨ Key Features

- **5 Evolution Stages**: Mini Snake ➔ Neon Snake ➔ Plasma Snake ➔ Cyber Snake ➔ Ultimate Snake, with visual aura transformations, glow radius upgrades, and score/coin/magnet perks.
- **5 Game Modes**:
  - **Classic Arcade**: Grow, evolve, and chase high scores.
  - **Survival Overdrive**: Escalating speed and dynamic hazard deployment.
  - **Tactical Missions**: 8 structured missions with custom objectives and coin payouts.
  - **Time Attack (90s)**: High-density orb frenzy against the clock.
  - **Boss Challenge**: Battle 4 unique Boss Cores (*Plasma Core*, *Cyber Guardian*, *Toxic Reactor*, *Neon Overlord*) with telegraphed laser patterns.
- **7 Futuristic Arenas**: *Neon City*, *Cyber Grid*, *Plasma Arena*, *Dark Space* (warp borders), *Toxic Zone*, *Crystal Lab*, and *Void Arena*.
- **7 Special Power-Ups**: Speed Boost (`⚡`), Holo Shield (`🛡️`), Energy Magnet (`🧲`), Chrono Slow (`⏳`), Instant Jackpot (`💎`), Ghost Phase (`👻`), and 2X Overdrive (`✖️2`).
- **Progression & Customization**:
  - **10 Unlockable Snake Skins** with live Canvas previews.
  - **6-Skill Cyber Upgrade Tree** (5 levels each).
  - **Offline Daily Challenges**, **8 Achievements**, and **Career Statistics**.
- **Synthesized Cyberpunk Audio & Haptics**: Zero-dependency procedural `AudioTrack` synth sound effects, background D-minor bassline loop, and haptic feedback.
- **Full Arabic (RTL) & English (LTR) Support**.

---

## 🏗️ Project Architecture

```text
app/src/main/java/com/example/
├── audio/
│   └── SoundManager.kt                 # Procedural PCM synth SFX, music loop & haptics
├── data/preferences/
│   └── GamePreferencesRepository.kt    # Jetpack DataStore persistence
├── domain/models/
│   ├── GameModels.kt                   # Grid, Orbs, Evolution, PowerUps, Modes, Arenas, Bosses
│   └── ProgressionModels.kt            # Skins, Skill Tree, Missions, Achievements, Settings
├── game/
│   ├── engine/
│   │   ├── GameEngine.kt               # Fixed-step 2D grid game loop & state machine
│   │   └── GameEngineState.kt          # Immutable engine snapshot
│   ├── renderer/
│   │   └── GameRenderer.kt             # High-FPS Compose Canvas 2D renderer
│   └── systems/
│       ├── BossSystem.kt               # Fair telegraphed boss attack patterns
│       └── GameSystems.kt              # Collision detection & safe entity spawning
├── ui/
│   ├── components/
│   │   └── NeonComponents.kt           # Glassmorphic cards, neon buttons & localization helper
│   ├── screens/
│   │   ├── MainMenuScreen.kt           # Animated main menu
│   │   ├── GameplayScreen.kt           # Arena HUD, Swipe/D-Pad controls, Pause & Revive modals
│   │   ├── GameModesScreen.kt          # Arena & Mode selector
│   │   ├── ProgressionScreens.kt       # Missions, Skins & Skill Tree screens
│   │   └── MetaScreens.kt              # Daily Challenges, Achievements, Statistics & Settings
│   ├── theme/
│   │   ├── Color.kt                    # Neon Arcade Cyberpunk palette
│   │   ├── Theme.kt                    # Material 3 Dark Neon theme
│   │   └── Type.kt                     # Bundled Orbitron & Rajdhani typography
│   └── SnakeViewModel.kt               # MVVM state holder & coroutine game loop controller
└── MainActivity.kt                     # App entry point, RTL/LTR layout & lifecycle handling
```

---

## 🚀 Building & Running

1. Clone this repository and open it in **Android Studio**.
2. Sync Gradle and run the `app` configuration on any Android device or emulator (`minSdk 24`).
3. To build a debug APK via command line:
   ```bash
   ./gradlew :app:assembleDebug
   ```
   Output APK location: `app/build/outputs/apk/debug/app-debug.apk`
