# Mods
One line per mod. Side is what packwiz installs: `client`, `server`, or `both`.
Versions are the verified 1.21.1 NeoForge builds on Modrinth unless noted.
| Mod | Version | Side | Why |
|---|---|---|---|
| Create | 6.0.10 (create-1.21.1-6.0.10.jar) | both | The industrial tree. Rotation, contraptions, brass, precision. Aeronautics depends on it. |
| Sable | 2.0.5 (sable-neoforge-1.21.1-2.0.5.jar) | both | Physics engine under Create Aeronautics. Embeds Veil. |
| Create Aeronautics | 1.3.2 (create-aeronautics-bundled-1.21.1-1.3.2.jar) | both | Physics airships. The reason the pack is on 1.21.1 NeoForge. Needs Create and Sable. |
| Curios API | 9.5.1+1.21.1 | both | Dependency of Ars Nouveau (equipment slots). |
| GeckoLib | 4.9.2 | both | Dependency of Ars Nouveau (animated models). |
| Ars Nouveau | 5.13.1 (ars_nouveau-1.21.1-5.13.1.jar) | both | The magic tree. Glyphs, source, rituals. |
| EMI | 1.1.24 (emi-1.1.24+1.21.1+neoforge.jar) | both | Recipe viewer. Release builds on 1.21.1 NeoForge; JEI is beta-only there. |
| Lithostitched | (pulled by Tectonic) | both | Worldgen library Tectonic depends on. Version pinned in `pack/mods/lithostitched.pw.toml`. |
| Tectonic | 3.0.26 | both | Terrain with real height. Tested alone first per the brief. |
| Distant Horizons | 3.2.0-b | client | Long-range LODs. The look the brief asks for. Client only; server skips it. |
| Sodium | 0.8.13 (mc1.21.1-0.8.13-neoforge) | client | Renderer. Client ran clean alongside Sable/Veil, Aeronautics, DH. Perf table in `docs/PERF.md`. |
| Iris | 1.8.14-beta.1 | client | Shader loader on Sodium. Beta is the only 1.21.1 NeoForge line. Ran clean; 261 FPS vs 320 without it, no shader pack loaded. |
| FerriteCore | 7.0.3 | both | Memory. Client heap 1361 MB → 1068 MB at the sample spot, FPS unchanged. Server boot green. |
| ModernFix | 5.27.24 | both | Load time and memory. Client load 58 s to in-game per its own log. Server boot green. |
| Entity Culling | 1.10.5 | client | Skips rendering entities behind blocks. Neutral at the sample spot (85 entities); matters in machine halls and towns. Ran clean. |
| ImmediatelyFast | 1.6.13 | client | Batches HUD, text, and entity draw calls. Neutral at the sample spot; helps with EMI panels and dense text. Ran clean. |
| spark | 1.10.124 | server | Profiler for the 20-chunks-of-machines test. Server boot green; no client change. |
| Noisium | 2.3.0 | server | Faster worldgen noise. Tectonic and Terralith are noise-heavy. Boot green. |
| Chunky | 1.4.23 | server | World pregeneration so runtime chunk gen is rare on the fixed 4-player world. Boot green. |
| Lithium | 0.15.4 | both | General tick optimization. Boot green. |
| Create: Copycats+ | 3.0.9 | both | Copycat hull blocks; every community airship schematic uses them. Ships a bad Sable physics file for `copycat_catwalk`; overridden in `pack/kubejs/data`. |
| Create Deco | 2.1.3 | both | Industrial decor: catwalks, windows, lamps. `placard` recipe uses a 1.21.2 ingredient form; overridden in `pack/kubejs/data`. |
| KubeJS | 2101.7.2 | both | Scripts and the pack's datapack folder (`pack/kubejs/data`). Phase 4. Pulls Rhino and Better Advanced Tooltips. |
| Create Enchantment Industry | 2.5.3b | both | Enchanting as a Create process. Replaces vanilla enchanting per the tier rules. |
| Ars Creo | 5.4.0 | both | The Create and Ars bridge. One bridge per cross-link. |
| Create: Dragons Plus | 1.11.8b | both | Dependency of Create Enchantment Industry (auto-added by packwiz). |
| Mechanicals Lib | (pulled by Enchantment Industry) | both | Library dependency of Create Enchantment Industry. |

## Rejected after testing

- Ars Ocultas 2.6.1: requires Occultism (excluded second magic path).
- Ars Additions 21.3.0: manifest is 1.21.0-only ([1.21,1.21.1)); breaks Create block registration on 1.21.1.
- Aeronautics: Simulated Copycats 1.3.2: with the ModernFix mixin fixed it still fails, now on the server, with Create's `AllAdvancements` hitting an unbound `create:chocolate_bucket`. Genuine load-order conflict with Create 6.0.10. See `docs/DEV_CLIENT_REGISTRATE.md`.
- C2ME 0.4.0-alpha: server ERRORs locally, and upstream reports chunk loading freezing with Sable physics ships (closed as not planned). See `docs/SABLE_CONFLICTS.md`.
- Moonrise 0.1.0-beta.15: crashes Sable's explosion mixin at boot; known open upstream issue with no owner. See `docs/SABLE_CONFLICTS.md`.
- ScalableLux 0.3.0-alpha: Sable declares it incompatible; both replace light storage. See `docs/SABLE_CONFLICTS.md`.
| Create Connected | 1.3.3 | both | Kinetic parts that finish contraption logistics. Dye Depot compat loot tables overridden. |
| Create Encased | 1.9.0 | both | Encased cogs and shafts. Slicer compat loot tables overridden. |
| Not Enough Glyphs | 4.6.1 | both | Enough glyphs that each chapter has its own. |
| Separate Sable Render Distance | 1.8.6 | client | Airships render out to Distant Horizons range. Requires Sodium, so client-only. High Country must-have. |
| Ars Elemental | 0.7.10.1 (CurseForge; no Modrinth build for 1.21.1) | both | Element-school glyphs and spells. The Four Elements chapters depend on it. |
| FTB Library | (CurseForge) | both | Base library for the FTB suite. Phase 5. |
| architectury-api | (auto-added with ftb-library-forge) | both | Dependency. |
| FTB Teams | (CurseForge) | both | Team scoping for quest progress. Phase 5. |
| FTB Quests | (CurseForge) | both | The quest book. Phase 5. |
| ProbeJS | 8.0.3 | client | Typings for KubeJS scripts. Dev-side aid; harmless in a release. |
| Ars Elemancy | 1.17 | both | Element-flavored spells on Ars Elemental. Four Elements chapters. |
| Create Mechanical Extruder | 2.2.2 | both | Stone generation a water glyph can feed. |
| Steam 'n' Rails (NeoForge port) | 0.3.0-beta.2 | both | Ground freight for the valley tier, before flight exists. Community port, beta. |
| Towns and Towers | 1.13.11 | both | Villages worth flying to. Sky Trade destinations. |
| cristel-lib | (auto-added with towns-and-towers) | both | Dependency. |
