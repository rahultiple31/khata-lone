$ErrorActionPreference = 'Stop'
$repoRoot = Resolve-Path (Join-Path (Split-Path -Parent $MyInvocation.MyCommand.Definition) '..')
Push-Location $repoRoot
try {
    npm install
    npm run build
    New-Item -Path (Join-Path $repoRoot 'dist\.nojekyll') -ItemType File -Force | Out-Null
    Write-Host "Built React web app at: $(Join-Path $repoRoot 'dist')"
} finally {
    Pop-Location
}
