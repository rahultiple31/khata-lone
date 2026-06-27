# Hisably

Hisably is a mobile-first web ledger for small businesses. It includes customers, credit/payment entries, balance totals, reminders, reports, browser-local backup, multilingual UI, a shop profile, PDF-ready print output, dark mode, and installable PWA support.

## Run Locally

Open `index.html` directly, or serve this folder with any static file server.

```powershell
python -m http.server 8000
```

Then open:

```text
http://localhost:8000
```

All demo data and changes are stored in browser `localStorage`. Use **Settings > Backup & restore** in the web app to download a JSON backup.

## Web App Files

```text
index.html
styles.css
app.js
manifest.webmanifest
sw.js
icon.svg
assets/icons/icon-192.png
assets/icons/icon-512.png
scripts/build_dist.ps1
.github/workflows/static.yml
.github/workflows/ci-cd.yml
```

## Deploy To GitHub Pages

This repository deploys only the web application.

The GitHub Pages workflow is:

```text
.github/workflows/static.yml
```

It runs automatically when code is pushed to `main` and deploys the static web app to GitHub Pages.

Live site:

```text
https://rahultiple31.github.io/khata-lone/
```

## Build Web Package

To prepare a static `dist/` folder on Windows:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_dist.ps1
```

The generated `dist/` folder can be uploaded to any static host.

## GitHub Actions

- `static.yml`: deploys the web app to GitHub Pages.
- `ci-cd.yml`: validates required web/PWA files and uploads a ready web artifact.

There is no native mobile package pipeline in this repo anymore. The native mobile project, build files, and mobile packaging workflow were removed so this repository deploys only the web application.

## Install As Web App On Mobile

After the site is deployed over HTTPS:

1. Open `https://rahultiple31.github.io/khata-lone/` in Chrome on your phone.
2. Tap the browser menu.
3. Choose **Add to Home screen** or **Install app**.
4. Open Hisably from the home screen.

This installs the web PWA experience, not a native app package.
