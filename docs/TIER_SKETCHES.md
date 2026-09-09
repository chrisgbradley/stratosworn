# Stratosworn — Tier Sketches

Progression ideas supplied by Christian on 2026-09-09. Input for `PROGRESSION.md` (Phase 4).
Not a spec: pick one, or take the spine from one and the flavor from another.

Five ways to cross-gate Create and Ars Nouveau. Each one forces both trees at every
tier.

Common rules for all five:

- No tier can be finished in one tree.
- Remove vanilla enchanting, brewing, elytra, and any third power or flight path.
- Each forced step pays off within an hour of play.
- Verify every named item exists on 1.21.1 NeoForge before you script it.

---

## 1. Material Ladder

**Hook:** Each tier has one key material. Both mods need it. Neither mod can make it alone.

| Tier | Key material | Create side | Ars side |
|---|---|---|---|
| 1 | Source-touched andesite | Andesite alloy needs it | Made by casting a spell on andesite |
| 2 | Arcane brass | Brass needs a source gem in the mixer | Imbuement chamber needs a brass casing |
| 3 | Attuned precision core | Precision mechanism passes through the enchanting apparatus | Tier 3 spellbook needs a mechanical crafter chain |
| 4 | Aether alloy | Aeronautics engine needs it | Only a ritual makes it; ritual needs a Create-fed source engine |

**Why it works:** simple to script and simple to explain. One item per tier.
**Risk:** feels like a checklist. Quests must give it a story.

---

## 2. Power Exchange

**Hook:** Rotation and source are two forms of the same energy. Each tier raises the
exchange rate. You cannot scale one without the other.

| Tier | Exchange | Create side | Ars side |
|---|---|---|---|
| 1 | Hand crank → source | A crank feeds a source jar slowly | Spellbook needs a cog to craft |
| 2 | Source → rotation | A source motor turns a shaft. Only source can run brass machines | Source jars need brass fittings |
| 3 | Steam needs mana | Steam engine needs a source-fed heater | Tier 3 glyphs need steam-pressed parts |
| 4 | Flight needs both | Aeronautics engine burns source and pressure together | Warp glyphs need an engine core |

**Why it works:** the theme lives in the mechanics, not the recipes. Players feel it.
**Risk:** needs a bridge mod or custom blocks. More code. Balance the exchange rate
early or one side becomes free.

---

## 3. Altitude Ladder

**Hook:** The world gates you. Materials sit at heights and distances only reachable
with the last tier's flight or warp. Tectonic and custom biomes carry the design.

| Tier | Reach | Create side | Ars side |
|---|---|---|---|
| 1 | Valleys | Andesite from valley floors | Source berries only grow in valleys |
| 2 | Foothills | Zinc only in foothill ore | Glide glyph needs foothill crystals |
| 3 | Peaks | First balloon needs peak-only silk | Peak shrines hold tier 3 glyphs |
| 4 | Sky islands | Engines need sky-island stone | Warp anchors only work between islands |

**Why it works:** shows off the landscape. Flight is the reward, not a side trip.
**Risk:** worldgen work. Custom biomes must exist and must generate on Tectonic.
Players who cheese travel (boats, horses) skip a tier. Gate travel too.

---

## 4. Hybrid Tools

**Hook:** The two core tools — the wrench and the spellbook — are hybrids. Each tier
upgrades one with the other mod. You cannot use a machine or cast a spell without both.

| Tier | Wrench | Spellbook |
|---|---|---|
| 1 | Needs a novice glyph to craft | Needs a cogwheel to craft |
| 2 | Brass wrench needs an imbued gem | Apprentice book needs a brass hand |
| 3 | Mechanical wrench casts a spell on hit | Archmage book needs a precision mechanism |
| 4 | Aeronautics wrench needs a ritual | Book gains a flight glyph from an engine core |

**Why it works:** cheapest to build. Four recipes per tier. The tool is always in hand,
so the theme is always in view.
**Risk:** thin. Past the tool, players can still live in one tree. Pair it with
sketch 1 or 3 for depth.

---

## 5. Four Elements

**Hook:** Ars has elements. Create has subsystems. Map them. Each tier is one element
and one subsystem, and each needs the other.

| Tier | Element | Create subsystem | The link |
|---|---|---|---|
| 1 | Earth | Drills and belts | Earth glyphs need drill heads. Drills need an earth glyph to craft |
| 2 | Water | Pumps and steam | Water glyphs need pumps. Boilers need water-attuned pipes |
| 3 | Fire | Blaze burners and smelting | Fire glyphs need burner ash. Burners need a fire glyph to light |
| 4 | Air | Aeronautics | Air glyphs need propellers. Engines need an air ritual |

**Why it works:** clean story. Four chapters. Each chapter has a look and a color.
Custom biomes can match (deep caves, wetlands, volcanic, sky).
**Risk:** elements addon must exist on 1.21.1 NeoForge. Verify. Order is fixed, which
limits player choice.

---

## Picking one

- Fast to build: 4, then 1.
- Best theme: 2, then 5.
- Best fit for the landscape goal: 3.
- Best combined: 3 as the spine, 5 for the chapters, 4 for the tools.
