package com.github.bstartweaks.hook

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.bstartweaks.BuildConfig
import com.github.bstartweaks.Constant
import com.github.bstartweaks.MethodMaps
import com.github.bstartweaks.XposedInit
import com.github.bstartweaks.dialog.SettingsDialog
import com.github.bstartweaks.ui.Preference
import com.github.bstartweaks.utils.Log
import com.github.bstartweaks.utils.callMethod
import com.github.bstartweaks.utils.hookAfterMethod
import java.lang.reflect.Modifier

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
                    summary =
                        "魔法 (${Constant.supported_bstar_version_min}-${Constant.supported_bstar_version_max})"
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

                // experimental
                val filteredMethods = helpFragmentClazz.declaredMethods.filter {
                    Modifier.isPrivate(it.modifiers) && it.returnType == Void.TYPE && it.parameterTypes.isEmpty()
                }
                if (filteredMethods.size >= 4) {
                    param.thisObject.callMethod(filteredMethods[3].name) // should always at 4
                } else { // fallback
                    val mapData =
                        findMap() ?: throw NoClassDefFoundError("startHook: ShareHook failed")
                    param.thisObject.callMethod(mapData)
                }
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