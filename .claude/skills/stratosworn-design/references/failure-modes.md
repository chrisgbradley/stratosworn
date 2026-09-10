# Failure modes a gate design has to survive

Use this as the checklist for the adversarial pass. Each entry names the failure, why it
happens to reasonable-looking designs, and how to check for it. Order is by how often it
has actually happened in this project.

## 1. Circular dependency
A needs B, B needs C, C needs A. Nothing in the loop can ever be made. It reads as two or
three individually sensible rules ("burners need a fire glyph", "fire glyphs need burner
ash"), which is why tables hide it. **Check:** `check_graph.py`; the board draws these red.
**Fix pattern:** one item in the loop gets a non-crafting source (world loot, a shrine, a
starter kit, a villager trade).

## 2. Dead-end resource
An item is obtainable only from a source that itself sits behind the item, or only in a
biome or altitude the player cannot reach at that tier. Not a graph cycle, but the same
result. **Check:** for every gate item, name where its *first* copy comes from and which
tier that source is in.

## 3. Single-tree tier
A tier whose gates can all be cleared inside Create alone or Ars alone. Breaks the pack's
first rule. **Check:** `check_graph.py` flags a band with one tree; also read each tier's
rows and ask "could a player who refuses to touch the other mod finish this?"

## 4. Third path
A recipe change that quietly opens Forge Energy, a second flight method, or a second magic
school (boats past the foothills count as travel cheese). **Check:** every new mod or
recipe against `docs/DIRECTION.md` rules and the rejected list in `MODS.md`.

## 5. Payoff too far away
A forced step that does not give the player something usable within about an hour. Gates
that only exist to be gates make the pack feel like a checklist (tier sketch 1's own stated
risk). **Check:** for each gate, name the payoff and estimate the hour.

## 6. Unverified id
A recipe or script referencing an item that is not spelled exactly as the mod registers it
(`create:andesite_alloy` yes; `create:andesite_ingot` no). Silent failure at load, or a
recipe that never appears. **Check:** `item_ids.py` statically, `recipe_lookup` live.

## 7. Bridge duplication
Two bridge mods (Ars Creo, Create Ars Nouveau) both offering the same cross-link, so the
gate has a free bypass. Rule: one bridge per link. **Check:** when a gate uses a bridge
recipe, confirm the other bridge is not in the pack or does not offer the same conversion.

## 8. Server cost hidden in a gate
A gate that rewards or requires chunk loaders, many contraptions, or large physics hulls
early. Four community airships cost 25 ms/tick on the dev box (`docs/PERF.md`). **Check:**
anything that multiplies loaded contraptions or physics objects gets a cap or a later tier.

## 9. Upstream data you did not read
A mod's own recipe or tag is already broken on 1.21.1 (Create Deco's placard, Copycats+'s
catwalk physics file). Designing on top of it inherits the break. **Check:** the boot log
of the current pack is clean; if a design touches a mod's recipe, open the real JSON in the
jar first.

## Running the pass
Spawn three subagents on the same tier text with these roles, each told to argue for its
side and to name concrete item ids, then converge on a written resolution per objection:
- **Create specialist**: is every Create step real, reachable, and worth doing?
- **Ars specialist**: same for Ars; does the source economy support the demands?
- **Balance skeptic**: hunts items 1 to 9 above and asks how a player would cheese it.
Fresh contexts matter: an agent that did not write the design will call out the loop the
author stopped seeing.
