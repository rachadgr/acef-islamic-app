package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE type = :type ORDER BY timestamp DESC")
    fun getFavoritesByType(type: String): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE arabicText = :text LIMIT 1)")
    fun isFavorite(text: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity): Long

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavoriteById(id: Long)

    @Query("DELETE FROM favorites WHERE arabicText = :text")
    suspend fun deleteFavoriteByText(text: String)
}

@Dao
interface ReadingProgressDao {
    @Query("SELECT * FROM reading_progress WHERE id = 1 LIMIT 1")
    fun getReadingProgress(): Flow<ReadingProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReadingProgress(progress: ReadingProgressEntity)
}

@Dao
interface TasbihDao {
    @Query("SELECT * FROM tasbih_counters WHERE dhikrText = :dhikrText LIMIT 1")
    fun getCounter(dhikrText: String): Flow<TasbihCountEntity?>

    @Query("SELECT * FROM tasbih_counters ORDER BY lastUpdated DESC")
    fun getAllCounters(): Flow<List<TasbihCountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCounter(counter: TasbihCountEntity)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettingsSync(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AppSettingsEntity)
}

@Dao
interface AiExplanationDao {
    @Query("SELECT * FROM ai_explanations WHERE `key` = :key LIMIT 1")
    suspend fun getExplanationByKey(key: String): AiExplanationEntity?

    @Query("SELECT * FROM ai_explanations ORDER BY timestamp DESC LIMIT 30")
    fun getAllExplanations(): Flow<List<AiExplanationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExplanation(entity: AiExplanationEntity)

    @Query("DELETE FROM ai_explanations WHERE `key` = :key")
    suspend fun deleteExplanationByKey(key: String)
}
