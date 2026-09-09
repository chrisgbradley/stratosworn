# Changelog

Every mod add, config change, and why. Newest first.

## 0.1.0 (in progress)

- 2026-09-09: Added Sable 2.0.5, Create Aeronautics 1.3.2, Curios 9.5.1, GeckoLib 4.9.2, Ars Nouveau 5.13.1. Each booted green. Ars Nouveau declares Curios and GeckoLib as mandatory even though Modrinth lists no dependencies for it.
- 2026-09-09: Added Create 6.0.10. Server boot green (41s, 0 ERROR). Ponder refmap WARN is expected outside dev.
- 2026-09-09: Local server config: `online-mode=false` so the Gradle dev client can join; view-distance 10; seed `stratosworn`. Local JVM: 6G heap, G1GC (Aikar flags). Production host args are an open question.
- 2026-09-09: Phase 0 telemetry mod builds (`stratosworn_telemetry-0.1.0.jar`). Marked `side="client"`, optional, default off; `scripts/export.sh` removes it from releases.
- 2026-09-09: `packwiz init` — Minecraft 1.21.1, NeoForge 21.1.250 (latest 21.1.x on maven.neoforged.net at time of init).
- 2026-09-09: Installed local dedicated server (NeoForge 21.1.250) into `server/` for the boot test loop.
- 2026-09-09: Chose EMI over JEI. EMI ships release builds for 1.21.1 NeoForge; JEI 19.x is beta-only there.
