#!/usr/bin/env bash
set -euo pipefail

./gradlew testDebugUnitTest
./gradlew ktlintCheck