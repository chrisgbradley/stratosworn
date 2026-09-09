#!/usr/bin/env bash
# Release export. Strips the dev-only telemetry metafile, refreshes the index, exports
# Modrinth (.mrpack) and CurseForge/Prism-compatible packs into exports/, then restores.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/pack"
mkdir -p "$ROOT/exports"
TELEM=mods/stratosworn-telemetry.pw.toml
trap 'git -C "$ROOT" checkout -- "pack/$TELEM" pack/index.toml pack/pack.toml 2>/dev/null || true' EXIT
rm -f "$TELEM"
packwiz refresh
packwiz modrinth export -o "$ROOT/exports/Stratosworn-$(grep -m1 '^version' pack.toml | cut -d'"' -f2).mrpack"
packwiz curseforge export -o "$ROOT/exports/Stratosworn-$(grep -m1 '^version' pack.toml | cut -d'"' -f2)-curseforge.zip"
echo "exports written to $ROOT/exports"
