# Stratosworn

A Minecraft 1.21.1 NeoForge modpack where magic meets industry. Create and Create
Aeronautics drive the industrial side. Ars Nouveau drives the magic side. The world
is built to look good at long range.

Personal project, built for fun. Claude Code did most of the typing: scripts, docs, and
commits are largely AI-authored, reviewed and steered by Christian. Not affiliated with any
mod author.

## Get it

```bash
git clone https://github.com/chrisgbradley/stratosworn.git
cd stratosworn
```

Tooling: Java 21, Go (for `go install github.com/packwiz/packwiz@latest`), Python 3.12.

- **Client (Prism):** create a NeoForge 1.21.1 instance, put `packwiz-installer-bootstrap.jar`
  in its `.minecraft`, and in Settings > Custom commands set the pre-launch command to
  `"$INST_JAVA" -jar packwiz-installer-bootstrap.jar -g -s client <pack URL>/pack.toml`.
  Set it through the dialog, not by editing `instance.cfg`: a hand-written quoted value loses
  the space after `$INST_JAVA` when Prism re-saves the file.
- **Server:** `server/` is a local NeoForge install for the boot loop (`scripts/boot-server.py`).
  The shipped server pack (Phase 6) installs the same way with `-s server`.
- **Telemetry mod:** `scripts/build-telemetry.sh` builds it and points the pack at the jar.

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

## Prism dev instance

Prism instance `Stratosworn (dev)` installs the pack from this repo on every launch through
`packwiz-installer`. It needs the pack served locally first:

```bash
scripts/serve.sh
```

That runs `packwiz serve` on 8080 so the instance installs this checkout, uncommitted changes included. Machines without the checkout can install from `https://raw.githubusercontent.com/chrisgbradley/stratosworn/master/pack/pack.toml`.

Then launch the instance in Prism. It pulls everything in `pack/index.toml` for the client
side, including the dev-only telemetry mod (served from `pack/mods/`, never indexed, never
exported). JVM: 8 GB G1, matching the decision for the shipped pack.

## Telemetry MCP

After a client with the telemetry mod is running:

```bash
claude mcp add --transport http mc http://127.0.0.1:25590/mcp
```
