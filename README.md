# GO Paint! : Drawing accessories

تطبيق أندرويد (Kotlin + Jetpack Compose) مربوط بـ Supabase. يحتوي على 4 أقسام
(فرش ibis Paint، باليتات ألوان، مرجعيات عيون، مرجعيات هيكل جسم)، نظام حسابات
(Email + Google)، ورفع محتوى من الفنانين يحتاج موافقة admin قبل النشر.

## بيانات Supabase (جاهزة، مثبتة في app/build.gradle.kts)

- Project URL: `https://cdxkktwewqtvttlyjolb.supabase.co`
- Anon Key: مثبت مسبقاً في `defaultConfig` كـ `BuildConfig`

## خطوة ضرورية: تفعيل Google Sign-In

1. روح لـ [Google Cloud Console](https://console.cloud.google.com) وأنشئ OAuth Client ID (نوع Web application).
2. في **Authorized redirect URIs** حط:
   `https://cdxkktwewqtvttlyjolb.supabase.co/auth/v1/callback`
3. روح للوحة Supabase → **Authentication → Providers → Google** وفعّلها، وحط
   الـ Client ID والـ Client Secret اللي جبتهم من Google.
4. في نفس الصفحة (Authentication → URL Configuration) زيد Redirect URL:
   `gopaint://login-callback`

بدون هذي الخطوة، زر "المتابعة عبر Google" ما يخدمش، لكن تسجيل الدخول بالإيميل
يخدم مباشرة بدون أي إعداد إضافي.

## كيفاش تخلي حساب "أدمن" (باش توافق على المحتوى)

بعد ما تسجل دخولك مرة وحدة بالتطبيق (باش يتصنعلك صف في جدول profiles تلقائياً)،
روح للوحة Supabase → **SQL Editor** ونفذ:

```sql
update public.profiles set role = 'admin' where username = 'اسم_المستخدم_متاعك';
```

## البناء عبر GitHub Actions (نفس أسلوبك المعتاد)

1. اصنع repo جديد على GitHub وارفع هذا المجلد كامل.
2. أي push لـ `main` يشغل `.github/workflows/build-apk.yml` أوتوماتيكياً
   ويبني APK ويرفعه كـ artifact قابل للتحميل من تبويب Actions.
3. تقدر أيضاً تشغلها يدوياً من تبويب Actions → Build APK → Run workflow.

## هيكلة المشروع

```
app/src/main/java/com/gopaint/app/
  MainActivity.kt              نقطة الدخول + استقبال رابط Google OAuth
  data/
    SupabaseClientProvider.kt  عميل Supabase الموحد
    AuthRepository.kt          تسجيل الدخول (Email + Google)
    ContentRepository.kt       جلب/رفع/موافقة المحتوى
    model/Models.kt            نماذج البيانات
  nav/NavGraph.kt               التنقل بين الشاشات
  ui/screens/                   LoginScreen, HomeScreen, ItemDetailScreen,
                                 UploadScreen, AdminScreen
  ui/theme/                     ألوان وتصميم العلامة التجارية
  util/QrCodeGenerator.kt       توليد QR code محلياً (ZXing)
```

## الخطوات الجاية المقترحة

- زيادة صفحة "حسابي" (My profile) لعرض عناصر المستخدم المرفوعة وحالتها.
- زر "مفضلة" (likes) - الجدول `likes` جاهز في قاعدة البيانات.
- رفع صور فعلية لـ Supabase Storage بدل الاكتفاء بروابط خارجية للصور.
- فلترة/بحث داخل كل قسم.
