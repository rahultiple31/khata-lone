#!/usr/bin/env sh
##############################################################################
##
## Gradle start up script for UN*X
##
##############################################################################

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
else
  JAVA_CMD="java"
fi

exec "$JAVA_CMD" -jar "$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar" "$@"
