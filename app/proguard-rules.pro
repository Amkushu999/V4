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
-keep class com.wangyiheng.vcamsx.MainHook { *; }
-keep class com.wangyiheng.vcamsx.MainHook$Companion { *; }

# Keep the Xposed API classes (compileOnly)
-keep class de.robv.android.xposed.** { *; }

# ─────────────────────────────────────────────────────────────
# KOIN DEPENDENCY INJECTION RULES (CRITICAL - MUST KEEP)
# ─────────────────────────────────────────────────────────────
# Koin uses reflection to inject dependencies.
-keep class org.koin.** { *; }
-keep class com.wangyiheng.vcamsx.modules.** { *; }
-keep class com.wangyiheng.vcamsx.interfaces.** { *; }

# Keep the specific implementations injected by Koin
-keep class com.wangyiheng.vcamsx.utils.InfoManager { *; }
-keep class com.wangyiheng.vcamsx.utils.VideoFileManager { *; }
-keep class com.wangyiheng.vcamsx.utils.MediaPlayerController { *; }
-keep class com.wangyiheng.vcamsx.modules.home.controllers.HomeViewModel { *; }

# ─────────────────────────────────────────────────────────────
# GSON DATA MODELS RULES (CRITICAL - MUST KEEP)
# ─────────────────────────────────────────────────────────────
# Gson uses reflection to parse JSON. If ProGuard renames these fields, 
# your app will fail to load/save settings from RemotePreferences.
-keep class com.wangyiheng.vcamsx.data.models.** { *; }

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
-keep class com.wangyiheng.vcamsx.ui.** { *; }
-keep class com.wangyiheng.vcamsx.components.** { *; }

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
# - com.wangyiheng.vcamsx.utils.ActivationApi
# - com.wangyiheng.vcamsx.utils.SignatureUtils
# - com.wangyiheng.vcamsx.utils.Secrets
# - com.wangyiheng.vcamsx.utils.SafeHooker
# - com.wangyiheng.vcamsx.utils.ImageToNV21
# - com.wangyiheng.vcamsx.utils.MediaTransformState
# - com.wangyiheng.vcamsx.utils.VideoToFrames
# - com.wangyiheng.vcamsx.camerahook.CameraOne
# - com.wangyiheng.vcamsx.camerahook.CameraTwo
# - com.wangyiheng.vcamsx.hooks.bypass.**