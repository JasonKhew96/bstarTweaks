package com.github.bstartweaks.hook

import android.content.Context
import com.github.bstartweaks.MainHook.Companion.dexKit
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookBefore

object DebugHook : BaseHook() {
    override fun init() {
        val blkvGetBooleanMethod = dexKit.findMethodUsingString(
            usingString = "return blkv boolean --> ",
            methodReturnType = Boolean::class.java.name,
            methodParamTypes = arrayOf(
                Context::class.java.name,
                String::class.java.name,
                String::class.java.name,
                Boolean::class.java.name
            ),
        ).firstNotNullOfOrNull { it } ?: throw NoSuchMethodError()

        findMethod(blkvGetBooleanMethod.declaringClassName) {
            name == blkvGetBooleanMethod.name
        }.hookBefore { param ->
            if (param.args[2] == "pref_is_show_debug_tool") {
                param.result = true
            }
        }
    }
}