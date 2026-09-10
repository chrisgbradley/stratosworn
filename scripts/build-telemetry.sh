#!/usr/bin/env bash
# Build the telemetry mod, drop the jar into pack/mods (git-ignored, packwiz-ignored), refresh the metafile hash.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
(cd "$ROOT/telemetry" && ./gradlew build --console=plain -q)
JAR=$(ls "$ROOT"/telemetry/build/libs/stratosworn_telemetry-*.jar | grep -v sources | head -1)
cp "$JAR" "$ROOT/pack/mods/"
H=$(sha256sum "$ROOT/pack/mods/$(basename "$JAR")" | cut -d' ' -f1)
sed -i "s/^hash = \".*\"/hash = \"$H\"/; s#^filename = \".*\"#filename = \"$(basename "$JAR")\"#; s#/mods/stratosworn_telemetry-[^\"]*\.jar#/mods/$(basename "$JAR")#" "$ROOT/pack/mods/stratosworn-telemetry.pw.toml"
(cd "$ROOT/pack" && packwiz refresh >/dev/null)
echo "telemetry jar $(basename "$JAR") sha256 $H"
