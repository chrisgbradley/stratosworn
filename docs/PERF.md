# Perf samples

Same spot, spectator, 16 chunks, DH 128, 1920x1080, vsync off. `scripts/perf-sample.py`.

| Label | When | FPS | Frame ms | Tick ms | Heap MB | Chunks | Entities | Mods | Focused reads |
|---|---|---|---|---|---|---|---|---|---|
| baseline (vanilla renderer) | 2026-09-09 20:48 | 89.7 | 11.5 | 3.5 | 1197.5 | 453.0 | 87.0 | 25 | 10/10 |
| + Sodium | 2026-09-09 20:46 | 320.2 | 2.7 | 4.2 | 1628.8 | 453.0 | 86.0 | 30 | 10/10 |
| + Iris | 2026-09-09 20:50 | 261.2 | 3.1 | 3.9 | 1360.7 | 453.0 | 87.0 | 31 | 10/10 |
| + FerriteCore | 2026-09-09 21:19 | 262.8 | 3.6 | 3.6 | 1068.1 | 453.0 | 85.0 | 32 | 10/10 |
