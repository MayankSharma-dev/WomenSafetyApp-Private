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

## TomTom Maps SDK - Keep the necessary classes and interfaces
#-keep class com.tomtom.sdk.maps.** { *; }
#-keep class com.tomtom.sdk.search.** { *; }
#
## Keep native methods used by TomTom SDK
#-keepclassmembers class * {
#    native <methods>;
#}
#
## Remove any unused TomTom SDK warnings
#-dontwarn com.tomtom.sdk.**
#
## Remove unused TomTom SDK resources (if any)
#-dontwarn com.tomtom.sdk.maps.resources.**
#-dontwarn com.tomtom.sdk.search.resources.**
#
## If TomTom uses reflection to load resources, keep them intact
#-keep class com.tomtom.sdk.maps.utils.** { *; }
#-keep class com.tomtom.sdk.search.utils.** { *; }
#
#

# Existing rules
-keep class androidx.activity.** { *; }
-dontwarn androidx.activity.**

-keep class androidx.fragment.** { *; }
-dontwarn androidx.fragment.**

-keep class androidx.recyclerview.** { *; }
-dontwarn androidx.recyclerview.**

-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.location.**

-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**

-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

-keep class androidx.cardview.** { *; }
-dontwarn androidx.cardview.**

-keep class androidx.core.splashscreen.** { *; }
-dontwarn androidx.core.splashscreen.**

-keep class com.squareup.seismic.** { *; }
-dontwarn com.squareup.seismic.**

-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

#-keep class com.tomtom.** { *; }
#-dontwarn com.tomtom.**

-keep class com.tomtom.sdk.maps.** { *; }
-dontwarn com.tomtom.sdk.maps.**

-keep class com.tomtom.sdk.search.** { *; }
-dontwarn com.tomtom.sdk.search.**

-keep class androidx.glance.** { *; }
-dontwarn androidx.glance.**

# Keep all extension functions
-keepclassmembers class ** {
    public static ** *(...);
}

# Keep all first-class functions
-keepclassmembers class ** {
    kotlin.jvm.functions.Function1 **;
    kotlin.jvm.functions.Function2 **;
    kotlin.jvm.functions.Function3 **;
    kotlin.jvm.functions.Function4 **;
    kotlin.jvm.functions.Function5 **;
    kotlin.jvm.functions.Function6 **;
    kotlin.jvm.functions.Function7 **;
    kotlin.jvm.functions.Function8 **;
    kotlin.jvm.functions.Function9 **;
    kotlin.jvm.functions.Function10 **;
}

# Keep Kotlin metadata
-keepattributes KotlinMetadata

# Keep Kotlin data classes
-keepclassmembers class ** {
    public <init>(...);
    public final <fields>;
}

# Keep Kotlin companion objects
-keepclassmembers class **$Companion {
    *;
}

# Keep Kotlin synthetic classes
-keep class kotlin.** { *; }
-dontwarn kotlin.**

# Suppress warnings for missing classes
-dontwarn java.lang.invoke.MethodHandleProxies
-dontwarn java.lang.reflect.AnnotatedType

# Remove logging
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
