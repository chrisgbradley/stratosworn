#!/usr/bin/env bash
# Install the pack client-side into the Gradle dev client's run dir, plus the freshly built
# telemetry jar. Then `cd telemetry && ./gradlew runClient`.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
RUN="$ROOT/telemetry/run"
mkdir -p "$RUN"
cd "$RUN"
java -jar "$ROOT/tools/packwiz-installer-bootstrap.jar" -g -s client "$ROOT/pack/pack.toml"
rm -f "$RUN"/mods/stratosworn_telemetry-*.jar
echo "installed pack client-side into $RUN (telemetry mod comes from the Gradle classpath)"
