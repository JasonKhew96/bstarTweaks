package com.github.bstartweaks.hook

import android.content.Context
import com.github.bstartweaks.MainHook.Companion.dexKit
import com.github.bstartweaks.MainHook.Companion.isDexKitNeeded
import com.github.bstartweaks.modulePrefs
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder

object DebugHook : BaseHook() {
    private lateinit var blkvGetBooleanClassName: String
    private lateinit var blkvGetBooleanMethodName: String

    private const val HOOK_BLKV_GET_BOOLEAN_CLASS = "hook_blkv_get_boolean_class"
    private const val HOOK_BLKV_GET_BOOLEAN_METHOD = "hook_blkv_get_boolean_method"

    private fun searchHook() {
        val blkvGetBooleanMethod = dexKit.findMethodUsingString {
            usingString = "^return blkv boolean --> $"
            methodReturnType = Boolean::class.java.name
            methodParamTypes = arrayOf(
                Context::class.java.name,
                String::class.java.name,
                String::class.java.name,
                Boolean::class.java.name
            )
        }.firstNotNullOfOrNull { it } ?: throw NoSuchMethodError()

        blkvGetBooleanClassName = blkvGetBooleanMethod.declaringClassName
        blkvGetBooleanMethodName = blkvGetBooleanMethod.name

        modulePrefs.edit().apply {
            putString(HOOK_BLKV_GET_BOOLEAN_CLASS, blkvGetBooleanClassName)
            putString(HOOK_BLKV_GET_BOOLEAN_METHOD, blkvGetBooleanMethodName)
        }.apply()
    }

    private fun loadCachedHook() {
        modulePrefs.apply {
            blkvGetBooleanClassName = getString(HOOK_BLKV_GET_BOOLEAN_CLASS, "")!!
            blkvGetBooleanMethodName = getString(HOOK_BLKV_GET_BOOLEAN_METHOD, "")!!
        }
    }

    override fun init() {
        if (isDexKitNeeded) {
            searchHook()
        } else {
            loadCachedHook()
        }

        MethodFinder.fromClass(loadClass(blkvGetBooleanClassName))
            .filterByName(blkvGetBooleanMethodName).first().createHook {
                before { param ->
                    if (param.args[2] == "pref_is_show_debug_tool") {
                        param.result = true
                    }
                }
            }
    }
}