package com.example.data.local

import com.example.data.model.Ayah
import com.example.data.model.City
import com.example.data.model.Dhikr
import com.example.data.model.Hadith
import com.example.data.model.HourlyAyahTafsir
import com.example.data.model.HourlyHadithExplanation
import com.example.data.model.Surah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

object IslamicDataProvider {

    val all114Surahs: List<Surah> = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", "The Opening", 7, "مكية"),
        Surah(2, "البقرة", "Al-Baqarah", "The Cow", 286, "مدنية"),
        Surah(3, "آل عمران", "Ali 'Imran", "Family of Imran", 200, "مدنية"),
        Surah(4, "النساء", "An-Nisa", "The Women", 176, "مدنية"),
        Surah(5, "المائدة", "Al-Ma'idah", "The Table Spread", 120, "مدنية"),
        Surah(6, "الأنعام", "Al-An'am", "The Cattle", 165, "مكية"),
        Surah(7, "الأعراف", "Al-A'raf", "The Heights", 206, "مكية"),
        Surah(8, "الأنفال", "Al-Anfal", "The Spoils of War", 75, "مدنية"),
        Surah(9, "التوبة", "At-Tawbah", "The Repentance", 129, "مدنية"),
        Surah(10, "يونس", "Yunus", "Jonah", 109, "مكية"),
        Surah(11, "هود", "Hud", "Hud", 123, "مكية"),
        Surah(12, "يوسف", "Yusuf", "Joseph", 111, "مكية"),
        Surah(13, "الرعد", "Ar-Ra'd", "The Thunder", 43, "مدنية"),
        Surah(14, "إبراهيم", "Ibrahim", "Abraham", 52, "مكية"),
        Surah(15, "الحجر", "Al-Hijr", "The Rocky Tract", 99, "مكية"),
        Surah(16, "النحل", "An-Nahl", "The Bee", 128, "مكية"),
        Surah(17, "الإسراء", "Al-Isra", "The Night Journey", 111, "مكية"),
        Surah(18, "الكهف", "Al-Kahf", "The Cave", 110, "مكية"),
        Surah(19, "مريم", "Maryam", "Mary", 98, "مكية"),
        Surah(20, "طه", "Ta-Ha", "Ta-Ha", 135, "مكية"),
        Surah(21, "الأنبياء", "Al-Anbiya", "The Prophets", 112, "مكية"),
        Surah(22, "الحج", "Al-Hajj", "The Pilgrimage", 78, "مدنية"),
        Surah(23, "المؤمنون", "Al-Mu'minun", "The Believers", 118, "مكية"),
        Surah(24, "النور", "An-Nur", "The Light", 64, "مدنية"),
        Surah(25, "الفرقان", "Al-Furqan", "The Criterion", 77, "مكية"),
        Surah(26, "الشعراء", "Ash-Shu'ara", "The Poets", 227, "مكية"),
        Surah(27, "النمل", "An-Naml", "The Ant", 93, "مكية"),
        Surah(28, "القصص", "Al-Qasas", "The Stories", 88, "مكية"),
        Surah(29, "العنكبوت", "Al-'Ankabut", "The Spider", 69, "مكية"),
        Surah(30, "الروم", "Ar-Rum", "The Romans", 60, "مكية"),
        Surah(31, "لقمان", "Luqman", "Luqman", 34, "مكية"),
        Surah(32, "السجدة", "As-Sajdah", "The Prostration", 30, "مكية"),
        Surah(33, "الأحزاب", "Al-Ahzab", "The Combined Forces", 73, "مدنية"),
        Surah(34, "سبأ", "Saba", "Sheba", 54, "مكية"),
        Surah(35, "فاطر", "Fatir", "Originator", 45, "مكية"),
        Surah(36, "يس", "Ya-Sin", "Ya-Sin", 83, "مكية"),
        Surah(37, "الصافات", "As-Saffat", "Those Who Set The Ranks", 182, "مكية"),
        Surah(38, "ص", "Sad", "The Letter Sad", 88, "مكية"),
        Surah(39, "الزمر", "Az-Zumar", "The Troops", 75, "مكية"),
        Surah(40, "غافر", "Ghafir", "The Forgiver", 85, "مكية"),
        Surah(41, "فصلت", "Fussilat", "Explained in Detail", 54, "مكية"),
        Surah(42, "الشورى", "Ash-Shura", "The Consultation", 53, "مكية"),
        Surah(43, "الزخرف", "Az-Zukhruf", "The Ornaments of Gold", 89, "مكية"),
        Surah(44, "الدخان", "Ad-Dukhan", "The Smoke", 59, "مكية"),
        Surah(45, "الجاثية", "Al-Jathiyah", "The Crouching", 37, "مكية"),
        Surah(46, "الأحقاف", "Al-Ahqaf", "The Wind-Curved Sandhills", 35, "مكية"),
        Surah(47, "محمد", "Muhammad", "Muhammad", 38, "مدنية"),
        Surah(48, "الفتح", "Al-Fath", "The Victory", 29, "مدنية"),
        Surah(49, "الحجرات", "Al-Hujurat", "The Rooms", 18, "مدنية"),
        Surah(50, "ق", "Qaf", "The Letter Qaf", 45, "مكية"),
        Surah(51, "الذاريات", "Adh-Dhariyat", "The Winnowing Winds", 60, "مكية"),
        Surah(52, "الطور", "At-Tur", "The Mount", 49, "مكية"),
        Surah(53, "النجم", "An-Najm", "The Star", 62, "مكية"),
        Surah(54, "القمر", "Al-Qamar", "The Moon", 55, "مكية"),
        Surah(55, "الرحمن", "Ar-Rahman", "The Beneficent", 78, "مدنية"),
        Surah(56, "الواقعة", "Al-Waqi'ah", "The Inevitable", 96, "مكية"),
        Surah(57, "الحديد", "Al-Hadid", "The Iron", 29, "مدنية"),
        Surah(58, "المجادلة", "Al-Mujadila", "The Pleading Woman", 22, "مدنية"),
        Surah(59, "الحشر", "Al-Hashr", "The Exile", 24, "مدنية"),
        Surah(60, "الممتحنة", "Al-Mumtahanah", "She That Is To Be Examined", 13, "مدنية"),
        Surah(61, "الصف", "As-Saff", "The Ranks", 14, "مدنية"),
        Surah(62, "الجمعة", "Al-Jumu'ah", "The Congregation", 11, "مدنية"),
        Surah(63, "المنافقون", "Al-Munafiqun", "The Hypocrites", 11, "مدنية"),
        Surah(64, "التغابن", "At-Taghabun", "The Mutual Disillusion", 18, "مدنية"),
        Surah(65, "الطلاق", "At-Talaq", "The Divorce", 12, "مدنية"),
        Surah(66, "التحريم", "At-Tahrim", "The Prohibition", 12, "مدنية"),
        Surah(67, "الملك", "Al-Mulk", "The Sovereignty", 30, "مكية"),
        Surah(68, "القلم", "Al-Qalam", "The Pen", 52, "مكية"),
        Surah(69, "الحاقة", "Al-Haqqah", "The Reality", 52, "مكية"),
        Surah(70, "المعارج", "Al-Ma'arij", "The Ascending Stairways", 44, "مكية"),
        Surah(71, "نوح", "Nuh", "Noah", 28, "مكية"),
        Surah(72, "الجن", "Al-Jinn", "The Jinn", 28, "مكية"),
        Surah(73, "المزمل", "Al-Muzzammil", "The Enshrouded One", 20, "مكية"),
        Surah(74, "المدثر", "Al-Muddathir", "The Cloaked One", 56, "مكية"),
        Surah(75, "القيامة", "Al-Qiyamah", "The Resurrection", 40, "مكية"),
        Surah(76, "الإنسان", "Al-Insan", "The Man", 31, "مدنية"),
        Surah(77, "المرسلات", "Al-Mursalat", "The Emissaries", 50, "مكية"),
        Surah(78, "النبأ", "An-Naba", "The Tidings", 40, "مكية"),
        Surah(79, "النازعات", "An-Nazi'at", "Those Who Drag Forth", 46, "مكية"),
        Surah(80, "عبس", "Abasa", "He Frowned", 42, "مكية"),
        Surah(81, "التكوير", "At-Takwir", "The Overthrowing", 29, "مكية"),
        Surah(82, "الانفطار", "Al-Infitar", "The Cleaving", 19, "مكية"),
        Surah(83, "المطففين", "Al-Mutaffifin", "The Defrauding", 36, "مكية"),
        Surah(84, "الانشقاق", "Al-Inshiqaq", "The Splitting Open", 25, "مكية"),
        Surah(85, "البروج", "Al-Buruj", "The Mansions of the Stars", 22, "مكية"),
        Surah(86, "الطارق", "At-Tariq", "The Morning Star", 17, "مكية"),
        Surah(87, "الأعلى", "Al-A'la", "The Most High", 19, "مكية"),
        Surah(88, "الغاشية", "Al-Ghashiyah", "The Overwhelming", 26, "مكية"),
        Surah(89, "الفجر", "Al-Fajr", "The Dawn", 30, "مكية"),
        Surah(90, "البلد", "Al-Balad", "The City", 20, "مكية"),
        Surah(91, "الشمس", "Ash-Shams", "The Sun", 15, "مكية"),
        Surah(92, "الليل", "Al-Layl", "The Night", 21, "مكية"),
        Surah(93, "الضحى", "Ad-Duha", "The Morning Hours", 11, "مكية"),
        Surah(94, "الشرح", "Ash-Sharh", "The Relief", 8, "مكية"),
        Surah(95, "التين", "At-Tin", "The Fig", 8, "مكية"),
        Surah(96, "العلق", "Al-'Alaq", "The Clot", 19, "مكية"),
        Surah(97, "القدر", "Al-Qadr", "The Power", 5, "مكية"),
        Surah(98, "البينة", "Al-Bayyinah", "The Clear Proof", 8, "مدنية"),
        Surah(99, "الزلزلة", "Az-Zalzalah", "The Earthquake", 8, "مدنية"),
        Surah(100, "العاديات", "Al-'Adiyat", "The Courser", 11, "مكية"),
        Surah(101, "القارعة", "Al-Qari'ah", "The Calamity", 11, "مكية"),
        Surah(102, "التكاثر", "At-Takathur", "The Rivalry in World Increase", 8, "مكية"),
        Surah(103, "العصر", "Al-'Asr", "The Declining Day", 3, "مكية"),
        Surah(104, "الهمزة", "Al-Humazah", "The Traducer", 9, "مكية"),
        Surah(105, "الفيل", "Al-Fil", "The Elephant", 5, "مكية"),
        Surah(106, "قريش", "Quraysh", "Quraysh", 4, "مكية"),
        Surah(107, "الماعون", "Al-Ma'un", "The Small Kindness", 7, "مكية"),
        Surah(108, "الكوثر", "Al-Kawthar", "The Abundance", 3, "مكية"),
        Surah(109, "الكافرون", "Al-Kafirun", "The Disbelievers", 6, "مكية"),
        Surah(110, "النصر", "An-Nasr", "The Divine Support", 3, "مدنية"),
        Surah(111, "المسد", "Al-Masad", "The Palm Fiber", 5, "مكية"),
        Surah(112, "الإخلاص", "Al-Ikhlas", "The Sincerity", 4, "مكية"),
        Surah(113, "الفلق", "Al-Falaq", "The Daybreak", 5, "مكية"),
        Surah(114, "الناس", "An-Nas", "Mankind", 6, "مكية")
    )

    val allSurahs: List<Surah> get() = all114Surahs

    val surahVerses: Map<Int, List<Ayah>> = mapOf(
        1 to listOf(
            Ayah(1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "أبتدئ قراءتي مستعيناً بالله الرحمن بجميع خلقه، الرحيم بالمؤمنين.", "https://everyayah.com/data/Alafasy_128kbps/001001.mp3", textWarsh = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"),
            Ayah(1, 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "الثناء الكامل والشكر الخالص لله وحده، خالق العوالم ومدبرها ومالكها.", "https://everyayah.com/data/Alafasy_128kbps/001002.mp3", textWarsh = "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ"),
            Ayah(1, 3, "الرَّحْمَٰنِ الرَّحِيمِ", "الذي وسعت رحمته كل شيء، وأفاض نعمه على عباده.", "https://everyayah.com/data/Alafasy_128kbps/001003.mp3", textWarsh = "الرَّحْمَٰنِ الرَّحِيمِ"),
            Ayah(1, 4, "مَالِكِ يَوْمِ الدِّينِ", "المتصرف وحده بيوم الجزاء والحساب، يوم القيامة الذي لا ملك فيه لأحد سواه.", "https://everyayah.com/data/Alafasy_128kbps/001004.mp3", textWarsh = "مَلِكِ يَوْمِ الدِّينِ"),
            Ayah(1, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "نخصك وحدك بالعبادة وإخلاص التوحيد، ونستعين بك وحدك في سائر أمورنا.", "https://everyayah.com/data/Alafasy_128kbps/001005.mp3", textWarsh = "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ"),
            Ayah(1, 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "وفقنا وأرشدنا وثبتنا على الطريق القويم وهو دين الإسلام الحق.", "https://everyayah.com/data/Alafasy_128kbps/001006.mp3", textWarsh = "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ"),
            Ayah(1, 7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "طريق النبيين والصديقين والشهداء والصالحين، لا طريق المعاندين ولا الجاهلين الضالين.", "https://everyayah.com/data/Alafasy_128kbps/001007.mp3", textWarsh = "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ")
        ),
        112 to listOf(
            Ayah(112, 1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "قل أيها النبي لمن سألوك عن ربك: هو الله المنفرد بالألوهية والربوبية والأسماء والصفات، لا شريك له.", "https://everyayah.com/data/Alafasy_128kbps/112001.mp3", textWarsh = "قُلْ هُوَ اللَّهُ أَحَدٌ"),
            Ayah(112, 2, "اللَّهُ الصَّمَدُ", "السيد الكامل الذي تصمد وتتوجه إليه الخلائق كلها في حوائجها ورغائبها.", "https://everyayah.com/data/Alafasy_128kbps/112002.mp3", textWarsh = "اللَّهُ الصَّمَدُ"),
            Ayah(112, 3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "ليس له ولد ولا والد ولا صاحبة، لكمال غناه وأزليته.", "https://everyayah.com/data/Alafasy_128kbps/112003.mp3", textWarsh = "لَمْ يَلِدْ وَلَمْ يُولَدْ"),
            Ayah(112, 4, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "وليس له مكافئ ولا مثيل ولا شبيه في ذاته ولا أسمائه وصفاته.", "https://everyayah.com/data/Alafasy_128kbps/112004.mp3", textWarsh = "وَلَمْ يَكُن لَّهُ كُفُؤًا أَحَدٌ")
        ),
        113 to listOf(
            Ayah(113, 1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "قل: أعتصم وألتجئ برب الصبح ونور النهار بعد ظلمة الليل.", "https://everyayah.com/data/Alafasy_128kbps/113001.mp3", textWarsh = "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ"),
            Ayah(113, 2, "مِن شَرِّ مَا خَلَقَ", "من شر جميع المخلوقات المؤذية من الإنس والجن والهوام.", "https://everyayah.com/data/Alafasy_128kbps/113002.mp3", textWarsh = "مِن شَرِّ مَا خَلَقَ"),
            Ayah(113, 3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "ومن شر الليل الشديد الظلمة إذا دخل وغمر الكون بما فيه من الشرور.", "https://everyayah.com/data/Alafasy_128kbps/113003.mp3", textWarsh = "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ"),
            Ayah(113, 4, "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "ومن شر السواحر اللاتي يعقدن العقد وينفثن فيها بالسحر.", "https://everyayah.com/data/Alafasy_128kbps/113004.mp3", textWarsh = "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ"),
            Ayah(113, 5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "ومن شر كل حاسد يتمنى زوال النعمة عن غيره ويسعى في إيذائه.", "https://everyayah.com/data/Alafasy_128kbps/113005.mp3", textWarsh = "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ")
        ),
        114 to listOf(
            Ayah(114, 1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "قل: أتحصن وأستجير بخالق الناس ومدبر أمورهم وحافظهم.", "https://everyayah.com/data/Alafasy_128kbps/114001.mp3", textWarsh = "قُلْ أَعُوذُ بِرَبِّ النَّاسِ"),
            Ayah(114, 2, "مَلِكِ النَّاسِ", "ملكهم المتصرف فيهم بعدله وحكمته، لا مالك حقيقي غيره.", "https://everyayah.com/data/Alafasy_128kbps/114002.mp3", textWarsh = "مَلِكِ النَّاسِ"),
            Ayah(114, 3, "إِلَٰهِ النَّاسِ", "معبودهم الحق الذي لا معبود سواه ولا تنبغي العبادة إلا له.", "https://everyayah.com/data/Alafasy_128kbps/114003.mp3", textWarsh = "إِلَٰهِ النَّاسِ"),
            Ayah(114, 4, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "من شر الشيطان الذي يلقي الشبه والوساوس في القلب، ويخنس ويختفي عند ذكر الله.", "https://everyayah.com/data/Alafasy_128kbps/114004.mp3", textWarsh = "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ"),
            Ayah(114, 5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "الذي يزرع الشر والأوهام في قلوب بني آدم.", "https://everyayah.com/data/Alafasy_128kbps/114005.mp3", textWarsh = "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ"),
            Ayah(114, 6, "مِنَ الْجِنَّةِ وَالنَّاسِ", "شياطين الإنس والجن الذين يوسوسون بالباطل والمعاصي.", "https://everyayah.com/data/Alafasy_128kbps/114006.mp3", textWarsh = "مِنَ الْجِنَّةِ وَالنَّاسِ")
        ),
        2 to listOf(
            Ayah(2, 1, "الم", "حروف مقطعة للتحدي والإعجاز، تدل على أن القرآن مركب من جنس هذه الحروف العربية.", "https://everyayah.com/data/Alafasy_128kbps/002001.mp3", textWarsh = "الم"),
            Ayah(2, 2, "ذَٰلِكَ الْكِتَابُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِّلْمُتَّقِينَ", "هذا القرآن العظيم حق لا شك فيه بوجه من الوجوه، نزل هداية للمتقين الذين يتقون عذاب الله بطاعته.", "https://everyayah.com/data/Alafasy_128kbps/002002.mp3", textWarsh = "ذَٰلِكَ الْكِتَابُ لَا رَيْبَ فِيهِ هُدًى لِلْمُتَّقِينَ"),
            Ayah(2, 3, "الَّذِينَ يُؤْمِنُونَ بِالْغَيْبِ وَيُقِيمُونَ الصَّلَاةَ وَمِمَّا رَزَقْنَاهُمْ يُنفِقُونَ", "الذين يصدقون بما غاب عن حسهم من الإيمان بالله وملائكته وكتبه ورسله واليوم الآخر، ويؤدون الصلاة بأركانها وشروطها، وينفقون في سبيل الله.", "https://everyayah.com/data/Alafasy_128kbps/002003.mp3", textWarsh = "الَّذِينَ يُؤْمِنُونَ بِالْغَيْبِ وَيُقِيمُونَ الصَّلَاةَ وَمِمَّا رَزَقْنَاهُمْ يُنفِقُونَ"),
            Ayah(2, 255, "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ", "آية الكرسي، أعظم آية في كتاب الله؛ تضمنت إثبات توحيد الألوهية والربوبية والأسماء والصفات، وإحاطة علم الله، وكمال قيوميته وعظمته وقدرته.", "https://everyayah.com/data/Alafasy_128kbps/002255.mp3", textWarsh = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ وَلَا يَئُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ")
        ),
        67 to listOf(
            Ayah(67, 1, "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "تعاظم وكثر خير الله الذي بيده التصرف المطلق والملك في الدنيا والآخرة، وهو على كل شيء قدير.", "https://everyayah.com/data/Alafasy_128kbps/067001.mp3"),
            Ayah(67, 2, "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ الْعَزِيزُ الْغَفُورُ", "الذي أوجد الموت والحياة ليختبركم: أيكم أخلص وأصوب عملاً لله، وهو العزيز الذي لا يغلبه شيء، الغفور لمن تاب.", "https://everyayah.com/data/Alafasy_128kbps/067002.mp3")
        ),
        94 to listOf(
            Ayah(94, 1, "أَلَمْ نَشْرَحْ لَكَ صَدْرَكَ", "ألم نفسح لك يا محمد صدرك بنور النبوة والحكمة والإيمان؟", "https://everyayah.com/data/Alafasy_128kbps/094001.mp3"),
            Ayah(94, 2, "وَوَضَعْنَا عَنكَ وِزْرَكَ", "وحططنا عنك ما أثقل ظهرك من الهم والمسؤولية.", "https://everyayah.com/data/Alafasy_128kbps/094002.mp3"),
            Ayah(94, 5, "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا", "فإن مع الضيق والشدة فرجاً ويسراً عظيماً وملازماً له.", "https://everyayah.com/data/Alafasy_128kbps/094005.mp3"),
            Ayah(94, 6, "إِنَّ مَعَ الْعُسْرِ يُسْرًا", "تأكيد ووعد إلهي قاطع بأن العسر لا يغلب يسرين.", "https://everyayah.com/data/Alafasy_128kbps/094006.mp3")
        )
    )

    private val surahVersesCache = ConcurrentHashMap<Int, List<Ayah>>()

    fun getAyahAudioUrl(reciterId: String, surahNumber: Int, ayahNumber: Int): String {
        val folder = when (reciterId) {
            "frs_a" -> "Fares_Abbad_64kbps"
            "afs" -> "Alafasy_128kbps"
            "basit" -> "Abdul_Basit_Murattal_192kbps"
            "husr" -> "Husary_128kbps"
            "s_gmd" -> "Ghamadi_40kbps"
            "yasser" -> "Yasser_Ad-Dussary_128kbps"
            "a_jbr" -> "Ali_Jaber_64kbps"
            "ajm" -> "Ahmed_ibn_Ali_al-Ajamy_128kbps_ketaballah.net"
            "maher" -> "Maher_AlMuaiqly_64kbps"
            else -> "Alafasy_128kbps"
        }
        val s = String.format("%03d", surahNumber)
        val a = String.format("%03d", ayahNumber)
        return "https://everyayah.com/data/$folder/$s$a.mp3"
    }

    suspend fun fetchOnlineSurahVerses(surahNumber: Int, reciterId: String = "afs"): List<Ayah> {
        val cached = surahVersesCache[surahNumber]
        if (cached != null && cached.isNotEmpty()) return cached

        return withContext(Dispatchers.IO) {
            try {
                // Request authentic certified Uthmani script from Quran Cloud API
                val url = URL("https://api.alquran.cloud/v1/surah/$surahNumber/quran-uthmani")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 7000
                conn.readTimeout = 8000
                conn.requestMethod = "GET"
                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = reader.readText()
                    reader.close()
                    val json = JSONObject(response)
                    val data = json.getJSONObject("data")
                    val ayahsJson = data.getJSONArray("ayahs")
                    val list = mutableListOf<Ayah>()
                    for (i in 0 until ayahsJson.length()) {
                        val ayahObj = ayahsJson.getJSONObject(i)
                        val numInSurah = ayahObj.getInt("numberInSurah")
                        var text = ayahObj.getString("text")
                        // In AlQuran Cloud quran-uthmani, Bismillah may be prepended to ayah 1 for surahs > 1 (except At-Tawbah 9)
                        if (surahNumber != 1 && surahNumber != 9 && numInSurah == 1) {
                            val basmalahPrefix = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
                            val altPrefix = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
                            if (text.startsWith(basmalahPrefix)) {
                                text = text.removePrefix(basmalahPrefix).trim()
                            } else if (text.startsWith(altPrefix)) {
                                text = text.removePrefix(altPrefix).trim()
                            }
                        }
                        val audioUrl = getAyahAudioUrl(reciterId, surahNumber, numInSurah)
                        list.add(
                            Ayah(
                                surahNumber = surahNumber,
                                ayahNumber = numInSurah,
                                textArabic = text,
                                tafsir = "تدبر وتأمل في الآية الكريمة ($numInSurah) من سورة المباركة.",
                                audioUrl = audioUrl
                            )
                        )
                    }
                    if (list.isNotEmpty()) {
                        surahVersesCache[surahNumber] = list
                        return@withContext list
                    }
                }
            } catch (e: Exception) {
                // Return offline verified verses on network error
            }
            getVersesForSurah(surahNumber)
        }
    }

    fun getVersesForSurah(surahNumber: Int): List<Ayah> {
        val cached = surahVersesCache[surahNumber]
        if (cached != null && cached.isNotEmpty()) return cached
        return surahVerses[surahNumber] ?: generateDefaultVerses(surahNumber)
    }

    private fun generateDefaultVerses(surahNumber: Int): List<Ayah> {
        val surah = all114Surahs.find { it.number == surahNumber } ?: return emptyList()
        // Provide authentic initial verses for the surah without inventing AI text
        val list = mutableListOf<Ayah>()
        val audioUrl = getAyahAudioUrl("afs", surahNumber, 1)
        val initialText = if (surahNumber == 1) {
            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
        } else {
            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ • سورة ${surah.nameArabic} (يرجى الاتصال بالإنترنت لتحميل كامل السورة بالرسم العثماني المعتمد)"
        }
        list.add(
            Ayah(
                surahNumber = surahNumber,
                ayahNumber = 1,
                textArabic = initialText,
                tafsir = "تدبر في آيات سورة ${surah.nameArabic} المباركة.",
                audioUrl = audioUrl
            )
        )
        return list
    }

    // 24 Hourly Explained Verses (آية وتفسيرها الميسر لكل ساعة من ساعات اليوم)
    val hourlyExplainedVerses: List<HourlyAyahTafsir> = listOf(
        HourlyAyahTafsir(
            surahName = "البقرة",
            surahNumber = 2,
            ayahNumber = 255,
            textArabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
            tafsir = "سيدة آي القرآن وأعظمها؛ تثبت كمال الألوهية والربوبية لله الحي الذي لا يموت، القيوم القائم بتدبير خلقه، الذي لا يغلبه نعاس ولا نوم، وسع علمه وسلطانه السماوات والأرض ولا يثقله حفظهما.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/002255.mp3",
            hourLabel = "الساعة ١: تدبر آية الكرسي"
        ),
        HourlyAyahTafsir(
            surahName = "البقرة",
            surahNumber = 2,
            ayahNumber = 186,
            textArabic = "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ ۖ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ ۖ فَلْيَسْتَجِيبُوا لِي وَلْيُؤْمِنُوا بِي لَعَلَّهُمْ يَرْشُدُونَ",
            tafsir = "بشارة عظيمة بقرب الله ولطفه بعباده؛ يسمع نداءهم ويستجيب دعاءهم بفضله ورحمته، فينبغي للمؤمن أن يستجيب لأمر ربه ويداوم على الإيمان والدعاء ليهتدي للرشاد.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/002186.mp3",
            hourLabel = "الساعة ٢: قرب الله وإجابة الدعاء"
        ),
        HourlyAyahTafsir(
            surahName = "الشرح",
            surahNumber = 94,
            ayahNumber = 5,
            textArabic = "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا ۝ إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            tafsir = "وعد إلهي محكم؛ إن مع كل شدة وضيق فرجاً ويسراً عظيماً وملازماً له، ولن يغلب عسر واحد يسرين كريمين من فضل الله وإحسانه.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/094005.mp3",
            hourLabel = "الساعة ٣: فرج الله وتيسيره"
        ),
        HourlyAyahTafsir(
            surahName = "الطلاق",
            surahNumber = 65,
            ayahNumber = 2,
            textArabic = "وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا ۝ وَيَرْزُقْهُ مِنْ حَيْثُ لَا يَحْتَسِبُ ۚ وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ",
            tafsir = "من خاف الله واتقاه بامتثال أوامره واجتناب نواهيه، فتح الله له أبواب الفرج من كل ضيق ومأزق، ورزقه من وجوه لا تخطر بباله، ومن فوض أمره لله كفاه كل ما أهمه.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/065002.mp3",
            hourLabel = "الساعة ٤: ثمار التقوى والتوكل"
        ),
        HourlyAyahTafsir(
            surahName = "الإسراء",
            surahNumber = 17,
            ayahNumber = 78,
            textArabic = "أَقِمِ الصَّلَاةَ لِدُلُوكِ الشَّمْسِ إِلَىٰ غَسَقِ اللَّيْلِ وَقُرْآنَ الْفَجْرِ ۖ إِنَّ قُرْآنَ الْفَجْرِ كَانَ مَشْهُودًا",
            tafsir = "أمر بأداء الصلوات الخمس في أوقاتها؛ وخص صلاة الفجر وقراءتها بالذكر لأن ملائكة الليل وملائكة النهار يجتمعون ويشهدونها، ففيها البركة والنور والسكينة.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/017078.mp3",
            hourLabel = "الساعة ٥: نور صلاة الفجر"
        ),
        HourlyAyahTafsir(
            surahName = "طه",
            surahNumber = 20,
            ayahNumber = 130,
            textArabic = "فَاصْبِرْ عَلَىٰ مَا يَقُولُونَ وَسَبِّحْ بِحَمْدِ رَبِّكَ قَبْلَ طُلُوعِ الشَّمْسِ وَقَبْلَ غُرُوبِهَا ۖ وَمِنْ آنَاءِ اللَّيْلِ فَسَبِّحْ وَأَطْرَافَ النَّهَارِ لَعَلَّكَ تَرْضَىٰ",
            tafsir = "التوجيه النبوي الرباني بالصبر والاستعانة بالتسبيح وذكر الله في أطراف النهار وساعات الليل، فالتسبيح والذكر يملأ النفس طمأنينة ورضاً وسعادة.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/020130.mp3",
            hourLabel = "الساعة ٦: التسبيح ورضا النفس"
        ),
        HourlyAyahTafsir(
            surahName = "الضحى",
            surahNumber = 93,
            ayahNumber = 5,
            textArabic = "وَلَسَوْفَ يُعْطِيكَ رَبُّكَ فَتَرْضَىٰ",
            tafsir = "بشارة كريمة للنبي ﷺ ولأمته بالخير العظيم والفضل الوافر في الدنيا والآخرة حتى يبلغ تمام الرضا والسرور في جنات النعيم.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/093005.mp3",
            hourLabel = "الساعة ٧: العطاء الرباني والرضا"
        ),
        HourlyAyahTafsir(
            surahName = "الأنعام",
            surahNumber = 6,
            ayahNumber = 162,
            textArabic = "قُلْ إِنَّ صَلَاتِي وَنُسُكِي وَمَحْيَايَ وَمَمَاتِي لِلَّهِ رَبِّ الْعَالَمِينَ ۝ لَا شَرِيكَ لَهُ ۖ وَبِذَٰلِكَ أُمِرْتُ وَأَنَا أَوَّلُ الْمُسْلِمِينَ",
            tafsir = "إعلان الإخلاص التام لله وحده؛ فالعبادة والعمل والحياة والممات خالصة لوجه الله المنفرد بالملك والخلق، لا شريك له في شيء.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/006162.mp3",
            hourLabel = "الساعة ٨: إخلاص النية لله"
        ),
        HourlyAyahTafsir(
            surahName = "الرعد",
            surahNumber = 13,
            ayahNumber = 28,
            textArabic = "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ اللَّهِ ۗ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            tafsir = "القلوب المؤمنة تأنس وتستريح بذكر الله وتوحيده وكلامه، فحقيقة الطمأنينة وسكون الروح لا تكون إلا بالصلة برب العالمين وذكره الدائم.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/013028.mp3",
            hourLabel = "الساعة ٩: طمأنينة القلوب بالذكر"
        ),
        HourlyAyahTafsir(
            surahName = "إبراهيم",
            surahNumber = 14,
            ayahNumber = 7,
            textArabic = "وَإِذْ تَأَذَّنَ رَبُّكُمْ لَئِن شَكَرْتُمْ لَأَزِيدَنَّكُمْ ۖ وَلَئِن كَفَرْتُمْ إِنَّ عَذَابِي لَشَدِيدٌ",
            tafsir = "قاعدة ربانية راسخة؛ شكر نعم الله باللسان والقلب والعمل يوجب دوام النعم ومضاعفتها، أما الجحود فإنه سبب زوالها واستجلاب النقمة.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/014007.mp3",
            hourLabel = "الساعة ١٠: الشكر مفتاح الزيادة"
        ),
        HourlyAyahTafsir(
            surahName = "النحل",
            surahNumber = 16,
            ayahNumber = 97,
            textArabic = "مَنْ عَمِلَ صَالِحًا مِّن ذَكَرٍ أَوْ أُنثَىٰ وَهُوَ مُؤْمِنٌ فَلَنُحْيِيَنَّهُ حَيَاةً طَيِّبَةً ۖ وَلَنَجْزِيَنَّهُمْ أَجْرَهُم بِأَحْسَنِ مَا كَانُوا يَعْمَلُونَ",
            tafsir = "من أخلص العمل الصالح مقترناً بالإيمان بالله ورسوله، رزقه الله في الدنيا حياة مطمئنة مليئة بالقناعة والسكينة، وفي الآخرة جنات تجري من تحتها الأنهار.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/016097.mp3",
            hourLabel = "الساعة ١١: الحياة الطيبة بالإيمان"
        ),
        HourlyAyahTafsir(
            surahName = "الجمعة",
            surahNumber = 62,
            ayahNumber = 10,
            textArabic = "فَإِذَا قُضِيَتِ الصَّلَاةُ فَانتَشِرُوا فِي الْأَرْضِ وَابْتَغُوا مِن فَضْلِ اللَّهِ وَاذْكُرُوا اللَّهَ كَثِيرًا لَّعَلَّكُمْ تُفْلِحُونَ",
            tafsir = "توازن الإسلام بين العبادة والسعي للرزق الحلال؛ فإذا فرغ المسلم من صلاته فليسع في كسب معاشه متذكراً ربه في تجارته ومعاملاته ليفوز بالفلاح.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/062010.mp3",
            hourLabel = "الساعة ١٢: الجمع بين العمل والذكر"
        ),
        HourlyAyahTafsir(
            surahName = "الكهف",
            surahNumber = 18,
            ayahNumber = 10,
            textArabic = "إِذْ أَوَى الْفِتْيَةُ إِلَى الْكَهْفِ فَقَالُوا رَبَّنَا آتِنَا مِن لَّدُنكَ رَحْمَةً وَهَيِّئْ لَنَا مِنْ أَمْرِنَا رَشَدًا",
            tafsir = "دعاء أصحاب الكهف المبارك؛ التجأوا إلى الله وطلبوا منه الرحمة الشاملة والتوفيق والسداد في أمورهم، فآواهم الله ورفع ذكرهم وجعلهم آية للعالمين.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/018010.mp3",
            hourLabel = "الساعة ١٣: دعاء الرشد والرحمة"
        ),
        HourlyAyahTafsir(
            surahName = "الأنبياء",
            surahNumber = 21,
            ayahNumber = 87,
            textArabic = "لَّا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ",
            tafsir = "دعوة ذي النون (يونس عليه السلام) في بطن الحوت؛ ما دعا بها مكروب إلا فرج الله كربه؛ جمعت التوحيد وتنزيه الله والاعتراف بالتقصير والاستغفار.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/021087.mp3",
            hourLabel = "الساعة ١٤: مفتاح تفريج الكروب"
        ),
        HourlyAyahTafsir(
            surahName = "النور",
            surahNumber = 24,
            ayahNumber = 35,
            textArabic = "اللَّهُ نُورُ السَّمَاوَاتِ وَالْأَرْضِ ۚ مَثَلُ نُورِهِ كَمِشْكَاةٍ فِيهَا مِصْبَاحٌ ۖ الْمِصْبَاحُ فِي زُجَاجَةٍ ۖ الزُّجَاجَةُ كَأَنَّهَا كَوْكَبٌ دُرِّيٌّ",
            tafsir = "الله منور السماوات والأرض بنوره وبهداه، ومثل نوره في قلب عبده المؤمن كنور المصباح المشرق الصافي، يهدي الله لنوره من يشاء من عباده المخلصين.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/024035.mp3",
            hourLabel = "الساعة ١٥: آية النور والهداية"
        ),
        HourlyAyahTafsir(
            surahName = "الفرقان",
            surahNumber = 25,
            ayahNumber = 74,
            textArabic = "وَالَّذِينَ يَقُولُونَ رَبَّنَا هَبْ لَنَا مِنْ أَزْوَاجِنَا وَذُرِّيَّاتِنَا قُرَّةَ أَعْيُنٍ وَاجْعَلْنَا لِلْمُتَّقِينَ إِمَامًا",
            tafsir = "من صفات عباد الرحمن أنهم يسألون الله صلاح الأزواج والذرية بطاعتهم لله وتقواهم، وأن يجعلهم قدوة وأئمة في الهدى والخير والإحسان.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/025074.mp3",
            hourLabel = "الساعة ١٦: دعاء الأسرة والصلاح"
        ),
        HourlyAyahTafsir(
            surahName = "العنكبوت",
            surahNumber = 29,
            ayahNumber = 69,
            textArabic = "وَالَّذِينَ جَاهَدُوا فِينَا لَنَهْدِيَنَّهُمْ سُبُلَنَا ۚ وَإِنَّ اللَّهَ لَمَعَ الْمُحْسِنِينَ",
            tafsir = "من جاهد نفسه وهواه في طاعة الله، وفقه الله وأعانه وسدد خطاه إلى سبل الخير والصلاح، فمعية الله الخاصة بالنصر والتأييد للمحسنين.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/029069.mp3",
            hourLabel = "الساعة ١٧: هداية المجاهدين لأنفسهم"
        ),
        HourlyAyahTafsir(
            surahName = "الأحزاب",
            surahNumber = 33,
            ayahNumber = 41,
            textArabic = "يَا أَيُّهَا الَّذِينَ آمَنُوا اذْكُرُوا اللَّهَ ذِكْرًا كَثِيرًا ۝ وَسَبِّحُوهُ بُكْرَةً وَأَصِيلًا",
            tafsir = "أمر إلهي للمؤمنين بالإكثار من ذكر الله في كل أحوالهم، وتسبيحه في أول النهار وآخره، ليبقى القلب موصولاً بالخالق العظيم في الغداة والعشي.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/033041.mp3",
            hourLabel = "الساعة ١٨: أذكار المساء والتسبيح"
        ),
        HourlyAyahTafsir(
            surahName = "فصلت",
            surahNumber = 41,
            ayahNumber = 30,
            textArabic = "إِنَّ الَّذِينَ قَالُوا رَبُّنَا اللَّهُ ثُمَّ اسْتَقَامُوا تَتَنَزَّلُ عَلَيْهِمُ الْمَلَائِكَةُ أَلَّا تَخَافُوا وَلَا تَحْزَنُوا وَأَبْشِرُوا بِالْجَنَّةِ الَّتِي كُنتُمْ تُوعَدُونَ",
            tafsir = "بشارة لأهل الاستقامة على التوحيد والشريعة؛ تثبتهم الملائكة عند الموت وتؤمنهم من الخوف والحزن وتبشرهم برضوان الله والجنة.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/041030.mp3",
            hourLabel = "الساعة ١٩: فضل الاستقامة والبشرى"
        ),
        HourlyAyahTafsir(
            surahName = "الحجرات",
            surahNumber = 49,
            ayahNumber = 10,
            textArabic = "إِنَّمَا الْمُؤْمِنُونَ إِخْوَةٌ فَأَصْلِحُوا بَيْنَ أَخَوَيْكُمْ ۚ وَاتَّقُوا اللَّهَ لَعَلَّكُمْ تُرْحَمُونَ",
            tafsir = "الأخوة الإيمانية أعظم رابطة؛ توجب التراحم والتآلف والمبادرة بالإصلاح بين المتخاصمين بالعدل والإنصاف ابتغاء رحمة الله ورضوانه.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/049010.mp3",
            hourLabel = "الساعة ٢٠: رابطة الأخوة والمحبة"
        ),
        HourlyAyahTafsir(
            surahName = "الحديد",
            surahNumber = 57,
            ayahNumber = 4,
            textArabic = "هُوَ الَّذِي خَلَقَ السَّمَاوَاتِ وَالْأَرْضَ فِي سِتَّةِ أَيَّامٍ ثُمَّ اسْتَوَىٰ عَلَى الْعَرْشِ ۚ يَعْلَمُ مَا يَلِجُ فِي الْأَرْضِ وَمَا يَخْرُجُ مِنْهَا وَمَا يَنزِلُ مِنَ السَّمَاءِ وَمَا يَعْرُجُ فِيهَا ۖ وَهُوَ مَعَكُمْ أَيْنَ مَا كُنتُمْ ۚ وَاللَّهُ بِمَا تَعْمَلُونَ بَصِيرٌ",
            tafsir = "إحاطة علم الله ومراقبته؛ مطلع على خلقه في كل مكان وزمان، يرى أعمالهم ويسمع أقوالهم ويعلم سرائرهم، فالمؤمن يستشعر مراقبة الله في كل حين.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/057004.mp3",
            hourLabel = "الساعة ٢١: مراقبة الله ومعيته"
        ),
        HourlyAyahTafsir(
            surahName = "الملك",
            surahNumber = 67,
            ayahNumber = 1,
            textArabic = "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ ۝ الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ الْعَزِيزُ الْغَفُورُ",
            tafsir = "افتتاح السورة المنجية من عذاب القبر؛ تبين عظمة ملك الله وقدرته، وأن حكمة خلق الموت والحياة هي ابتلاء العباد: أيهم أخلص صواباً وأحسن عملاً.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/067001.mp3",
            hourLabel = "الساعة ٢٢: تدبر سورة الملك"
        ),
        HourlyAyahTafsir(
            surahName = "السجدة",
            surahNumber = 32,
            ayahNumber = 16,
            textArabic = "تَتَجَافَىٰ جُنُوبُهُمْ عَنِ الْمَضَاجِعِ يَدْعُونَ رَبَّهُمْ خَوْفًا وَطَمَعًا وَمِمَّا رَزَقْنَاهُمْ يُنفِقُونَ ۝ فَلَا تَعْلَمُ نَفْسٌ مَّا أُخْفِيَ لَهُم مِّن قُرَّةِ أَعْيُنٍ جَزَاءً بِمَا كَانُوا يَعْمَلُونَ",
            tafsir = "فضل قيام الليل وصلاة الوتر؛ يتركون لذيذ النوم لمناجاة ربهم رغبة في رحمته وخشية من عذابه، فأعد الله لهم ما لا عين رأت ولا أذن سمعت من النعيم.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/032016.mp3",
            hourLabel = "الساعة ٢٣: فضل قيام الليل ومناجاة الله"
        ),
        HourlyAyahTafsir(
            surahName = "الزمر",
            surahNumber = 39,
            ayahNumber = 53,
            textArabic = "قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنفُسِهِمْ لَا تَقْنَطُوا مِن رَّحْمَةِ اللَّهِ ۚ إِنَّ اللَّهَ يَغْفِرُ الذُّنُوبَ جَمِيعًا ۚ إِنَّهُ هُوَ الْغَفُورُ الرَّحِيمُ",
            tafsir = "أرجى آية في كتاب الله؛ نداء ملؤه اللطف والرحمة لمن كثرت ذنوبهم ألا ييأسوا من مغفرة الله، فالتوبة الصادقة تمحو ما قبلها والله غفور رحيم.",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/039053.mp3",
            hourLabel = "الساعة ٢٤: باب التوبة والمغفرة"
        )
    )

    fun getHourlyVerse(hourOffset: Int = 0): HourlyAyahTafsir {
        val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val targetIndex = Math.floorMod(hourOfDay + hourOffset, hourlyExplainedVerses.size)
        return hourlyExplainedVerses[targetIndex]
    }

    // 24 Hourly Explained Hadiths (تحديث وتفسير الأحاديث النبوية الشريفة كل ساعة تلقائياً)
    val hourlyExplainedHadiths: List<HourlyHadithExplanation> = listOf(
        HourlyHadithExplanation(
            id = 1,
            hourOfDay = 0,
            title = "الإخلاص وحضور النية في سائر الأعمال",
            textArabic = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى، فَمَنْ كَانَتْ هِجْرَتُهُ إِلَى اللَّهِ وَرَسُولِهِ، فَهِجْرَتُهُ إِلَى اللَّهِ وَرَسُولِهِ، وَمَنْ كَانَتْ هِجْرَتُهُ إِلَى دُنْيَا يُصِيبُهَا أَوْ إِلَى امْرَأَةٍ يَنْكِحُهَا، فَهِجْرَتُهُ إِلَى مَا هَاجَرَ إِلَيْهِ.",
            narrator = "أمير المؤمنين عمر بن الخطاب رضي الله عنه",
            source = "صحيح البخاري ومسلم",
            grading = "متفق عليه",
            explanation = "أصل عظيم وقاعدة كبرى من قواعد الإسلام؛ فالنية هي ميزان الأعمال الباطنة ومحول العادات إلى عبادات، وبها يثاب العبد أو يحرم. فمن قصد بعمله وجه الله نال الأجر والقبول، ومن قصد الرياء والشهرة كان حظه ما قصد.",
            keyLessons = listOf(
                "استحضار الإخلاص لله في كل حركة وسكون وأمر ديني ودنيوي",
                "صلاح العمل وقبوله مرتبط بصدق القصد وطهارة السريرة",
                "التحذير من الرياء والسمعة وإرادة الدنيا بعمل الآخرة"
            ),
            hourLabel = "الساعة ١: مدار الأعمال على النيات"
        ),
        HourlyHadithExplanation(
            id = 2,
            hourOfDay = 1,
            title = "فضل الصلوات الخمس وتكفير الخطايا والذنوب",
            textArabic = "مَا مِنْ امْرِئٍ مُسْلِمٍ تَحْضُرُهُ صَلَاةٌ مَكْتُوبَةٌ فَيُحْسِنُ وُضُوءَهَا وَخُشُوعَهَا وَرُكُوعَهَا، إِلَّا كَانَتْ كَفَّارَةً لِمَا قَبْلَهَا مِنْ الذُّنُوبِ مَا لَمْ يُؤْتِ كَبِيرَةً، وَذَلِكَ الدَّهْرَ كُلَّهُ.",
            narrator = "عثمان بن عفان رضي الله عنه",
            source = "صحيح مسلم (٢٢٨)",
            grading = "صحيح",
            explanation = "الصلاة هي عماد الدين وركنه الأعظم بعد الشهادتين؛ فإذا حافظ المسلم على إسباغ الوضوء وأقبل بقلبه خاشعاً لله متمماً للركوع والسجود، جعل الله صلاته ممحاة لخطاياه السالفة ونوراً في قلبه ووجهه.",
            keyLessons = listOf(
                "إسباغ الوضوء على المكاره ومراعاة سننه وآدابه",
                "حضور القلب والخشوع جوهر الصلاة وسر قبولها",
                "فضل الله العميم بتكفير الصغائر مع المحافظة على الفرائض"
            ),
            hourLabel = "الساعة ٢: الصلوات الخمس كفارات"
        ),
        HourlyHadithExplanation(
            id = 3,
            hourOfDay = 2,
            title = "بناء الإسلام على أركانه الخمسة",
            textArabic = "بُنِيَ الإِسْلاَمُ عَلَى خَمْسٍ: شَهَادَةِ أَنْ لاَ إِلَهَ إِلاَّ اللَّهُ وَأَنَّ مُحَمَّدًا رَسُولُ اللَّهِ، وَإِقَامِ الصَّلاَةِ، وَإِيتَاءِ الزَّكَاةِ، وَالحَجِّ، وَصَوْمِ رَمَضَانَ.",
            narrator = "عبد الله بن عمر رضي الله عنهما",
            source = "صحيح البخاري (٨) ومسلم (١٦)",
            grading = "متفق عليه",
            explanation = "شبه النبي ﷺ دين الإسلام بالبنيان المحكم الشامخ القائم على خمس دعائم متينة؛ أصلها وأساسها التوحيد الخالص لله والإقرار برسالته لنبيه، ثم أداء الصلاة والزكاة والصيام وحج البيت لمن استطاع.",
            keyLessons = listOf(
                "التوحيد هو الأساس الذي لا يصح عمل بدونه",
                "الأركان الخمسة متلازمة ولا يكتمل إسلام العبد إلا بها",
                "أهمية تعلم فقه هذه الأركان وتطبيقها في الحياة"
            ),
            hourLabel = "الساعة ٣: دعائم الإيمان وأركان الإسلام"
        ),
        HourlyHadithExplanation(
            id = 4,
            hourOfDay = 3,
            title = "فضل تلاوة القرآن الكريم وتعلّمه وتعليمه",
            textArabic = "خَيْرُكُمْ مَنْ تَعَلَّمَ القُرْآنَ وَعَلَّمَهُ.",
            narrator = "عثمان بن عفان رضي الله عنه",
            source = "صحيح البخاري (٥٠٢٧)",
            grading = "صحيح",
            explanation = "أرفع الناس رتبة وخيرهم منزلة عند الله من اشتغل بكلام الله عز وجل تلاوة وتدبراً وفهماً وعملاً، ثم بذل وسعه في نشره وتعليمه للناس ابتغاء مرضاة الله.",
            keyLessons = listOf(
                "القرآن الكريم شرف الأمة وعزها وهدايتها",
                "الجمع بين تعلم القرآن وتعليمه هو قمة الخيرية",
                "المداومة على ورد يومي من القراءة والتدبر"
            ),
            hourLabel = "الساعة ٤: خيرية أهل القرآن"
        ),
        HourlyHadithExplanation(
            id = 5,
            hourOfDay = 4,
            title = "منزلة حسن الخلق والقرب من النبي ﷺ",
            textArabic = "إِنَّ مِنْ أَحَبِّكُمْ إِلَيَّ وَأَقْرَبِكُمْ مِنِّي مَجْلِسًا يَوْمَ القِيَامَةِ أَحَاسِنَكُمْ أَخْلاقًا.",
            narrator = "جابر بن عبد الله رضي الله عنهما",
            source = "سنن الترمذي (٢٠١٨)",
            grading = "حسن",
            explanation = "حسن الخلق يثقل ميزان العبد ويدرك به درجة الصائم القائم؛ وهو كف الأذى، وبذل الندى، وطلاقة الوجه، ومحبة الخير للناس، والتواضع والصفح.",
            keyLessons = listOf(
                "حسن الخلق أعظم ما يوضع في ميزان العبد يوم القيامة",
                "محبة النبي ﷺ والقرب منه تنال بمكارم الأخلاق",
                "التحلي بالصبر والحلم عند التعامل مع الخلق"
            ),
            hourLabel = "الساعة ٥: حسن الخلق وأثره الأخروي"
        ),
        HourlyHadithExplanation(
            id = 6,
            hourOfDay = 5,
            title = "حياة القلوب وسكينتها بذكر الله تعالى",
            textArabic = "مَثَلُ الَّذِي يَذْكُرُ رَبَّهُ وَالَّذِي لاَ يَذْكُرُ رَبَّهُ، مَثَلُ الحَيِّ وَالمَيِّتِ.",
            narrator = "أبو موسى الأشعري رضي الله عنه",
            source = "صحيح البخاري (٦٤٠٧)",
            grading = "صحيح",
            explanation = "الذكر للقلب كالماء للسمك، فكيف يكون حال السمك إذا فارق الماء؟! الذاكر لله حي القلب مستنير البصيرة محاط بالملائكة، والغافل ميت الروح وإن كان يمشي بين الأحياء.",
            keyLessons = listOf(
                "المحافظة على أذكار الصباح والمساء وأدبار الصلوات",
                "ذكر الله طمأنينة للقلب وجلاء للهموم وتفريج للكرب",
                "الحذر من الغفلة ومجالس اللغو"
            ),
            hourLabel = "الساعة ٦: حياة الروح بذكر الله"
        ),
        HourlyHadithExplanation(
            id = 7,
            hourOfDay = 6,
            title = "محبة الخير للمسلمين وعلامة كمال الإيمان",
            textArabic = "لاَ يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لِأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ.",
            narrator = "أنس بن مالك رضي الله عنه",
            source = "صحيح البخاري (١٣) ومسلم (٤٥)",
            grading = "متفق عليه",
            explanation = "نفي لكمال الإيمان الواجب؛ فلا يستقر الإيمان الحق في قلب عبد إلا إذا صفا قلبه من الغل والحسد، وأحب لجميع إخوانه المسلمين من الخير والصلاح والبركة ما يتمناه لنفسه.",
            keyLessons = listOf(
                "سلامة الصدر ونقاء القلب من الحسد والبغضاء",
                "الفرح بنعم الله على عباده والدعاء لهم بالبركة",
                "معاونة المسلمين وقضاء حوائجهم"
            ),
            hourLabel = "الساعة ٧: سلامة الصدر ومحبة الخير"
        ),
        HourlyHadithExplanation(
            id = 8,
            hourOfDay = 7,
            title = "فضل ملازمة الاستغفار وبشارة الفرج وسعة الرزق",
            textArabic = "مَنْ لَزِمَ الاسْتِغْفَارَ جَعَلَ اللَّهُ لَهُ مِنْ كُلِّ ضِيقٍ مَخْرَجًا، وَمِنْ كُلِّ هَمٍّ فَرَجًا، وَرَزَقَهُ مِنْ حَيْثُ لا يَحْتَسِبُ.",
            narrator = "عبد الله بن عباس رضي الله عنهما",
            source = "سنن أبي داود (١٥١٨)",
            grading = "حسن",
            explanation = "الاستغفار مفتاح كل خير ومغلاق كل شر؛ يزيل الران عن القلوب، ويجلب الغيث والبركات والذرية الطيبة، ويدفع البلاء والمحن ويورث سكينة النفس.",
            keyLessons = listOf(
                "الإكثار من سيد الاستغفار وأستغفر الله وأتوب إليه",
                "الاستغفار سبب مباشر لتفريج الكروب والهموم الدنيوية",
                "ملازمة الاستغفار في الأسحار وأدبار الصلوات"
            ),
            hourLabel = "الساعة ٨: مفاتيح الفرج بالاستغفار"
        ),
        HourlyHadithExplanation(
            id = 9,
            hourOfDay = 8,
            title = "فضل طلب العلم الشرعي وتيسير طريق الجنة",
            textArabic = "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا، سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الجَنَّةِ، وَإِنَّ المَلاَئِكَةَ لَتَضَعُ أَجْنِحَتَهَا رِضًا لِطَالِبِ العِلْمِ.",
            narrator = "أبو هريرة رضي الله عنه",
            source = "صحيح مسلم (٢٦٩٩)",
            grading = "صحيح",
            explanation = "العلم نور وبصيرة يقود صاحبه إلى معرفة الله وتوحيده وعبادته على بصيرة، والملائكة تبسط أجنحتها إجلالاً وإكراماً لطالب العلم لما في سعيه من إحياء للدين ونفع للخلق.",
            keyLessons = listOf(
                "طلب العلم الشرعي واجب بقدر ما تصح به العبادة",
                "فضل مدارسة الفقه والتفسير والحديث النبوي",
                "العمل بالعلم ونشره ابتغاء وجه الله"
            ),
            hourLabel = "الساعة ٩: طريق الجنة بطلب العلم"
        ),
        HourlyHadithExplanation(
            id = 10,
            hourOfDay = 9,
            title = "وصية جامعة: تقوى الله، الحسنة بعد السيئة، وحسن الخلق",
            textArabic = "اتَّقِ اللَّهِ حَيْثُمَا كُنْتَ، وَأَتْبِعِ السَّيِّئَةَ الحَسَنَةَ تَمْحُهَا، وَخَالِقِ النَّاسَ بِخُلُقٍ حَسَنٍ.",
            narrator = "أبو ذر ومعاذ بن جبل رضي الله عنهما",
            source = "سنن الترمذي (١٩٨٧)",
            grading = "حسن صحيح",
            explanation = "وصية نبوية موجزة جمعت حق الله وحق النفس وحق العباد؛ فتقوى الله في السر والعلن حق الله، ومحو السيئة بالتوبة والعمل الصالح حق النفس، والإحسان للناس حقهم.",
            keyLessons = listOf(
                "مراقبة الله عز وجل في الخلوات كما في الجلوات",
                "المسارعة إلى الحسنات والتوبة إثر الوقوع في أي زلة",
                "معاملة الناس باللطف والتواضع وطلاقة الوجه"
            ),
            hourLabel = "الساعة ١٠: ثلاثية النجاة النبوية"
        ),
        HourlyHadithExplanation(
            id = 11,
            hourOfDay = 10,
            title = "حفظ أوامر الله وتفويض الأمور والتوكل عليه وحده",
            textArabic = "احْفَظِ اللَّهَ يَحْفَظْكَ، احْفَظِ اللَّهَ تَجِدْهُ تُجَاهَكَ، إِذَا سَأَلْتَ فَاسْأَلِ اللَّهَ، وَإِذَا اسْتَعَنْتَ فَاسْتَعِنْ بِاللَّهِ، وَاعْلَمْ أَنَّ الأُمَّةَ لَوْ اجْتَمَعَتْ عَلَى أَنْ يَنْفَعُوكَ بِشَيْءٍ لَمْ يَنْفَعُوكَ إِلَّا بِشَيْءٍ قَدْ كَتَبَهُ اللَّهُ لَكَ.",
            narrator = "عبد الله بن عباس رضي الله عنهما",
            source = "سنن الترمذي (٢٥١٦)",
            grading = "صحيح",
            explanation = "من أعظم وصايا العقيدة والتوكل؛ من حفظ حدود الله وأوامره ونواهيه كان الله معه بالحفظ والتوفيق والمعونة في كل شدة، ولا تنفع الحيلة أمام قدر الله ومشيئته.",
            keyLessons = listOf(
                "تعليق القلب بالله وحده في جلب النفع ودفع الضر",
                "اليقين الجازم بأن النصر مع الصبر وأن مع العسر يسراً",
                "الاستعانة التامة بالله وسؤاله في كل صغيرة وكبيرة"
            ),
            hourLabel = "الساعة ١١: ثمار حفظ الله والتوكل عليه"
        ),
        HourlyHadithExplanation(
            id = 12,
            hourOfDay = 11,
            title = "فضل الطهور، والحمد لله، والتسبيح، والصبر نور",
            textArabic = "الطُّهُورُ شَطْرُ الإِيمَانِ، وَالحَمْدُ لِلَّهِ تَمْلأُ المِيزَانَ، وَسُبْحَانَ اللَّهِ وَالحَمْدُ لِلَّهِ تَمْلَآنِ مَا بَيْنَ السَّمَاوَاتِ وَالأَرْضِ، وَالصَّلاَةُ نُورٌ، وَالصَّدَقَةُ بُرْهَانٌ، وَالصَّبْرُ ضِيَاءٌ، وَالقُرْآنُ حُجَّةٌ لَكَ أَوْ عَلَيْكَ.",
            narrator = "أبو مالك الأشعري رضي الله عنه",
            source = "صحيح مسلم (٢٢٣)",
            grading = "صحيح",
            explanation = "بيان لعظمة العبادات وأجورها؛ الطهارة نصف الإيمان، والحمد لله كلمتان تملآن ميزان الحسنات، والصلاة تنير درب المؤمن، والصدقة حجة على صدق إيمانه، والصبر ضياء لا يخبو.",
            keyLessons = listOf(
                "العناية بالطهارة الحسية من الأحداث والمعنوية من الذنوب",
                "المداومة على التحميد والتسبيح ملء السماوات والأرض",
                "الصبر على أقدار الله المؤلمة وعن معاصيه وفي طاعته"
            ),
            hourLabel = "الساعة ١٢: موازين الحسنات ومنازل الصبر"
        ),
        HourlyHadithExplanation(
            id = 13,
            hourOfDay = 12,
            title = "حقيقة المسلم والمؤمن وحفظ اللسان واليد",
            textArabic = "المُسْلِمُ مَنْ سَلِمَ المُسْلِمُونَ مِنْ لِسَانِهِ وَيَدِهِ، وَالمُؤْمِنُ مَنْ أَمِنَهُ النَّاسُ عَلَى دِمَائِهِمْ وَأَمْوَالِهِمْ.",
            narrator = "عبد الله بن عمرو رضي الله عنهما",
            source = "صحيح البخاري (١٠) ومسلم (٤٠)",
            grading = "متفق عليه",
            explanation = "المسلم الحقيقي هو الذي يفيض أماناً وسلاماً على مجتمعه؛ فلا يؤذي أحداً بغيبة أو نميمة أو شتم أو بهتان، ولا يعتدي على أحد بيد أو بطش أو أخذ مال بالباطل.",
            keyLessons = listOf(
                "خطر آفات اللسان والغيبة والنميمة على حسنات العبد",
                "كف الأذى عن المسلمين واجب شرعي أساسي",
                "إشاعة الأمان والصدق وحفظ الأمانات"
            ),
            hourLabel = "الساعة ١٣: سلامة الجوارح وعصمة الدماء"
        ),
        HourlyHadithExplanation(
            id = 14,
            hourOfDay = 13,
            title = "النهي عن التحاسد والتباغض وأمر المسلمين بالأخوة",
            textArabic = "لاَ تَحَاسَدُوا، وَلاَ تَنَاجَشُوا، وَلاَ تَبَاغَضُوا، وَلاَ تَدَابَرُوا، وَلاَ يَبِعْ بَعْضُكُمْ عَلَى بَيْعِ بَعْضٍ، وَكُونُوا عِبَادَ اللَّهِ إِخْوَانًا، المُسْلِمُ أَخُو المُسْلِمِ: لاَ يَظْلِمُهُ، وَلاَ يَخْذُلُهُ، وَلاَ يَحْقِرُهُ.",
            narrator = "أبو هريرة رضي الله عنه",
            source = "صحيح مسلم (٢٥٦٤)",
            grading = "صحيح",
            explanation = "دستور نبوي عظيم يحمي النسيج الاجتماعي للمسلمين من أمراض القلوب والأنانية والتنافر، ويؤكد على أن التقوى هاهنا في القلب وليست بالمظاهر الدنيوية.",
            keyLessons = listOf(
                "اجتناب الحسد والمكر والغش والتدابر",
                "نصرة المسلم لأخيه بالحق وعدم خذلانه عند الشدائد",
                "النهي عن احتقار أي مسلم أو الاستعلاء عليه"
            ),
            hourLabel = "الساعة ١٤: حقوق الأخوة الإيمانية"
        ),
        HourlyHadithExplanation(
            id = 15,
            hourOfDay = 14,
            title = "سعة رحمة الله وبشارة قبول التوبة ومحو الذنوب",
            textArabic = "التَّائِبُ مِنَ الذَّنْبِ كَمَنْ لاَ ذَنْبَ لَهُ، وَإِذَا أَحَبَّ اللَّهُ عَبْدًا لَمْ يَضُرَّهُ ذَنْبٌ، ثُمَّ تَلَا: ﴿إِنَّ اللَّهَ يُحِبُّ التَّوَّابِينَ وَيُحِبُّ الْمُتَطَهِّرِينَ﴾.",
            narrator = "عبد الله بن مسعود رضي الله عنه",
            source = "سنن ابن ماجه (٤٢٥٠)",
            grading = "حسن",
            explanation = "التوبة الصادقة المستوفية لشروطها من الإقلاع والندم والعزم على عدم العود ورد المظالم، تطهر الصحيفة حتى يرجع العبد نقياً كيوم ولدته أمه برحمة الله وفضله.",
            keyLessons = listOf(
                "تجديد التوبة والاستغفار في كل وقت وحين",
                "عدم اليأس من رحمة الله مهما عظمت الذنوب والخطايا",
                "رد المظالم إلى أهلها واستحلال أصحاب الحقوق"
            ),
            hourLabel = "الساعة ١٥: فضائل التوبة النصوح"
        ),
        HourlyHadithExplanation(
            id = 16,
            hourOfDay = 15,
            title = "فضل الصلاة والتسليم على النبي المصطفى ﷺ",
            textArabic = "مَنْ صَلَّى عَلَيَّ صَلاَةً، صَلَّى اللَّهُ عَلَيْهِ بِهَا عَشْرًا، وَحُطَّتْ عَنْهُ عَشْرُ خَطِيئَاتٍ، وَرُفِعَتْ لَهُ عَشْرُ دَرَجَاتٍ.",
            narrator = "أنس بن مالك رضي الله عنه",
            source = "سنن النسائي (١٢٩٧)",
            grading = "صحيح",
            explanation = "الصلاة على النبي ﷺ كنز عظيم؛ يكفي الله بها الهموم، ويغفر الذنوب، وينال العبد ثناء الله عليه في الملأ الأعلى وشفاعة النبي الكريم ﷺ يوم القيامة.",
            keyLessons = listOf(
                "الإكثار من الصلاة الإبراهيمية لاسيما في يوم الجمعة وليلتها",
                "الصلاة على النبي ﷺ سبب لإجابة الدعاء وتفريج الكروب",
                "البخل الحقيقي هو من ذكر عنده النبي ﷺ ولم يصل عليه"
            ),
            hourLabel = "الساعة ١٦: بركات الصلاة على الحبيب ﷺ"
        ),
        HourlyHadithExplanation(
            id = 17,
            hourOfDay = 16,
            title = "أفضل الذكر لا إله إلا الله وأفضل الدعاء الحمد لله",
            textArabic = "أَفْضَلُ الذِّكْرِ لاَ إِلَهَ إِلاَّ اللَّهُ، وَأَفْضَلُ الدُّعَاءِ الحَمْدُ لِلَّهِ.",
            narrator = "جابر بن عبد الله رضي الله عنهما",
            source = "سنن الترمذي (٣٣٨٣)",
            grading = "حسن",
            explanation = "لا إله إلا الله هي كلمة التوحيد وحبل النجاة والعروة الوثقى، والحمد لله هو رأس الشكر وثناؤه سبحانه على نعمائه المتتابعة، فالجمع بينهما جمع بين التوحيد والاعتراف بالفضل.",
            keyLessons = listOf(
                "ترطيب اللسان بكلمة التوحيد في كل حال",
                "الشكر والحمد يستجلب المزيد من النعم الإلهية",
                "استفتاح الأدعية بالحمد والثناء والصلاة على النبي ﷺ"
            ),
            hourLabel = "الساعة ١٧: درر التهليل والتحميد"
        ),
        HourlyHadithExplanation(
            id = 18,
            hourOfDay = 17,
            title = "البكاء من خشية الله والحراسة في سبيل الله",
            textArabic = "عَيْنَانِ لاَ تَمَسُّهُمَا النَّارُ: عَيْنٌ بَكَتْ مِنْ خَشْيَةِ اللَّهِ، وَعَيْنٌ بَاتَتْ تَحْرُسُ فِي سَبِيلِ اللَّهِ.",
            narrator = "عبد الله بن عباس رضي الله عنهما",
            source = "سنن الترمذي (١٦٣٩)",
            grading = "صحيح",
            explanation = "دمعة صادقة تسيل في خلوة خوفاً من الله ورجاءً لرحمته كفيلة بإطفاء بحار من لهيب النيران؛ والجهاد والحراسة لحماية ثغور المسلمين وأمنهم من أسمى القربات.",
            keyLessons = listOf(
                "استحضار عظمة الله في الخلوات واستدرار الدمع بالخشوع",
                "فضل حراسة الثغور وحماية أوطان المسلمين وأعراضهم",
                "الجمع بين الخوف من عذاب الله والرجاء في رحمته"
            ),
            hourLabel = "الساعة ١٨: منزلة الخشية والحراسة"
        ),
        HourlyHadithExplanation(
            id = 19,
            hourOfDay = 18,
            title = "محبة الله للرفق ولين الجانب في كل شأن",
            textArabic = "إِنَّ اللَّهَ رَفِيقٌ يُحِبُّ الرِّفْقَ، وَيُعْطِي عَلَى الرِّفْقِ مَا لاَ يُعْطِي عَلَى العُنْفِ وَمَا لاَ يُعْطِي عَلَى مَا سِوَاهُ، مَا كَانَ الرِّفْقُ فِي شَيْءٍ إِلَّا زَانَهُ، وَلاَ نُزِعَ مِنْ شَيْءٍ إِلَّا شَانَهُ.",
            narrator = "أم المؤمنين عائشة رضي الله عنها",
            source = "صحيح مسلم (٢٥٩٣)",
            grading = "صحيح",
            explanation = "الرفق هو اللين في القول والفعل والأخذ بالأسهل في كل أمر من غير إخلال؛ وهو مفتاح القلوب وزينة السلوك، بينما العنف والغلظة تنفر النفوس وتفسد العلاقات.",
            keyLessons = listOf(
                "التعامل باللين والرفق مع الأهل والأبناء والناس أجمعين",
                "الحلم عند الغضب وكظم الغيظ والتروي قبل اتخاذ القرارات",
                "الدعوة إلى الله بالحكمة والموعظة الحسنة"
            ),
            hourLabel = "الساعة ١٩: جمال الرفق وثماره"
        ),
        HourlyHadithExplanation(
            id = 20,
            hourOfDay = 19,
            title = "حفظ اللسان وإكرام الجار والضيف من علامات الإيمان",
            textArabic = "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَاليَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ، وَمَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَاليَوْمِ الآخِرِ فَلْيُكْرِمْ جَارَهُ، وَمَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَاليَوْمِ الآخِرِ فَلْيُكْرِمْ ضَيْفَهُ.",
            narrator = "أبو هريرة رضي الله عنه",
            source = "صحيح البخاري (٦٠١٨) ومسلم (٤٧)",
            grading = "متفق عليه",
            explanation = "اللسان جِرمه صغير وجُرمه كبير؛ فإذا أراد المسلم أن يتكلم فليفكر في عاقبة كلامه، فإن كان خيراً تكلم وإن كان شراً أو لا فائدة فيه أمسك وسلم.",
            keyLessons = listOf(
                "الصمت حكمة وسلامة عند خشية اللغو أو الخطأ",
                "الإحسان إلى الجار ورعاية حقوقه وكف الأذى عنه",
                "إكرام الضيف من أمارات الشهامة والإيمان الصادق"
            ),
            hourLabel = "الساعة ٢٠: أدب اللسان وحقوق الجوار"
        ),
        HourlyHadithExplanation(
            id = 21,
            hourOfDay = 20,
            title = "الكلمتان الحبيبتان إلى الرحمن وثقلهما في الميزان",
            textArabic = "كَلِمَتَانِ خَفِيفَتَانِ عَلَى اللِّسَانِ، ثَقِيلَتَانِ فِي المِيزَانِ، حَبِيبَتَانِ إِلَى الرَّحْمَنِ: سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ العَظِيمِ.",
            narrator = "أبو هريرة رضي الله عنه",
            source = "صحيح البخاري (٦٦٨٢) ومسلم (٢٦٩٤)",
            grading = "متفق عليه",
            explanation = "خاتمة صحيح البخاري؛ أيسر الذكر وأعظمه أجراً، يجمع بين تنزيه الله عن كل نقص (التسبيح) وإثبات صفات الكمال والجلال له (التحميد والعظمة)، فينال العبد بهما محبة الرحمن.",
            keyLessons = listOf(
                "المداومة على هذا الذكر العظيم طوال اليوم والليلة",
                "مراعاة حضور المعنى والتأمل عند التلفظ بالتسبيح",
                "سعة فضل الله بتضعيف الأجر الجزيل على العمل اليسير"
            ),
            hourLabel = "الساعة ٢١: حبيبتان إلى الرحمن"
        ),
        HourlyHadithExplanation(
            id = 22,
            hourOfDay = 21,
            title = "الدعاء بالثبات على الدين في زمن الفتن والتقلبات",
            textArabic = "كَانَ رَسُولُ اللَّهِ ﷺ يُكْثِرُ أَنْ يَقُولَ: «يَا مُقَلِّبَ القُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ»، فَقِيلَ: يَا رَسُولَ اللَّهِ وَإِنَّ القُلُوبَ لَتَتَقَلَّبُ؟ قَالَ: «نَعَمْ، مَا مِنْ قَلْبٍ إِلَّا بَيْنَ أُصْبُعَيْنِ مِنْ أَصَابِعِ اللَّهِ يُقَلِّبُهُ كَيْفَ يَشَاءُ».",
            narrator = "أنس بن مالك وأم سلمة رضي الله عنهما",
            source = "سنن الترمذي (٢١٤٠)",
            grading = "صحيح",
            explanation = "القلب سريع التقلب والتحول؛ فلا يأمن أحد على دينه وإيمانه مهما بلغ من العلم والعبادة، بل يظل ملحاً على ربه بالثبات والهداية حتى يلقاه وهو راضٍ عنه.",
            keyLessons = listOf(
                "كثرة التضرع بدعاء الثبات في السجود وأدبار الصلوات",
                "الافتقار الدائم إلى هداية الله وتوفيقه",
                "الابتعاد عن مواطن الشبهات والفتن المؤثرة في القلب"
            ),
            hourLabel = "الساعة ٢٢: دعاء الثبات على الصراط"
        ),
        HourlyHadithExplanation(
            id = 23,
            hourOfDay = 22,
            title = "أسرار الخشوع في السجود وأقرب ما يكون العبد من ربه",
            textArabic = "أَقْرَبُ مَا يَكُونُ العَبْدُ مِنْ رَبِّهِ وَهُوَ سَاجِدٌ، فَأَكْثِرُوا الدُّعَاءَ، فَقَمِنٌ أَنْ يُسْتَجَابَ لَكُمْ.",
            narrator = "أبو هريرة رضي الله عنه",
            source = "صحيح مسلم (٤٨٢)",
            grading = "صحيح",
            explanation = "السجود هو غاية التواضع والخضوع لله سبحانه، بوضع أشرف ما في الإنسان (وجهه وجبهته) على الأرض إجلالاً للخالق، ولذا كان أقرب المواضع من رحمة الله وأجدرها بالإجابة.",
            keyLessons = listOf(
                "استثمار موضع السجود في الدعاء بخيري الدنيا والآخرة",
                "الإخبات والتذلل لله ومناجاته بطلب المغفرة والهدى",
                "إطالة السجود والخشوع في صلوات الفريضة والنافلة"
            ),
            hourLabel = "الساعة ٢٣: مناجاة السجود وإجابة الدعاء"
        ),
        HourlyHadithExplanation(
            id = 24,
            hourOfDay = 23,
            title = "شرف المؤمن بقيام الليل وأفضل الصلاة بعد المكتوبة",
            textArabic = "أَفْضَلُ الصِّيَامِ بَعْدَ شَهْرِ رَمَضَانَ صِيَامُ شَهْرِ اللَّهِ المُحَرَّمِ، وَأَفْضَلُ الصَّلاَةِ بَعْدَ الفَرِيضَةِ صَلاَةُ اللَّيْلِ.",
            narrator = "أبو هريرة رضي الله عنه",
            source = "صحيح مسلم (١١٦٣)",
            grading = "صحيح",
            explanation = "صلاة الليل شرف المؤمن وسر طمأنينته؛ فيها الخلوة التامة بالحبيب سبحانه بعيداً عن أعين الناس والرياء، وتتنزل فيها الرحمات والنفحات في الثلث الأخير من الليل.",
            keyLessons = listOf(
                "الحرص على ركعات من صلاة الوتر والتهجد كل ليلة",
                "اغتنام الثلث الأخير من الليل في الاستغفار وسؤال الله",
                "قيام الليل مطردة للداء عن الجسد ونور للوجه في النهار"
            ),
            hourLabel = "الساعة ٢٤: شرف قيام الليل والوتر"
        )
    )

    fun getHourlyHadith(hourOffset: Int = 0): HourlyHadithExplanation {
        val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val targetIndex = Math.floorMod(hourOfDay + hourOffset, hourlyExplainedHadiths.size)
        return hourlyExplainedHadiths[targetIndex]
    }

    val dailyVerse = Ayah(
        surahNumber = hourlyExplainedVerses[0].surahNumber,
        ayahNumber = hourlyExplainedVerses[0].ayahNumber,
        textArabic = hourlyExplainedVerses[0].textArabic,
        tafsir = hourlyExplainedVerses[0].tafsir,
        audioUrl = hourlyExplainedVerses[0].audioUrl
    )

    val hadithsList: List<Hadith> = listOf(
        Hadith(1, "الإخلاص والنية", "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى، فَمَنْ كَانَتْ هِجْرَتُهُ إِلَى دُنْيَا يُصِيبُهَا أَوْ إِلَى امْرَأَةٍ يَنْكِحُهَا، فَهِجْرَتُهُ إِلَى مَا هَاجَرَ إِلَيْهِ.", "عمر بن الخطاب رضي الله عنه", "صحيح البخاري", "1", "متفق عليه"),
        Hadith(2, "فضل الصلاة", "مَا مِنْ امْرِئٍ مُسْلِمٍ تَحْضُرُهُ صَلَاةٌ مَكْتُوبَةٌ فَيُحْسِنُ وُضُوءَهَا وَخُشُوعَهَا وَرُكُوعَهَا، إِلَّا كَانَتْ كَفَّارَةً لِمَا قَبْلَهَا مِنْ الذُّنُوبِ مَا لَمْ يُؤْتِ كَبِيرَةً، وَذَلِكَ الدَّهْرَ كُلَّهُ.", "عثمان بن عفان رضي الله عنه", "صحيح مسلم", "233", "صحيح"),
        Hadith(3, "أركان الإسلام", "بُنِيَ الإِسْلاَمُ عَلَى خَمْسٍ: شَهَادَةِ أَنْ لاَ إِلَهَ إِلاَّ اللَّهُ وَأَنَّ مُحَمَّدًا رَسُولُ اللَّهِ، وَإِقَامِ الصَّلاَةِ، وَإِيتَاءِ الزَّكَاةِ، وَالحَجِّ، وَصَوْمِ رَمَضَانَ.", "عبد الله بن عمر رضي الله عنهما", "صحيح البخاري", "8", "متفق عليه"),
        Hadith(4, "فضل القرآن", "خَيْرُكُمْ مَنْ تَعَلَّمَ القُرْآنَ وَعَلَّمَهُ.", "عثمان بن عفان رضي الله عنه", "صحيح البخاري", "5027", "صحيح"),
        Hadith(5, "حسن الخلق", "إِنَّ مِنْ أَحَبِّكُمْ إِلَيَّ وَأَقْرَبِكُمْ مِنِّي مَجْلِسًا يَوْمَ القِيَامَةِ أَحَاسِنَكُمْ أَخْلاقًا.", "جابر بن عبد الله رضي الله عنهما", "سنن الترمذي", "3559", "حسن"),
        Hadith(6, "ذكر الله والصلة به", "مَثَلُ الَّذِي يَذْكُرُ رَبَّهُ وَالَّذِي لاَ يَذْكُرُ رَبَّهُ، مَثَلُ الحَيِّ وَالمَيِّتِ.", "أبو موسى الأشعري رضي الله عنه", "صحيح البخاري", "6407", "صحيح"),
        Hadith(7, "محبة الخير للمسلمين", "لاَ يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لِأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ.", "أنس بن مالك رضي الله عنه", "صحيح البخاري", "13", "متفق عليه"),
        Hadith(8, "الاستغفار والتوبة", "مَنْ لَزِمَ الاسْتِغْفَارَ جَعَلَ اللَّهُ لَهُ مِنْ كُلِّ ضِيقٍ مَخْرَجًا، وَمِنْ كُلِّ هَمٍّ فَرَجًا، وَرَزَقَهُ مِنْ حَيْثُ لا يَحْتَسِبُ.", "عبد الله بن عباس رضي الله عنهما", "سنن أبي داود", "2699", "حسن")
    )

    val hadithList: List<Hadith> get() = hadithsList

    val dailyHadith: Hadith = hadithsList[0]

    val morningAdhkar: List<Dhikr> = listOf(
        Dhikr(
            id = 101,
            category = "أذكار الصباح",
            text = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ.",
            count = 1,
            fadl = "استفتاح اليوم بتوحيد الله والاعتراف بملكه وفضله.",
            reference = "مسلم 658"
        ),
        Dhikr(
            id = 102,
            category = "أذكار الصباح",
            text = "اللَّهُمَّ أَنْتَ رَبِّي لاَ إِلَهَ إِلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لاَ يَغْفِرُ الذُّنُوبَ إِلاَّ أَنْتَ.",
            count = 1,
            fadl = "سيد الاستغفار: من قاله موقناً به ومات دخل الجنة.",
            reference = "البخاري 6306"
        ),
        Dhikr(
            id = 103,
            category = "أذكار الصباح",
            text = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ: عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ.",
            count = 3,
            fadl = "يعدل أضعافاً مضاعفة من أجر التسبيح طوال اليوم.",
            reference = "مسلم 2723"
        ),
        Dhikr(
            id = 104,
            category = "أذكار الصباح",
            text = "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لاَ إِلَهَ إِلاَّ أَنْتَ.",
            count = 3,
            fadl = "سؤال العافية التامة في الجوارح والأبدان.",
            reference = "أبو داود 3391"
        ),
        Dhikr(
            id = 105,
            category = "أذكار الصباح",
            text = "بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.",
            count = 3,
            fadl = "حرز ووقاية من كل سوء وفجأة بلاء.",
            reference = "الترمذي 5082"
        ),
        Dhikr(
            id = 106,
            category = "أذكار الصباح",
            text = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالإِسْلاَمِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا.",
            count = 3,
            fadl = "كان حقاً على الله أن يرضيه يوم القيامة.",
            reference = "الترمذي 5088"
        ),
        Dhikr(
            id = 107,
            category = "أذكار الصباح",
            text = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ، أَصْلِحْ لِي شَأْنِي كُلَّهُ، وَلاَ تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ.",
            count = 3,
            fadl = "طلب الرعاية الربانية وتفويض الأمور لله تعالى.",
            reference = "الحاكم 18967"
        ),
        Dhikr(
            id = 108,
            category = "أذكار الصباح",
            text = "أَصْبَحْنَا عَلَى فِطْرَةِ الإِسْلاَمِ، وَعَلَى كَلِمَةِ الإِخْلاَصِ، وَعَلَى دِينِ نَبِيِّنَا مُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ.",
            count = 1,
            fadl = "تجديد العهد مع الله على التوحيد وسنة نبيه.",
            reference = "أحمد 10405"
        ),
        Dhikr(
            id = 109,
            category = "أذكار الصباح",
            text = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ.",
            count = 100,
            fadl = "حطت خطاياه وإن كانت مثل زبد البحر.",
            reference = "مسلم 2726"
        ),
        Dhikr(
            id = 110,
            category = "أذكار الصباح",
            text = "حَسْبِيَ اللَّهُ لاَ إِلَهَ إِلاَّ هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ.",
            count = 7,
            fadl = "كفاه الله ما أهمه من أمر دنياه وآخرته.",
            reference = "أبو داود 5081"
        )
    )

    val eveningAdhkar: List<Dhikr> = listOf(
        Dhikr(
            id = 201,
            category = "أذكار المساء",
            text = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ.",
            count = 1,
            fadl = "شكر الله والثناء عليه في نهاية النهار ومطلع الليل.",
            reference = "مسلم 658"
        ),
        Dhikr(
            id = 202,
            category = "أذكار المساء",
            text = "اللَّهُمَّ أَنْتَ رَبِّي لاَ إِلَهَ إِلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لاَ يَغْفِرُ الذُّنُوبَ إِلاَّ أَنْتَ.",
            count = 1,
            fadl = "سيد الاستغفار: من قاله حين يمسي فمات دخل الجنة.",
            reference = "البخاري 6306"
        ),
        Dhikr(
            id = 203,
            category = "أذكار المساء",
            text = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ.",
            count = 3,
            fadl = "لم يضره شيء حتى يصبح ووقاية من الحشرات والهوام.",
            reference = "مسلم 2709"
        ),
        Dhikr(
            id = 204,
            category = "أذكار المساء",
            text = "بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.",
            count = 3,
            fadl = "لم تصبه فجأة بلاء حتى يصبح.",
            reference = "الترمذي 5082"
        ),
        Dhikr(
            id = 205,
            category = "أذكار المساء",
            text = "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لاَ إِلَهَ إِلاَّ أَنْتَ.",
            count = 3,
            fadl = "سؤال الصحة وسلامة الحواس من الآفات.",
            reference = "أبو داود 3391"
        ),
        Dhikr(
            id = 206,
            category = "أذكار المساء",
            text = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالإِسْلاَمِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا.",
            count = 3,
            fadl = "كان حقاً على الله أن يرضيه يوم يلقاه.",
            reference = "الترمذي 5088"
        ),
        Dhikr(
            id = 207,
            category = "أذكار المساء",
            text = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ، أَصْلِحْ لِي شَأْنِي كُلَّهُ، وَلاَ تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ.",
            count = 3,
            fadl = "استمطار الرحمة والتوكل التام على الحي القيوم.",
            reference = "الحاكم 18967"
        ),
        Dhikr(
            id = 208,
            category = "أذكار المساء",
            text = "أَمْسَيْنَا عَلَى فِطْرَةِ الإِسْلاَمِ، وَعَلَى كَلِمَةِ الإِخْلاَصِ، وَعَلَى دِينِ نَبِيِّنَا مُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ.",
            count = 1,
            fadl = "الثبات على ملة الإسلام وإخلاص الدين لله.",
            reference = "أحمد 10405"
        ),
        Dhikr(
            id = 209,
            category = "أذكار المساء",
            text = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ.",
            count = 100,
            fadl = "لم يأت أحد يوم القيامة بأفضل مما جاء به إلا من زاد عليه.",
            reference = "مسلم 2726"
        ),
        Dhikr(
            id = 210,
            category = "أذكار المساء",
            text = "حَسْبِيَ اللَّهُ لاَ إِلَهَ إِلاَّ هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ.",
            count = 7,
            fadl = "كفاه الله ما أهمه صادقاً كان بها أو كاذباً.",
            reference = "أبو داود 5081"
        )
    )

    val otherAzkar: List<Dhikr> = listOf(
        Dhikr(301, "أذكار الاستيقاظ", "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ.", 1, "شكر الله على نعمة الحياة بعد النوم.", "البخاري"),
        Dhikr(401, "أذكار النوم", "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي وَبِكَ أَرْفَعُهُ، إِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ.", 1, "حفظ النفس والروح في المنام.", "البخاري ومسلم"),
        Dhikr(501, "أذكار بعد الصلاة", "أَسْتَغْفِرُ اللَّهَ (3 مرات)، اللَّهُمَّ أَنْتَ السَّلاَمُ وَمِنْكَ السَّلاَمُ تَبَارَكْتَ يَا ذَا الْجَلاَلِ وَالإِكْرَامِ.", 1, "سنة مؤكدة بعد كل صلاة مكتوبة.", "مسلم"),
        Dhikr(502, "التسبيح والتحميد", "سُبْحَانَ اللَّهِ (33) والْحَمْدُ لِلَّهِ (33) واللَّهُ أَكْبَرُ (33) لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ.", 33, "غفرت خطاياه ولو كانت مثل زبد البحر.", "مسلم"),
        Dhikr(601, "دعاء الخروج من المنزل", "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ، وَلاَ حَوْلَ وَلاَ قُوَّةَ إِلاَّ بِاللَّهِ.", 3, "يقال له: هُديت وكُفيت ووُقيت.", "الترمذي: 201")
    )

    val azkarList: List<Dhikr> = morningAdhkar + eveningAdhkar + otherAzkar

    val dailyDhikr: Dhikr = morningAdhkar[0]

    val defaultCities: List<City> = listOf(
        City("الجزائر العاصمة", "Algiers", "الجزائر", 36.7538, 3.0588, 1.0),
        City("وهران", "Oran", "الجزائر", 35.6976, -0.6337, 1.0),
        City("قسنطينة", "Constantine", "الجزائر", 36.3650, 6.6147, 1.0),
        City("عنابة", "Annaba", "الجزائر", 36.9000, 7.7667, 1.0),
        City("سطيف", "Setif", "الجزائر", 36.1911, 5.4137, 1.0),
        City("باتنة", "Batna", "الجزائر", 35.5558, 6.1741, 1.0),
        City("تلمسان", "Tlemcen", "الجزائر", 34.8783, -1.3150, 1.0),
        City("مكة المكرمة", "Makkah", "السعودية", 21.4225, 39.8262, 3.0),
        City("المدينة المنورة", "Madinah", "السعودية", 24.4672, 39.6111, 3.0),
        City("القدس الشريف", "Jerusalem", "فلسطين", 31.7683, 35.2137, 2.0),
        City("القاهرة", "Cairo", "مصر", 30.0444, 31.2357, 2.0),
        City("الرياض", "Riyadh", "السعودية", 24.7136, 46.6753, 3.0),
        City("دبي", "Dubai", "الإمارات", 25.2048, 55.2708, 4.0),
        City("إسطنبول", "Istanbul", "تركيا", 41.0082, 28.9784, 3.0),
        City("الدار البيضاء", "Casablanca", "المغرب", 33.5731, -7.5898, 1.0),
        City("تونس العاصمة", "Tunis", "تونس", 36.8065, 10.1815, 1.0),
        City("لندن", "London", "بريطانيا", 51.5074, -0.1278, 0.0),
        City("باريس", "Paris", "فرنسا", 48.8566, 2.3522, 1.0)
    )

    fun getAzkarByCategory(category: String): List<Dhikr> {
        return when (category) {
            "أذكار الصباح" -> morningAdhkar
            "أذكار المساء" -> eveningAdhkar
            "أذكار النوم" -> otherAzkar.filter { it.category == "أذكار النوم" }
            "أذكار بعد الصلاة" -> otherAzkar.filter { it.category == "أذكار بعد الصلاة" }
            else -> azkarList.filter { it.category.contains(category) || category == "الكل" }.ifEmpty { azkarList }
        }
    }
}
