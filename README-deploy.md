# Deploying Hisably React Web App

Hisably is deployed as a Vite React static web app. GitHub Actions builds the app with Node.js and publishes the generated `dist/` folder.

## Local Build

```powershell
npm install
npm run build
```

Or use the Windows helper:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_dist.ps1
```

## Preview Build

```powershell
npm run preview
```

## GitHub Pages Deployment

Push to `main`. The workflow below builds and deploys the React app:

```text
.github/workflows/static.yml
```

The deployed website is:

```text
https://rahultiple31.github.io/khata-lone/
```

## Notes

- App data is stored in the browser with `localStorage`.
- Authentication and backup are client-side demo features.
- This repository contains only Node.js, React, CSS, and PWA web code.
