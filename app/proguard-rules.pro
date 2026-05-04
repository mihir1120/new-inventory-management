# Keep Firebase and Kotlin metadata needed at runtime.
-keep class com.google.firebase.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class kotlin.Metadata { *; }

# Keep parcelized/serialized models commonly reflected by Firebase.
-keepclassmembers class ** {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}

# Keep activities, providers, and composable entry points referenced from the manifest/runtime.
-keep class * extends android.app.Activity
-keep class * extends android.content.ContentProvider
-keep class * extends android.app.Application

# Make stack traces less revealing in release.
-renamesourcefileattribute SourceFile
-keepattributes LineNumberTable
