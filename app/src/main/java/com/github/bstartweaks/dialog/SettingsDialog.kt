@file:Suppress("DEPRECATION")

package com.github.bstartweaks.dialog

import android.app.Activity
import android.app.AlertDialog
import android.app.AndroidAppHelper
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.*
import com.github.bstartweaks.BuildConfig
import de.robv.android.xposed.XposedHelpers
import kotlin.system.exitProcess

class SettingsDialog(context: Context) : AlertDialog.Builder(context) {
    companion object {
        @JvmStatic
        fun restartApplication(activity: Activity) {
            // https://stackoverflow.com/a/58530756
            val pm = activity.packageManager
            val intent = pm.getLaunchIntentForPackage(activity.packageName)
            activity.finishAffinity()
            activity.startActivity(intent)
            exitProcess(0)
        }
    }

    class PrefFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {
        private lateinit var prefs: SharedPreferences
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            XposedHelpers.setObjectField(
                preferenceManager,
                "mSharedPreferences",
                (AndroidAppHelper.currentApplication() as Context).getSharedPreferences(
                    "bstar_tweaks",
                    Context.MODE_PRIVATE
                )
            )
            val screen = preferenceManager.createPreferenceScreen(activity)
            preferenceScreen = screen

            val prefAllowDownload = SwitchPreference(screen.context).apply {
                title = "强制允许下载"
                key = "force_allow_download"
            }
            screen.addPreference(prefAllowDownload)

            val prefForceMobileNetwork = SwitchPreference(screen.context).apply {
                title = "流量网络欺骗"
                key = "force_mobile_network"
                summary = "WiFi 情况下也禁止自动播放"
            }
            screen.addPreference(prefForceMobileNetwork)

            val prefCleanShareUrl = SwitchPreference(screen.context).apply {
                title = "链接清理"
                key = "clean_share_url"
                summary = "去除分享链接里的追踪参数"
            }
            screen.addPreference(prefCleanShareUrl)

            val prefPrivacyMode = SwitchPreference(screen.context).apply {
                title = "隐私模式(试验)"
                key = "privacy_mode"
                summary = "禁止对服务器发送 buvid"
            }
            screen.addPreference(prefPrivacyMode)

            prefs = preferenceManager.sharedPreferences
        }

        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            return true
        }

        override fun onPreferenceClick(preference: Preference?): Boolean {
            return true
        }

    }

    init {

        val activity = context as Activity
        val prefFragment = PrefFragment()
        activity.fragmentManager.beginTransaction().add(prefFragment, "Setting").commit()
        activity.fragmentManager.executePendingTransactions()

        prefFragment.onActivityCreated(null)

        setView(prefFragment.view)
        setTitle("bstar 工具箱 ${BuildConfig.VERSION_NAME}")
        setNegativeButton("返回") { _, _ ->

        }
        setPositiveButton("确定并重启客户端") { _, _ ->
            restartApplication(activity)
        }
    }

}