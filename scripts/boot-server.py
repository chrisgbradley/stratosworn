#!/usr/bin/env python3
"""Stratosworn test loop: install the pack server-side, boot NeoForge headless, stop it once
it reports Done, then fail if latest.log has ERROR lines or mixin failures.

Usage: python scripts/boot-server.py [--no-install] [--timeout 300] [--keep]
"""
import argparse, os, re, subprocess, sys, time, pathlib, shutil

ROOT = pathlib.Path(__file__).resolve().parent.parent
SERVER = ROOT / "server"
PACK = ROOT / "pack" / "pack.toml"
BOOTSTRAP = ROOT / "tools" / "packwiz-installer-bootstrap.jar"
NEO = "21.1.250"

def find_args_file():
    d = SERVER / "libraries" / "net" / "neoforged" / "neoforge" / NEO
    return d / ("win_args.txt" if os.name == "nt" else "unix_args.txt")

def install():
    print(f"[boot] packwiz-installer -s server {PACK}")
    r = subprocess.run(["java", "-jar", str(BOOTSTRAP), "-g", "-s", "server", str(PACK)],
                       cwd=SERVER, text=True, capture_output=True)
    sys.stdout.write(r.stdout[-4000:])
    if r.returncode != 0:
        sys.stdout.write(r.stderr[-4000:])
        print(f"[boot] installer failed ({r.returncode})")
        sys.exit(2)

def port_busy(port=25565):
    import socket
    with socket.socket() as s:
        s.settimeout(0.5)
        return s.connect_ex(("127.0.0.1", port)) == 0

def boot(timeout):
    if port_busy():
        print("[boot] port 25565 is already in use (detached test server running?). Stop it first: python scripts/rcon.py stop")
        sys.exit(3)
    log = SERVER / "logs" / "latest.log"
    if log.exists():
        log.unlink()
    cmd = ["java", "@user_jvm_args.txt", f"@{find_args_file().relative_to(SERVER).as_posix()}", "nogui"]
    print("[boot] " + " ".join(cmd))
    p = subprocess.Popen(cmd, cwd=SERVER, stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                         stderr=subprocess.STDOUT, text=True, encoding="utf-8", errors="replace")
    start = time.time()
    done = False
    tail = []
    while True:
        line = p.stdout.readline()
        if line:
            tail.append(line.rstrip())
            tail = tail[-60:]
            if "Done (" in line and not done:
                done = True
                print(f"[boot] server reached Done after {time.time()-start:.0f}s; sending stop")
                try:
                    p.stdin.write("stop\n"); p.stdin.flush()
                except Exception:
                    pass
        if p.poll() is not None:
            break
        if time.time() - start > timeout:
            print(f"[boot] timeout after {timeout}s; killing")
            p.kill()
            break
    if not done:
        print("[boot] server never reached Done. Last output:")
        print("\n".join(tail))
    return done

def scan():
    log = SERVER / "logs" / "latest.log"
    if not log.exists():
        print("[scan] no latest.log"); return False
    text = log.read_text(encoding="utf-8", errors="replace")
    errors = [l for l in text.splitlines() if "/ERROR]" in l or " ERROR " in l]
    # Real mixin failures only. Refmap notices and annotation-class lookups during scanning are WARN noise.
    mixin = [l for l in text.splitlines() if re.search(
        r"MixinApplyError|MixinTransformerError|InvalidMixinException|InjectionError|Mixin apply failed|Mixin .*failed to apply|Critical injection failure|Mixin.*could not be applied", l, re.I)]
    warns = [l for l in text.splitlines() if "/WARN]" in l]
    print(f"[scan] lines={len(text.splitlines())} ERROR={len(errors)} mixin-failures={len(mixin)} WARN={len(warns)}")
    for l in errors[:40]: print("  E " + l[:300])
    for l in mixin[:20]: print("  M " + l[:300])
    for l in warns[:30]: print("  W " + l[:220])
    return not errors and not mixin

if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--no-install", action="store_true")
    ap.add_argument("--timeout", type=int, default=300)
    args = ap.parse_args()
    if not args.no_install:
        install()
    ok = boot(args.timeout)
    clean = scan()
    print("[result] " + ("GREEN" if ok and clean else "RED"))
    sys.exit(0 if ok and clean else 1)
