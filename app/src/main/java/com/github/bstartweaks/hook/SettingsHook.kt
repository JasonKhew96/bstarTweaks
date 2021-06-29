package com.github.bstartweaks.hook

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.bstartweaks.*
import com.github.bstartweaks.dialog.SettingsDialog
import com.github.bstartweaks.ui.Preference
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

class SettingsHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        XposedInit.log("startHook: SettingsHook")
        val helpFragmentClazz =
            mClassLoader.loadClass("com.bilibili.app.preferences.fragment.HelpFragment")
        XposedHelpers.findAndHookMethod(helpFragmentClazz,
            "onCreateView",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Bundle::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    super.afterHookedMethod(param)
                    val activity = XposedHelpers.callMethod(
                        param.thisObject,
                        "getActivity"
                    )
//                                    val context = XposedHelpers.callMethod(
//                                        activity,
//                                        "getApplicationContext"
//                                    ) as Context

                    val preferenceScreen = XposedHelpers.callMethod(
                        param.thisObject,
                        "getPreferenceScreen"
                    )

                    if (XposedHelpers.callMethod(
                            preferenceScreen,
                            "findPreference",
                            "bstar_tweaks"
                        ) != null
                    ) {
                        return
                    }


                    val hookPreference = Preference(activity as Context).apply {
                        title = "bstar 工具箱 ${BuildConfig.VERSION_NAME}"
                        summary = "魔法 (${Constants.supported_bstar_version_min}-${Constants.supported_bstar_version_max})"
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
                    XposedHelpers.callMethod(
                        preferenceScreen,
                        "addPreference",
                        hookPreference.build()
                    )

                    XposedHelpers.callMethod(param.thisObject, findMap())
                }
            })
    }

    companion object {
        fun findMap(): String? {
            if (MethodMaps.help.containsKey(XposedInit.getMajorVersionCode())) {
                return MethodMaps.help[XposedInit.getMajorVersionCode()]
            }
            return MethodMaps.help.maxByOrNull { p-> p.key }?.value
        }
    }
}