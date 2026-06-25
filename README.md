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
