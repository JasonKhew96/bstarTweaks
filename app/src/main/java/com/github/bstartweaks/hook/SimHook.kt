package com.github.bstartweaks.hook

import android.telephony.TelephonyManager
import com.github.bstartweaks.XposedInit
import com.github.kyuubiran.ezxhelper.utils.findMethodByCondition
import com.github.kyuubiran.ezxhelper.utils.hookReplace

class SimHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        XposedInit.log("startHook: SimHook")

        findMethodByCondition(TelephonyManager::class.java) {
            it.name == "getSimOperator"
        }.also { m ->
            m.hookReplace {
                ""
            }
        }
    }
}