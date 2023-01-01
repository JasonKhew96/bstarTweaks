package com.github.bstartweaks.hook

import com.github.bstartweaks.modulePrefs
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.getObjectAs
import com.github.kyuubiran.ezxhelper.utils.getObjectOrNull
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.loadClass
import com.github.kyuubiran.ezxhelper.utils.putObject
import java.lang.reflect.Type

object JsonHook : BaseHook() {
    private val bangumiApiResponseClass by lazy { loadClass("com.bilibili.bangumi.data.common.api.BangumiApiResponse") }
    private val bangumiUniformSeason by lazy { loadClass("com.bilibili.bangumi.data.page.detail.entity.BangumiUniformSeason") }

    override fun init() {
        val fastJsonClass = loadClass("com.alibaba.fastjson.JSON")

        findMethod(fastJsonClass) {
            name == "parseObject" && parameterTypes.size == 4 && parameterTypes[0] == String::class.java && parameterTypes[1] == Type::class.java && parameterTypes[2] == Int::class.javaPrimitiveType
        }.hookAfter { param ->
            val result = param.result ?: return@hookAfter
            if (result.javaClass == bangumiApiResponseClass) {
                val data = result.getObjectOrNull("data") ?: return@hookAfter
                if (data.javaClass == bangumiUniformSeason) {
                    if (!modulePrefs.getBoolean("force_allow_download", false)) return@hookAfter
                    val allowDownload = data.getObjectAs<Boolean>("allowDownload")
                    if (!allowDownload) {
                        data.putObject("allowDownload", true)
                        Log.toast("已强制启用下载")
                    }
                }
            }
        }
    }
}
