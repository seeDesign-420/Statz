# Statz ProGuard Rules

# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Room TypeConverters — keep enum classes used in converters
-keep class com.statz.app.domain.model.CategoryType { *; }
-keep class com.statz.app.domain.model.QueryStatus { *; }
-keep class com.statz.app.domain.model.QueryUrgency { *; }

# Hilt — these are usually handled by the plugin, but explicit for safety
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }
-keep @dagger.hilt.android.EarlyEntryPoint interface * { *; }
-keep @dagger.hilt.EntryPoint interface * { *; }

# WorkManager + Hilt
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep @androidx.hilt.work.HiltWorker class * { *; }

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

# Keep R8 from stripping Compose @Immutable / @Stable annotations
-keepattributes RuntimeVisibleAnnotations
