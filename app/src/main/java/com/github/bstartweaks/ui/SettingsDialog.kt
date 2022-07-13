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
import com.github.bstartweaks.BilibiliPackage.Companion.dexHelper
import com.github.bstartweaks.BuildConfig
import com.github.bstartweaks.R
import com.github.bstartweaks.modulePrefs
import com.github.kyuubiran.ezxhelper.init.InitFields.appContext
import com.github.kyuubiran.ezxhelper.utils.*
import java.text.SimpleDateFormat

class SettingsDialog(context: Context) : AlertDialog.Builder(context) {

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

            try {
                val biliAccountClass = dexHelper.findMethodUsingString(
                    "BiliAccount",
                    false,
                    dexHelper.encodeClassIndex(Void.TYPE),
                    0,
                    null,
                    -1,
                    null,
                    null,
                    null,
                    true,
                ).asSequence().firstNotNullOfOrNull {
                    dexHelper.decodeMethodIndex(it)?.declaringClass
                } ?: throw Throwable("Class not found")
                val biliPassportClass = dexHelper.findMethodUsingString(
                    "BiliPassport",
                    false,
                    -1,
                    0,
                    null,
                    -1,
                    null,
                    null,
                    null,
                    true,
                ).asSequence().firstNotNullOfOrNull {
                    dexHelper.decodeMethodIndex(it)?.declaringClass
                } ?: throw Throwable("Class not found")
                val passportControllerClass = dexHelper.findMethodUsingString(
                    "PassportController",
                    false,
                    -1,
                    1,
                    null,
                    -1,
                    null,
                    null,
                    null,
                    true,
                ).asSequence().firstNotNullOfOrNull {
                    dexHelper.decodeMethodIndex(it)?.declaringClass
                } ?: throw Throwable("Class not found")
                val accessTokenClass = dexHelper.findMethodUsingString(
                    "AccessToken{mExpiresIn=",
                    false,
                    -1,
                    0,
                    null,
                    -1,
                    null,
                    null,
                    null,
                    true,
                ).asSequence().firstNotNullOfOrNull {
                    dexHelper.decodeMethodIndex(it)?.declaringClass
                } ?: throw Throwable("Class not found")

                val biliAccountClassMethod = biliAccountClass.declaredMethods.firstOrNull {
                    it.isStatic && it.parameterTypes.size == 1 && it.parameterTypes[0] == Context::class.java && it.returnType == biliAccountClass
                } ?: throw Throwable("Method not found")

                val biliPassportClassField = biliAccountClass.declaredFields.firstOrNull {
                    it.type == biliPassportClass
                } ?: throw Throwable("Field not found")

                val passportControllerClassField = biliPassportClass.declaredFields.firstOrNull {
                    it.type == passportControllerClass
                } ?: throw Throwable("Field not found")

                val accessTokenClassField = passportControllerClass.declaredFields.firstOrNull {
                    it.type == accessTokenClass
                } ?: throw Throwable("Field not found")

                val biliAccountObj = biliAccountClassMethod.invoke(null, context)
                val biliPassportObj = biliPassportClassField.get(biliAccountObj)
                val passportControllerObj = passportControllerClassField.get(biliPassportObj)
                val accessTokenObj = accessTokenClassField.get(passportControllerObj)
                Log.d("accessTokenObj: $accessTokenObj")

//                var expiresIn: Long = 0L
//                var mid: Long = 0L
                var accessToken = ""
                var refreshToken = ""
                var expires = 0L

                accessTokenClass.declaredFields.forEach { f ->
                    f.annotations.forEach { a ->
                        a.toString().let { annotation ->
//                            if (annotation.contains("name=expires_in")) {
//                                expiresIn = f.getLong(accessTokenObj)
//                            } else if (annotation.contains("name=mid")) {
//                                mid = f.getLong(accessTokenObj)
//                            }
                            if (annotation.contains("name=access_token")) {
                                f.get(accessTokenObj)?.let {
                                    accessToken = it.toString()
                                }
                            } else if (annotation.contains("name=refresh_token")) {
                                f.get(accessTokenObj)?.let {
                                    refreshToken = it.toString()
                                }
                            } else if (annotation.contains("name=expires")) {
                                expires = f.getLong(accessTokenObj)
                            }
                        }
                    }
                }

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
            } catch (t: Throwable) {
                Log.d(t)
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
