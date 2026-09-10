# Direction

Christian's picks from the mod-set proposals (2026-09-09): **High Country** as the spine,
**Foundry & Grimoire** for the two trees, and **Sky Trade** for the economy. Plus an RPG
layer (Path of Exile passive tree or RuneScape skills; one, not both).

They do not conflict. They answer three different questions:

| Question | Set | What it adds |
|---|---|---|
| Where can I go? | High Country | Altitude tiers, cold as the travel gate, custom biomes |
| What can I build? | Foundry & Grimoire | Create and Ars addons, a bridge each way |
| Why do I build it? | Sky Trade | Coins, contracts, towns that pay for cargo |

## How they lock together

- **Tiers are trade routes.** Valley goods sell in the foothills, foothill goods on the
  peaks, peak goods on the islands. Each altitude tier opens a market the tier below
  cannot reach. Flight is the reward because it is the only way to move the top goods.
- **Trains are the ground economy.** Steam 'n' Rails carries bulk between valley towns
  before flight exists. Airships take over for anything that crosses an altitude tier.
  That gives the two Create transport systems distinct jobs instead of overlapping.
- **Foundry output is the traded good.** Create processing and Ars imbuement make the
  items towns want (brass, source gems, cooked food, attuned parts). The bridge recipes
  in `PROGRESSION.md` are also the high-value trade goods.
- **Coins are a sink, not a source.** Numismatics currency buys tier access (waystone
  links, town permits, contracts), not raw materials. Nothing that a machine can make
  should be cheaper to buy.
- **The RPG layer gates both.** A passive tree node or a skill level is what lets you
  place the machine, bind the glyph, or accept the contract.

## What needs planning before scripts

1. **Server load policy.** Trains, chunk loaders (Create Power Loader), and structures
   all cost ticks. Chunk loading is quest-gated and capped per team. spark profile with
   the brief's 20 chunks of machines plus 3 airships is the gate for shipping any of it.
2. **Price table.** One spreadsheet: every traded good, its tier, buy and sell price,
   which town type wants it. Written before any Numismatics config.
3. **Town types per tier.** Towns and Towers plus IDAS structures assigned to altitude
   bands, so a "peak town" is a real thing the datapack places.
4. **Contract source.** Bountiful boards in towns, or FTB Quests reward tables. Pick one.
5. **RPG spine.** Passive tree or skill levels. Decides whether gates are nodes or levels.

## Build order

Skeleton (done) → world (Tectonic, Terralith, DH, custom biomes) → perf floor (in progress)
→ Foundry & Grimoire addons → Sky Trade mods → progression scripts → quests → ship.
