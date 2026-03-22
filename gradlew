#!/bin/sh
# Gradle wrapper script

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

# Determine the Gradle wrapper script location.
APP_HOME=$(cd "$(dirname "$0")" && pwd)

exec "$JAVACMD" \
  -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
  org.gradle.wrapper.GradleWrapperMain "$@"
