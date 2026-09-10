# Why the chunk and lighting optimizers are out

Sable is the physics engine under Create Aeronautics, and Aeronautics cannot run without it.
Sable rewrites collision, explosion, and light storage, which is exactly the surface the
chunk and lighting optimizers rewrite. Three were tested and rejected; upstream confirms two
of the three, and the upstream reasons are worse than what the local logs showed.

## Moonrise

Local symptom: server boot dies with `MixinApplyError` on Sable's `explosion.ExplosionMixin`.

Upstream: [Tuinity/Moonrise issue #177](https://github.com/Tuinity/Moonrise/issues/177),
filed as a Sable incompatibility. Same failure: Sable cannot inject into
`net.minecraft.world.level.Explosion::explode()` because Moonrise's
`ca.spottedleaf.moonrise.mixin.collisions.ExplosionMixin` has already merged it, plus a
second conflict where Sable's `EntityGetterMixin` overwrites a Moonrise-modified method.
The issue is open and untriaged, with no owner and no proposed fix. Both mods are doing the
same job to the same methods for opposite reasons, so a fix needs coordination between the
two authors.

**Verdict: not fixable from the pack side.**

## C2ME

Local symptom: two `RuntimeDistCleaner` ERRORs on the dedicated server, from the bundled
`c2me_client_uncapvd` module pulling client classes onto the server. That is a packaging bug
and looked, at the time, like something a future build would fix.

Upstream is worse:
[RelativityMC/C2ME-neoforge issue #67](https://github.com/RelativityMC/C2ME-neoforge/issues/67).
With C2ME and Sable together, flying a physics ship into ungenerated terrain makes chunk
loading stop and the game tick freeze. **Closed as not planned.**

That is precisely the failure mode the brief told us to watch for, and it lands on the one
activity this pack is built around. Even a C2ME build that fixed the packaging bug would
still be disqualified.

**Verdict: rejected on the merits, not just the boot log.**

## ScalableLux

No upstream issue needed: Sable's own metadata declares it incompatible, so NeoForge refuses
to start. Sable replaces light storage wholesale and ScalableLux is a lighting engine.

**Verdict: mutually exclusive by design.**

## What this leaves

No async chunk system on this pack. The substitutes are Noisium (faster worldgen noise) and
Chunky (pregenerate so runtime generation is rare), both green and both in. For a fixed
world with 4 to 6 players this is a reasonable trade: pregeneration removes most of what
C2ME would have accelerated.

## Related find

[Create Aeronautics: Compatibility](https://modrinth.com/project/aLVC5usA) (1.21.1 NeoForge)
patches 13 mods that misbehave on physics ships, among them Alex's Mobs and Alex's Caves
pathfinding, Storage Drawers, PneumaticCraft, and Another Furniture. It does **not** touch
any optimization mod, so it is no help here, but it is worth adding if any of those mods
ever enter the pack.
