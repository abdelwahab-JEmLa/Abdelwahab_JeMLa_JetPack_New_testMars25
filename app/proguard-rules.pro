# Preserve the Article class along with its constructors and all members
-keepclassmembers class a_RoomDB.BaseDonne {
    public <init>(...);
    <fields>;
    <methods>;
}

# Preserve class metadata for Firebase serialization
-keepnames class a_RoomDB.BaseDonne {
    *;
}

# Preserve Firebase dependencies
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Preserve Kotlin serialization metadata
-keepnames class kotlinx.serialization.** { *; }
-keep class kotlinx.serialization.** { *; }
# Keep constructors for Firebase model classes
-keepclassmembers class a_RoomDB.BaseDonne {
    public <init>();
}
