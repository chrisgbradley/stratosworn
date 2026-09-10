---
name: stratosworn-design
description: Design progression, gates, tiers, skill trees, quest chapters, bounties, and crafting or recipe chains for the Stratosworn modpack, grounded in the pack's real item IDs and rules, validated for cycles and single-tree tiers, and published to the draggable design board. Use this whenever Christian talks about progression, gating, tiers, the Altitude Ladder, Four Elements chapters, the passive skill tree, Pufferfish's Skills, FTB Quests, quest text, bounties, cross-links between Create and Ars, KubeJS recipe changes, PROGRESSION.md, QUESTS.md, or the design board, even when he does not say "design" or ask for a graph.
---

# Stratosworn design work

Stratosworn is a 1.21.1 NeoForge pack where Create (industry) and Ars Nouveau (magic) must
both be used at every tier, and airships are the payoff. Design here means deciding what
gates what, then proving the decision holds before it becomes a script. This skill exists
because the failures in this project have all been of one shape: a rule that reads sensibly
in a table and is impossible or trivial in the game. A circular dependency shipped in
`PROGRESSION.md` draft 2 and was only caught when the structure was drawn as a graph.

## Ground yourself first

Read these before proposing anything; they are the decisions already made:

1. `docs/DIRECTION.md` — the three sets (High Country, Foundry & Grimoire, Sky Trade),
   the rules, and the build order.
2. `PROGRESSION.md` — the current tier design and its gate tables.
3. `docs/TIER_SKETCHES.md` — Christian's five sketches; the common rules at the top are
   binding: no tier finishable in one tree, no vanilla enchanting/brewing/elytra, no third
   power or flight path, every forced step pays off within an hour.
4. `MODS.md` — what is in the pack and, at the bottom, what was rejected and why. A design
   that needs a rejected mod is dead on arrival.
5. `docs/OPEN_QUESTIONS.md` "Answered" section — hard mode, passive tree (Pufferfish's
   Skills), one bridge per cross-link, custom items allowed.
6. `design/graphs/*.json` — the graphs as they stand.

Decisions that shape everything: **hard mode** (recipe gates, not just quests), **passive
tree** for the RPG layer (not skill levels), **one bridge mod per link** (Ars Creo is in;
Create Ars Nouveau is not), and **custom content is allowed** in this order of preference:
datapack JSON, then KubeJS startup scripts for `stratosworn:*` items, then a small content
mod only when neither can do it.

## Use real IDs, not remembered ones

Item names in this pack are checked against the jars, not memory. Mod names drift
(`create:brass_casing` exists, `create:brass_gear` does not, and `create:brass_block` does even
though it sounds unlikely; Aeronautics items live under `aeronautics:`, `simulated:` and
`offroad:`, not `create_aeronautics:`). The first draft of this very skill got one of those
wrong from memory, which is the point.

- Static, always available: `python .claude/skills/stratosworn-design/scripts/item_ids.py <namespace> [filter]`
  lists what a mod registers, reading the installed jars. `--namespaces` lists the mods.
- Live, when the client is running: the `mc` MCP server's `recipe_lookup` gives real
  recipes and types, which is what a KubeJS change needs. If `mc` is disconnected, the game
  is not running; say so rather than guessing a recipe shape.

New `stratosworn:*` items are legitimate (source-touched andesite, attuned core, aether
alloy); mark them as new so the KubeJS registration list stays complete.

## Work in the graph, then the prose

The graph is the source of truth for structure; `PROGRESSION.md` carries the prose. Edit
`design/graphs/<id>.json` (schema in `references/formats.md`), then:

```bash
python .claude/skills/stratosworn-design/scripts/check_graph.py design/graphs/tiers.json
```

It fails on cycles, dangling edges, single-tree bands, and item ids that do not exist, and
lists the `stratosworn:*` items to register. Do not publish or write recipes from a graph
that does not pass. Keep node ids stable once Christian has arranged them; add nodes rather
than renaming, because his positions and notes are keyed by id.

For any non-trivial change, run the adversarial pass in `references/failure-modes.md`:
three subagents (Create specialist, Ars specialist, balance skeptic) on the same tier, fresh
contexts, concrete item ids, one written resolution per objection. This is the step that
catches what the author stopped seeing.

## Read Christian back before you revise

His arrangement and notes live in the board's database, not in git. Before changing a graph
he has worked on, read his notes:

- `Artifact` tool, `action: "read_db"`, `db_op: "get"`, `collection: "notes"`, `doc_id: "<graph id>"`

Treat every note as a review comment to answer in the revision. Never write to `layouts`.

## Publish and record

```bash
python .claude/skills/stratosworn-design/scripts/build_board.py --check
```

Then publish `build/board.html` with the `Artifact` tool using
`url: https://claude.ai/code/artifact/110a9c45-c9ad-4f22-ab2f-18cf88e89b0f` so it updates in
place (omit `favicon`; leave `capabilities` unset to carry `db` and `downloads` forward).
Read the artifact first if this conversation has not published it yet; the tool refuses
blind publishes.

Then, in the same commit: the matching `PROGRESSION.md` rows, a `CHANGELOG.md` line saying
what changed and why, and any item registrations added to the KubeJS list. Commit messages
follow the repo's pattern (`docs: PROGRESSION draft N ...`).

## What good output looks like

A design change is done when all of these are true:

- The graph passes `check_graph.py` with no errors.
- Each new gate names its payoff and roughly when the player reaches it.
- Every item id in it exists in the jars or is listed as a new `stratosworn:*` item.
- The board is republished and Christian's notes that prompted the change are answered.
- `PROGRESSION.md` and `CHANGELOG.md` say the same thing the graph says.

When a gate cannot be made to work under the rules, say that plainly and propose the
nearest gate that does, rather than bending a rule quietly. Christian would rather hear
"tier 3 cannot gate on burners without a world source for Ignite" than find it in play.

## Reference files

- `references/formats.md` — board graph schema, artifact database layout, PROGRESSION row
  format, and what is known about Pufferfish's Skills and FTB Quests file formats (with the
  instruction to read the mods' generated examples before emitting any).
- `references/failure-modes.md` — the nine ways a gate design fails here and how to run the
  three-agent adversarial pass.
- `scripts/item_ids.py`, `scripts/check_graph.py`, `scripts/build_board.py` — described above.
