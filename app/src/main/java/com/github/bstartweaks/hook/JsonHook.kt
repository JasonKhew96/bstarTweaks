package com.github.bstartweaks.hook

import com.github.bstartweaks.BilibiliPackage.Companion.instance
import com.github.bstartweaks.modulePrefs
import com.github.kyuubiran.ezxhelper.utils.*
import java.lang.reflect.Type

object JsonHook : BaseHook() {
    override fun init() {
        instance.fastJsonClass?.let {
            findAllMethods(it) {
                name == "parseObject" && parameterTypes.size == 4 && parameterTypes[0] == String::class.java && parameterTypes[1] == Type::class.java && parameterTypes[2] == Int::class.javaPrimitiveType && parameterTypes[3] == instance.fastJsonFeatureClass
            }.hookAfter { param ->
                val result = param.result ?: return@hookAfter
                if (result.javaClass == instance.generalResponseClass) {
                    return@hookAfter
                }
                if (result.javaClass == instance.bangumiApiResponseClass) {
                    val newResult = result.getObjectOrNull("data") ?: return@hookAfter
                    if (newResult.javaClass == instance.bangumiUniformSeason) {
                        if (modulePrefs.getBoolean("force_allow_download", false)) return@hookAfter
                        val allowDownload =
                            newResult.getObjectAs<Boolean>("allowDownload") ?: return@hookAfter
                        if (!allowDownload) {
                            newResult.putObject("allowDownload", true)
                            Log.toast("已强制启用下载")
                        }
                    }
                }
            }
        }
    }
}
