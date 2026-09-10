#!/usr/bin/env python3
"""Neutralize datapack entries that fail to load because they reference content from absent
optional mods (a common pattern in Create and structure addons). Reads a server log and
writes overrides into pack/kubejs/data:

  - "Couldn't parse element ... loot_table]:<ns>:<path>"  -> empty block loot table
  - "Couldn't load tag <ns>:<path> as it is missing ..."   -> empty tag

Tag overrides need the registry directory, which the log does not give, so it is resolved by
looking the tag path up inside the mod jars the local server has installed.

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
# tags: "Couldn't load tag <ns>:<path> as it is missing following references"
tagpat = re.compile(r"Couldn't load tag ([a-z0-9_.-]+):([a-z0-9_/.-]+) as it is missing")
tags = set()
for lg in logs:
    if lg.exists():
        tags.update(tagpat.findall(lg.read_text(encoding="utf-8", errors="replace")))

def tag_dir(ns, path):
    """Find which tag registry directory this tag lives in, by looking in the server's jars."""
    import zipfile
    for jar in sorted((ROOT / "server" / "mods").glob("*.jar")):
        try:
            with zipfile.ZipFile(jar) as z:
                for n in z.namelist():
                    if n.startswith(f"data/{ns}/tags/") and n.endswith(f"/{path}.json"):
                        return n[len(f"data/{ns}/tags/"):-len(f"/{path}.json")]
        except Exception:
            continue
    return None

written = []
for ns, path in sorted(tags):
    d = tag_dir(ns, path)
    if d is None:
        print(f"  ? {ns}:{path} - could not find its tag directory, skipped")
        continue
    out = ROOT / "pack" / "kubejs" / "data" / ns / "tags" / d / (path + ".json")
    if out.exists():
        continue
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps({
        "_comment": f"Override: {ns} ships this tag referencing content from optional mods this pack does not have.",
        "replace": False, "values": []}, indent=2) + chr(10), encoding="utf-8")
    written.append(f"tag {ns}:{d}/{path}")

for ns, path in sorted(ids):
    out = ROOT / "pack" / "kubejs" / "data" / ns / "loot_table" / (path + ".json")
    if out.exists():
        continue
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps({
        "_comment": f"Override: {ns} ships this loot table for an optional-compat block that is not registered in this pack; the original fails to parse at load.",
        "type": "minecraft:block", "pools": []}, indent=2) + "\n", encoding="utf-8")
    written.append(f"{ns}:{path}")
print(f"{len(ids)} failing loot tables and {len(tags)} failing tags found, {len(written)} overrides written")
for w in written: print("  " + w)
