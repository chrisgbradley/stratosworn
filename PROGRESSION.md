# Progression (draft 2)

Spine: **Altitude Ladder** (tier sketch 3). Chapters: **Four Elements** (sketch 5). Tools:
**Hybrid Tools** (sketch 4). Mod sets: High Country plus Foundry & Grimoire. RPG spine is
an open question (see `docs/OPEN_QUESTIONS.md`); this doc gates only through recipes.

Rules carried over from the tier sketches:

- No tier can be finished in one tree.
- Vanilla enchanting, brewing, elytra go. Create Enchantment Industry owns enchanting.
- No third power path and no third flight path.
- Every forced step pays off within an hour of play.
- Every item ID below gets checked with the telemetry `recipe_lookup` tool before a script
  references it. IDs come from the jars in the pack as of draft 2; `stratosworn:*` items do not exist yet.

## Tier 1 — Valleys · Earth · drills and belts

You start on the valley floor. Andesite and source berries are here and nowhere else.

| Gate | Create side | Ars side | Item IDs |
|---|---|---|---|
| Wrench needs a glyph | `create:wrench` recipe takes a novice glyph in place of one andesite alloy | | `create:wrench`, `ars_nouveau:glyph_break` |
| Spellbook needs a cog | | `ars_nouveau:novice_spell_book` recipe takes a `create:cogwheel` | |
| Source-touched andesite | `create:andesite_alloy` needs andesite that has been hit by an Earth-tier spell (Ars imbuement of `minecraft:andesite` → `stratosworn:source_touched_andesite`) | Imbuement chamber recipe: andesite + source → source-touched andesite | `ars_nouveau:imbuement_chamber` |
| Earth glyphs need drill heads | | Earth-school glyphs (`glyph_break`, `glyph_crush`, `glyph_fell`, `glyph_harvest`) cost a `create:mechanical_drill` in the scribe's table recipe | `ars_nouveau:scribes_table` |

Payoff: a belt-fed drill line and a spellbook with Break. Both within the first hour.

## Tier 2 — Foothills · Water · pumps and steam

Zinc is only in foothill ore. Foothills are cold enough to need Cold Sweat gear, which is
the travel gate. Boats are locked behind this tier.

| Gate | Create side | Ars side | Item IDs |
|---|---|---|---|
| Arcane brass | `create:brass_ingot` mixer recipe needs a `ars_nouveau:source_gem` alongside copper and zinc | | `create:brass_ingot`, `ars_nouveau:source_gem` |
| Imbuement needs brass | | `ars_nouveau:imbuement_chamber` upgrade / `ars_nouveau:source_jar` needs a `create:brass_casing` | |
| Water glyphs need pumps | | Water-school glyphs (`glyph_conjure_water`, `glyph_bubble`, `glyph_freeze`, `glyph_evaporate`) cost a `create:mechanical_pump` | |
| Boilers need attuned pipes | `create:steam_engine` and `create:fluid_tank` recipes take a water-attuned pipe (Ars enchanting apparatus recipe on `create:fluid_pipe`) | | `ars_nouveau:enchanting_apparatus` |
| Apprentice book needs a brass hand | | `ars_nouveau:apprentice_spell_book` takes `create:brass_hand` | |

Payoff: brass machines and a source jar network. First glide glyph opens the peaks.

## Tier 3 — Peaks · Fire · blaze burners and smelting

Peak shrines hold the tier-3 glyphs. The first balloon envelope (`aeronautics:white_envelope`) needs peak-only silk (a
datapack loot table on peak structures).

| Gate | Create side | Ars side | Item IDs |
|---|---|---|---|
| Attuned precision core | `create:precision_mechanism` passes through the enchanting apparatus with fire essence to become `stratosworn:attuned_core`; Aeronautics parts take the attuned core | | `create:precision_mechanism` |
| Fire glyphs need burner ash | | Fire-school glyphs (`glyph_ignite`, `glyph_flare`, `glyph_smelt`, `glyph_explosion`) cost `create:cinder_flour` (blaze burner byproduct, verified) | |
| Burners need a fire glyph | `create:blaze_burner` needs an Ars Ignite glyph in the recipe | | |
| Archmage book needs a precision core | | `ars_nouveau:archmage_spell_book` takes the attuned core | |
| Mechanical wrench | A brass-tier wrench that casts a bound spell on hit (KubeJS item + Ars API, or Ars Creo if it ships one) | | Ars Creo not yet in the pack; check its item list when added |

Payoff: steam power at scale and the first balloon.

## Tier 4 — Sky islands · Air · Aeronautics

Engines need sky-island stone. Waystones only link islands. Warp is the Ars answer to the
same problem.

| Gate | Create side | Ars side | Item IDs |
|---|---|---|---|
| Aether alloy | `simulated:engine_assembly` (via `simulated:incomplete_engine_assembly`) needs `stratosworn:aether_alloy` | Only a ritual makes aether alloy; the ritual's source feed must come from a Create-driven source engine (Ars Creo source generator?) | `aeronautics:*`, `simulated:*` |
| Air glyphs need propellers | | Air-school glyphs (`glyph_gust`, `glyph_wind_shear`, `glyph_wind_burst`, `glyph_launch`, `glyph_glide`) cost `aeronautics:wooden_propeller` (tier 4: `aeronautics:smart_propeller`) | |
| Engines need an air ritual | `simulated:gyroscopic_mechanism` needs a ritual output item; `aeronautics:levitite` (sky-island stone) is the ritual reagent | | |
| Flight glyph from engine core | | `glyph_glide` and `glyph_launch` need `simulated:engine_assembly` in the scribe's table | |

Payoff: an engine-driven airship and warp between islands.

## Cross-links to verify with `recipe_lookup`

- Does Ars Creo add a source-to-rotation block (source motor)? If so it is the Tier 2
  power exchange and no custom block is needed.
- Does Create Ars Nouveau (the second bridge) duplicate any of the above? Use one per link.
- Aeronautics IDs (from the 1.3.2 bundle): `aeronautics:wooden_propeller`, `andesite_propeller`, `smart_propeller`, `propeller_bearing`, `gyroscopic_propeller_bearing`, `*_envelope`, `steam_vent`, `adjustable_burner`, `levitite`, `pearlescent_levitite`; `simulated:engine_assembly`, `gyroscopic_mechanism`, `physics_assembler`, `*_portable_engine`, `white_symmetric_sail`, `navigation_table`; `offroad:*` wheels. Create itself ships `create:propeller`.
- Ars glyph item IDs per school.

## Ore and ingot dedup

One copper (`minecraft:copper_ingot`), one zinc (`create:zinc_ingot`). Unify via KubeJS
tag rewrites once Foundry & Grimoire addons are in and any duplicate ores show up.
