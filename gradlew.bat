@echo off
where gradle >nul 2>nul
if %ERRORLEVEL% equ 0 (
  gradle %*
  exit /b %ERRORLEVEL%
)

echo Gradle is not installed or available on PATH.
echo Install Gradle 8.9+ locally, or run this project through GitHub Actions.
exit /b 1
