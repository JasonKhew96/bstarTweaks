package com.github.bstartweaks.hook

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.bstartweaks.BilibiliPackage.Companion.instance
import com.github.bstartweaks.BuildConfig
import com.github.bstartweaks.ui.Preference
import com.github.bstartweaks.ui.SettingsDialog
import com.github.kyuubiran.ezxhelper.utils.*

object SettingsHook : BaseHook() {
    override fun init() {
        findMethod("com.bilibili.base.e") {
            parameterTypes.size == 4 && returnType == Boolean::class.java && parameterTypes[0] == Context::class.java && parameterTypes[1] == String::class.java && parameterTypes[2] == String::class.java && parameterTypes[3] == Boolean::class.java
        }.hookAfter { param ->
            if (param.args[2] == "pref_is_show_debug_tool") {
                param.result = true
            }
        }

        instance.helpFragmentClass?.let {
            findMethod(it) {
                name == "onCreateView" && parameterTypes.size == 3 && parameterTypes[0] == LayoutInflater::class.java && parameterTypes[1] == ViewGroup::class.java && parameterTypes[2] == Bundle::class.java
            }.hookAfter { param ->
//                param.thisObject.invokeMethod("i1")

                val activity = param.thisObject.invokeMethodAs<Activity>("getActivity")

                val preferenceScreen = param.thisObject.invokeMethod("getPreferenceScreen")

                if (preferenceScreen?.invokeMethodAuto(
                        "findPreference", "bstar_tweaks"
                    ) != null
                ) {
                    return@hookAfter
                }

                val hookPreference = Preference(activity as Context).apply {
                    title = "bstar 工具箱 ${BuildConfig.VERSION_NAME}"
                    summary = "@JasonKhew96"
                    key = "bstar_tweaks"
                    setOnPreferenceClickListener(object : Preference.OnPreferenceClickListener {
                        override fun onPreferenceClick(preference: Preference): Boolean {
                            SettingsDialog(activity)
                            return true
                        }
                    })
                }

                preferenceScreen?.invokeMethodAuto("addPreference", hookPreference.build())
            }
        }
    }
}
