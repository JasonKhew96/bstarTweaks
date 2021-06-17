package com.github.bstartweaks

import android.telephony.TelephonyManager
import com.github.bstartweaks.hook.BaseHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers

class SimHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        XposedInit.log("startHook: SimHook")
        XposedHelpers.findAndHookMethod(
            TelephonyManager::class.java,
            "getSimOperator",
            XC_MethodReplacement.returnConstant("")
        )
    }
}