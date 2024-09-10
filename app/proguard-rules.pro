# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class getChannelName to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file getChannelName.
#-renamesourcefileattribute SourceFile

# no case mis package. aA Aa
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-ignorewarnings
-dontwarn androidx.**
-dontwarn okhttp3.**
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
# keep native method
-keepclasseswithmembernames class * {
    native <methods>;
}
#keep android sdk
-keep class com.google.android.material.** {*;}
-keep class android.** {*;}
-keep public class * extends android.**
-keep interface android.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-keep class org.** { *; }
-keep class * implements androidx.viewbinding.ViewBinding { *; }
-keep class **.R$* {*;}
-keep class android.net.ConnectivityManager { *; }
-keepattributes *Annotation*
-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface * extends java.lang.annotation.Annotation { *; }
###############################################
#keep Interceptor
-keep class * implements acquire.base.chain.Interceptor
#keep json package
-keep class acquire.core.bean.json.** {*;}
### evetnbus
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
### newland libraries
#keep newland nsdk
-keep class android.newland.** {*;}
-keep class com.newland.nsdk.** {*;}
-keep class com.newland.me.** {*;}
#keep newland emvL3
-keep class com.newland.sdk.emvl3.** {*;}