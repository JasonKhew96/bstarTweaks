package com.github.bstartweaks.hook

import com.github.bstartweaks.XposedInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

class PrivacyHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        XposedInit.log("startHook: PrivacyHook")

        XposedHelpers.findAndHookMethod(
            "com.bilibili.api.b",
            mClassLoader,
            "a",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    param.result = ""
                }
            }
        )

        XposedHelpers.findAndHookMethod(
            "com.bilibili.api.b",
            mClassLoader,
            "a",
            String::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    param.args[0] = ""
                }
            }
        )
    }
}