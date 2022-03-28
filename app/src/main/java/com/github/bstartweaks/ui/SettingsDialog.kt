@file:Suppress("DEPRECATION")

package com.github.bstartweaks.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import com.github.bstartweaks.R
import com.github.kyuubiran.ezxhelper.utils.addModuleAssetPath
import com.github.kyuubiran.ezxhelper.utils.restartHostApp

class SettingsDialog(context: Context) : AlertDialog.Builder(context) {

    companion object {
        private lateinit var outDialog: AlertDialog
        private lateinit var prefs: SharedPreferences
        const val PREFS_NAME = "bstar_tweaks"
    }

    class PrefsFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            preferenceManager.sharedPreferencesName = PREFS_NAME
            addPreferencesFromResource(R.xml.settings_dialog)
            prefs = preferenceManager.sharedPreferences
        }
    }

    init {
        context.addModuleAssetPath()

        val act = context as Activity

        outDialog = run {
            val prefsFragment = PrefsFragment()
            act.fragmentManager.beginTransaction().add(prefsFragment, "settings").commit()
            act.fragmentManager.executePendingTransactions()

            prefsFragment.onActivityCreated(null)

            setView(prefsFragment.view)

            setTitle(context.getString(R.string.settings))
            setPositiveButton(context.getString(R.string.save_restart)) { _, _ ->
                restartHostApp(act)
            }
            setNegativeButton(context.getString(R.string.dismiss), null)
            setCancelable(false)
            show()
        }
    }
}
