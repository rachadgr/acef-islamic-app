package com.example.data.model

import com.example.data.local.IslamicDataProvider

data class QuranJuzInfo(
    val number: Int,
    val nameArabic: String,
    val startSurahNumber: Int,
    val startSurahName: String,
    val startAyahNumber: Int,
    val startPage: Int
)

object QuranMushafMetadata {

    val all30Ajzaa: List<QuranJuzInfo> = listOf(
        QuranJuzInfo(1, "الجزء الأول", 1, "الفاتحة", 1, 1),
        QuranJuzInfo(2, "الجزء الثاني (سيقول السفهاء)", 2, "البقرة", 142, 22),
        QuranJuzInfo(3, "الجزء الثالث (تلك الرسل)", 2, "البقرة", 253, 42),
        QuranJuzInfo(4, "الجزء الرابع (لن تنالوا البر)", 3, "آل عمران", 93, 62),
        QuranJuzInfo(5, "الجزء الخامس (والمحصنات)", 4, "النساء", 24, 82),
        QuranJuzInfo(6, "الجزء السادس (لا يحب الله)", 4, "النساء", 148, 102),
        QuranJuzInfo(7, "الجزء السابع (وإذا سمعوا)", 5, "المائدة", 82, 121),
        QuranJuzInfo(8, "الجزء الثامن (ولو أننا)", 6, "الأنعام", 111, 142),
        QuranJuzInfo(9, "الجزء التاسع (قال الملأ)", 7, "الأعراف", 88, 162),
        QuranJuzInfo(10, "الجزء العاشر (واعلموا)", 8, "الأنفال", 41, 182),
        QuranJuzInfo(11, "الجزء الحادي عشر (يعتذرون)", 9, "التوبة", 93, 201),
        QuranJuzInfo(12, "الجزء الثاني عشر (وما من دابة)", 11, "هود", 6, 222),
        QuranJuzInfo(13, "الجزء الثالث عشر (وما أبرئ نفسي)", 12, "يوسف", 53, 242),
        QuranJuzInfo(14, "الجزء الرابع عشر (ربما)", 15, "الحجر", 1, 262),
        QuranJuzInfo(15, "الجزء الخامس عشر (سبحان الذي)", 17, "الإسراء", 1, 282),
        QuranJuzInfo(16, "الجزء السادس عشر (قال ألم)", 18, "الكهف", 75, 302),
        QuranJuzInfo(17, "الجزء السابع عشر (اقترب للناس)", 21, "الأنبياء", 1, 322),
        QuranJuzInfo(18, "الجزء الثامن عشر (قد أفلح)", 23, "المؤمنون", 1, 342),
        QuranJuzInfo(19, "الجزء التاسع عشر (وقال الذين لا يرجون)", 25, "الفرقان", 21, 362),
        QuranJuzInfo(20, "الجزء العشرون (فما كان جواب)", 27, "النمل", 56, 382),
        QuranJuzInfo(21, "الجزء الحادي والعشرون (ولا تجادلوا)", 29, "العنكبوت", 46, 402),
        QuranJuzInfo(22, "الجزء الثاني والعشرون (ومن يقنت)", 33, "الأحزاب", 31, 422),
        QuranJuzInfo(23, "الجزء الثالث والعشرون (وما أنزلنا)", 36, "يس", 28, 442),
        QuranJuzInfo(24, "الجزء الرابع والعشرون (فمن أظلم)", 39, "الزمر", 32, 462),
        QuranJuzInfo(25, "الجزء الخامس والعشرون (إليه يرد)", 41, "فصلت", 47, 482),
        QuranJuzInfo(26, "الجزء السادس والعشرون (حم)", 46, "الأحقاف", 1, 502),
        QuranJuzInfo(27, "الجزء السابع والعشرون (قال فما خطبكم)", 51, "الذاريات", 31, 522),
        QuranJuzInfo(28, "الجزء الثامن والعشرون (قد سمع الله)", 58, "المجادلة", 1, 542),
        QuranJuzInfo(29, "الجزء التاسع والعشرون (تبارك الذي)", 67, "الملك", 1, 562),
        QuranJuzInfo(30, "الجزء الثلاثون (عمّ يتساءلون)", 78, "النبأ", 1, 582)
    )

