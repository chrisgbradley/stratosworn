# Formats the design work reads and writes

## Board graph JSON (`design/graphs/<id>.json`)

```json
{
  "id": "tiers",
  "title": "Progression tiers",
  "description": "One or two sentences shown in the inspector when nothing is selected.",
  "nodes": [
    { "id": "brass", "label": "Arcane brass", "kind": "hybrid",
      "item": "create:brass_ingot", "note": "Mixer recipe gains a source gem.", "band": 2 }
  ],
  "edges": [ ["source_gem", "brass"], ["brass", "brass_casing"] ]
}
```

- `id`: stable, lowercase, never renamed once Christian has arranged it (positions and notes
  are keyed by it in the database).
- `kind`: `create` (brass stripe), `ars` (violet), `hybrid` (teal: an item both trees make or
  need, usually a `stratosworn:*` item), `world` (blue: tiers, biomes, structures), `gate`
  (red: an open question or a payoff).
- `item`: a real item id when the node is one item. Free text with spaces or `/` is allowed
  for a family ("glyph_break / crush / fell") and is skipped by validation.
- `note`: the design intent. Christian's own notes are separate and live in the database.
- `band`: the layout row. For the tier graph, band = tier. Graphs without bands lay out as
  one layered flow.
- `edges`: `[from, to]` meaning *to depends on from*. A cycle is an error.

## Artifact database layout

Artifact: https://claude.ai/code/artifact/110a9c45-c9ad-4f22-ab2f-18cf88e89b0f
Capabilities: `db`, `downloads` (carry forward; do not pass `capabilities` on republish).

| Collection | Doc id | Written by | Content |
|---|---|---|---|
| `layouts` | graph id | the page, on drag | `{ positions: { nodeId: {x, y} }, at }` |
| `notes`   | graph id | the page, on typing | `{ notes: { nodeId: "text" }, at }` |

Read before revising: `Artifact` tool, `action: "read_db"`, `db_op: "get"`, `collection: "notes"`,
`doc_id: "tiers"` (and `layouts` if you need to know what he has arranged). Never write
`layouts`. A node that is added gets an automatic position; a node that is renamed loses its
position, so add a new node instead of renaming.

## PROGRESSION.md gate row

```
| <Gate name> | <Create side> | <Ars side> | <Item IDs> |
```

One row per gate, inside the tier's table. Item IDs in backticks. When a row changes because
the graph changed, change both in the same commit; the graph is the source of truth for
structure, the doc for prose.

## Pufferfish's Skills (passive tree) — verify before emitting

Pack root: `config/puffish_skills/categories/<category>/`. The mod generates example files
on first run; **read those from `server/config` or `telemetry/run/config` before writing
any**, because the exact keys are the mod's, not this note's. What is known to be there:

- `category.json` — title, icon, background, unlock/points rules.
- `definitions.json` — reusable node definitions: title, description, icon, cost, rewards
  (attribute modifiers, `puffish_skills:command`-style rewards that can grant items or run
  KubeJS-facing commands).
- `skills.json` — node instances: `{ "<id>": { "x": 0, "y": 0, "definition": "<def id>", "root": true } }`.
  These `x`/`y` are what the board exports; the board's coordinates need scaling to the
  mod's units (check an example file for the range it uses).
- `connections.json` — `{ "normal": [["a","b"], ...] }` plus optional exclusive groups.

Node = board node. Connection = board edge. The board's `kind` maps to the category's icon
and colour, not to any mod field.

## FTB Quests (chapters) — verify before emitting

Pack root: `config/ftbquests/quests/`. Chapters are SNBT files in `chapters/`, one per
chapter, each quest with `x` and `y` (doubles, one unit = one quest cell) and a
`dependencies` list of quest ids. Quest ids are 16-hex strings the mod assigns; when
generating, assign stable ids yourself and keep a map in `design/quest-ids.json` so a
regenerate does not orphan player progress. Chapter groups live in `chapter_groups.snbt`.
Write quest text in `QUESTS.md` first; the SNBT is generated from it, never hand-edited.

The telemetry `quest_state` tool reads chapters and completion from the running client and
is the check that a generated chapter loaded at all.

## KubeJS recipe changes

Server scripts in `pack/kubejs/server_scripts/`, startup scripts (item registration) in
`pack/kubejs/startup_scripts/`. Overrides of other mods' broken data go in
`pack/kubejs/data/` (see `scripts/loot-overrides.py` at the repo root for the pattern).
Every recipe a gate changes must first be looked up with the telemetry `recipe_lookup`
tool (game running) or `item_ids.py` (static) so the id and recipe type are real.
