@file:Suppress("DEPRECATION")

package com.github.bstartweaks.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceFragment
import com.github.bstartweaks.BilibiliPackage.Companion.instance
import com.github.bstartweaks.BuildConfig
import com.github.bstartweaks.R
import com.github.bstartweaks.modulePrefs
import com.github.kyuubiran.ezxhelper.init.InitFields.appContext
import com.github.kyuubiran.ezxhelper.utils.*
import java.text.SimpleDateFormat

class SettingsDialog(context: Context) : AlertDialog.Builder(context) {

    companion object {
        private lateinit var outDialog: AlertDialog
        private lateinit var prefs: SharedPreferences
        const val PREFS_NAME = "bstar_tweaks"
    }

    class PrefsFragment : PreferenceFragment(),
        android.preference.Preference.OnPreferenceClickListener {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            preferenceManager.sharedPreferencesName = PREFS_NAME
            preferenceManager.putObject("mSharedPreferences", modulePrefs)
            addPreferencesFromResource(R.xml.settings_dialog)
            prefs = preferenceManager.sharedPreferences

            findPreference("version").summary = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            findPreference("source_code").onPreferenceClickListener = this

            // Lcom/bilibili/lib/account/e;
            val accountHelper = instance.accountHelperClass?.invokeStaticMethodAuto("a", context)

            // Lcom/bilibili/lib/passport/c;
            val cObj = accountHelper?.getObject("c")
            // Lcom/bilibili/lib/passport/f;
            val fObj = cObj?.getObject("a")
            // Lcom/bilibili/lib/passport/a;
            val aObj = fObj?.getObject("d")

            // Lcom/bilibili/lib/passport/a;
            val accessToken = aObj?.getObjectAs<String>("c")
            val refreshToken = aObj?.getObjectAs<String>("d")
            val expires = aObj?.getObjectAs<Long>("e")

            if (accessToken != null && refreshToken != null && expires != null) {
                findPreference("access_token")?.run {
                    summary = accessToken
                    onPreferenceClickListener = this@PrefsFragment
                }
                findPreference("refresh_token")?.run {
                    summary = refreshToken
                    onPreferenceClickListener = this@PrefsFragment
                }
                findPreference("expires")?.run {
                    summary = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(expires * 1000)
                    onPreferenceClickListener = this@PrefsFragment
                }

            } else {
                findPreference("access_token").summary = "未登录"
                findPreference("refresh_token").summary = "未登录"
                findPreference("expires").summary = "未登录"
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
            }
            return false
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
