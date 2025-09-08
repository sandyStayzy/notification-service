#!/bin/sh
# Maven wrapper script for Unix systems

MAVEN_WRAPPER_JAR=".mvn/wrapper/maven-wrapper.jar"
MAVEN_WRAPPER_PROPERTIES=".mvn/wrapper/maven-wrapper.properties"

if [ ! -f "$MAVEN_WRAPPER_JAR" ]; then
    echo "Maven wrapper jar not found. Please install Maven or create wrapper files."
    exit 1
fi

exec java -jar "$MAVEN_WRAPPER_JAR" "$@"