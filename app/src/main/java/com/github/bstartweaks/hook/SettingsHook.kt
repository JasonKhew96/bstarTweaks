package com.github.bstartweaks.hook

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.bstartweaks.BuildConfig
import com.github.bstartweaks.dialog.SettingsDialog
import com.github.bstartweaks.ui.Preference
import com.github.bstartweaks.utils.Log
import com.github.bstartweaks.utils.callMethod
import com.github.bstartweaks.utils.hookAfterMethod

class SettingsHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        Log.d("startHook: SettingsHook")
        val helpFragmentClazz =
            mClassLoader.loadClass("com.bilibili.app.preferences.fragment.HelpFragment")

        helpFragmentClazz.declaredMethods.firstOrNull {
            it.name == "onCreateView" && it.parameterTypes.size == 3 &&
                    it.parameterTypes[0] == LayoutInflater::class.java &&
                    it.parameterTypes[1] == ViewGroup::class.java &&
                    it.parameterTypes[2] == Bundle::class.java
        }.also { m ->
            m?.hookAfterMethod { param ->
                val activity = param.thisObject.callMethod("getActivity")

                val preferenceScreen = param.thisObject.callMethod("getPreferenceScreen")

                if (preferenceScreen?.callMethod("findPreference", "bstar_tweaks") != null) {
                    return@hookAfterMethod
                }

                val hookPreference = Preference(activity as Context).apply {
                    title = "bstar 工具箱 ${BuildConfig.VERSION_NAME}"
                    summary = "@JasonKhew96"
                    key = "bstar_tweaks"
                    setOnPreferenceClickListener(
                        object : Preference.OnPreferenceClickListener {
                            override fun onPreferenceClick(preference: Preference): Boolean {
                                SettingsDialog(activity).show()
                                return true
                            }
                        }
                    )
                }

                preferenceScreen?.callMethod("addPreference", hookPreference.build())
            }
        }

    }
}