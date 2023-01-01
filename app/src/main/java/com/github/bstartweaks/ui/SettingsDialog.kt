@file:Suppress("DEPRECATION")

package com.github.bstartweaks.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceFragment
import com.github.bstartweaks.BuildConfig
import com.github.bstartweaks.R
import com.github.bstartweaks.modulePrefs
import com.github.kyuubiran.ezxhelper.init.InitFields.appContext
import com.github.kyuubiran.ezxhelper.utils.*
import java.text.SimpleDateFormat

class SettingsDialog(context: Context, accessToken: String, refreshToken: String, expires: Long) :
    AlertDialog.Builder(context) {
    companion object {
        private lateinit var outDialog: AlertDialog
        const val PREFS_NAME = "bstar_tweaks"
    }

    class PrefsFragment : PreferenceFragment(),
        android.preference.Preference.OnPreferenceClickListener {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            preferenceManager.sharedPreferencesName = PREFS_NAME
            preferenceManager.putObject("mSharedPreferences", modulePrefs)
            addPreferencesFromResource(R.xml.settings_dialog)

            findPreference("version").summary =
                "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            findPreference("source_code").onPreferenceClickListener = this

            findPreference("access_token").let {
                it.summary = arguments?.getString("access_token")
                it.onPreferenceClickListener = this
            }
            findPreference("refresh_token").let {
                it.summary = arguments?.getString("refresh_token")
                it.onPreferenceClickListener = this
            }
            findPreference("expires").let {
                val expires = arguments?.getLong("expires") ?: return@let
                it.summary = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(expires * 1000)
                it.onPreferenceClickListener = this
            }
        }

        override fun onPreferenceClick(p0: android.preference.Preference?): Boolean {
            if (p0 == null) return false
            if (p0.key == "access_token" || p0.key == "refresh_token" || p0.key == "expires") {
                val clipboardManager =
                    appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText(p0.key, p0.summary)
                clipboardManager.setPrimaryClip(clipData)
                Log.toast("已复制到剪贴板")
                return true
            }
            if (p0.key == "source_code") {
                val webpage: Uri = Uri.parse(p0.summary as String?)
                val intent = Intent(Intent.ACTION_VIEW, webpage)
                startActivity(intent)
                return true
            }
            return false
        }
    }

    init {
        context.addModuleAssetPath()

        val act = context as Activity

        outDialog = run {
            val prefsFragment = PrefsFragment()
            prefsFragment.arguments = Bundle().apply {
                putString("access_token", accessToken)
                putString("refresh_token", refreshToken)
                putLong("expires", expires)
            }

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
