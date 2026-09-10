# Open questions for Christian

From the brief, plus what came up while building. Answers change what gets built next.

## Answered

- **JVM (2026-09-10):** G1GC, 8 to 12 GB heap, on both client and server. ZGC not tested and not needed.
- **Terrain (2026-09-10):** Tectonic plus Terralith.

## From the brief

1. **Server host and CPU/RAM budget.** Sets the production JVM args and the mod count.
   Local test server runs 6G heap, G1GC, on the Ryzen 2700X / 32 GB dev box.
2. **Player count.** Sets the target TPS load. Assumed 4 for now (`max-players=4` locally).
3. **Shaders in the release pack, or leave that to the player.** Iris is in the perf floor
   either way; the question is whether a shader pack ships and is on by default.
4. **Hard mode (recipe gates) or soft mode (quests only).** The tier sketches assume hard.

## Raised while building

5. **GitHub repo for the pack.** The telemetry mod needs a hosted jar for a real packwiz
   metafile (`docs/stratosworn-telemetry.pw.toml.template`). A GitHub release solves it.
   Also lets `packwiz-installer` on the server pull from a URL instead of a local path.
6. **Prism Launcher.** Not installed. The dev client (`gradlew runClientJoin`) covers the
   test loop, but the release test needs a real launcher install. Prism is the cleanest
   packwiz host. Install it, or the vanilla launcher gets a NeoForge profile plus a manual
   `packwiz-installer` step.
7. **RPG spine.** Passive tree (Pufferfish's Skills, Path of Exile feel) or skill levels
   (Project MMO, RuneScape feel). One, not both. See the RPG layer in the mod-set artifact.
8. ~~Tectonic alone vs Tectonic + Terralith.~~ Answered: both.
9. **Bridge mod.** Ars Creo and Create Ars Nouveau both exist. Each cross-link recipe should
   use one, not both.
10. **Disk.** C: is at 98% (about 10 GB free). Gradle caches, the server, and client assets
    are already on it. A second launcher install or a spark profile dump could run it out.
