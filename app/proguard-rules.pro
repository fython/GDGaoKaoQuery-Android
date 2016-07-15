-ignorewarn

-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.** {*;}
-optimizations !code/simplification/arithmetic
-optimizationpasses 5

-keepattributes *Annotation*
-keepattributes Signature

# Gson
-keep class sun.misc.Unsafe { *; }

-dontobfuscate
-keep public class * extends android.app.Activity
-keep public class * extends android.view.View {*;}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * implements android.os.Parcelable {
    static android.os.Parcelable$Creator CREATOR;
}
-keepclassmembers class **.R$* {
    public static <fields>;
}
-keep class moe.feng.gd.gkquery.ui.** {*;}
-keep class moe.feng.gd.gkquery.view.** {*;}
-keep class moe.feng.gd.gkquery.model.** {*;}
-keep class moe.feng.gd.gkquery.api.** {*;}
-keep class android.support.** {*;}
-keep class com.google.** {*;}