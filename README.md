<div align="center">

# تطبيق آصف الإسلامي — Asif Islamic App

**رفيقك اليومي للصلوات والقرآن والأذكار، بواجهة عصرية مستوحاة من Gemini**

[![Build APK](https://github.com/rachadgr/acef-islamic-app/actions/workflows/build-apk.yml/badge.svg)](https://github.com/rachadgr/acef-islamic-app/actions/workflows/build-apk.yml)

</div>

---

## ✨ المميزات

- 🕌 **مواقيت الصلاة** الدقيقة (طرق حساب متعددة: الجزائر، رابطة العالم الإسلامي، أم القرى، مصر، ISNA)
- 🔊 **الأذان والإقامة التلقائيان** مع خدمات أمامية وتنبيهات دقيقة
- 📖 **المصحف الكريم** بروايتي حفص وورش، مع تتبّع آية بآية وتحميل أوفلاين
- 🫧 **فقاعة الأذكار العائمة** فوق التطبيقات الأخرى
- 🧭 **القبلة** بمستشعرات الجهاز مع المسافة إلى الكعبة
- 🧠 **المستشار الإسلامي الذكي** وتفسير الآيات والأحاديث بالذكاء الاصطناعي (Google Gemini)
- 🌍 **ثلاث لغات**: العربية، الإنجليزية، الفرنسية
- 🎨 **واجهة Gemini العصرية**: تدرّجات لونية (أزرق → بنفسجي → وردي)، أسطح زجاجية، حركات سلسة

## 🛠️ إصلاح الانهيار (Crash Fix)

تم إصلاح انهيار التطبيق جذرياً عبر:

1. **إزالة Firebase غير المستخدم** — لم يكن هناك أي استدعاء لـ Firebase في الكود، وكانت التبعيات تُثقل وتعرّض التهيئة للفشل (خصوصاً مع `google-services.json` غير موجود).
2. **توقيع آمن للبناء** — تم توليد `debug.keystore` تلقائياً، مع سقوط آمن إلى توقيع التصحيح إذا لم يوجد مخزن مفاتيح للإصدار (لم يعد البناء يفشل).
3. **إزالة اعتماد الإصدار على مفاتيح خارجية إجبارية** — قراءة مفتاح Gemini من `.env` بشكل آمن دون إجباره.
4. **شبكة أمان عامة** — التقاط الاستثناءات غير المتوقعة وتسجيلها بوسم `AcefCrash`.
5. **ترقية بيئة البناء** إلى JDK 21 + Gradle 9.3.1 + AGP 9.1.1 + Kotlin 2.2.10 + KSP 2.3.5.

## ⚙️ بيئة البناء

| المكوّن | الإصدار |
|---|---|
| JDK | **21** |
| Gradle | 9.3.1 |
| Android Gradle Plugin | 9.1.1 |
| Kotlin | 2.2.10 |
| KSP | 2.3.5 |
| compileSdk / targetSdk | 36 |
| minSdk | 24 |

## 🚀 البناء محلياً

```bash
# 1) أنشئ ملف .env وضع فيه مفتاح Gemini (اختياري)
echo "GEMINI_API_KEY=YOUR_KEY" > .env

# 2) ابنِ نسخة التصحيح
./gradlew :app:assembleDebug

# 3) أو نسخة الإصدار
./gradlew :app:assembleRelease
```

الناتج:
- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`

## 🤖 البناء عبر GitHub Actions

يوجد مسار عمل جاهز في `.github/workflows/build-apk.yml` يقوم تلقائياً بـ:

1. تجهيز **JDK 21** و Android SDK
2. بناء **APK** بنسختي التصحيح والإصدار
3. رفع ملفات APK كـ Artifacts
4. إنشاء **Release** تلقائي على GitHub مع ملفات APK عند الدفع إلى فرع `main`

> 💡 لإضافة مفتاح Gemini كسرّ: `Settings → Secrets and variables → Actions → New repository secret` باسم `GEMINI_API_KEY`.

## 🔐 توقيع الإصدار

انسخ `key.properties.example` إلى `key.properties` واملأ بيانات مخزن المفاتيح:

```properties
storeFile=/absolute/path/to/release-key.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=acef
keyPassword=YOUR_KEY_PASSWORD
```

> إذا لم يتوفر `key.properties`، يستخدم البناء توقيع التصحيح تلقائياً لضمان نجاح البناء دائماً.

---

**إعداد وتطوير: محمد آصف ڨرارة**
