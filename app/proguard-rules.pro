# Spendwise Proguard Rules
-keepattributes *Annotation*
-keepclassmembers class * extends androidx.room.RoomDatabase
-dontwarn com.google.mlkit.**
