package com.github.bstartweaks

import android.content.Context
import android.content.SharedPreferences
import com.github.bstartweaks.ui.SettingsDialog
import com.github.kyuubiran.ezxhelper.EzXHelper.appContext

@Suppress("DEPRECATION")
val modulePrefs: SharedPreferences by lazy {
    appContext.getSharedPreferences(
        SettingsDialog.PREFS_NAME, Context.MODE_MULTI_PROCESS
    )
}
