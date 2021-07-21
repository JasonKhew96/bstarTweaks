package com.github.bstartweaks.hook

import com.github.bstartweaks.utils.*
import java.lang.reflect.Type

class DownloadHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        Log.d("startHook: DownloadHook")

        val generalResponseClass =
            "com.bilibili.okretro.GeneralResponse".findClassOrNull(mClassLoader)
        val bangumiApiResponseClass =
            "com.bilibili.bangumi.data.common.api.BangumiApiResponse".findClassOrNull(mClassLoader)
        val bangumiUniformSeason =
            "com.bilibili.bangumi.data.page.detail.entity.BangumiUniformSeason".findClassOrNull(
                mClassLoader
            )
        val fastJsonClass = "com.alibaba.fastjson.JSON".findClassOrNull(mClassLoader)

        fastJsonClass?.hookAfterMethod(
            "parseObject",
            String::class.java,
            Type::class.java,
            Int::class.javaPrimitiveType,
            "com.alibaba.fastjson.parser.Feature[]"
        ) { param ->
            val result = param.result ?: return@hookAfterMethod
            if (result.javaClass == generalResponseClass) {
                return@hookAfterMethod
            }
            if (result.javaClass == bangumiApiResponseClass) {
                val newResult = result.getObjectField("result")
                if (newResult?.javaClass == bangumiUniformSeason) {
                    val rights = newResult?.getObjectField("rights")
                    val allowDownload = rights?.getBooleanFieldOrNull("allowDownload")
                    if (allowDownload?.equals(false) == true) {
                        rights.setBooleanField("allowDownload", true)
                        Log.toast("已强制启用下载")
                    }
                }
            }
//            Log.d(result.javaClass)
        }
    }
}