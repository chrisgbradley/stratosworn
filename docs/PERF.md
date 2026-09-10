# Perf samples

Same spot, spectator, 16 chunks, DH 128, 1920x1080, vsync off. `scripts/perf-sample.py`.

| Label | When | FPS | Frame ms | Tick ms | Heap MB | Chunks | Entities | Mods | Focused reads |
|---|---|---|---|---|---|---|---|---|---|
| baseline (vanilla renderer) | 2026-09-09 20:48 | 89.7 | 11.5 | 3.5 | 1197.5 | 453.0 | 87.0 | 25 | 10/10 | 10/10 |
| + Sodium | 2026-09-09 20:46 | 320.2 | 2.7 | 4.2 | 1628.8 | 453.0 | 86.0 | 30 | 10/10 | 10/10 |
| + Iris | 2026-09-09 20:50 | 261.2 | 3.1 | 3.9 | 1360.7 | 453.0 | 87.0 | 31 | 10/10 | 10/10 |
| + FerriteCore | 2026-09-09 21:19 | 262.8 | 3.6 | 3.6 | 1068.1 | 453.0 | 85.0 | 32 | 10/10 | 10/10 |
| + ModernFix | 2026-09-09 21:29 | 315.2 | 3.2 | 3.5 | 1032.4 | 453.0 | 86.0 | 33 |
| + Entity Culling | 2026-09-09 21:31 | 279.1 | 3.0 | 3.5 | 1295.0 | 453.0 | 85.7 | 36 |
| + ImmediatelyFast | 2026-09-09 21:33 | 280.5 | 3.1 | 3.1 | 1132.3 | 453.0 | 85.0 | 37 |
| + spark (server) | 2026-09-10 02:04 | 304.2 | 2.7 | 4.2 | 1443.5 | 453.0 | 86.7 | 37 |
| + Noisium (server) | 2026-09-10 02:26 | 264.2 | 3.4 | 3.7 | 1227.4 | 453.0 | 88.0 | 37 |
| + Chunky (server) | 2026-09-10 02:43 | 312.2 | 2.8 | 4.0 | 1266.2 | 453.0 | 86.0 | 37 |
| + Lithium | 2026-09-10 02:49 | 269.7 | 3.2 | 3.7 | 1153.2 | 453.0 | 86.0 | 38 |

## Server load test (dev box, Ryzen 2700X, 8G heap G1)

`scripts/loadtest.py`: 20 forced chunks at y=200, 100 creative motors, ~500 kinetic blocks
(shafts, presses, fans, cogs), 160 mechanical bearings of which 148 assembled into rotating
contraptions. Measured with `neoforge tps`; spark profile saved to `server/config/spark/`.

| State | TPS | ms/tick (overall) |
|---|---|---|
| Idle, no players | 20.0 | 1.4 |
| Field running, 148 contraptions | 20.0 | 3.2 |

Headroom against the 50 ms budget is large. Not yet covered: item processing on belts, and
airships (Aeronautics physics contraptions run through Sable, a different cost model than
bearing contraptions). Those need a hand-built base and hull.
