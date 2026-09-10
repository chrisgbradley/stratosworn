#!/usr/bin/env python3
"""List the item IDs a mod actually registers, read from the jars installed in server/mods.

Item models are the most reliable static signal: every registered item (block items included)
has assets/<namespace>/models/item/<name>.json. Bundled jar-in-jar mods (Create Aeronautics
ships aeronautics, simulated and offroad inside one jar) are descended into.

Usage:
  python item_ids.py <namespace> [substring]     list IDs in a namespace, optionally filtered
  python item_ids.py --namespaces                list every namespace that has items
  python item_ids.py --refresh                   rebuild the cache (after the pack changes)

The scan is cached in build/item_ids.json because reading ~60 jars takes a few seconds.
"""
import io, json, pathlib, re, sys, zipfile

ROOT = pathlib.Path(__file__).resolve().parents[4]
MODS = ROOT / "server" / "mods"
CACHE = ROOT / "build" / "item_ids.json"
PAT = re.compile(r"^assets/([a-z0-9_.-]+)/models/item/([a-z0-9_./-]+)\.json$")


def scan_jar(zf, out):
    for name in zf.namelist():
        m = PAT.match(name)
        if m:
            out.setdefault(m.group(1), set()).add(m.group(2))
        elif name.startswith("META-INF/jarjar/") and name.endswith(".jar"):
            try:
                with zipfile.ZipFile(io.BytesIO(zf.read(name))) as inner:
                    scan_jar(inner, out)
            except zipfile.BadZipFile:
                pass


def build_index():
    out = {}
    if not MODS.exists():
        sys.exit(f"no server mods folder at {MODS}; run the boot loop once so the pack is installed")
    for jar in sorted(MODS.glob("*.jar")):
        try:
            with zipfile.ZipFile(jar) as zf:
                scan_jar(zf, out)
        except zipfile.BadZipFile:
            continue
    data = {ns: sorted(v) for ns, v in out.items()}
    CACHE.parent.mkdir(parents=True, exist_ok=True)
    CACHE.write_text(json.dumps(data, indent=1), encoding="utf-8")
    return data


def load_index(refresh=False):
    if not refresh and CACHE.exists():
        # rebuild if any jar is newer than the cache
        newest = max((p.stat().st_mtime for p in MODS.glob("*.jar")), default=0)
        if CACHE.stat().st_mtime >= newest:
            return json.loads(CACHE.read_text(encoding="utf-8"))
    return build_index()


def item_exists(index, item_id):
    """True/False, or None when the namespace is not in the pack at all."""
    if ":" not in item_id:
        item_id = "minecraft:" + item_id
    ns, path = item_id.split(":", 1)
    if ns == "minecraft":
        return True  # vanilla items are not in mod jars; trust them
    if ns not in index:
        return None
    return path in index[ns]


if __name__ == "__main__":
    args = sys.argv[1:]
    if not args or args[0] in ("-h", "--help"):
        print(__doc__); sys.exit(0)
    if args[0] == "--refresh":
        idx = build_index(); print(f"indexed {sum(len(v) for v in idx.values())} items in {len(idx)} namespaces"); sys.exit(0)
    idx = load_index()
    if args[0] == "--namespaces":
        for ns in sorted(idx): print(f"{ns:28s} {len(idx[ns])}")
        sys.exit(0)
    ns = args[0]; sub = args[1] if len(args) > 1 else ""
    if ns not in idx:
        sys.exit(f"namespace '{ns}' has no items in the installed pack (mod missing or not yet booted?)")
    for name in idx[ns]:
        if sub in name: print(f"{ns}:{name}")
