# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep native method names for JNI
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Keep mpv-android library (JNI entry points and native methods)
-keep,allowoptimization class is.xyz.mpv.** { public protected *; }
-keep class is.xyz.mpv.MPVLib { *; }
-keepclassmembers class is.xyz.mpv.** {
    native <methods>;
}

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.*

# Moshi
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonQualifier @interface *
-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
    <fields>;
    **[] values();
}
-keepnames @com.squareup.moshi.JsonClass class *

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.uw.mpvDroid.**$$serializer { *; }
-keepclassmembers class com.uw.mpvDroid.** {
    *** Companion;
}
-keepclasseswithmembers class com.uw.mpvDroid.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Network libraries (SMB, FTP, WebDAV)
-dontwarn com.hierynomus.**
-dontwarn com.rapidgopher.**
-dontwarn org.apache.commons.net.**
-keep class com.hierynomus.** { *; }
-keep class com.rapidgopher.** { *; }
-keep class org.apache.commons.net.** { *; }

# MediaInfo
-keep class org.devio.** { *; }
-dontwarn org.devio.**

# NanoHTTPD
-keep class fi.iki.elonen.** { *; }
-dontwarn fi.iki.elonen.**

# MBassy library (event bus) - missing EL dependencies
-dontwarn javax.el.BeanELResolver
-dontwarn javax.el.ELContext
-dontwarn javax.el.ELResolver
-dontwarn javax.el.ExpressionFactory
-dontwarn javax.el.FunctionMapper
-dontwarn javax.el.ValueExpression
-dontwarn javax.el.VariableMapper
-dontwarn net.engio.mbassy.**

# Preserve data classes used across the app
-keep class com.uw.mpvDroid.domain.** { *; }
-keep class com.uw.mpvDroid.database.entities.** { *; }

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}