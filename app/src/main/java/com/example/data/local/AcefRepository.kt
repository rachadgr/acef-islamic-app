package com.example.data.local

import kotlinx.coroutines.flow.Flow

class AcefRepository(private val db: AcefDatabase) {
    val allFavorites: Flow<List<FavoriteEntity>> = db.favoriteDao().getAllFavorites()

    fun getFavoritesByType(type: String): Flow<List<FavoriteEntity>> =
        db.favoriteDao().getFavoritesByType(type)

    fun isFavorite(text: String): Flow<Boolean> =
        db.favoriteDao().isFavorite(text)

    suspend fun addFavorite(favorite: FavoriteEntity): Long =
        db.favoriteDao().insertFavorite(favorite)

    suspend fun removeFavoriteById(id: Long) =
        db.favoriteDao().deleteFavoriteById(id)

    suspend fun removeFavoriteByText(text: String) =
        db.favoriteDao().deleteFavoriteByText(text)

    val readingProgress: Flow<ReadingProgressEntity?> =
        db.readingProgressDao().getReadingProgress()

    suspend fun saveReadingProgress(surahNumber: Int, ayahNumber: Int, surahName: String) {
        db.readingProgressDao().saveReadingProgress(
            ReadingProgressEntity(
                id = 1,
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                surahName = surahName,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun getTasbihCounter(dhikrText: String): Flow<TasbihCountEntity?> =
        db.tasbihDao().getCounter(dhikrText)

    suspend fun saveTasbihCounter(counter: TasbihCountEntity) =
        db.tasbihDao().saveCounter(counter)

    val appSettings: Flow<AppSettingsEntity?> =
        db.settingsDao().getSettings()

    suspend fun saveSettings(settings: AppSettingsEntity) =
        db.settingsDao().saveSettings(settings)

    suspend fun getAiExplanation(key: String): AiExplanationEntity? =
        db.aiExplanationDao().getExplanationByKey(key)

    val allAiExplanations: Flow<List<AiExplanationEntity>> =
        db.aiExplanationDao().getAllExplanations()

    suspend fun saveAiExplanation(entity: AiExplanationEntity) =
        db.aiExplanationDao().insertExplanation(entity)

    suspend fun deleteAiExplanation(key: String) =
        db.aiExplanationDao().deleteExplanationByKey(key)
}
