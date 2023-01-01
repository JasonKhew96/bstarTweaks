package com.github.bstartweaks.hook

import com.github.bstartweaks.modulePrefs
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.getObjectAs
import com.github.kyuubiran.ezxhelper.utils.getObjectOrNull
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.loadClassOrNull
import com.github.kyuubiran.ezxhelper.utils.putObject
import java.lang.reflect.Type

object JsonHook : BaseHook() {
    private val fastJsonFeatureClass by lazy { loadClassOrNull("com.alibaba.fastjson.parser.Feature[]") }
    private val generalResponseClass by lazy { loadClassOrNull("com.bilibili.okretro.GeneralResponse") }
    private val bangumiApiResponseClass by lazy { loadClassOrNull("com.bilibili.bangumi.data.common.api.BangumiApiResponse") }
    private val bangumiUniformSeason by lazy { loadClassOrNull("com.bilibili.bangumi.data.page.detail.entity.BangumiUniformSeason") }
    private val fastJsonClass by lazy { loadClassOrNull("com.alibaba.fastjson.JSON") }

    override fun init() {
        fastJsonClass?.let {
            findAllMethods(it) {
                name == "parseObject" && parameterTypes.size == 4 && parameterTypes[0] == String::class.java && parameterTypes[1] == Type::class.java && parameterTypes[2] == Int::class.javaPrimitiveType && parameterTypes[3] == fastJsonFeatureClass
            }.hookAfter { param ->
                val result = param.result ?: return@hookAfter
                if (result.javaClass == generalResponseClass) {
                    return@hookAfter
                }
                if (result.javaClass == bangumiApiResponseClass) {
                    val newResult = result.getObjectOrNull("data") ?: return@hookAfter
                    if (newResult.javaClass == bangumiUniformSeason) {
                        if (!modulePrefs.getBoolean("force_allow_download", false)) return@hookAfter
                        val allowDownload = newResult.getObjectAs<Boolean>("allowDownload")
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
