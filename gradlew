#!/usr/bin/env sh

if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi

echo "Gradle is not installed or available on PATH."
echo "Install Gradle 8.9+ locally, or run this project through GitHub Actions."
exit 1
