package com.yoshisgarden.readit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Phrase::class,
        Flashcard::class,
        ReviewLog::class,
        QuizResult::class,
        StudyLog::class,
        UserProgress::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun phraseDao(): PhraseDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun reviewLogDao(): ReviewLogDao
    abstract fun quizResultDao(): QuizResultDao
    abstract fun studyLogDao(): StudyLogDao
    abstract fun userProgressDao(): UserProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * v1 -> v2: adds `review_logs` (per-answer history).
         *
         * Purely additive — existing flashcards / study logs / progress are kept.
         * The statements must match what Room generates for [ReviewLog] exactly,
         * or Room's schema validation will reject the migrated database.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `review_logs` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`phraseId` INTEGER NOT NULL, " +
                        "`rating` INTEGER NOT NULL, " +
                        "`answeredAt` INTEGER NOT NULL, " +
                        "`sessionId` INTEGER NOT NULL, " +
                        "`isRetry` INTEGER NOT NULL)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_review_logs_phraseId` " +
                        "ON `review_logs` (`phraseId`)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_review_logs_sessionId` " +
                        "ON `review_logs` (`sessionId`)",
                )
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "readit.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
