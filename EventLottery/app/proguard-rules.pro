# Add project specific ProGuard rules here.

# Firebase Firestore
-keep class com.google.firebase.** { *; }
-keep class com.example.eventlottery.models.** { *; }

# ZXing
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.** { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# CameraX
-keep class androidx.camera.** { *; }

# Suppress warnings
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
