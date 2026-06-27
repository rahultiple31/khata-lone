# Hisably

Hisably is a mobile-first React web ledger for small businesses. It helps track customers, credit entries, payments, balances, transaction history, reports, reminders, shop settings, dark mode, local browser storage, JSON backup, and installable PWA support.

This repository is web-only. Native mobile code and mobile package workflows are not included.

## Tech Stack

- Node.js
- React
- Vite
- CSS
- Browser `localStorage`
- Progressive Web App assets in `public/`
- GitHub Actions for web build and GitHub Pages deployment

## Run Locally

Install Node.js 20 or newer, then run:

```powershell
npm install
npm run dev
```

Open the local URL printed by Vite, usually:

```text
http://localhost:5173/khata-lone/
```

## Build Web App

```powershell
npm run build
```

The production web app is generated in:

```text
dist/
```

On Windows, you can also run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_dist.ps1
```

## Project Structure

```text
index.html
package.json
vite.config.js
src/
  App.jsx
  App.css
  main.jsx
public/
  manifest.webmanifest
  sw.js
  icon.svg
  assets/icons/icon-192.png
  assets/icons/icon-512.png
scripts/
  validate-source.mjs
  build_dist.ps1
.github/workflows/
  ci-cd.yml
  static.yml
```

## GitHub Actions

- `ci-cd.yml`: installs Node dependencies, tests source files, builds the React app, validates PWA output, and uploads the `dist/` artifact.
- `static.yml`: installs Node dependencies, builds the React app, and deploys `dist/` to GitHub Pages.

Live site:

```text
https://rahultiple31.github.io/khata-lone/
```

## Install As Web App On Mobile

After the site is deployed over HTTPS:

1. Open `https://rahultiple31.github.io/khata-lone/` in Chrome on your phone.
2. Tap the browser menu.
3. Choose **Add to Home screen** or **Install app**.
4. Open Hisably from the home screen.

This installs the React PWA experience.
