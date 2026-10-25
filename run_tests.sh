#!/bin/sh
# Build and run the dependency-free test harness.
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -d out $(find src tests -name '*.java')
java -cp out com.oems.TestRunner
