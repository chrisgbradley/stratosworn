#!/usr/bin/env bash
# One Phase 3 step: add a mod, reinstall, relaunch what changed, sample perf, commit on green.
# Usage: scripts/phase3-step.sh <modrinth-slug> <client|server|both> "<label>"
set -uo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"; cd "$ROOT"
WROOT="$(cygpath -w "$ROOT")"   # Windows-style path for PowerShell Start-Process
SLUG="$1"; SIDE="$2"; LABEL="$3"
if [ "$SLUG" != "-" ]; then
  echo "== add $SLUG ($SIDE)"
  (cd pack && packwiz modrinth add "$SLUG" -y 2>&1 | tail -1) || exit 1
  META=$(ls -t pack/mods/*.pw.toml | head -1)
  if [ "$SIDE" != "both" ]; then sed -i "s/^side = \".*\"/side = \"$SIDE\"/" "$META"; (cd pack && packwiz refresh >/dev/null); fi
  grep -E "^(name|filename|side)" "$META"
fi

# $1 = client | server. The dev client's java has no distinctive command line, so match its window title.
stop_java () { if [ "$1" = client ]; then powershell -NoProfile -Command "Get-Process java -ErrorAction SilentlyContinue | Where-Object { \$_.MainWindowTitle -like 'Minecraft*' } | ForEach-Object { Stop-Process -Id \$_.Id -Force -Confirm:\$false }"; else powershell -NoProfile -Command "Get-Process java -ErrorAction SilentlyContinue | ForEach-Object { \$cl=(Get-CimInstance Win32_Process -Filter \"ProcessId=\$(\$_.Id)\").CommandLine; if (\$cl -like '*win_args.txt*') { Stop-Process -Id \$_.Id -Force -Confirm:\$false } }"; fi; for i in 1 2 3 4 5; do sleep 1; done; }
start_server () { rm -f server/logs/latest.log; powershell -NoProfile -Command "Start-Process -FilePath java -ArgumentList '@user_jvm_args.txt','@libraries/net/neoforged/neoforge/21.1.250/win_args.txt','nogui' -WorkingDirectory '$WROOT\server' -WindowStyle Hidden -RedirectStandardOutput \"\$env:TEMP\stratos-server.out\" -RedirectStandardError \"\$env:TEMP\stratos-server.err\""; for i in $(seq 1 80); do grep -q "Done (" server/logs/latest.log 2>/dev/null && return 0; sleep 3; done; echo "server never reached Done"; return 1; }

if [ "$SIDE" != "client" ]; then
  echo "== server boot loop"
  python scripts/rcon.py stop >/dev/null 2>&1; sleep 5; stop_java server; sleep 2
  python scripts/boot-server.py --timeout 400 2>&1 | grep -E "^\[boot\] server|^\[scan\]|^  E |^  M |^\[result\]|never reached|Mod ID" | tail -12
  grep -q "Done (" server/logs/latest.log && ! grep -q "/ERROR]" server/logs/latest.log || { echo "SERVER RED"; exit 2; }
  echo "== restart detached server"; start_server || exit 2
fi

echo "== client reinstall + relaunch"
stop_java client; sleep 2
bash scripts/client-install.sh 2>&1 | grep -E "Downloaded|Failed|Finished"
for i in $(seq 1 20); do netstat -an | grep -q ":25590 .*LISTENING" || break; sleep 1; done
netstat -an | grep -q ":25590 .*LISTENING" && { echo "old client still holds 25590"; exit 3; }
JOINS_BEFORE=$(grep -c "joined the game" server/logs/latest.log 2>/dev/null); JOINS_BEFORE=${JOINS_BEFORE:-0}
rm -f telemetry/run/logs/latest.log
# Detached with every fd redirected, so the game inherits no handle from this script (a held pipe keeps the task alive).
(cd telemetry && nohup ./gradlew runClientJoin --console=plain > "$TEMP/stratos-client.out" 2> "$TEMP/stratos-client.err" < /dev/null & disown) 2>/dev/null
for i in $(seq 1 120); do
  n=$(grep -c "joined the game" server/logs/latest.log 2>/dev/null); [ "${n:-0}" -gt "$JOINS_BEFORE" ] && break
  grep -qE "has crashed|Exception in thread \"main\"|BUILD FAILED" telemetry/run/logs/latest.log "$TEMP/stratos-client.out" 2>/dev/null && { echo "CLIENT RED"; grep -m5 -E "has crashed|Exception|Caused by" telemetry/run/logs/latest.log | cut -c1-240; exit 3; }
  sleep 3
done
n=$(grep -c "joined the game" server/logs/latest.log 2>/dev/null); [ "${n:-0}" -gt "$JOINS_BEFORE" ] || { echo "CLIENT never joined"; tail -5 telemetry/run/logs/latest.log | cut -c1-200; exit 3; }
echo "== client log errors:"; grep -c "/ERROR]" telemetry/run/logs/latest.log; grep "/ERROR]" telemetry/run/logs/latest.log | head -5 | cut -c1-240
for i in $(seq 1 30); do netstat -an | grep -q ":25590 .*LISTENING" && break; sleep 2; done
echo "== perf"; python -u scripts/perf-sample.py --label "$LABEL" --samples 10 < /dev/null
