# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/USER/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# attribute in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep rules here:

# If you use reflection or JNI define all interfaces and members that should be preserved.
# -keep class MyClass
# -keepclassmembers class MyClass {
#   public <fields>;
#   public <methods>;
# }

# If your project uses WebView with HTML exposing a JavaScript interface to Java,
# then you must include the following line in your pro-guard configuration file:
# -keepclassmembers class * {
#   @android.webkit.JavascriptInterface <methods>;
# }

# If you're using the Gson library, you might need to add the following lines:
# -keep class com.google.gson.stream.** { *; }
# -keep class com.google.gson.Gson
# -keep class com.google.gson.TypeAdapter
# -keep class com.google.gson.reflect.TypeToken
# -keep class * implements com.google.gson.TypeAdapterFactory
# -keep class * implements com.google.gson.JsonSerializer
# -keep class * implements com.google.gson.JsonDeserializer

# If you are using a library that requires certain classes to be preserved,
# refer to the library's documentation for specific ProGuard rules.

# For example, if you are using Retrofit:
# -dontwarn retrofit2.**
# -keep class retrofit2.** { *; }
# -keepattributes Signature
# -keepattributes Exceptions

# For Hilt
-dontwarn dagger.hilt.android.internal.**
-dontwarn dagger.hilt.internal.aggregatedroot.codegen.**
-dontwarn dagger.hilt.processor.internal.disableinstallincheck.**
-keepnames @dagger.hilt.android.HiltAndroidApp class * extends android.app.Application
-keepnames @dagger.hilt.android.AndroidEntryPoint class *
-keepclassmembers class * {
    @dagger.hilt.android.AndroidEntryPoint *;
    @dagger.hilt.android.HiltAndroidApp *;
}