    // Accurate standard starting page for each of the 114 Surahs in Madani Mushaf (604 pages)
    val surahStartPages: Map<Int, Int> = mapOf(
        1 to 1, 2 to 2, 3 to 50, 4 to 77, 5 to 106, 6 to 128, 7 to 151, 8 to 177,
        9 to 187, 10 to 208, 11 to 221, 12 to 235, 13 to 249, 14 to 255, 15 to 262, 16 to 267,
        17 to 282, 18 to 293, 19 to 305, 20 to 312, 21 to 322, 22 to 332, 23 to 342, 24 to 350,
        25 to 359, 26 to 367, 27 to 377, 28 to 385, 29 to 396, 30 to 404, 31 to 411, 32 to 415,
        33 to 418, 34 to 428, 35 to 434, 36 to 440, 37 to 446, 38 to 453, 39 to 458, 40 to 467,
        41 to 477, 42 to 483, 43 to 489, 44 to 496, 45 to 499, 46 to 502, 47 to 507, 48 to 511,
        49 to 515, 50 to 518, 51 to 520, 52 to 523, 53 to 526, 54 to 528, 55 to 531, 56 to 534,
        57 to 537, 58 to 542, 59 to 545, 60 to 549, 61 to 551, 62 to 553, 63 to 554, 64 to 556,
        65 to 558, 66 to 560, 67 to 562, 68 to 564, 69 to 566, 70 to 568, 71 to 570, 72 to 572,
        73 to 574, 74 to 575, 75 to 577, 76 to 578, 77 to 580, 78 to 582, 79 to 583, 80 to 585,
        81 to 586, 82 to 587, 83 to 587, 84 to 589, 85 to 590, 86 to 591, 87 to 591, 88 to 592,
        89 to 593, 90 to 594, 91 to 595, 92 to 595, 93 to 596, 94 to 596, 95 to 597, 96 to 597,
        97 to 598, 98 to 598, 99 to 599, 100 to 599, 101 to 600, 102 to 600, 103 to 601, 104 to 601,
        105 to 601, 106 to 602, 107 to 602, 108 to 602, 109 to 603, 110 to 603, 111 to 603, 112 to 604,
        113 to 604, 114 to 604
    )

    fun getPageForSurah(surahNumber: Int): Int {
        return surahStartPages[surahNumber] ?: 1
    }

    fun getSurahForPage(pageNumber: Int): Surah {
        val page = pageNumber.coerceIn(1, 604)
        // Find highest surah whose start page <= page
        var matchedSurahNumber = 1
        for (num in 1..114) {
            val start = surahStartPages[num] ?: 1
            if (start <= page) {
                matchedSurahNumber = num
            } else {
                break
            }
        }
        return IslamicDataProvider.all114Surahs.find { it.number == matchedSurahNumber }
            ?: IslamicDataProvider.all114Surahs.first()
    }

    fun getJuzForPage(pageNumber: Int): QuranJuzInfo {
        val page = pageNumber.coerceIn(1, 604)
        var matched = all30Ajzaa.first()
        for (juz in all30Ajzaa) {
            if (juz.startPage <= page) {
                matched = juz
            } else {
                break
            }
        }
        return matched
    }

    fun getJuzNumber(surahNumber: Int, ayahNumber: Int): Int {
        var currentJuz = 1
        for (juz in all30Ajzaa) {
            if (surahNumber > juz.startSurahNumber ||
                (surahNumber == juz.startSurahNumber && ayahNumber >= juz.startAyahNumber)) {
                currentJuz = juz.number
            } else {
                break
            }
        }
        return currentJuz
    }

    fun getJuzArabicName(juzNumber: Int): String {
        return all30Ajzaa.find { it.number == juzNumber }?.nameArabic ?: "الجزء $juzNumber"
    }
}
