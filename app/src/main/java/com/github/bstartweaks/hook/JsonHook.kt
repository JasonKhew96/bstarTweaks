package com.github.bstartweaks.hook

import com.github.bstartweaks.modulePrefs
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.finders.MethodFinder
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Type

object JsonHook : BaseHook() {
    private val bangumiApiResponseClass by lazy { loadClass("com.bilibili.bangumi.data.common.api.BangumiApiResponse") }
    private val bangumiUniformSeason by lazy { loadClass("com.bilibili.bangumi.data.page.detail.entity.BangumiUniformSeason") }

    override fun init() {
        val fastJsonClass = loadClass("com.alibaba.fastjson.JSON")

        MethodFinder.fromClass(fastJsonClass).filterByName("parseObject").filterByParamCount(4)
            .filterByParamTypes { param ->
                param[0] == String::class.java && param[1] == Type::class.java && param[2] == Int::class.javaPrimitiveType
            }.first().createHook {
                after { param ->
                    val result = param.result ?: return@after
                    if (result.javaClass == bangumiApiResponseClass) {
                        val data = XposedHelpers.getObjectField(result, "data") ?: return@after
                        if (data.javaClass == bangumiUniformSeason) {
                            if (!modulePrefs.getBoolean("force_allow_download", false)) return@after
                            val allowDownload = XposedHelpers.getBooleanField(data, "allowDownload")
                            if (!allowDownload) {
                                XposedHelpers.setBooleanField(data, "allowDownload", true)
                                Log.toast("已强制启用下载")
                            }
                        }
                    }
                }
            }
    }
}
