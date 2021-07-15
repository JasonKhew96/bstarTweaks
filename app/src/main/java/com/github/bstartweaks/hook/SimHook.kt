package com.github.bstartweaks.hook

import android.telephony.TelephonyManager
import com.github.bstartweaks.utils.Log
import com.github.bstartweaks.utils.replaceMethod

class SimHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        Log.d("startHook: SimHook")

        TelephonyManager::class.java.declaredMethods.firstOrNull {
            it.name == "getSimOperator"
        }.also { m ->
            m?.replaceMethod {
                ""
            }
        }
    }
}