package com.wangyiheng.vcamsx.utils

import com.crossbowffs.remotepreferences.RemotePreferenceProvider

// 🛡️ FIXED: Added all cross-process preference files to the whitelist.
// If a file isn't listed here, the ContentProvider silently rejects hook requests.
class MultiprocessSharedPreferences : RemotePreferenceProvider(
    "com.wangyiheng.vcamsx.preferences", 
    arrayOf(
        "main_prefs", 
        "transform_prefs", 
        "device_spoof_prefs", 
        "hidden_apps"
    )
)