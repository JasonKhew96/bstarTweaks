package com.github.bstartweaks.hook

import android.telephony.TelephonyManager
import com.github.bstartweaks.XposedInit
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