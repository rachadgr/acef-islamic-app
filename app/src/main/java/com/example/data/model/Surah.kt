package com.example.data.model

data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val nameTranslation: String,
    val versesCount: Int,
    val revelationType: String // "مكية" or "مدنية"
) {
    val numberOfAyahs: Int get() = versesCount
    val revelationTypeAr: String get() = revelationType
    val englishName: String get() = nameEnglish
}

enum class QuranRiwayah(
    val code: String,
    val titleArabic: String,
    val subtitleArabic: String,
    val shortName: String
) {
    HAFS(
        code = "HAFS",
        titleArabic = "رواية حفص عن عاصم",
        subtitleArabic = "المصحف الشريف بالرسم العثماني المعتمد في المشرق والعالم الإسلامي",
        shortName = "حفص"
    ),
    WARSH(
        code = "WARSH",
        titleArabic = "رواية ورش عن نافع",
        subtitleArabic = "المصحف الشريف بالرسم العثماني المعتمد في الجزائر والمغرب العربي",
        shortName = "ورش"
    );

    val displayName: String get() = titleArabic
}

data class Ayah(
    val surahNumber: Int,
    val ayahNumber: Int,
    val textArabic: String,
    val tafsir: String = "",
    val audioUrl: String = "",
    val textWarsh: String? = null
) {
    fun getText(riwayah: QuranRiwayah = QuranRiwayah.HAFS): String {
        return if (riwayah == QuranRiwayah.WARSH && !textWarsh.isNullOrBlank()) {
            textWarsh
        } else {
            textArabic
        }
    }
}

data class HourlyAyahTafsir(
    val surahName: String,
    val surahNumber: Int,
    val ayahNumber: Int,
    val textArabic: String,
    val tafsir: String,
    val audioUrl: String = "",
    val hourLabel: String = ""
)
