#!/usr/bin/env bash
# Dev serving for the Prism instance: the pack on :8080 (packwiz serve) and the dev-only
# telemetry jar on :8081 (plain static server over pack/mods, which packwiz serve will not
# serve because the jar is deliberately not in the index). Ctrl+C stops both.
set -uo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
python -m http.server 8081 --bind 127.0.0.1 --directory "$ROOT/pack/mods" > /dev/null 2>&1 &
JARPID=$!
trap 'kill $JARPID 2>/dev/null' EXIT
echo "telemetry jar server on http://127.0.0.1:8081 (pid $JARPID)"
cd "$ROOT/pack" && packwiz serve -p 8080
