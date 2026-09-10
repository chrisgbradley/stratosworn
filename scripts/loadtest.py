#!/usr/bin/env python3
"""Spark load test scaffold: builds 20 chunks of running Create machinery near spawn via RCON,
then profiles the server with spark for 60 s and saves the result to a file (no upload).

Layout: a 5x4 chunk grid (80 x 64 blocks) at y=200 on a stone platform. Each chunk gets a
kinetic block set driven by creative motors: shafts, cogwheels, mechanical presses, mixers,
encased fans and deployers. Idle kinetic networks still tick every block entity, which is
the steady-state cost of a machine hall; item processing is added later with a real base.

Usage: python scripts/loadtest.py build | profile | clear
"""
import subprocess, sys, time, pathlib
ROOT = pathlib.Path(__file__).resolve().parent.parent
def rcon(*cmds):
    out = subprocess.run([sys.executable, str(ROOT / "scripts" / "rcon.py"), *cmds], capture_output=True, text=True, timeout=120)
    return out.stdout

X0, Y, Z0 = 1000, 200, 1000   # far from spawn so player chunks don't overlap
CHUNKS_X, CHUNKS_Z = 5, 4

def build():
    x1, z1 = X0 + CHUNKS_X * 16 - 1, Z0 + CHUNKS_Z * 16 - 1
    print(rcon(f"forceload add {X0} {Z0} {x1} {z1}"))
    print(rcon(f"fill {X0} {Y-1} {Z0} {x1} {Y-1} {z1} minecraft:stone"))
    cmds = []
    for cx in range(CHUNKS_X):
        for cz in range(CHUNKS_Z):
            bx, bz = X0 + cx * 16, Z0 + cz * 16
            # a creative motor every 4 blocks along z, each driving a shaft line east with machines on it
            for row in range(0, 16, 4):
                z = bz + row
                # shaft line with machines, driven by a creative motor
                cmds.append(f"setblock {bx} {Y} {z} create:creative_motor[facing=east]")
                cmds.append(f"fill {bx+1} {Y} {z} {bx+14} {Y} {z} create:shaft[axis=x]")
                cmds.append(f"setblock {bx+3} {Y} {z} create:mechanical_press[facing=north]")
                cmds.append(f"setblock {bx+6} {Y} {z} create:encased_fan[facing=east]")
                cmds.append(f"setblock {bx+15} {Y} {z} create:cogwheel[axis=x]")
                # two rotating contraptions per row: motor (up) -> mechanical bearing -> anchor block.
                # Contraption entities are the expensive part of Create, and what an airship is.
                for off in (9, 13):
                    cmds.append(f"setblock {bx+off} {Y+1} {z+1} create:creative_motor[facing=up]")
                    cmds.append(f"setblock {bx+off} {Y+2} {z+1} create:mechanical_bearing[facing=up]")
                    cmds.append(f"setblock {bx+off} {Y+3} {z+1} minecraft:oak_planks")
                    cmds.append(f"setblock {bx+off} {Y+4} {z+1} minecraft:oak_planks")
    for i in range(0, len(cmds), 20):
        rcon(*cmds[i:i+20])
    print(f"placed {len(cmds)} commands over {CHUNKS_X*CHUNKS_Z} chunks")

def profile(seconds=60):
    # No --timeout: spark would auto-upload the result to spark.lucko.me when it fires.
    print(rcon("neoforge tps", "execute if entity @e[type=create:stationary_contraption]"))
    print(rcon("spark profiler start"))
    time.sleep(seconds)
    print(rcon("spark profiler stop --save-to-file"))
    print(rcon("neoforge tps"))

def clear():
    x1, z1 = X0 + CHUNKS_X * 16 - 1, Z0 + CHUNKS_Z * 16 - 1
    print(rcon(f"kill @e[type=create:stationary_contraption]", f"fill {X0} {Y-1} {Z0} {x1} {Y+4} {z1} minecraft:air", f"forceload remove {X0} {Z0} {x1} {z1}"))

if __name__ == "__main__":
    {"build": build, "profile": profile, "clear": clear}[sys.argv[1]]()
