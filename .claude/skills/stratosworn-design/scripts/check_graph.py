#!/usr/bin/env python3
"""Validate a design-board graph before it goes near a script or the board.

Checks, in order of how much they have cost this project already:
  1. Circular dependencies. In a progression graph a cycle means nothing in the loop is
     obtainable. Tier 3 shipped one in draft 2 (burner -> cinder -> fire glyph -> burner).
  2. Dangling edges: an edge naming a node that does not exist.
  3. Single-tree bands: a tier whose gated items all come from one mod. The pack rule is
     that no tier can be finished in one tree.
  4. Item IDs that do not exist in the installed jars. stratosworn:* is allowed (new items
     to be registered) and is reported as such; an unknown namespace means the mod is not
     in the pack.
  5. Shape problems: missing/unknown kind, duplicate ids, nodes with band on a graph where
     others have none.

Usage: python check_graph.py design/graphs/tiers.json [more.json ...]
Exit code 1 if any error; warnings do not fail.
"""
import json, pathlib, sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
from item_ids import load_index, item_exists  # noqa: E402

KINDS = {"create", "ars", "hybrid", "world", "gate"}
TREE_KINDS = {"create", "ars", "hybrid"}


def find_cycles(nodes, edges):
    out = {n: [] for n in nodes}
    for a, b in edges:
        if a in out and b in out:
            out[a].append(b)
    state, stack, cycles = {}, [], []

    def visit(n):
        state[n] = 1; stack.append(n)
        for m in out[n]:
            if state.get(m) == 1:
                cycles.append(stack[stack.index(m):] + [m])
            elif not state.get(m):
                visit(m)
        stack.pop(); state[n] = 2

    for n in nodes:
        if not state.get(n):
            visit(n)
    return cycles


def check(path, index):
    g = json.loads(pathlib.Path(path).read_text(encoding="utf-8"))
    errors, warns, infos = [], [], []
    nodes = g.get("nodes", []); edges = g.get("edges", [])
    ids = [n.get("id") for n in nodes]
    dup = {i for i in ids if ids.count(i) > 1}
    if dup: errors.append(f"duplicate node ids: {sorted(dup)}")
    idset = set(ids)

    for n in nodes:
        if n.get("kind") not in KINDS:
            errors.append(f"node {n.get('id')}: kind '{n.get('kind')}' not in {sorted(KINDS)}")
        if not n.get("label"):
            errors.append(f"node {n.get('id')}: no label")

    for a, b in edges:
        for x in (a, b):
            if x not in idset:
                errors.append(f"edge {a} -> {b}: '{x}' is not a node")

    for cyc in find_cycles(ids, edges):
        errors.append("circular dependency: " + " -> ".join(cyc) + "  (nothing in this loop is obtainable)")

    banded = [n for n in nodes if "band" in n]
    if banded and len(banded) != len(nodes):
        warns.append(f"{len(nodes) - len(banded)} node(s) have no band; they will be laid out in band 0")
    bands = {}
    for n in nodes:
        bands.setdefault(n.get("band", 0), []).append(n)
    for band, members in sorted(bands.items()):
        trees = {n["kind"] for n in members if n.get("kind") in TREE_KINDS}
        if len(members) > 2 and trees and trees <= {"create"} or trees <= {"ars"} and trees:
            if len(members) > 2:
                errors.append(f"band {band}: every gated item is {next(iter(trees))}-only; a tier must need both trees")

    new_items = []
    for n in nodes:
        item = n.get("item")
        if not item or " " in item or "/" in item:
            continue  # descriptive text like "glyph_break / crush", not an id
        ok = item_exists(index, item)
        if item.startswith("stratosworn:"):
            new_items.append(item)
        elif ok is None:
            errors.append(f"node {n['id']}: item '{item}' names a namespace that is not in the pack")
        elif ok is False:
            errors.append(f"node {n['id']}: item '{item}' does not exist in the installed jars (check the exact id with item_ids.py)")
    if new_items:
        infos.append("new items to register via KubeJS startup script: " + ", ".join(sorted(set(new_items))))

    print(f"== {path}: {len(nodes)} nodes, {len(edges)} edges, {len(bands)} band(s)")
    for e in errors: print("  ERROR  " + e)
    for w in warns: print("  warn   " + w)
    for i in infos: print("  info   " + i)
    if not errors: print("  ok")
    return not errors


if __name__ == "__main__":
    paths = sys.argv[1:]
    if not paths:
        print(__doc__); sys.exit(2)
    index = load_index()
    good = all([check(p, index) for p in paths])
    sys.exit(0 if good else 1)
