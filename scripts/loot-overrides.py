#!/usr/bin/env python3
"""Neutralize loot tables that fail to parse because they reference blocks from absent
optional mods (a common pattern in Create addons). Reads a server log, finds every
"Couldn't parse element ... loot_table]:<ns>:<path>" line, and writes an empty block loot
table override to pack/kubejs/data/<ns>/loot_table/<path>.json.

Usage: python scripts/loot-overrides.py [logfile ...]   (default: server/logs/latest.log)
"""
import json, pathlib, re, sys
ROOT = pathlib.Path(__file__).resolve().parent.parent
logs = [pathlib.Path(a) for a in sys.argv[1:]] or [ROOT / "server" / "logs" / "latest.log"]
pat = re.compile(r"Couldn't parse element ResourceKey\[minecraft:root / minecraft:loot_table\]:([a-z0-9_.-]+):([a-z0-9_/.-]+) - Unknown registry key")
ids = set()
for lg in logs:
    if lg.exists():
        ids.update(pat.findall(lg.read_text(encoding="utf-8", errors="replace")))
written = []
for ns, path in sorted(ids):
    out = ROOT / "pack" / "kubejs" / "data" / ns / "loot_table" / (path + ".json")
    if out.exists():
        continue
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps({
        "_comment": f"Override: {ns} ships this loot table for an optional-compat block that is not registered in this pack; the original fails to parse at load.",
        "type": "minecraft:block", "pools": []}, indent=2) + "\n", encoding="utf-8")
    written.append(f"{ns}:{path}")
print(f"{len(ids)} failing loot tables found, {len(written)} overrides written")
for w in written: print("  " + w)
