# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Inventory App — Project Spec

## Stack
- Kotlin, Jetpack Compose, MVVM + Repository
- Room (SQLite), Hilt, CameraX + ML Kit, Navigation Compose, DataStore, Coroutines + Flow

## Roles
- owner: full access including cost_price visibility
- admin: scan in/out, manual input, statistics (no cost_price restriction)
- kasir: scan out only, cost_price hidden everywhere

## Database Tables
- users (id, username, password_hash, role)
- products (sku, brand_name, product_name, category, stock, cost_price, selling_price, low_stock_threshold, created_at, updated_at)
- transactions (id, sku, type[IN/OUT], source[SCAN/MANUAL], quantity, cost_price_snapshot, selling_price_snapshot, notes, user_id, created_at)
- price_histories (id, sku, cost_price, selling_price, changed_by_user_id, created_at)

## Transaction type values: "IN" / "OUT"
## Transaction source values: "SCAN" / "MANUAL"

## Key Rules
- cost_price & cost_price_snapshot hidden from kasir on ALL screens
- Manual input accessible to owner & admin only
- Max quantity validation on all OUT flows
- Prices formatted as Rupiah: Rp 150.000
- Passwords hashed with SHA-256
- All manual transactions show "Manual" purple badge in history
- Low stock threshold default = 5, configurable per product or global via DataStore

## Build / test commands

Run from the repo root using the Gradle wrapper (`./gradlew` on macOS/Linux, `gradlew.bat` on Windows).

- Build Android debug APK: `./gradlew :androidApp:assembleDebug`
- Run Android (host) unit tests: `./gradlew :shared:testAndroidHostTest`
- Run iOS simulator tests: `./gradlew :shared:iosSimulatorArm64Test`
- Run a single test class: append `--tests "com.example.stoku.SharedCommonTest"` to the relevant test task
- iOS app itself must be opened and run from Xcode via `iosApp/iosApp.xcodeproj` (no Gradle task builds/runs the iOS app binary)

There is no separate lint task configured beyond what's bundled in the Android Gradle Plugin / Kotlin compiler.

## Module layout

- `:shared` — Kotlin Multiplatform module (Android + iOS targets) containing all shared code and the Compose Multiplatform UI (`App.kt`). Source sets:
  - `commonMain` — shared Kotlin + Compose UI code, plus `composeResources` for shared resources
  - `androidMain` / `iosMain` — platform-specific `actual` implementations (e.g. `Platform.android.kt`, `Platform.ios.kt` implement the `expect fun getPlatform()` declared in `commonMain`)
  - `commonTest`, `androidHostTest`, `iosTest` — tests per source set; `androidHostTest` runs on the JVM host (not an emulator)
- `:androidApp` — thin Android application module; depends on `:shared` via `projects.shared` and just hosts `MainActivity.kt`
- `iosApp/` — native iOS app entry point (SwiftUI `iOSApp.swift`/`ContentView.swift`) that embeds the `Shared` framework produced by `:shared`'s iOS targets. This is not a Gradle module — open/build it in Xcode.

Cross-platform code lives in `:shared`; anything platform-specific (e.g. APIs unavailable on one platform) should be added as an `expect`/`actual` pair rather than duplicated.

## Dependency versions

All dependency and plugin versions are centralized in `gradle/libs.versions.toml` (the Gradle version catalog) and referenced via `libs.*` accessors in build scripts — do not hardcode version strings directly in a `build.gradle.kts`. `settings.gradle.kts` enables `TYPESAFE_PROJECT_ACCESSORS`, so cross-module references use `projects.shared` rather than string project paths.

The Gradle daemon JVM toolchain is pinned via `gradle/gradle-daemon-jvm.properties` (Amazon Corretto 21); module compiler options separately target JVM 11 bytecode.
