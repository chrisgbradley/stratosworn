#!/usr/bin/env python3
"""Repeatable client perf sample via the telemetry MCP server.
Focuses the game window, puts the player in spectator at a fixed spot and facing,
waits for chunks, then averages N perf reads. Prints one JSON line; appends to docs/PERF.md
when --label is given.

Usage: python scripts/perf-sample.py --label "baseline" [--samples 10] [--spot "-61 180 -162 0 35"]
"""
import argparse, json, subprocess, sys, time, urllib.request, pathlib

URL = "http://127.0.0.1:25590/mcp"
ROOT = pathlib.Path(__file__).resolve().parent.parent

def rpc(method, params):
    body = json.dumps({"jsonrpc": "2.0", "id": 1, "method": method, "params": params}).encode()
    req = urllib.request.Request(URL, data=body, headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req, timeout=60) as r:
        return json.load(r)

def tool(name, args=None):
    d = rpc("tools/call", {"name": name, "arguments": args or {}})
    txt = d["result"]["content"][0]["text"]
    try:
        return json.loads(txt)
    except Exception:
        return {"raw": txt}

def focus():
    ps = "$w=New-Object -ComObject WScript.Shell; $ok=$w.AppActivate('Minecraft'); Start-Sleep -Milliseconds 600; if($ok){$w.SendKeys('{ESC}')}; $ok"
    out = subprocess.run(["powershell", "-NoProfile", "-Command", ps], capture_output=True, text=True).stdout.strip()
    return out.endswith("True")

if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--label")
    ap.add_argument("--samples", type=int, default=10)
    ap.add_argument("--spot", default="-61 180 -162 0 35", help="x y z yaw pitch")
    ap.add_argument("--settle", type=int, default=25, help="seconds to wait for chunks/LODs")
    a = ap.parse_args()

    focused = focus()
    tool("run_command", {"command": "gamemode spectator", "wait_ms": 300})
    x, y, z, yaw, pitch = a.spot.split()
    tool("run_command", {"command": f"tp Dev {x} {y} {z} {yaw} {pitch}", "wait_ms": 300})
    tool("perf")  # subscribes to tick samples
    time.sleep(a.settle)
    if not focused:
        focus()
    # Only count reads taken with the window focused; Minecraft throttles when unfocused.
    reads, tries, skipped = [], 0, 0
    while len(reads) < a.samples and tries < a.samples * 6:
        tries += 1
        r = tool("perf")
        if r.get("window_focused") is False or r.get("screen"):
            skipped += 1
            focus()
            time.sleep(1.5)
            continue
        reads.append(r)
        time.sleep(1)
    if len(reads) < a.samples:
        print(f"[perf] only {len(reads)} focused reads after {tries} tries (skipped {skipped}); not recording", file=sys.stderr)
        print(json.dumps({"label": a.label, "error": "unfocused", "focused_reads": len(reads)}))
        sys.exit(1)
    def avg(key):
        vals = [r[key] for r in reads if isinstance(r.get(key), (int, float))]
        return round(sum(vals) / len(vals), 1) if vals else None
    mods = tool("mods")
    out = {
        "label": a.label, "at": time.strftime("%Y-%m-%d %H:%M"),
        "fps": avg("fps"), "frame_ms": avg("frame_time_ms"), "tick_ms": avg("server_tick_ms"),
        "heap_mb": avg("heap_used_mb"), "chunks": avg("loaded_chunks"), "entities": avg("entity_count"),
        "render_distance": reads[-1].get("render_distance"), "window": reads[-1].get("window"),
        "focused": True, "skipped_unfocused": skipped, "mod_count": len(mods) if isinstance(mods, list) else None,
    }
    print(json.dumps(out))
    if a.label:
        p = ROOT / "docs" / "PERF.md"
        if not p.exists():
            p.write_text("# Perf samples\n\nSame spot, spectator, 16 chunks, DH 128, 1920x1080, vsync off. `scripts/perf-sample.py`.\n\n| Label | When | FPS | Frame ms | Tick ms | Heap MB | Chunks | Entities | Mods |\n|---|---|---|---|---|---|---|---|---|\n", encoding="utf-8")
        with p.open("a", encoding="utf-8") as f:
            f.write(f"| {out['label']} | {out['at']} | {out['fps']} | {out['frame_ms']} | {out['tick_ms']} | {out['heap_mb']} | {out['chunks']} | {out['entities']} | {out['mod_count']} |\n")
