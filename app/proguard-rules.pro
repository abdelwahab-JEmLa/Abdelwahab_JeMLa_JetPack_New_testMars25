# Preserve the Article class along with its constructors and all members
-keepclassmembers class com.example.abdelwahabjemlajetpack.Article {
    public <init>(...);
    <fields>;
    <methods>;
}

# Preserve class metadata for Firebase serialization
-keepnames class com.example.abdelwahabjemlajetpack.Article {
    *;
}

# Preserve Firebase dependencies
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Preserve Kotlin serialization metadata
-keepnames class kotlinx.serialization.** { *; }
-keep class kotlinx.serialization.** { *; }
