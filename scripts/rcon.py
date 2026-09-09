#!/usr/bin/env python3
"""Tiny RCON client for the local test server. Usage: python scripts/rcon.py "say hi" ["stop"]"""
import socket, struct, sys
HOST, PORT, PASS = "127.0.0.1", 25575, "stratosworn-local"
def pkt(rid, kind, body):
    data = struct.pack("<ii", rid, kind) + body.encode() + b"\x00\x00"
    return struct.pack("<i", len(data)) + data
def read(s):
    ln = struct.unpack("<i", s.recv(4))[0]
    d = b""
    while len(d) < ln: d += s.recv(ln - len(d))
    rid, kind = struct.unpack("<ii", d[:8])
    return rid, kind, d[8:-2].decode(errors="replace")
s = socket.create_connection((HOST, PORT), timeout=10)
s.sendall(pkt(1, 3, PASS)); rid, _, _ = read(s)
if rid == -1: sys.exit("rcon auth failed")
for cmd in sys.argv[1:]:
    s.sendall(pkt(2, 2, cmd)); _, _, out = read(s); print(f"> {cmd}\n{out}")
s.close()
