# Stratosworn

A Minecraft 1.21.1 NeoForge modpack where magic meets industry. Create and Create
Aeronautics drive the industrial side. Ars Nouveau drives the magic side. The world
is built to look good at long range.

## Layout

| Path | What |
|---|---|
| `pack/` | The packwiz pack. `pack.toml`, `index.toml`, `mods/*.pw.toml`, configs. |
| `telemetry/` | Client-only NeoForge mod that hosts an MCP server for Claude Code (dev only). |
| `server/` | Local headless NeoForge server used for the boot test loop (git-ignored). |
| `docs/` | Design notes: tier sketches, progression, quests. |
| `MODS.md` | One line per mod: name, version, side, why. |
| `CHANGELOG.md` | Every config change and why. |

## Hard constraints

- Minecraft 1.21.1, NeoForge 21.1.x, Java 21. Create Aeronautics only runs here.
- Every mod is verified on Modrinth or CurseForge for a 1.21.1 NeoForge build before it is added.
- packwiz manages the pack. No hand-copied jars.
- One mod at a time. Boot the server after each add. Commit after each green boot.

## Test loop

1. Change one thing.
2. `scripts/boot-server.sh` — headless NeoForge boot; `latest.log` must have no ERROR lines and no mixin failures.
3. If the change touches the client, launch the client and use the telemetry MCP `screenshot` and `perf` tools.
4. Commit.

## Telemetry MCP

After a client with the telemetry mod is running:

```bash
claude mcp add --transport http mc http://127.0.0.1:25590/mcp
```
