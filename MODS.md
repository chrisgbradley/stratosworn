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
