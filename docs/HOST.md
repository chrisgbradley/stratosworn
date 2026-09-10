# Host: DS001

Production server for Stratosworn. Provided by Christian 2026-09-10.

| | |
|---|---|
| Machine | Bare metal, Ubuntu 24.04 LTS (kernel 6.8) |
| CPU | Dual Xeon E5-2687W v4, 24c/48t, 3.5 GHz max, 2 NUMA nodes (~32 GB each) |
| RAM | 62 GB, 8 GB swapfile |
| Root disk | LVM on a 465 GB 7200 RPM WD HDD, ~74% full |
| Fast disk | 1 TB Crucial T500 NVMe at `/mnt/nvme`, mostly empty; ATM10 AMP instance lives there |
| Containers | Docker 26.1.4 / Compose v2.27.1; Docker data root still on the HDD |
| Also running | 3 AMP instances plus a services stack (homarr, homepage, memos, grafana, prometheus, cadvisor, node_exporter, stirling-pdf, socket-proxy) |
| Control panel | AMP |

## What this means for the pack

- **Heap: `-Xms12G -Xmx12G`, G1GC.** 12 GB sits inside one NUMA node with room for the
  other AMP instances and the services stack. Equal Xms/Xmx with `-XX:+AlwaysPreTouch`
  avoids resize pauses. The Aikar G1 flag set in `server/user_jvm_args.txt` is the
  production set; only the heap size differs from the local 8G.
- **Put the instance on the NVMe.** Chunk generation with Tectonic + Terralith and Create
  contraption saves are I/O heavy. The HDD is 74% full and shared with Docker's data
  root. Same placement as the ATM10 instance.
- **Pin to one NUMA node** if AMP allows a launch wrapper: `numactl --cpunodebind=1
  --membind=1` keeps the heap and the server thread on the same node. Minecraft's
  main thread wants clock speed, not cores; the 3.5 GHz turbo is fine.
- **Java 21** is required by NeoForge 21.1. AMP's Minecraft module manages Java
  runtimes; select 21 for this instance.
- **`max-players=6`**, `view-distance=10`, `simulation-distance=8` to start. Raise
  view distance only after the spark load test shows headroom at 20 TPS.
- **Chunky pregeneration** runs once on this host before players join (radius to be
  set with the world border), on the NVMe.
- The LAN address is deliberately not in this repo.
