# Open defect: dev-client Registrate crash

Three unrelated mods fail the same way and only on the Gradle dev client
(`telemetry/gradlew runClientJoin`, launch target `forgeclientdev`):

| Mod | Server boot | Dev client |
|---|---|---|
| Create Mechanical Extruder 2.2.2 | green | crash |
| Aeronautics: Simulated Copycats 1.3.2 | green | crash |
| Ars Elemancy 1.17 | green | crash |

Crash, identical in all three:

```
Mod loading issue for: create
  Failure message: Create (create) encountered an error while dispatching
    the net.neoforged.neoforge.registries.RegisterEvent event
  Exception message: java.lang.IllegalStateException: Found unused register callbacks
  at com.tterrag.registrate.AbstractRegistrate.onRegister(AbstractRegistrate.java:262)
```

Registrate's "see logs" listing is at DEBUG, so the client log at INFO never names the
callbacks.

## What has been ruled out

- **Version mismatch.** All three declare Create `[6.0.10,6.1.0)`; the pack has 6.0.10.
- **A shared bundled library.** Only Ars Elemancy bundles anything (`sauce`), the other two
  bundle nothing.
- **Mod count or a load threshold.** Ars Elemental was added between two of the failures and
  the dev client loaded it fine.
- **Stale server jars.** Reproduced on a clean run after the server-resync fix.

## What has not been tested

The real client. Every one of these was judged on the Gradle dev client, which uses the
`forgeclientdev` launch target and loads the telemetry mod from the Gradle classpath rather
than from `mods/`. The Prism instance has never seen any of the three.

## Next step

Add all three to the pack, launch the Prism instance once, and see whether it loads. If it
does, the dev client is not a valid client-test surface for Registrate-based addons and the
loop should launch Prism instead. If it crashes the same way, the three are genuinely
incompatible and stay rejected.
