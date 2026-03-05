# Statz ProGuard Rules

# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.statz.app.data.backup.**$$serializer { *; }
-keepclassmembers class com.statz.app.data.backup.** {
    *** Companion;
}
-keepclasseswithmembers class com.statz.app.data.backup.** {
    kotlinx.serialization.KSerializer serializer(...);
}
