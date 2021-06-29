package com.github.bstartweaks.hook

import android.app.AndroidAppHelper
import android.content.Context
import com.github.bstartweaks.ClassMaps
import com.github.bstartweaks.XposedInit
import com.github.kyuubiran.ezxhelper.utils.findMethodByCondition
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.invokeStaticMethodAuto

class DownloadHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        XposedInit.log("startHook: DownloadHook")

        val mapData = findMap() ?: throw NoClassDefFoundError("startHook: DownloadHook failed")
        val toastMapData =
            findToastMap() ?: throw NoClassDefFoundError("startHook: DownloadHook failed")
        val bangumiHelperClazz = mClassLoader.loadClass(mapData.first)
        val bangumiUniformSeasonClazz =
            mClassLoader.loadClass("com.bilibili.bangumi.data.page.detail.entity.BangumiUniformSeason")

        findMethodByCondition(bangumiHelperClazz) {
            it.name == mapData.second &&
                    it.parameterTypes.size == 1 &&
                    it.parameterTypes[0] == bangumiUniformSeasonClazz
        }.also { m ->
            m.hookAfter { param ->
                if (param.result == false) {
                    val toastHelperClazz =
                        mClassLoader.loadClass(toastMapData.first)
                    toastHelperClazz.invokeStaticMethodAuto(
                        toastMapData.second,
                        AndroidAppHelper.currentApplication() as Context,
                        "已强制启用下载"
                    )
                    param.result = true
                }
            }
        }
    }

    companion object {
        fun findMap(): Pair<String, String>? {
            if (ClassMaps.download.containsKey(XposedInit.getMajorVersionCode())) {
                return ClassMaps.download[XposedInit.getMajorVersionCode()]
            }
            return ClassMaps.download.maxByOrNull { p -> p.key }?.value
        }

        fun findToastMap(): Pair<String, String>? {
            if (ClassMaps.toast.containsKey(XposedInit.getMajorVersionCode())) {
                return ClassMaps.toast[XposedInit.getMajorVersionCode()]
            }
            return ClassMaps.toast.maxByOrNull { p -> p.key }?.value
        }
    }
}