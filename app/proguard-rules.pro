# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# -----------------------------------------------------------------
# 맘편한 이동: Retrofit + SimpleXML (BusData.kt) 난독화 방지 (개선본)
# -----------------------------------------------------------------

# 1. 제네릭 타입 정보 보존 (가장 중요!)
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# 2. BusData.kt의 모든 데이터 클래스 완전 보존
-keep class com.example.bfdream_android.data.** { *; }
-keepclassmembers class com.example.bfdream_android.data.** { *; }

# 3. BusApiService 인터페이스 보존
-keep interface com.example.bfdream_android.network.BusApiService { *; }

# 4. SimpleXML 전체 보존 (리플렉션 사용)
-keep class org.simpleframework.xml.** { *; }
-keep interface org.simpleframework.xml.** { *; }
-keepclassmembers class * {
    @org.simpleframework.xml.* <fields>;
    @org.simpleframework.xml.* <init>(...);
}
-keep @org.simpleframework.xml.Root class *
-keep @org.simpleframework.xml.Element class * { *; }
-keep @org.simpleframework.xml.ElementList class * { *; }

# 5. Retrofit + Converter 보존
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Retrofit Converter (SimpleXML 등) 보존
-keep class retrofit2.converter.** { *; }
-keep class * extends retrofit2.Converter { *; }

# 6. OkHttp 보존
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# 7. Kotlin 리플렉션 보존 (Kotlin 데이터 클래스용)
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# 8. 제네릭 타입을 사용하는 클래스 보존
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}

# 9. R8 최적화 비활성화 (문제가 계속되면)
# -dontoptimize