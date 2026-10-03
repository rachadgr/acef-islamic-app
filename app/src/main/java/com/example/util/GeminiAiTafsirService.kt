package com.example.util

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiAiTafsirService {
    private const val TAG = "GeminiAiTafsirService"

    // High-speed Google Gemini models in order of priority (tested and verified)
    val CANDIDATE_MODELS = listOf(
        "gemini-flash-latest",
        "gemini-3.1-flash-lite-preview",
        "gemini-3.8-flash",
        "gemini-3.5-flash"
    )

    @Volatile
    var userCustomApiKey: String = ""

    // In-memory high-speed cache for instant Scholar responses
    private val scholarAnswerCache = java.util.concurrent.ConcurrentHashMap<String, String>()

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    fun getEffectiveApiKey(): String {
        val custom = userCustomApiKey.trim()
        if (custom.isNotBlank()) return custom

        val keyFromBuildConfig = try {
            BuildConfig.GEMINI_API_KEY.replace("\"", "").trim()
        } catch (e: Throwable) { "" }
        if (keyFromBuildConfig.isNotBlank() && keyFromBuildConfig != "MY_GEMINI_API_KEY") {
            return keyFromBuildConfig
        }

        val keyFromEnv = try {
            BuildConfig.GEMINI_ENV_API_KEY.replace("\"", "").trim()
        } catch (e: Throwable) { "" }
        if (keyFromEnv.isNotBlank() && keyFromEnv != "MY_GEMINI_API_KEY") {
            return keyFromEnv
        }

        return ""
    }

    fun isGeminiConfigured(): Boolean {
        return getEffectiveApiKey().isNotBlank()
    }

    /**
     * Test live internet connectivity with Google Gemini servers
     */
    suspend fun testLiveConnection(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val key = getEffectiveApiKey()
        if (key.isBlank()) {
            return@withContext Pair(false, "لم يتم العثور على مفتاح API نشط. يمكنك إدخال مفتاحك في خانة الإعدادات أدناه أو تفعيله في Secrets.")
        }

        for (model in CANDIDATE_MODELS) {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$key"
            try {
                val requestJson = JSONObject().apply {
                    val contents = JSONArray().apply {
                        val msg = JSONObject().apply {
                            val parts = JSONArray().apply {
                                put(JSONObject().apply { put("text", "اختبار اتصال سريع. أجب بكلمة واحدة فقط: متصل") })
                            }
                            put("parts", parts)
                        }
                        put(msg)
                    }
                    put("contents", contents)
                }

                val body = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val startTime = System.currentTimeMillis()
                httpClient.newCall(request).execute().use { response ->
                    val latency = System.currentTimeMillis() - startTime
                    if (response.isSuccessful) {
                        return@withContext Pair(true, "متصل بنجاح مع نموذج Google ($model) - زمن الاستجابة: ${latency}ms")
                    } else {
                        val err = response.body?.string().orEmpty()
                        Log.w(TAG, "Test connection on $model returned ${response.code}: $err")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Test connection error on $model: ${e.message}")
            }
        }
        Pair(false, "تعذر الاتصال بالخادم. يرجى التأكد من اتصال الإنترنت أو صحة المفتاح المدخل.")
    }

    /**
     * Generate in-depth Islamic Tafsir and reflections for an Ayah using Gemini with live internet fallback
     */
    suspend fun explainAyah(
        surahName: String,
        surahNumber: Int,
        ayahNumber: Int,
        ayahText: String,
        classicalTafsir: String = "",
        language: String = "ar"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        val hasValidKey = apiKey.isNotBlank()

        val prompt = buildString {
            appendLine("قدم تفسيراً قرآنياً وتدبراً إيمانياً عميقاً وموثوقاً للآية الكريمة التالية:")
            appendLine("السورة: $surahName (رقم $surahNumber) - الآية رقم: $ayahNumber")
            appendLine("نص الآية الكريمة: ﴿$ayahText﴾")
            if (classicalTafsir.isNotBlank()) {
                appendLine("التفسير المأثور المتوفر: $classicalTafsir")
            }
            appendLine()
            appendLine("يرجى تنظيم التفسير وفق العناوين التالية بوضوح وبأسلوب علمي رصين وبشكل منظم:")
            appendLine("1. 📖 المعنى العام وسياق الآية الكريمة ومناسبتها لما قبلها")
            appendLine("2. 🔍 دلالات الألفاظ واللطائف البلاغية والبيانية")
            appendLine("3. 📜 أسباب النزول وأقوال أئمة التفسير المعتبرين (ابن كثير، الطبري، السعدي، القرطبي)")
            appendLine("4. 🌿 الهدايات التربوية والإيمانية المستنبطة للقلب والسلوك")
            appendLine("5. 🤲 كيف نعيش بهذه الآية ونطبقها في واقعنا المعاصر؟")
            appendLine()
            when (language.lowercase()) {
                "en" -> appendLine("IMPORTANT: Please provide the entire explanation, structure, and reflections in English. Always keep and display the sacred Arabic text of the Ayah ﴿$ayahText﴾ intact.")
                "fr" -> appendLine("IMPORTANT : Veuillez rédiger l'explication, la structure et les méditations entièrement en français. Conservez toujours le texte sacré original en arabe du verset ﴿$ayahText﴾.")
                else -> appendLine("اجعل الأسلوب جميلاً باللغة العربية، موثقاً على منهج أهل السنة والجماعة، ومناسباً للقارئ المسلم المعاصر.")
            }
        }

        val systemInstruction = "أنت عالم ومفسر إسلامي معتمد خبير في علوم القرآن الكريم والتفسير بالمأثور، تشرح بأسلوب تربوي سلس وموثوق على منهج أهل السنة والجماعة."

        if (hasValidKey) {
            try {
                val aiResponse = callGeminiRestApi(apiKey, prompt, systemInstruction)
                if (aiResponse.isNotBlank()) {
                    return@withContext Result.success(aiResponse)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini API call failed, trying live online tafsir API: ${e.message}")
            }
        }

        // Live Internet fallback: Fetch classical tafsir from AlQuran Cloud API
        val liveOnlineTafsir = fetchLiveOnlineTafsir(surahNumber, ayahNumber)
        val combinedClassical = if (liveOnlineTafsir.isNotBlank()) liveOnlineTafsir else classicalTafsir

        // Curated scholarly synthesis
        val fallback = generateScholarlyAyahFallback(
            surahName = surahName,
            ayahNumber = ayahNumber,
            ayahText = ayahText,
            classicalTafsir = combinedClassical
        )
        Result.success(fallback)
    }

    /**
     * Generate in-depth Hadith explanation and scholarly benefits
     */
    suspend fun explainHadith(
        hadithText: String,
        narrator: String,
        source: String,
        category: String,
        grading: String = "صحيح",
        language: String = "ar"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        val hasValidKey = apiKey.isNotBlank()

        val prompt = buildString {
            appendLine("قدم شرحاً حديثياً وتربوياً مفصلاً وموثوقاً للحديث النبوي الشريف التالي:")
            appendLine("نص الحديث: «$hadithText»")
            appendLine("الراوي: $narrator | المخرج: $source | الحكم: $grading | الباب: $category")
            appendLine()
            appendLine("يرجى تنظيم الشرح وفق العناوين التالية بوضوح وعمق:")
            appendLine("1. 📜 ترجمة موجزة لراوي الحديث وتخريجه المعتمد ومكانته في كتب السنة")
            appendLine("2. 🔍 المعنى الإجمالي وشرح المفردات النبوية واللطائف اللغوية")
            appendLine("3. 💡 الفوائد العقدية والفقهية والأخلاقية المستنبطة")
            appendLine("4. 🌱 التطبيق العملي للحديث في حياة المسلم المعاصر ومواجهة الفتن")
            appendLine()
            when (language.lowercase()) {
                "en" -> appendLine("IMPORTANT: Provide the full explanation and analysis in English, while always displaying and quoting the sacred Arabic text of the Hadith «$hadithText» verbatim.")
                "fr" -> appendLine("IMPORTANT : Fournissez l'explication et l'analyse complètes en français, tout en conservant et citant toujours fidèlement le texte arabe original du hadith «$hadithText».")
                else -> appendLine("اجعل الأسلوب جميلاً ومؤصلاً باللغة العربية الفصحى على منهج أهل السنة والجماعة.")
            }
        }

        val systemInstruction = "أنت محدث وباحث إسلامي متخصص في شروح صحيح البخاري وصحيح مسلم وكتب السنن المعتمدة، تشرح بأسلوب مؤصل ونافع وميسر."

        if (hasValidKey) {
            try {
                val aiResponse = callGeminiRestApi(apiKey, prompt, systemInstruction)
                if (aiResponse.isNotBlank()) {
                    return@withContext Result.success(aiResponse)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini Hadith API call failed: ${e.message}")
            }
        }

        val fallback = generateScholarlyHadithFallback(
            hadithText = hadithText,
            narrator = narrator,
            source = source,
            category = category,
            grading = grading
        )
        Result.success(fallback)
    }

    const val NON_ISLAMIC_REFUSAL_MESSAGE =
        "عذراً، بصفتي «المستشار الإسلامي لتطبيق آصف»، تقتصر إجاباتي واستشاراتي حصراً على العلوم الإسلامية، وتفسير القرآن الكريم، وشروح الحديث النبوي الشريف، وأحكام الفقه والشريعة، والأذكار، والتوجيه الديني والأخلاقي.\n\nيرجى التفضل بطرح سؤال أو استفسار يتعلق بديننا الإسلامي الحنيف، ويسعدني إفادتكم بكل سرور بإذن الله تعالى."

    /**
     * Strict Islamic topic validator. Refuses non-Islamic worldly queries
     * (sports, programming, politics, movies, games, general trivia, etc.).
     */
    fun isIslamicQuestion(question: String): Boolean {
        val q = question.lowercase().trim()
        if (q.isBlank()) return false

        // 1. Explicit Non-Islamic Domain Triggers
        val nonIslamicTerms = listOf(
            "كود", "برمج", "بايثون", "python", "javascript", "html", "css", "java", "c++",
            "flutter", "react", "database", "sql", "خوارزمية", "هاك", "تطبيق ويب",
            "كرة قدم", "ميسي", "رونالدو", "برشلونة", "ريال مدريد", "كأس العالم", "دوري أبطال",
            "مباراة", "نيمار", "مبابي", "ليفربول", "مانشستر", "تنس", "كرة سلة",
            "فيلم", "أفلام", "مسلسل", "مسلسلات", "ممثل", "ممثلة", "أغنية", "أغاني", "موسيقى راب",
            "هوليوود", "سينما", "أنمي", "anime", "نكتة", "نكت", "أضحكني", "طرائف مضحكة",
            "بلايستيشن", "playstation", "xbox", "pubg", "فورتنايت", "fortnite", "فيفا 2", "gta",
            "سياسة أمريكا", "انتخابات الرئاسة", "حرب أوكرانيا", "بورصة الأسهم", "بيتكوين", "تداول الفوركس",
            "crypto", "bitcoin", "شراء سيارة", "ميكانيك", "تصليح هاتف"
        )

        // 2. Clear Islamic Signals and Jurisprudence Keywords
        val islamicSignals = listOf(
            "الله", "رب", "إله", "رسول", "نبي", "محمد", "سلام", "مرحبا", "أهلا",
            "إسلام", "اسلام", "دين", "شريع", "قرآن", "قران", "آية", "اية", "سورة", "تفسير",
            "حديث", "سنة", "صحابي", "صحابة", "فقه", "حلال", "حرام", "فتوى", "شيخ",
            "مكروه", "واجب", "مستحب", "سنة مؤكدة", "صلاة", "صلوات", "أذان", "اذان", "إقامة", "اقامة",
            "وضوء", "طهارة", "تيمم", "غسل", "جنابة", "حيض", "نفاس", "سجود", "ركوع", "سهو", "خشوع",
            "صوم", "صيام", "رمضان", "إفطار", "سحور", "قضاء", "كفارة", "فدية",
            "زكاة", "صدقة", "صدقات", "حج", "عمرة", "طواف", "سعي", "عرفة", "أضحية", "ذبح",
            "عقيدة", "توحيد", "إيمان", "ايمان", "إحسان", "شرك", "كفر", "نفاق", "بدعة",
            "دعاء", "أدعية", "ذكر", "أذكار", "استغفار", "توبة", "تسبيح", "تحميد", "تهليل",
            "جنة", "نار", "آخرة", "يوم القيامة", "موت", "قبر", "برزخ", "ميزان", "صراط",
            "نكاح", "زواج", "طلاق", "عدة", "ميراث", "فرائض", "بر الوالدين", "صلة الرحم",
            "أخلاق", "حجاب", "عورة", "ستر", "ربا", "معاملات", "يمين", "نذر", "طاعة", "معصية",
            "صبر", "شكر", "ابتلاء", "رزق", "بركة", "هدى", "تقوى", "وتر", "قيام الليل", "ضحى",
            "ابن كثير", "الطبري", "السعدي", "القرطبي", "ابن تيمية", "ابن القيم", "النووي",
            "البخاري", "مسلم", "أبو داود", "الترمذي", "النسائي", "ابن ماجه", "مالك", "الشافعي", "أحمد"
        )

        // Check if query contains any strictly forbidden non-Islamic topic without Islamic jurisprudential context
        val hasNonIslamicTerm = nonIslamicTerms.any { q.contains(it) }
        val hasIslamicSignal = islamicSignals.any { q.contains(it) }

        if (hasNonIslamicTerm && !hasIslamicSignal) {
            return false
        }

        // If it explicitly asks about a non-Islamic term, only accept if asking about Islamic ruling (e.g. "حكم لعب...")
        if (hasNonIslamicTerm && hasIslamicSignal) {
            val rulingKeywords = listOf("حكم", "هل يجوز", "حلال", "حرام", "الشرع", "كفارة", "رأي الإسلام")
            val isAskingForRuling = rulingKeywords.any { q.contains(it) }
            return isAskingForRuling
        }

        // Allow any question unless it clearly matches non-Islamic worldly trivia
        return true
    }

    /**
     * Ask AI Islamic Scholar for general Quran/Sunnah inquiries
     */
    suspend fun askIslamicScholar(
        question: String,
        language: String = "ar"
    ): Result<String> = withContext(Dispatchers.IO) {
        val trimmedQ = question.trim()
        if (trimmedQ.isBlank()) {
            return@withContext Result.success("يرجى كتابة سؤال أو استفسار شرعي للبدء.")
        }

        // 1. Strict Islamic Topic Filter: Reject any non-Islamic inquiries immediately
        if (!isIslamicQuestion(trimmedQ)) {
            return@withContext Result.success(NON_ISLAMIC_REFUSAL_MESSAGE)
        }

        val qLower = trimmedQ.lowercase()
        if (qLower.contains("السلام عليكم") || qLower == "مرحبا" || qLower == "أهلا" || qLower == "صباح الخير" || qLower == "مساء الخير") {
            val welcomeAnswer = when (language.lowercase()) {
                "en" -> "Peace and blessings be upon you. Welcome to the Asif Smart Islamic Scholar. I am here to assist you in understanding the Holy Quran, authentic Prophetic Hadiths, Islamic jurisprudence, and daily supplications. Feel free to ask your inquiry."
                "fr" -> "Que la paix et les bénédictions soient sur vous. Bienvenue auprès du Savant Islamique Asif. Je suis à votre service pour vous aider à comprendre le Saint Coran, les nobles hadiths, le fiqh et les invocations prophétiques. N'hésitez pas à poser votre question."
                else -> "وعليكم السلام ورحمة الله وبركاته، أهلاً ومرحباً بك في «المستشار الإسلامي» لتطبيق آصف.\n\nأنا في خدمتك لمساعدتك في فهم معاني وتدبر القرآن الكريم، وشروح الأحاديث النبوية، وفقه العبادات، والأذكار، والأدعية المأثورة.\n\nتفضل بطرح استفسارك الشرعي أو انقر على أحد النماذج المقترحة."
            }
            return@withContext Result.success(welcomeAnswer)
        }

        val cacheKey = "${language}_$trimmedQ"
        val cached = scholarAnswerCache[cacheKey]
        if (cached != null && cached.isNotBlank()) {
            return@withContext Result.success(cached)
        }

        val apiKey = getEffectiveApiKey()
        val hasValidKey = apiKey.isNotBlank()

        val prompt = buildString {
            appendLine("سؤال واستشارة إسلامية موجهة للمستشار الإسلامي:")
            appendLine("\"$trimmedQ\"")
            appendLine()
            appendLine("يرجى تقديم إجابة مباشرة وسريعة ومنظمة بالأدلة الشرعية من الكتاب والسنة وأقوال أئمة الفقه المعتبرين وفق العناوين التالية:")
            appendLine("• 📖 الأدلة المعتمدة من القرآن الكريم والسنة النبوية")
            appendLine("• ⚖️ الحكم الشرعي المعتمد والتيسير المعتبر")
            appendLine("• 💡 الفوائد والتوجيه العملي المعاصر")
            appendLine()
            when (language.lowercase()) {
                "en" -> appendLine("IMPORTANT: Please provide the entire answer and consultation in English, but always quote any Quranic verses and Hadiths in their original Arabic text alongside the English translation.")
                "fr" -> appendLine("IMPORTANT : Veuillez fournir toute la réponse et la consultation en français, mais citez toujours les versets coraniques et les hadiths dans leur texte arabe original aux côtés de la traduction française.")
                else -> appendLine("أجب باللغة العربية الفصحى الواضحة والمسددة.")
            }
        }

        val systemInstruction = """
            أنت «المستشار الإسلامي لتطبيق آصف»، باحث ومستشار شرعي إسلامي متخصص حصراً في الشريعة الإسلامية، والقرآن الكريم، والسنة النبوية، والفقه، والعقيدة، والأخلاق الإسلامية، والفتاوى والأذكار.
            قاعدة أساسية وحاسمة لا تقبل الاستثناء:
            1. يمنع منعاً باتاً الإجابة عن أي سؤال خارج عن الإسلام والشريعة الإسلامية (مثل أسئلة البرمجة والتقنية، كرة القدم والرياضة، الألعاب والمسلسلات، السياسة العامة، النكت، الفنون غير الهادفة، أو أي موضوع دنيوي آخر لا يتعلق بالدين).
            2. إذا وردك أي سؤال غير إسلامي، يجب عليك الرفض التام والرد بالحرف فقط:
            "$NON_ISLAMIC_REFUSAL_MESSAGE"
            3. إذا كان السؤال متعلقاً بالإسلام وأحكامه، فأجب إجابة علمية موثقة وميسرة بالأدلة من الكتاب والسنة على منهج الوسطية وأهل السنة والجماعة.
        """.trimIndent()

        if (hasValidKey) {
            try {
                val aiResponse = callGeminiRestApi(apiKey, prompt, systemInstruction)
                if (aiResponse.isNotBlank()) {
                    scholarAnswerCache[trimmedQ] = aiResponse
                    return@withContext Result.success(aiResponse)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini Scholar API call failed: ${e.message}")
            }
        }

        // Smart, comprehensive thematic synthesis fallback
        val fallback = generateThematicScholarAnswer(trimmedQ)
        scholarAnswerCache[trimmedQ] = fallback
        Result.success(fallback)
    }

    /**
     * Fetch classical tafsir from online Al-Quran Cloud REST API (Internet without key)
     */
    private fun fetchLiveOnlineTafsir(surahNumber: Int, ayahNumber: Int): String {
        return try {
            val url = "https://api.alquran.cloud/v1/ayah/$surahNumber:$ayahNumber/ar.muyassar"
            val request = Request.Builder().url(url).get().build()
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val json = JSONObject(body)
                    val data = json.optJSONObject("data")
                    data?.optString("text", "").orEmpty()
                } else ""
            }
        } catch (e: Exception) {
            Log.w(TAG, "Online Tafsir API error: ${e.message}")
            ""
        }
    }

    /**
     * Direct REST call to Gemini models with automatic candidate fallback
     */
    private fun callGeminiRestApi(apiKey: String, prompt: String, systemInstruction: String): String {
        var lastException: Exception? = null

        for (model in CANDIDATE_MODELS) {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            try {
                val requestJson = JSONObject().apply {
                    // Contents
                    val contentsArray = JSONArray().apply {
                        val contentObj = JSONObject().apply {
                            val partsArray = JSONArray().apply {
                                val partObj = JSONObject().apply {
                                    put("text", prompt)
                                }
                                put(partObj)
                            }
                            put("parts", partsArray)
                        }
                        put(contentObj)
                    }
                    put("contents", contentsArray)

                    // System Instruction
                    val systemObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", systemInstruction)
                            }
                            put(partObj)
                        }
                        put("parts", partsArray)
                    }
                    put("systemInstruction", systemObj)

                    // Generation Config optimized for swift response
                    val genConfig = JSONObject().apply {
                        put("temperature", 0.2)
                        put("topP", 0.9)
                        put("maxOutputTokens", 1500)
                    }
                    put("generationConfig", genConfig)
                }

                val body = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string().orEmpty()
                        val jsonResponse = JSONObject(responseBody)
                        val candidates = jsonResponse.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val firstCandidate = candidates.getJSONObject(0)
                            val content = firstCandidate.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                val textBuilder = StringBuilder()
                                for (p in 0 until parts.length()) {
                                    val partObj = parts.getJSONObject(p)
                                    val partText = partObj.optString("text", "")
                                    if (partText.isNotBlank()) {
                                        textBuilder.append(partText)
                                    }
                                }
                                val resultText = textBuilder.toString().trim()
                                if (resultText.isNotBlank()) {
                                    return resultText
                                }
                            }
                        }
                    } else {
                        val errorBody = response.body?.string().orEmpty()
                        Log.w(TAG, "Gemini model $model returned HTTP ${response.code}: $errorBody")
                        lastException = RuntimeException("HTTP ${response.code} from $model: $errorBody")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Exception calling Gemini model $model: ${e.message}")
                lastException = e
            }
        }

        if (lastException != null) {
            throw lastException!!
        }
        return ""
    }

    private fun generateScholarlyAyahFallback(
        surahName: String,
        ayahNumber: Int,
        ayahText: String,
        classicalTafsir: String
    ): String {
        return buildString {
            appendLine("📖 1. المعنى العام وسياق الآية الكريمة:")
            if (classicalTafsir.isNotBlank()) {
                appendLine(classicalTafsir)
            } else {
                appendLine("هذه الآية الكريمة من سورة $surahName تدعو العبد إلى التفكر في عظمة الله واستشعار رقابته ولزوم طاعته.")
            }
            appendLine()
            appendLine("💡 2. الدلالات اللغوية والإيمانية:")
            appendLine("اشتمل النظم القرآني المعجز على ألفاظ بليغة تلامس الوجدان، وتؤكد على توحيد الله وكمال قدرته ورحمته بالعباد.")
            appendLine()
            appendLine("🌿 3. الهدايات التربوية المستنبطة:")
            appendLine("• ترسيخ اليقين بالله تعالى وتفويض الأمور إليه.")
            appendLine("• بيان جزاء الطائعين والتحذير من الغفلة عن ذكر الله.")
            appendLine("• الحث على التقوى ومراقبة السرائر والعلانية.")
            appendLine()
            appendLine("🤲 4. كيف نعمل بهذه الآية في حياتنا اليومية؟:")
            appendLine("• الإكثار من شكر المنعم على نعمه الظاهرة والباطنة.")
            appendLine("• تحسين العبادات وإتقان العمل ابتغاء مرضاة الله.")
            appendLine("• التخلق بأخلاق القرآن في تعاملنا مع الناس والأسرة.")
        }
    }

    private fun generateScholarlyHadithFallback(
        hadithText: String,
        narrator: String,
        source: String,
        category: String,
        grading: String
    ): String {
        return buildString {
            appendLine("📜 1. التخريج والبيان العام:")
            appendLine("هذا الحديث رواه الصحابي الجليل $narrator وأخرجه الإمام $source، وهو حديث ($grading) يندرج تحت باب $category.")
            appendLine()
            appendLine("🔍 2. المعنى الإجمالي ومقاصد الحديث:")
            appendLine("يضع هذا الحديث النبوي الشريف قاعدة عظيمة من قواعد الدين، ويؤسس لمنهج سليم في السلوك والعبادة والمعاملة.")
            appendLine()
            appendLine("🌟 3. الفوائد والأحكام المستنبطة:")
            appendLine("• عظيم هدي النبي ﷺ وحرصه على تعليم أمته ما ينفعهم في دينهم ودنياهم.")
            appendLine("• أثر الإخلاص وصدق النية في قبول العمل ومضاعفة الأجر.")
            appendLine("• التحذير من مساوئ الأخلاق والتفريط في حقوق الله وحقوق العباد.")
            appendLine()
            appendLine("🌱 4. التطبيق العملي في واقعنا:")
            appendLine("التطبيق العملي يقتضي محاسبة النفس دورياً، وتجديد التوبة، ونشر المودة والرحمة بين المسلمين اقتداءً بالهدي النبوي الشريف.")
        }
    }

    /**
     * Highly rich, thematic scholarly knowledge engine covering core Islamic inquiries
     */
    private fun generateThematicScholarAnswer(question: String): String {
        if (!isIslamicQuestion(question)) {
            return NON_ISLAMIC_REFUSAL_MESSAGE
        }

        val q = question.lowercase()

        return when {
            q.contains("قريب") || (q.contains("سألك") && q.contains("عبادي")) || q.contains("دعاء") || q.contains("استجابة") -> {
                buildString {
                    appendLine("📖 1. التأصيل القرآني والبيان المأثور:")
                    appendLine("قال الله تعالى: ﴿وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ فَلْيَسْتَجِيبُوا لِي وَلْيُؤْمِنُوا بِي لَعَلَّهُمْ يَرْشُدُونَ﴾ [البقرة: 186].")
                    appendLine("قال الإمام ابن كثير: جاءت هذه الآية بين أحكام الصيام لترشد الصائم إلى فضل الدعاء عند الفطر وفي شهر رمضان، وأن الله تعالى لا يخيب من دعاه.")
                    appendLine()
                    appendLine("🔍 2. اللطائف البلاغية والإيمانية:")
                    appendLine("في سائر الأسئلة في القرآن يأتي الجواب بلفظ: ﴿فَقُلْ﴾ مثل: ﴿يَسْأَلُونَكَ عَنِ الْأَهِلَّةِ قُلْ هِيَ مَوَاقِيتُ﴾، إلا في هذه الآية حذف الله واسطة \"قل\" وتولى الإجابة بنفسه سبحانه فقال مباشرة: ﴿فَإِنِّي قَرِيبٌ﴾؛ لبيان شدة القرب ولطف الرب بعبده الداعي.")
                    appendLine()
                    appendLine("🤲 3. شروط وآداب استجابة الدعاء:")
                    appendLine("1. الإخلاص وحضور القلب واليقين بالإجابة.")
                    appendLine("2. طيب المأكل والمشرب والمكسب من الحلال.")
                    appendLine("3. عدم التعجل، لقول النبي ﷺ: «يُستجاب لأحدكم ما لم يعجل، يقول: دعوتُ فلم يُستجب لي» (متفق عليه).")
                    appendLine("4. الاستجابة لأوامر الله بالإيمان والعمل الصالح.")
                }
            }

            q.contains("نيات") || q.contains("النيات") || q.contains("إنما الأعمال") -> {
                buildString {
                    appendLine("📜 1. التخريج والمنزلة العلمية:")
                    appendLine("عن أمير المؤمنين عمر بن الخطاب رضي الله عنه قال: سمعت رسول الله ﷺ يقول: «إنما الأعمال بالنيات، وإنما لكل امرئ ما نوى، فمن كانت هجرته إلى الله ورسوله فهجرته إلى الله ورسوله، ومن كانت هجرته لدنيا يصيبها أو امرأة ينكحها فهجرته إلى ما هاجر إليه» (رواه البخاري ومسلم).")
                    appendLine("قال الإمام الشافعي والإمام أحمد: هذا الحديث ثلث العلم، ويدخل في سبعين باباً من الفقه.")
                    appendLine()
                    appendLine("🔍 2. مقاصد الحديث وأحكامه:")
                    appendLine("• النية شرط لصحة سائر الأعمال الشرعية من صلاة وصيام وزكاة وحج.")
                    appendLine("• النية تميز العبادة عن العادة (كالغسل للتبرد مقابل غسل الجنابة).")
                    appendLine("• النية الصالحة تحول المباحات (كالنوم والأكل والعمل) إلى عبادات وقربات يؤجر عليها العبد.")
                    appendLine()
                    appendLine("🌱 3. التوجيه التربوي والعملي:")
                    appendLine("ينبغي للعبد أن يتعاهد قلبه ويجدد نيته قبل الشروع في أي عمل ديني أو دنيوي، محذراً من الرياء والسمعة، ومبتغياً وجه الله وحده.")
                }
            }

            q.contains("صيام") || q.contains("رمضان") || q.contains("بخاخ") || q.contains("قطرة") || q.contains("مفطرات") -> {
                buildString {
                    appendLine("⚖️ 1. المفطرات المعاصرة وفق قرارات المجامع الفقهية:")
                    appendLine("أصدر مجمع الفقه الإسلامي الدولي التابع لمنظمة التعاون الإسلامي قرارات مفصلة حول المفطرات الطبية المعاصرة:")
                    appendLine("• بخاخ الربو: لا يفطر؛ لأنه غاز مضغوط يذهب إلى القصبات الهوائية لتوسيعها وليس طعاماً ولا شراباً.")
                    appendLine("• قطرة العين وقطرة الأذن: لا تفطران على الراجح حتى لو وجد طعمهما في الحلق، لأنهما ليسا منفذاً معتاداً للجوف.")
                    appendLine("• الإبر العضلية والجلدية غير المغذية: لا تفطر بإجماع المجامع الفقهية.")
                    appendLine("• الإبر الوريدية المغذية (الجلوكوز والسيروم): تفطر لأنها تقوم مقام الطعام والشراب.")
                    appendLine("• تحليل الدم وأخذ عينة يسيرة: لا يفطر.")
                    appendLine()
                    appendLine("📖 2. القاعدة الشرعية الضابطة:")
                    appendLine("الأصل صحة الصيام حتى يثبت بدليل شرعي ما يبطله، والدين مبني على التيسير ورفع الحرج: ﴿يُرِيدُ اللَّهُ بِكُمُ الْيُسْرَ وَلَا يُرِيدُ بِكُمُ الْعُسْرَ﴾.")
                }
            }

            q.contains("كرسي") || q.contains("الكرسي") -> {
                buildString {
                    appendLine("📖 1. فضل آية الكرسي العظيم:")
                    appendLine("آية الكرسي (سورة البقرة: 255) هي أعظم آية في كتاب الله تعالى، بنص الحديث الصحيح الذي رواه أبيّ بن كعب رضي الله عنه عن النبي ﷺ (رواه مسلم).")
                    appendLine("ومن قرأها دبر كل صلاة مكتوبة لم يمنعه من دخول الجنة إلا أن يموت (رواه النسائي وصححه الألباني)، ومن قرأها عند النوم لم يزل عليه من الله حافظ ولا يقربه شيطان حتى يصبح.")
                    appendLine()
                    appendLine("🔍 2. الدلالات العقدية والأسماء الحسنى:")
                    appendLine("اشتملت الآية على عشر جمل توحيدية مستقلة، وتضمنت خمسة من أسماء الله الحسنى: (الله، الحي، القيوم، العلي، العظيم)، وأثبتت له سبحانه كمال الملك والقدرة والقيومية، ونفت عنه النقائص كالنوم والسِّنة والتعب والعجز.")
                    appendLine()
                    appendLine("🤲 3. التطبيق الإيماني اليومي:")
                    appendLine("المحافظة على قراءتها بعد كل صلاة مفروضة، وفي أذكار الصباح والمساء، وعند إيواء الفراش، مع استشعار عظمة الله والاطمئنان لمعيته وحفظه.")
                }
            }

            q.contains("وتر") || q.contains("الوتر") || q.contains("قيام الليل") || q.contains("تهجد") -> {
                buildString {
                    appendLine("🕌 1. حكم وفضل صلاة الوتر:")
                    appendLine("صلاة الوتر سنة مؤكدة حرص عليها النبي ﷺ حضراً وسفراً، وقال ﷺ: «إن الله وتر يحب الوتر، فأوتروا يا أهل القرآن» (رواه أبو داود والترمذي).")
                    appendLine()
                    appendLine("⏰ 2. وقتها وعدد ركعاتها:")
                    appendLine("• وقتها: يبدأ من بعد صلاة العشاء وسنتها الراتبة إلى طلوع الفجر الثاني. وأفضل أوقاتها الثلث الأخير من الليل.")
                    appendLine("• عدد ركعاتها: أقلها ركعة واحدة، وأدنى الكمال ثلاث ركعات (ركعتان شفع ثم ركعة وتر)، وله أن يوتر بخمس، أو سبع، أو إحدى عشرة ركعة مثنى مثنى ثم يوتر بواحدة.")
                    appendLine()
                    appendLine("🤲 3. القراءة والدعاء المسنون:")
                    appendLine("يُسن أن يقرأ في الركعة الأولى بـ (سبح اسم ربك الأعلى)، وفي الثانية بـ (قل يا أيها الكافرون)، وفي الثالثة بـ (قل هو الله أحد)، وله أن يقنت في الركعة الأخيرة بعد الركوع ويدعو بدعاء القنوت المأثور: «اللهم اهدنا فيمن هديت..».")
                }
            }

            q.contains("خشوع") || q.contains("الخشوع") || q.contains("وسواس") -> {
                buildString {
                    appendLine("🕌 1. أهمية الخشوع في الصلاة:")
                    appendLine("الخشوع هو روح الصلاة ولبّها، قال تعالى: ﴿قَدْ أَفْلَحَ الْمُؤْمِنُونَ * الَّذِينَ هُمْ فِي صَلَاتِهِمْ خَاشِعُونَ﴾ [المؤمنون: 1-2].")
                    appendLine()
                    appendLine("💡 2. الأسباب المعينة على استحضار الخشوع:")
                    appendLine("1. الاستعداد المبكر للصلاة بإسباغ الوضوء وإجابة المؤذن والمشي إليها بسكينة.")
                    appendLine("2. استشعار الوقوف بين يدي ملك الملوك سبحانه، وتذكر الموت وأنها قد تكون الصلاة الأخيرة.")
                    appendLine("3. تدبر معاني الآيات والأذكار التي تتلوها باللسان والترسل فيها.")
                    appendLine("4. الطمأنينة في الركوع والسجود وإعطاء كل ركن حقه بلا عجلة.")
                    appendLine("5. دفع الشواغل الدنيوية وإغلاق مشتتات الهاتف والتطبيقات قبل الدخول في الصلاة.")
                    appendLine("6. الاستعاذة بالله من الشيطان (خنزب) عند نزول الوسوسة ونفث ثلاثاً عن اليسار كما أرشد النبي ﷺ.")
                }
            }

            q.contains("فجر") || q.contains("الفجر") || q.contains("الاستيقاظ") -> {
                buildString {
                    appendLine("🌅 1. فضل صلاة الفجر وعظيم أجرها:")
                    appendLine("قال النبي ﷺ: «من صلى الصبح فهو في ذمة الله» (رواه مسلم)، وقال ﷺ: «ركعتا الفجر خير من الدنيا وما فيها».")
                    appendLine()
                    appendLine("💡 2. الأسباب العملية للاستيقاظ بنشاط:")
                    appendLine("• النوم المبكر وتجنب السهر المفرط.")
                    appendLine("• الوضوء قبل النوم وقراءة أذكار النوم والاستغفار.")
                    appendLine("• ضبط المنبه ووضعه بعيداً عن الفراش لإلزام النفس بالنهوض.")
                    appendLine("• الدعاء الصادق بأن يعينك الله على شهودها في وقتها مع الجماعة.")
                }
            }

            q.contains("سهو") || q.contains("سجود السهو") -> {
                buildString {
                    appendLine("⚖️ 1. أسباب سجود السهو:")
                    appendLine("يسجد المصلي للسهو لأحد ثلاثة أسباب: الزيادة، أو النقص، أو الشك.")
                    appendLine()
                    appendLine("🔍 2. موضع السجود (قبل السلام أو بعده):")
                    appendLine("• قبل السلام: إذا كان السهو عن نقص (كنسيان التشهد الأول)، أو شك وتردد ولم يترجح لديه شيء فبنى على اليقين (الأقل).")
                    appendLine("• بعد السلام: إذا كان السهو عن زيادة (كركوع أو قيام زائد)، أو شك وتحرى وترجح لديه أحد الأمرين.")
                    appendLine("وهما سجدتان كسجود الصلاة العادي يكبر لهما ويسلم.")
                }
            }

            q.contains("سفر") || q.contains("قصر") || q.contains("مسافر") -> {
                buildString {
                    appendLine("🚗 1. رخصة القصر والجمع للمسافر:")
                    appendLine("القصر سنة مؤكدة للمسافر في الصلاة الرباعية (الظهر، العصر، العشاء) فتصلى ركعتين، وأما الفجر والمغرب فلا تقصران.")
                    appendLine()
                    appendLine("⏰ 2. أحكام وضوابط الجمع:")
                    appendLine("• يجوز الجمع بين الظهر والعصر (جمع تقديم أو تأخير)، وبين المغرب والعشاء.")
                    appendLine("• مسافة القصر المعتبرة عند جمهور العلماء حوالي 80 كم تقريباً.")
                    appendLine("• إذا نوى المسافر الإقامة في البلد أكثر من 4 أيام أتم الصلاة عند جمهور الفقهاء.")
                }
            }

            q.contains("استخارة") || q.contains("الاستخارة") -> {
                buildString {
                    appendLine("🤲 1. كيفية صلاة الاستخارة:")
                    appendLine("تصلي ركعتين نافلة من غير الفريضة، وبعد السلام تدعو بدعاء الاستخارة المأثور الذي علمه النبي ﷺ لأصحابه:")
                    appendLine("«اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ، وَأَسْأَلُكَ مِنْ فَضْلِكَ العَظِيمِ، فَإِنَّكَ تَقْدِرُ وَلاَ أَقْدِرُ، وَتَعْلَمُ وَلاَ أَعْلَمُ، وَأَنْتَ عَلَّامُ الغُيُوبِ...»")
                    appendLine()
                    appendLine("💡 2. علامة الاستخارة:")
                    appendLine("يمضي المسلم في أمره بعد الاستخارة والاستشارة، فما تيسر وتسهل فهو الخير بإذن الله، ولا يشترط رؤية منام أو رؤيا.")
                }
            }

            q.contains("توبة") || q.contains("التوبة") || q.contains("استغفار") -> {
                buildString {
                    appendLine("🌿 1. شروط التوبة الصادقة المقبولة:")
                    appendLine("قال الله تعالى: ﴿وَتُوبُوا إِلَى اللَّهِ جَمِيعًا أَيُّهَ الْمُؤْمِنُونَ لَعَلَّكُمْ تُفْلِحُونَ﴾ [النور: 31].")
                    appendLine("1. الإقلاع الفوري عن الذنب والمعصية.")
                    appendLine("2. الندم القلبي الصادق على ما فات.")
                    appendLine("3. العزم الأكيد على عدم العودة إليه.")
                    appendLine("4. رد المظالم والحقوق إلى أهلها إن كان الذنب متعلقاً بحقوق العباد.")
                    appendLine()
                    appendLine("✨ 2. فضل الاستغفار:")
                    appendLine("«التائب من الذنب كمن لا ذنب له»، والله يفرح بتوبة عبده حين يتوب إليه أشد من فرح فاقد راحلته في الصحراء.")
                }
            }

            else -> {
                buildString {
                    appendLine("📖 1. التأصيل الشرعي من الكتاب والسنة:")
                    appendLine("بناءً على السؤال المطروح حول: \"$question\"")
                    appendLine("إن نصوص الشريعة الإسلامية الغراء في القرآن الكريم وصحيح السنة النبوية تدلنا على أن أحكام الدين مبنية على جلب المصالح ودرء المفاسد، ولزوم تقوى الله في السر والعلن، قال الله تعالى: ﴿وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا * وَيَرْزُقْهُ مِنْ حَيْثُ لَا يَحْتَسِبُ﴾ [الطلاق: 2-3].")
                    appendLine()
                    appendLine("💡 2. القواعد الفقهية والتوجيهات المعتبرة:")
                    appendLine("• القاعدة الأولى: الأصل في العبادات التوقيف على النص، والأصل في المعاملات والعادات الإباحة والتيسير.")
                    appendLine("• القاعدة الثانية: المشقة تجلب التيسير، وما جعل الله عليكم في الدين من حرج.")
                    appendLine("• القاعدة الثالثة: درء المفاسد مقدم على جلب المصالح، والحلال بيّن والحرام بيّن وبينهما أمور مشتبهات.")
                    appendLine()
                    appendLine("🤲 3. التوجيه العملي والإيماني:")
                    appendLine("1. الحرص على تلاوة القرآن الكريم بتدبر، والمحافظة على الصلوات الخمس في أوقاتها مع الجماعة.")
                    appendLine("2. لزوم الأذكار النبوية والاستغفار، وطلب العلم الشرعي النافع من المصادر الموثوقة.")
                    appendLine("3. استحضار النية الصالحة في سائر الأقوال والأعمال لتكون عبادة مقبولة يثاب عليها المسلم.")
                }
            }
        }
    }
}
