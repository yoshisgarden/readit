"""Rewind a pulled readit.db to schema v1 carrying realistic user progress.

Used to prove that a schema migration preserves an existing user's data. Takes
the database the current app just created, strips the v2-only pieces, plants SRS
progress / study logs / a streak, and stamps it back to `user_version = 1`.

Room does not verify the *old* identity hash when upgrading (it only checks the
hash when the version already matches), so a hand-rewound file migrates exactly
like a real one from the Play/sideload install.

Run via run_migration_check.ps1 — it handles the adb push/pull dance.

WHEN THE SCHEMA CHANGES AGAIN (v2 -> v3): copy this file to make_v2_db.py, drop
the v3-only tables instead, and update verify_migration.py's expectations.
"""
import sqlite3
import sys
import time

DB = sys.argv[1] if len(sys.argv) > 1 else "readit.db"

con = sqlite3.connect(DB)
con.execute("PRAGMA wal_checkpoint(TRUNCATE)")

print("phrases seeded:", con.execute("SELECT COUNT(*) FROM phrases").fetchone()[0])

# --- strip everything v2 added so the file looks like v1 --------------------
con.execute("DROP TABLE IF EXISTS review_logs")
con.execute("DROP INDEX IF EXISTS index_review_logs_phraseId")
con.execute("DROP INDEX IF EXISTS index_review_logs_sessionId")

# --- plant the progress a real user would have ------------------------------
now = int(time.time() * 1000)
day = 86_400_000

con.execute("DELETE FROM flashcards")
ids = [r[0] for r in con.execute("SELECT id FROM phrases ORDER BY id LIMIT 12")]
for n, pid in enumerate(ids):
    con.execute(
        "INSERT INTO flashcards (phraseId, interval, easeFactor, dueDate, rating, reviewCount)"
        " VALUES (?,?,?,?,?,?)",
        (pid, n % 6 + 1, 2.5 - (n % 4) * 0.1, now + (n % 6 + 1) * day, n % 3, n % 5 + 1),
    )

con.execute("DELETE FROM study_logs")
con.executemany(
    "INSERT INTO study_logs (date, durationMin, phrasesStudied, phase) VALUES (?,?,?,?)",
    [("2026-07-1%d" % d, 5 * d, 4 * d, 1) for d in range(1, 8)],
)

con.execute("DELETE FROM user_progress")
con.execute(
    "INSERT INTO user_progress (id, currentPhase, streakDays, totalPhrases,"
    " lastStudyDate, phaseStartDate) VALUES (1, 2, 7, 96, '2026-07-17', ?)",
    (now - 30 * day,),
)

con.execute("PRAGMA user_version = 1")
con.commit()

print("rewound to user_version:", con.execute("PRAGMA user_version").fetchone()[0])
print("planted: 12 flashcards, 7 study_logs, streak 7 / total 96")
con.close()
