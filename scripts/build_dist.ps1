param()
$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$repoRoot = Resolve-Path (Join-Path $scriptDir '..')
$dist = Join-Path $repoRoot 'dist'

Write-Host "Cleaning old dist at $dist"
if (Test-Path $dist) { Remove-Item -Recurse -Force $dist }
New-Item -ItemType Directory -Path $dist | Out-Null

$files = @('index.html','styles.css','app.js','manifest.webmanifest','sw.js','icon.svg','README.md')
foreach ($f in $files) {
    $src = Join-Path $repoRoot $f
    if (Test-Path $src) {
        Copy-Item -Path $src -Destination $dist -Force
        Write-Host "Copied $f"
    } else {
        Write-Host "Warning: missing $f, skipping"
    }
}

$assetsSrc = Join-Path $repoRoot 'assets\icons'
$assetsDest = Join-Path $dist 'assets\icons'
if (Test-Path $assetsSrc) {
    New-Item -ItemType Directory -Path $assetsDest -Force | Out-Null
    Copy-Item -Path (Join-Path $assetsSrc '*') -Destination $assetsDest -Force
    Write-Host "Copied assets/icons"
} else {
    Write-Host "No assets/icons directory found, skipping"
}

# Ensure .nojekyll exists for GitHub Pages
New-Item -Path (Join-Path $dist '.nojekyll') -ItemType File -Force | Out-Null

Write-Host "Built dist at: $dist"
