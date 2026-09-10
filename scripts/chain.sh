#!/usr/bin/env bash
# Run a list of mods through the boot loop one at a time. Green ones are committed with
# their MODS.md row; red ones are backed out (with anything packwiz auto-added) and their
# logs kept. Loot-table-only failures generate overrides and retry once.
#
# Usage: scripts/chain.sh <logfile> ; reads "source|slug|side|label|mods-row" lines on stdin
set -uo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"; cd "$ROOT"
LOG="$1"; : > "$LOG"

while IFS='|' read -r src slug side label row; do
  [ -z "${slug:-}" ] && continue
  before=$(ls pack/mods/*.pw.toml | sort)
  echo "#### $(date +%H:%M:%S) START $slug ($src, $side)" >> "$LOG"
  if [ "$src" = "cf" ]; then
    (cd pack && packwiz curseforge add "$slug" -y >> "$LOG" 2>&1) || { echo "#### $slug: add failed" >> "$LOG"; continue; }
    # keep shared deps on their original source
    git checkout -q -- pack/mods 2>/dev/null || true
    (cd pack && packwiz curseforge add "$slug" -y >/dev/null 2>&1)
    META=pack/mods/$(ls -t pack/mods | head -1)
    [ "$side" != both ] && sed -i "s/^side = \".*\"/side = \"$side\"/" "$META" 2>/dev/null
    (cd pack && packwiz refresh >/dev/null 2>&1)
    bash scripts/phase3-step.sh - "$side" "$label" >> "$LOG" 2>&1 < /dev/null; rc=$?
  else
    bash scripts/phase3-step.sh "$slug" "$side" "$label" >> "$LOG" 2>&1 < /dev/null; rc=$?
  fi

  # The dev client's mod loading is nondeterministic: Create's Registrate intermittently
  # reports unused callbacks, and Create's AllAdvancements intermittently reads an unbound
  # item. Both clear on a plain retry. Never call a client failure red on one sample.
  tries=0
  while [ $rc -eq 3 ] && [ $tries -lt 2 ]; do
    tries=$((tries+1))
    echo "#### $(date +%H:%M:%S) client retry $tries for $slug (known flaky loader)" >> "$LOG"
    bash scripts/phase3-step.sh - "$side" "$label" >> "$LOG" 2>&1 < /dev/null; rc=$?
  done

  if [ $rc -eq 2 ] && grep -q "Couldn't parse element" server/logs/latest.log 2>/dev/null \
     && ! grep "/ERROR]" server/logs/latest.log | grep -qv "Couldn't parse element"; then
    python scripts/loot-overrides.py server/logs/latest.log >> "$LOG" 2>&1
    (cd pack && packwiz refresh >/dev/null 2>&1)
    echo "#### $(date +%H:%M:%S) RETRY $slug with loot overrides" >> "$LOG"
    bash scripts/phase3-step.sh - "$side" "$label" >> "$LOG" 2>&1 < /dev/null; rc=$?
  fi
  echo "#### $(date +%H:%M:%S) END $slug rc=$rc" >> "$LOG"

  if [ $rc -eq 0 ] && grep -q "\"label\": \"$label\"" "$LOG"; then
    echo "$row" >> MODS.md
    after=$(ls pack/mods/*.pw.toml | sort)
    comm -13 <(echo "$before") <(echo "$after") | sed 's#.*/##; s#\.pw\.toml##' | grep -v "^$slug$" \
      | while read -r d; do echo "| $d | (auto-added with $slug) | $side | Dependency. |" >> MODS.md; done
    git add -A && git commit -q -m "pack: add $slug (green)

Co-Authored-By: Claude Opus 5 <noreply@anthropic.com>" && git push -q origin master && echo "#### committed $slug" >> "$LOG"
  else
    cp server/logs/latest.log "$ROOT/../red-$slug-server.log" 2>/dev/null
    cp telemetry/run/logs/latest.log "$ROOT/../red-$slug-client.log" 2>/dev/null
    after=$(ls pack/mods/*.pw.toml | sort)
    comm -13 <(echo "$before") <(echo "$after") | sed 's#.*/##; s#\.pw\.toml##' \
      | while read -r d; do (cd pack && packwiz remove "$d" -y >/dev/null 2>&1); echo "#### backed out $d" >> "$LOG"; done
    (cd pack && packwiz refresh >/dev/null 2>&1)
    # resync the server folder now, or the backed-out jar stays loaded until the next boot loop
    (cd server && java -jar ../tools/packwiz-installer-bootstrap.jar -g -s server ../pack/pack.toml >/dev/null 2>&1)
    git checkout -q -- MODS.md 2>/dev/null
    git add -A; git commit -q -m "pack: back out $slug (red boot)

Co-Authored-By: Claude Opus 5 <noreply@anthropic.com>" >/dev/null 2>&1; git push -q origin master
    echo "#### RED $slug — backed out" >> "$LOG"
  fi
done
echo "#### CHAIN DONE" >> "$LOG"
