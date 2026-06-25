@echo off
where gradle >nul 2>nul
if %ERRORLEVEL% neq 0 (
  echo Gradle is not installed or available on PATH.
  echo Install Gradle 8.9+ locally, or run this project through GitHub Actions.
  exit /b 1
)

gradle %*
