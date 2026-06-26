$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Definition
$zipPath = Join-Path $root 'gradle-8.7.3-all.zip'
$extractDir = Join-Path $root 'gradle-download'
$wrapperDir = Join-Path $root 'gradle\wrapper'
$wrapperJar = Join-Path $wrapperDir 'gradle-wrapper.jar'

Remove-Item -Path $zipPath -Force -ErrorAction SilentlyContinue
Remove-Item -Path $extractDir -Recurse -Force -ErrorAction SilentlyContinue

Write-Host 'Downloading Gradle distribution...'
certutil -urlcache -split -f https://services.gradle.org/distributions/gradle-8.7.3-all.zip $zipPath

Write-Host 'Extracting Gradle distribution...'
Expand-Archive -Path $zipPath -DestinationPath $extractDir -Force

Write-Host 'Copying wrapper jar...'
$sourceJar = Join-Path $extractDir 'gradle-8.7.3\lib\gradle-wrapper.jar'
if (-not (Test-Path -Path $sourceJar)) {
    throw "Missing extracted gradle-wrapper.jar at $sourceJar"
}
Copy-Item -Path $sourceJar -Destination $wrapperJar -Force

Remove-Item -Path $zipPath -Force
Write-Host 'RESTORED_WRAPPER'
