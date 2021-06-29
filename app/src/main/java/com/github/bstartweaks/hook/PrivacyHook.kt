package com.github.bstartweaks.hook

import com.github.bstartweaks.XposedInit
import com.github.kyuubiran.ezxhelper.utils.findMethodByCondition
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

class PrivacyHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        XposedInit.log("startHook: PrivacyHook")

        findMethodByCondition("com.bilibili.api.b") {
            it.name == "a"
        }.also { m->
            m.hookAfter { param->
                param.result = ""
            }
        }

        findMethodByCondition("com.bilibili.api.b") {
            it.name == "a" && it.parameterTypes.size == 1 &&
                    it.parameterTypes[0] == String::class.java
        }.also { m->
            m.hookBefore { param->
                param.args[0] = ""
            }
        }
    }
}