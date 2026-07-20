"""Report a pulled database's state so the runner can wait on it.

Prints `version=<user_version> phrases=<count>`; phrases is -1 when the table is
missing. Never raises, so the runner can poll a half-written file safely.
"""
import sqlite3
import sys

path = sys.argv[1]
version, phrases = -1, -1
try:
    con = sqlite3.connect(path)
    version = con.execute("PRAGMA user_version").fetchone()[0]
    phrases = con.execute("SELECT COUNT(*) FROM phrases").fetchone()[0]
    con.close()
except Exception:
    pass
print(f"version={version} phrases={phrases}")
