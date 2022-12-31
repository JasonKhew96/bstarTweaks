package com.github.bstartweaks.hook

import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import java.util.*

object ParamHook : BaseHook() {
    override fun init() {
        findMethod(TreeMap::class.java) {
            name == "put"
        }.hookBefore { param ->
            if (param.args[0] == null || param.args[0].javaClass != String::class.java) return@hookBefore
            when (param.args[0] as String) {
                "s_locale", "c_locale" -> {
                    val value = param.args[1] as String
                    if (value == "") return@hookBefore
                    val forbiddenCountry = listOf("CN", "HK", "TW", "MO")
                    val (language, country) = value.split("_")
                    if (country == "" || country in forbiddenCountry) {
                        param.args[1] = "${language}_SG"
                    }
                }
                "sim_code" -> {
                    param.args[1] = ""
                }
            }
        }
    }
}
