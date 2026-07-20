"""Assert a migrated readit.db kept every byte of user progress.

Expectations below must match what make_v1_db.py planted. Exits non-zero on any
failure so the runner script can report pass/fail.
"""
import sqlite3
import sys

DB = sys.argv[1] if len(sys.argv) > 1 else "after.db"

EXPECTED_VERSION = 2
EXPECTED_PHRASES = 990          # bump when the seed JSON grows
EXPECTED_FLASHCARDS = 12
EXPECTED_STUDY_LOGS = 7
EXPECTED_PROGRESS = (7, 96)     # streakDays, totalPhrases

con = sqlite3.connect(DB)
q = lambda s: con.execute(s).fetchone()[0]
ok = True


def check(label, got, want):
    global ok
    good = got == want
    ok &= good
    print(f"  {'OK  ' if good else 'FAIL'}  {label:<34} {got!r}" +
          ("" if good else f"   expected {want!r}"))


print("schema")
check("user_version", q("PRAGMA user_version"), EXPECTED_VERSION)
check("review_logs table created",
      q("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='review_logs'"), 1)
check("index on phraseId",
      q("SELECT COUNT(*) FROM sqlite_master WHERE type='index'"
        " AND name='index_review_logs_phraseId'"), 1)
check("index on sessionId",
      q("SELECT COUNT(*) FROM sqlite_master WHERE type='index'"
        " AND name='index_review_logs_sessionId'"), 1)

# `id` is NOT NULL because Room declares it INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL.
check("review_logs columns",
      [(r[1], r[2], r[3]) for r in con.execute("PRAGMA table_info(review_logs)")],
      [("id", "INTEGER", 1), ("phraseId", "INTEGER", 1), ("rating", "INTEGER", 1),
       ("answeredAt", "INTEGER", 1), ("sessionId", "INTEGER", 1), ("isRetry", "INTEGER", 1)])

print("preserved user data")
check("phrases", q("SELECT COUNT(*) FROM phrases"), EXPECTED_PHRASES)
check("flashcards (SRS progress)", q("SELECT COUNT(*) FROM flashcards"), EXPECTED_FLASHCARDS)
check("study_logs", q("SELECT COUNT(*) FROM study_logs"), EXPECTED_STUDY_LOGS)
check("streak / total phrases",
      con.execute("SELECT streakDays, totalPhrases FROM user_progress").fetchone(),
      EXPECTED_PROGRESS)
check("SRS ease factors intact",
      [round(r[0], 2) for r in con.execute(
          "SELECT easeFactor FROM flashcards ORDER BY phraseId LIMIT 4")],
      [2.5, 2.4, 2.3, 2.2])
check("study minutes intact",
      q("SELECT SUM(durationMin) FROM study_logs"), 5 * sum(range(1, 8)))

print()
print("MIGRATION VERIFIED" if ok else "MIGRATION BROKEN")
con.close()
sys.exit(0 if ok else 1)
