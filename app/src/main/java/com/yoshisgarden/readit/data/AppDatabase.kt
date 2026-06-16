package com.yoshisgarden.readit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Phrase::class,
        Flashcard::class,
        QuizResult::class,
        StudyLog::class,
        UserProgress::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun phraseDao(): PhraseDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun quizResultDao(): QuizResultDao
    abstract fun studyLogDao(): StudyLogDao
    abstract fun userProgressDao(): UserProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "readit.db",
                ).fallbackToDestructiveMigration(true).build().also { INSTANCE = it }
            }
    }
}
