# Deploying Hisably (Static PWA)

This project is a Progressive Web App designed to be served as static files. The included `scripts/build_dist.ps1` prepares a deployable `dist/` folder.

## Build the `dist/` package (Windows PowerShell)

Open PowerShell in the repo root and run:

```powershell
cd .\scripts
powershell -NoProfile -ExecutionPolicy Bypass -File .\build_dist.ps1
```

After running the script, the `dist/` folder will contain the files required for deployment.

## Serve locally for verification

You can serve `dist/` with a simple static server. Using Node's `http-server`:

```bash
npx http-server dist -c-1
# open http://localhost:8080
```

Or with Python:

```bash
cd dist
python -m http.server 8000
# open http://localhost:8000
```

## Deploy options

- GitHub Pages: push the `dist/` contents to the `gh-pages` branch (or use Actions to deploy).
- Static hosts: Netlify, Vercel, Firebase Hosting, or any static host support.

## Notes

- Current authentication is client-side only (no backend). For production, add server-side authentication and secure storage for user accounts.
- Release APK creation is handled separately by the Android CI workflow.
