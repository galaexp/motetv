# Nova Remote (for Android TV & Xiaomi Mi Stick)

**Nova Remote** is a minimalist, high-performance Android TV remote control application engineered with modern Kotlin, Jetpack Compose (Material 3), Room local persistence, and socket-level protocol handlers. Supports all Android TV OS devices (5.0+) with dedicated optimizations for the **Xiaomi Mi TV Stick** and **Mi Box** series.

---

## 🌟 What's New

### 1. YouTube Ad Skip Shortcut ⚡
- **Instant Ad Skip**: Prominent dedicated `⚡ Skip Ad` button on the primary remote control, apps launcher, and programmable macro engine.
- **Smart Key Sequencing**: Automatically sends micro-timed pulses (`DPAD_UP` -> `DPAD_RIGHT` -> `DPAD_CENTER`) or direct ADB shell keyevents to focus and trigger the "Skip Ad" button on YouTube for Android TV without requiring touch/mouse cursor interaction.

### 2. Comprehensive Settings Screen
- **Full First-Class Screen**: Accessible directly from the bottom navigation bar (`Settings` tab) and top bar.
- **Device Discovery & Pairing**:
  - Live mDNS Bonjour (`_androidtvremote2`, `_googlecast`, `_adb`) and Bluetooth LE/Classic discovery.
  - Manual IP & Port connection with protocol override.
  - Saved & Paired device history management.
  - PIN code pairing modal.
- **Connection & Protocol Preferences**:
  - Wi-Fi and Bluetooth discovery toggle controls.
  - Preferred protocol: Android TV v2 (Port 6467) vs ADB TCP (Port 5555).
  - Standby Wake-on-LAN (WOL) magic packet broadcast toggle.
  - Socket keep-alive interval slider (5s to 30s).
  - Auto-reconnect on Wi-Fi drop toggle.
- **Xiaomi Mi Stick Compatibility Suite**:
  - Deep Sleep Wakeup protocol (wakes dormant Wi-Fi controller on compact HDMI sticks).
  - Xiaomi PatchWall hardware button mapping.
  - HDMI-CEC stepped volume smoothing.
- **Customization & Diagnostics**:
  - Haptic tactile feedback toggle.
  - Touchpad gesture speed slider (0.5x – 2.5x).
  - YouTube Ad Skip Turbo mode toggle.
  - Diagnostic readouts and "Reset to Defaults" option.

### 3. Luxury Dark Minimalist Aesthetics
- **Nordic / Scandinavian Hardware Design**: Deep obsidian background (`#090B0E`), sleek matte graphite surfaces (`#11141A`), fine 1dp borders, and high-contrast typography.
- **Ergonomic D-Pad**: Sculptured concave ring with recessed chevrons and a metallic tactile center OK disc.
- **Smooth Slender Rockers**: Slender vertical pills for Volume (+/−) and Channel (CH+/CH−) adhering to 48dp accessibility standards.
- **Zero UI Glitches & Proper Inset Handling**: Uses `WindowInsets.safeDrawing` and `.navigationBarsPadding()` so the bottom navigation bar never collides with Android gesture pills.

---

## 🏗️ Architecture

```
app/src/main/java/com/example/
├── MainActivity.kt                      # Edge-to-edge entry point
├── tvremote/
│   ├── domain/model/
│   │   ├── TvDevice.kt                  # Device entity (IP, MAC, protocol, Mi Stick flag)
│   │   ├── RemoteCommand.kt             # Android KeyEvents & SkipYouTubeAd
│   │   ├── RemoteMacro.kt               # Macro model with step delays
│   │   ├── ConnectionStatus.kt          # Sealed UI connection state
│   │   └── RemoteSettings.kt            # Discovery, haptics, and Mi Stick toggles
│   ├── data/
│   │   ├── local/
│   │   │   ├── TvDatabase.kt            # Room database (saved devices & macros)
│   │   │   ├── SettingsPreferences.kt   # Persistent user preferences
│   │   │   ├── dao/                     # DeviceDao, MacroDao
│   │   │   └── entity/                  # DeviceEntity, MacroEntity
│   │   └── repository/
│   │       ├── DeviceRepository.kt      # Device persistence & history
│   │       └── MacroRepository.kt       # Macro sequencing data access
│   ├── network/
│   │   ├── discovery/
│   │   │   ├── NsdDiscoveryManager.kt   # mDNS Bonjour discovery (_androidtvremote2)
│   │   │   └── BluetoothDiscoveryManager.kt # Bluetooth LE & Classic discovery
│   │   ├── protocol/
│   │   │   ├── TvProtocolHandler.kt     # Protocol abstraction interface
│   │   │   ├── AndroidTvV2Protocol.kt   # Google TV / Android TV Remote v2 (Port 6467)
│   │   │   ├── AdbTcpProtocol.kt        # ADB TCP Keyevent Handler (Port 5555)
│   │   │   └── MiStickAdapter.kt        # Xiaomi Mi Stick quirks & WOL adapter
│   │   └── client/
│   │       └── TvRemoteManager.kt       # Connection lifecycle, keepalive, macro runner
│   └── ui/
│       ├── RemoteViewModel.kt           # State coordinator & RemoteTab.SETTINGS
│       ├── RemoteScreen.kt              # Root UI with Scaffold & M3 NavigationBar
│       ├── screens/
│       │   └── SettingsScreen.kt        # Full-featured comprehensive settings screen
│       ├── components/
│       │   ├── TopBarComponents.kt      # Minimalist status capsule, voice & power
│       │   ├── DpadController.kt        # Tactile D-pad, OK, and Skip Ad shortcut
│       │   ├── TouchpadView.kt          # Swipe/drag cursor surface & sensitivity
│       │   ├── KeypadView.kt            # 0-9 numeric channel tuner
│       │   └── AppsAndMacrosView.kt     # App tiles & programmable macro builder
│       └── dialogs/
│           ├── DeviceDiscoveryDialog.kt # Quick pairing sheet
│           ├── VoiceSearchDialog.kt     # Mic speech recognition & TV text injection
│           └── AddMacroDialog.kt        # Custom macro creator
└── ui/theme/                            # Nova Obsidian luxury palette
```

---

## 🛠️ Build & Verification

```sh
# Compile debug APK
gradle :app:assembleDebug

# Run unit tests
gradle :app:testDebugUnitTest
```
