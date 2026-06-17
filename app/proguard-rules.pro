# ─────────────────────────────────────────────────────────────
# GENERAL ANDROID RULES
# ─────────────────────────────────────────────────────────────
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep the Application class
-keepclassmembers class * extends android.app.Application { *; }

# ─────────────────────────────────────────────────────────────
# XPOSED MODULE RULES (CRITICAL - MUST KEEP)
# ─────────────────────────────────────────────────────────────
# LSPosed looks for this exact class name in xposed_init
-keep class com.itsme.amkush.MainHook { *; }
-keep class com.itsme.amkush.MainHook$Companion { *; }

# Keep the Xposed API classes (compileOnly)
-keep class de.robv.android.xposed.** { *; }

# ─────────────────────────────────────────────────────────────
# KOIN DEPENDENCY INJECTION RULES (CRITICAL - MUST KEEP)
# ─────────────────────────────────────────────────────────────
# Koin uses reflection to inject dependencies.
-keep class org.koin.** { *; }
-keep class com.itsme.amkush.modules.** { *; }
-keep class com.itsme.amkush.interfaces.** { *; }

# Keep the specific implementations injected by Koin
-keep class com.itsme.amkush.utils.InfoManager { *; }
-keep class com.itsme.amkush.utils.VideoFileManager { *; }
-keep class com.itsme.amkush.utils.MediaPlayerController { *; }
-keep class com.itsme.amkush.modules.home.controllers.HomeViewModel { *; }

# ─────────────────────────────────────────────────────────────
# GSON DATA MODELS RULES (CRITICAL - MUST KEEP)
# ─────────────────────────────────────────────────────────────
# Gson uses reflection to parse JSON. If ProGuard renames these fields, 
# your app will fail to load/save settings from RemotePreferences.
-keep class com.itsme.amkush.data.models.** { *; }

# ─────────────────────────────────────────────────────────────
# IJKPLAYER & NATIVE LIBRARIES (CRITICAL - MUST KEEP)
# ─────────────────────────────────────────────────────────────
# Keep the manually integrated ijkplayer Java classes
-keep class tv.danmaku.ijk.media.player.** { *; }
-keep class tv.danmaku.ijk.media.player.pragma.** { *; }
-keep class tv.danmaku.ijk.media.player.misc.** { *; }
-keep class tv.danmaku.ijk.media.player.annotations.** { *; }

# Keep the logging utility used by the hooks
-keep class cn.dianbobo.dbb.util.** { *; }

# Prevent stripping of native JNI methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# ─────────────────────────────────────────────────────────────
# JETPACK COMPOSE RULES (CRITICAL - MUST KEEP)
# ─────────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
# Keep UI screens and components so Compose reflection works
-keep class com.itsme.amkush.ui.** { *; }
-keep class com.itsme.amkush.components.** { *; }

# ─────────────────────────────────────────────────────────────
# REMOTE PREFERENCES (Cross-process IPC)
# ─────────────────────────────────────────────────────────────
-keep class com.crossbowffs.remotepreferences.** { *; }

# ─────────────────────────────────────────────────────────────
# 🔥 OBFUSCATION TARGETS (DO NOT KEEP - LET PROGUARD SCRAMBLE)
# ─────────────────────────────────────────────────────────────
# We intentionally DO NOT add -keep rules for these classes.
# This allows ProGuard to rename their methods to a(), b(), c()
# making it extremely hard for hackers to understand the logic.
# 
# - com.itsme.amkush.utils.ActivationApi
# - com.itsme.amkush.utils.SignatureUtils
# - com.itsme.amkush.utils.Secrets
# - com.itsme.amkush.utils.SafeHooker
# - com.itsme.amkush.utils.ImageToNV21
# - com.itsme.amkush.utils.MediaTransformState
# - com.itsme.amkush.utils.VideoToFrames
# - com.itsme.amkush.camerahook.CameraOne
# - com.itsme.amkush.camerahook.CameraTwo
# - com.itsme.amkush.hooks.bypass.**
