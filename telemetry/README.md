# Stratosworn Telemetry

Client-only NeoForge 1.21.1 mod. Hosts an MCP server (Streamable HTTP) on
`http://127.0.0.1:25590/mcp` using only `com.sun.net.httpserver` and the Gson that ships
with Minecraft. Every game read runs on the client thread through `Minecraft.submit`.

Dev tool only. `scripts/export.sh` strips it from release builds. The pack references the built jar
through `pack/mods/stratosworn-telemetry.pw.toml`, served by `packwiz serve` from `pack/mods/`
(the jar itself is git-ignored and `.packwizignore`d). Rebuild, copy the jar to `pack/mods/`, and
update the sha256 in the metafile after any change; `scripts/build-telemetry.sh` does all three.

## Build

```bash
cd telemetry && ./gradlew build
```

Jar lands in `telemetry/build/libs/stratosworn_telemetry-<version>.jar`.

## Run a dev client

```bash
cd telemetry && ./gradlew runClient
```

The run directory is `telemetry/run/`. Production mod jars in `telemetry/run/mods/` load
alongside the telemetry mod, so `scripts/client-install.sh` can install the packwiz pack
there for a full client test without a launcher.

## Connect Claude Code

```bash
claude mcp add --transport http mc http://127.0.0.1:25590/mcp
```

Port override: `-Dstratosworn.telemetry.port=NNNN` on the client JVM.

## Tools

| Tool | Returns |
|---|---|
| `screenshot` | PNG of the game window, base64. `scale` 0.1-1.0, default 0.5. |
| `perf` | FPS, frame time ms, server tick ms (integrated directly; remote via debug sample subscription), heap used/max, loaded chunks, entity count, render distance. |
| `log_tail` | Last `lines` of `logs/latest.log`, optional `grep`. |
| `mods` | Loaded mod IDs, versions, names. |
| `look_at` | Block or entity under the crosshair with state properties and NBT. `range` default 20. |
| `player` | Position, dimension, biome, health, food, XP, game mode, inventory summary. |
| `run_command` | Sends a command as the player, returns chat lines received within `wait_ms`. |
| `recipe_lookup` | Recipes that make or use an item ID, encoded via `Recipe.CODEC` to JSON. |
| `quest_state` | FTB Quests chapters and quest completion via reflection. Best effort until Phase 5 pins the API. |

## Security

Binds to the loopback address only. Requests carrying a non-localhost `Origin` header are
rejected. There is no auth: anything on this machine can drive the game while the mod is
loaded, which is why it never ships.
