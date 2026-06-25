# Hisably

An original, mobile-first digital ledger for small businesses. Hisably includes customers, credit/payment entries, balance totals, reminders, reports, browser-local backup, multilingual UI, a shop profile, PDF-ready print output, and dark mode.

## Run locally

Open `index.html` directly, or serve this folder with any static file server. When served over HTTP/HTTPS, the included web manifest and service worker make the app installable on supported Android browsers.

All demo data and changes are stored in browser `localStorage`. Use **Settings > Backup & restore** to download a JSON backup.

## CI/CD deployment

This repository includes a GitHub Actions workflow at `.github/workflows/ci-cd.yml`.

- Pull requests validate the Android-installable PWA files, manifest, and service worker cache list.
- Pushes to `main` publish the static app to GitHub Pages.
- In GitHub, set **Settings > Pages > Build and deployment > Source** to **GitHub Actions** before the first deployment.
