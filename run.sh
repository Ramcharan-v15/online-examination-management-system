#!/bin/sh
# Build and run the interactive app. Requires a JDK (javac/java) on PATH.
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -d out $(find src -name '*.java')
java -cp out com.oems.ui.Main "$@"
