#!/usr/bin/env python3
"""Assemble the design board page from the template and the graph data files.

  design/board.template.html   the page (interaction, layout, persistence) - rarely changes
  design/graphs/*.json         one graph per file - this is what design work edits
  build/board.html             output, publish this with the Artifact tool

Graph structure lives here in git. Christian's node positions and notes live in the
artifact's database (collections `layouts` and `notes`), so republishing a new structure
never disturbs his arrangement. Read his notes back with the Artifact tool's read_db before
revising a graph.

Usage: python build_board.py            (then publish build/board.html)
       python build_board.py --check    (validate every graph first; stops on errors)
"""
import json, pathlib, subprocess, sys

ROOT = pathlib.Path(__file__).resolve().parents[4]
TEMPLATE = ROOT / "design" / "board.template.html"
GRAPHS = ROOT / "design" / "graphs"
OUT = ROOT / "build" / "board.html"
ARTIFACT_URL = "https://claude.ai/code/artifact/110a9c45-c9ad-4f22-ab2f-18cf88e89b0f"
# tabs appear in this order; anything not listed follows alphabetically
ORDER = ["tiers", "crosslinks", "chapters", "skilltree", "bounties"]

files = sorted(GRAPHS.glob("*.json"), key=lambda p: (ORDER.index(p.stem) if p.stem in ORDER else 99, p.stem))
if not files:
    sys.exit(f"no graphs in {GRAPHS}")

if "--check" in sys.argv:
    r = subprocess.run([sys.executable, str(pathlib.Path(__file__).with_name("check_graph.py")), *map(str, files)])
    if r.returncode:
        sys.exit("fix the errors above before building")

graphs = {}
for f in files:
    g = json.loads(f.read_text(encoding="utf-8"))
    gid = g.pop("id", f.stem)
    graphs[gid] = g

tpl = TEMPLATE.read_text(encoding="utf-8")
start = tpl.index("/*GRAPHS*/"); end = tpl.index("/*END GRAPHS*/") + len("/*END GRAPHS*/")
page = tpl[:start] + json.dumps(graphs, ensure_ascii=False, indent=2) + tpl[end:]
OUT.parent.mkdir(parents=True, exist_ok=True)
OUT.write_text(page, encoding="utf-8")
print(f"built {OUT} with {len(graphs)} graph(s): {', '.join(graphs)}")
print(f"publish: Artifact tool, file_path={OUT}, url={ARTIFACT_URL}  (omit favicon; keep capabilities)")
