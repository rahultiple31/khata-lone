# Hisably

An original, mobile-first digital ledger for small businesses. Hisably includes customers, credit/payment entries, balance totals, reminders, reports, browser-local backup, multilingual UI, a shop profile, PDF-ready print output, and dark mode.

## Run locally

Open `index.html` directly, or serve this folder with any static file server. When served over HTTP/HTTPS, the included web manifest and service worker make the app installable on supported Android browsers.

All demo data and changes are stored in browser `localStorage`. Use **Settings > Backup & restore** to download a JSON backup.

## Android PWA build

This repository includes a GitHub Actions workflow at `.github/workflows/ci-cd.yml`.

- Pull requests and pushes validate the Android-installable PWA files, manifest, icons, and service worker cache list.
- The workflow uploads a ready static artifact named `hisably-android-pwa`.
- For manual GitHub Pages deployment, serve the repository root or the downloaded artifact contents.
- After it is available over HTTPS, Android users can open the site in Chrome and choose **Add to Home screen** or **Install app**.

## Native Android app

This repository also includes a simple beginner-friendly native Android project built with Kotlin.

### Important Android languages and tools

- **Kotlin**: The main recommended language for modern Android development. Use it for screens, app logic, events, and data handling.
- **Java**: Older Android apps and many libraries still use Java. It is useful to understand because Kotlin and Java work together on Android.
- **XML**: Used for traditional Android layouts, colors, strings, themes, and manifest configuration.
- **Gradle**: The Android build system. It downloads dependencies, compiles Kotlin/Java, runs tests, and creates APK/AAB files.
- **Basic SQL/SQLite**: Useful for saving local app data on the phone, such as customers, transactions, notes, and settings.
- **Optional JavaScript/React Native**: A cross-platform option when you want to build Android and iOS apps with JavaScript/TypeScript.
- **Optional Dart/Flutter**: A cross-platform option from Google for Android, iOS, web, and desktop apps using Dart.

### Project structure

```text
settings.gradle
build.gradle
gradle.properties
gradlew
gradlew.bat
app/
  build.gradle
  src/main/AndroidManifest.xml
  src/main/java/com/hisably/ledger/MainActivity.kt
  src/main/java/com/hisably/ledger/GreetingMessage.kt
  src/main/res/layout/activity_main.xml
  src/main/res/values/colors.xml
  src/main/res/values/strings.xml
  src/main/res/values/styles.xml
  src/test/java/com/hisably/ledger/GreetingMessageTest.kt
.github/workflows/android-ci.yml
```

### Build locally

Install Android Studio or the Android SDK, Java 17, and Gradle 8.9 or newer. Then run:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

On macOS/Linux, use:

```bash
chmod +x ./gradlew
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

The debug APK will be created at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Push code to GitHub

For a new repository:

```powershell
git init
git status -sb
git add .
git commit -m "Initial Android app"
git branch -M main
git remote add origin https://github.com/USERNAME/REPO-NAME.git
git push -u origin main
```

For this existing repository, use:

```powershell
git add .
git commit -m "Update Android app"
git push origin main
```

### Run GitHub Actions pipeline

The Android CI pipeline runs automatically when you push to `main`.

### Android APK delivery flow

```text
Developer
  |
  v
Push code to GitHub
  |
  v
GitHub Actions builds APK
  |
  v
Choose one:
  |-- GitHub Artifacts: download for testing
  |-- GitHub Releases: share APK
  |-- Firebase App Distribution: beta testing
  `-- Google Play Internal Testing: recommended before production
```

### Download APK artifact

1. Open the completed **Android CI** workflow run.
2. Scroll to **Artifacts**.
3. Download `android-debug-apk`.
4. Unzip it to get `app-debug.apk`.

Install it on an Android phone for testing. If Android blocks the install, enable **Settings > Security > Install unknown apps > Allow** for the app you use to open the APK.

### Deployment options

- **GitHub Actions Artifacts**: Best for simple testing after every push.
- **GitHub Releases**: Best for sharing APK files with other people.
- **Firebase App Distribution**: Good for sharing APK builds with testers quickly.
- **Google Play Console internal testing track**: Recommended before production.
- **Google Play Store**: Main public production release channel.

For Google Play Store, upload a signed Android App Bundle (`.aab`). For Firebase App Distribution or GitHub Releases, you can share a signed APK with testers.
