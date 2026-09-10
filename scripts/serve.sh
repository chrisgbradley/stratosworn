#!/usr/bin/env bash
# Dev serving for the Prism instance: serves this checkout's pack on :8080 so the instance
# installs uncommitted changes. The telemetry jar comes from a GitHub release, so nothing
# else needs to be served. Other machines can install straight from
# https://raw.githubusercontent.com/chrisgbradley/stratosworn/master/pack/pack.toml
set -uo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/pack" && packwiz serve -p 8080
