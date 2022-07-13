package com.github.bstartweaks.hook

import android.content.Context
import com.github.bstartweaks.BilibiliPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

object DebugHook : BaseHook() {
    override fun init() {
        val blkvGetBooleanMethod = BilibiliPackage.dexHelper.findMethodUsingString(
            "return blkv boolean --> ",
            false,
            BilibiliPackage.dexHelper.encodeClassIndex(Boolean::class.java),
            4,
            null,
            -1,
            longArrayOf(
                BilibiliPackage.dexHelper.encodeClassIndex(Context::class.java),
                BilibiliPackage.dexHelper.encodeClassIndex(String::class.java),
                BilibiliPackage.dexHelper.encodeClassIndex(String::class.java),
                BilibiliPackage.dexHelper.encodeClassIndex(Boolean::class.java)
            ),
            null,
            null,
            true,
        ).asSequence().firstNotNullOfOrNull {
            BilibiliPackage.dexHelper.decodeMethodIndex(it)
        } ?: return

        XposedBridge.hookMethod(blkvGetBooleanMethod, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (param.args[2] == "pref_is_show_debug_tool") {
                    param.result = true
                }
            }
        })
    }
}