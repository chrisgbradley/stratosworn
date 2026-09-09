# Changelog

Every mod add, config change, and why. Newest first.

## 0.1.0 (in progress)

- 2026-09-09: Shipped `config/DistantHorizons.toml` with `lodChunkRenderDistanceRadius = 128` (DH default is 256). Matches the brief's target of 16 render + DH 128 and halves LOD memory and generation load.
- 2026-09-09: Added Distant Horizons 3.2.0-b, `side = "client"` (packwiz defaulted to both because Modrinth marks it optional/optional). Loaded on the dev client alongside Create, Aeronautics, Ars, Tectonic, Terralith; joined the local server clean.
- 2026-09-09: Local server: `ops.json` grants the dev client's offline player `Dev` op so `run_command` and remote tick sampling work; RCON enabled on 25575 (local password in `server.properties`, git-ignored) so `scripts/rcon.py stop` can stop the detached server.
- 2026-09-09: Telemetry `recipe_lookup` now classifies makes/uses from the vanilla result item when known, and parses only result-keyed JSON for modded recipes. Previously a brass-nugget recipe was filed under brass-ingot makes.
- 2026-09-09: Added Terralith 2.5.8 on top of Tectonic. Boot green. Terralith logs axolotl mob-category WARNs for its water biomes; known upstream noise, not an error.
- 2026-09-09: Telemetry metafile moved out of the pack to `docs/stratosworn-telemetry.pw.toml.template`. packwiz-installer's CLI mode auto-accepts optional files and then fails on the placeholder URL, which broke client installs. The dev client loads telemetry from the Gradle classpath instead. Restore the metafile with a real release URL once the repo has one.
- 2026-09-09: Added EMI 1.1.24, then Tectonic 3.0.26 (+ Lithostitched dependency). Both boot green. Tectonic alone first; Terralith pairing tested next.
- 2026-09-09: Added Sable 2.0.5, Create Aeronautics 1.3.2, Curios 9.5.1, GeckoLib 4.9.2, Ars Nouveau 5.13.1. Each booted green. Ars Nouveau declares Curios and GeckoLib as mandatory even though Modrinth lists no dependencies for it.
- 2026-09-09: Added Create 6.0.10. Server boot green (41s, 0 ERROR). Ponder refmap WARN is expected outside dev.
- 2026-09-09: Local server config: `online-mode=false` so the Gradle dev client can join; view-distance 10; seed `stratosworn`. Local JVM: 6G heap, G1GC (Aikar flags). Production host args are an open question.
- 2026-09-09: Phase 0 telemetry mod builds (`stratosworn_telemetry-0.1.0.jar`). Marked `side="client"`, optional, default off; `scripts/export.sh` removes it from releases. Metafile lives in docs/ as a template until a release URL exists.
- 2026-09-09: `packwiz init` — Minecraft 1.21.1, NeoForge 21.1.250 (latest 21.1.x on maven.neoforged.net at time of init).
- 2026-09-09: Installed local dedicated server (NeoForge 21.1.250) into `server/` for the boot test loop.
- 2026-09-09: Chose EMI over JEI. EMI ships release builds for 1.21.1 NeoForge; JEI 19.x is beta-only there.
