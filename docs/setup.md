# Setup Guide

How to get the project running from a clean clone on a new machine.

---

## Prerequisites

### 1. JDK — any version 17 or later

The build uses Gradle toolchains, so Gradle compiles everything with JDK 17
regardless of which JDK runs Gradle itself. You can run Gradle with JDK 21,
17, or even a newer version — compilation targets are always pinned to 17.

If you don't have any JDK installed:

```bash
# macOS — installs JDK 21 (recommended)
brew install --cask temurin@21

# Verify
java -version
```

If Gradle can't find a JDK 17 installation at build time, it will auto-download
one via toolchain provisioning (controlled by `org.gradle.java.installations.auto-download=true`
in `gradle.properties`).

> **Note:** You may see warnings like "Kotlin does not yet support JDK X target,
> falling back to JVM_Y" if your Gradle daemon runs on a very recent JDK. These
> are warnings only — the build still succeeds. Compilation always targets JVM 17.

### 2. Gradle 8.11+

Only needed once to generate the wrapper. After that, always use `./gradlew`.

```bash
brew install gradle
gradle --version  # should show 8.x.x
```

### 3. Docker Desktop

Required for local Postgres and Nango (OAuth). Download: https://docs.docker.com/get-docker/

### 4. Android Studio (for Android builds)

Download: https://developer.android.com/studio

After installing, add to your shell profile (`~/.zshrc`):

```bash
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

### 5. Xcode (macOS, for iOS builds)

Install from the Mac App Store, then:

```bash
sudo xcode-select --switch /Applications/Xcode.app
```

The iOS Xcode project is generated separately — see [iOS Setup](#ios-setup) below.

---

## First-time setup

### Step 1 — Clear any stale Gradle cache

Always start from a clean state on a fresh clone:

```bash
rm -rf .gradle build build-logic/build
```

This is safe and idempotent. Gradle rebuilds all caches on the next run.

> **Why:** Gradle caches compiled convention plugins and task graphs. If a
> previous failed build (e.g. from checking out a broken commit) left stale
> cache entries, subsequent builds replay the broken state instead of
> re-evaluating. Always clear when something seems wrong.

### Step 2 — Generate the Gradle wrapper

```bash
gradle wrapper --gradle-version $(gradle --version | grep "^Gradle" | awk '{print $2}') --distribution-type bin
```

This generates `gradlew`. You only run this once per clone.

### Step 3 — Set up your environment file

```bash
cp .env.example .env
# Open .env and fill in your credentials
```

The app compiles without credentials. Backend and sync require them at runtime.

| Variable | Where to get it |
|---|---|
| `SUPABASE_URL` | Supabase project → Settings → API |
| `SUPABASE_ANON_KEY` | Supabase project → Settings → API |
| `SUPABASE_SERVICE_ROLE_KEY` | Supabase project → Settings → API |
| `JWT_SECRET` | `openssl rand -base64 32` |
| `NANGO_SECRET_KEY` | Nango dashboard, or leave empty for local |

### Step 4 — Start local services

```bash
docker compose -f scripts/deploy/docker-compose.yml up -d
```

Starts local Postgres (port 5432) and Nango (port 3003).

### Step 5 — Verify

```bash
# Hard-gate tests: ArchUnit + domain tests
./gradlew :domain:jvmTest :application:jvmTest

# Full build across all modules
./gradlew build
```

Both should pass with no errors.

---

## Running each target

```bash
./gradlew :clients:desktop:run                  # Desktop app
./gradlew :backend:run                          # Backend → http://localhost:8080/health
./gradlew :clients:web:wasmJsBrowserRun         # Web (opens browser)
./gradlew :clients:android:installDebug         # Android (needs device/emulator)
./gradlew :domain:jvmTest :application:jvmTest  # Tests
```

---

### iOS Setup

The iOS client is a standard Xcode project that integrates the shared Kotlin UI.

1. Open Xcode
2. Select **Open a project or file**
3. Navigate to `clients/ios/iosApp.xcodeproj` and open it
4. Select a simulator target (e.g., iPhone 15) and press **Run** (⌘R)

> **Note:** The first build in Xcode may take several minutes as it compiles all KMP dependencies via the Gradle build phase.

## Troubleshooting

### Build fails with "Plugin not found" or "Unresolved reference"

Clear the cache and retry:

```bash
rm -rf .gradle build build-logic/build
gradle wrapper --gradle-version $(gradle --version | grep "^Gradle" | awk '{print $2}') --distribution-type bin
```

**"Plugin not found":** Every plugin applied inside a convention plugin
(`build-logic/src/main/kotlin/*.gradle.kts`) must use `alias(libs.plugins.xxx)`
syntax, not bare `id("...")` strings. Bare IDs without a version can't be
resolved when the convention plugin is applied to a subproject.

**"Unresolved reference 'libs'":** Type-safe catalog accessors for *libraries*
don't work inside precompiled script plugins. Only `alias(libs.plugins.xxx)` in
`plugins {}` blocks works. All `libs.xxx` library references belong in each
module's own `build.gradle.kts`.

### "Kotlin does not yet support X JDK target, falling back to JVM_Y"

This warning appears when the JDK running Gradle is newer than Kotlin's named
target list. It is a **warning only** — the build succeeds and code compiles to
JVM 17 bytecode as configured. No action needed.

The build uses Gradle toolchains (`jvmToolchain(17)`) so compilation always
targets JVM 17 regardless of which JDK runs Gradle.

### Stale builds after changing convention plugins

Any time you edit files under `build-logic/src/main/kotlin/`, clear caches:

```bash
rm -rf .gradle build build-logic/build
```

### Docker services not starting

```bash
docker compose -f scripts/deploy/docker-compose.yml ps    # check status
docker compose -f scripts/deploy/docker-compose.yml logs  # check logs
docker compose -f scripts/deploy/docker-compose.yml down && \
docker compose -f scripts/deploy/docker-compose.yml up -d  # restart
```
