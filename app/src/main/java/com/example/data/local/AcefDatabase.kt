package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteEntity::class,
        ReadingProgressEntity::class,
        TasbihCountEntity::class,
        AppSettingsEntity::class,
        AiExplanationEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AcefDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun readingProgressDao(): ReadingProgressDao
    abstract fun tasbihDao(): TasbihDao
    abstract fun settingsDao(): SettingsDao
    abstract fun aiExplanationDao(): AiExplanationDao

    companion object {
        @Volatile
        private var INSTANCE: AcefDatabase? = null

        fun getDatabase(context: Context): AcefDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AcefDatabase::class.java,
                    "acef_islamic_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
