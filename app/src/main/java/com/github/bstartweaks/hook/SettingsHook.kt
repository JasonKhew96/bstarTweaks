package com.github.bstartweaks.hook

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.bstartweaks.*
import com.github.bstartweaks.dialog.SettingsDialog
import com.github.bstartweaks.ui.Preference
import com.github.kyuubiran.ezxhelper.utils.findMethodByCondition
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.invokeMethodAuto

class SettingsHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        XposedInit.log("startHook: SettingsHook")
        val helpFragmentClazz =
            mClassLoader.loadClass("com.bilibili.app.preferences.fragment.HelpFragment")

        findMethodByCondition(helpFragmentClazz) {
            it.name == "onCreateView" && it.parameterTypes.size == 3 &&
                    it.parameterTypes[0] == LayoutInflater::class.java &&
                    it.parameterTypes[1] == ViewGroup::class.java &&
                    it.parameterTypes[2] == Bundle::class.java
        }.also { m ->
            m.hookAfter { param ->
                val activity = param.thisObject.invokeMethodAuto("getActivity")

                val preferenceScreen = param.thisObject.invokeMethodAuto("getPreferenceScreen")

                if (preferenceScreen?.invokeMethodAuto("findPreference", "bstar_tweaks") != null) {
                    return@hookAfter
                }

                val hookPreference = Preference(activity as Context).apply {
                    title = "bstar 工具箱 ${BuildConfig.VERSION_NAME}"
                    summary =
                        "魔法 (${Constants.supported_bstar_version_min}-${Constants.supported_bstar_version_max})"
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

                preferenceScreen?.invokeMethodAuto("addPreference", hookPreference.build())

                val mapData = findMap() ?: throw NoClassDefFoundError("startHook: ShareHook failed")
                param.thisObject.invokeMethodAuto(mapData)
            }
        }
    }

    companion object {
        fun findMap(): String? {
            if (MethodMaps.help.containsKey(XposedInit.getMajorVersionCode())) {
                return MethodMaps.help[XposedInit.getMajorVersionCode()]
            }
            return MethodMaps.help.maxByOrNull { p -> p.key }?.value
        }
    }
}