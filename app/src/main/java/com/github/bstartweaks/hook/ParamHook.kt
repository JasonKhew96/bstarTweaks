package com.github.bstartweaks.hook

import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import java.util.*

object ParamHook : BaseHook() {
    override fun init() {
        findMethod(TreeMap::class.java) {
            name == "put"
        }.hookBefore { param ->
            if (param.args[0].javaClass != String::class.java) return@hookBefore
            when (param.args[0] as String) {
                "s_locale", "c_locale" -> {
                    val value = param.args[1] as String
                    if (value == "zh_CN" || value == "zh_TW") {
                        param.args[1] = "zh_SG"
                    }
                }
                "sim_code" -> {
                    param.args[1] = ""
                }
            }
        }
    }
}
