package com.github.bstartweaks.hook

import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder
import java.util.*

object ParamHook : BaseHook() {
    override fun init() {
        MethodFinder.fromClass(TreeMap::class.java).filterByName("put").first().createHook {
            before { param ->
                if (param.args[0] == null || param.args[0].javaClass != String::class.java) return@before
                when (param.args[0] as String) {
                    "s_locale", "c_locale" -> {
                        val value = param.args[1] as String
                        if (value == "") return@before
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
}
