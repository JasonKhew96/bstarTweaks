package com.github.bstartweaks.hook

import android.app.AndroidAppHelper
import android.content.Context
import com.github.bstartweaks.ClassMaps
import com.github.bstartweaks.XposedInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

class DownloadHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        XposedInit.log("startHook: DownloadHook")
        val mapData = findMap() ?: throw NoClassDefFoundError("startHook: DownloadHook failed")
        val toastMapData = findToastMap() ?: throw NoClassDefFoundError("startHook: DownloadHook failed")
        val bangumiHelperClazz = mClassLoader.loadClass(mapData.first)
        val bangumiUniformSeasonClazz =
            mClassLoader.loadClass("com.bilibili.bangumi.data.page.detail.entity.BangumiUniformSeason")
        XposedHelpers.findAndHookMethod(
            bangumiHelperClazz,
            mapData.second,
            bangumiUniformSeasonClazz,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    super.afterHookedMethod(param)
                    if (param.result == false) {
                        val toastHelperClazz =
                            mClassLoader.loadClass(toastMapData.first)
                        XposedHelpers.callStaticMethod(
                            toastHelperClazz,
                            toastMapData.second,
                            AndroidAppHelper.currentApplication() as Context,
                            "已强制启用下载"
                        )
                        param.result = true
                    }
                }
            }
        )
    }
    companion object {
        fun findMap(): Pair<String, String>? {
            if (ClassMaps.download.containsKey(XposedInit.versionCode)) {
                return ClassMaps.download[XposedInit.versionCode]
            }
            return ClassMaps.download.maxByOrNull { p-> p.key }?.value
        }
        fun findToastMap(): Pair<String, String>? {
            if (ClassMaps.toast.containsKey(XposedInit.versionCode)) {
                return ClassMaps.toast[XposedInit.versionCode]
            }
            return ClassMaps.toast.maxByOrNull { p-> p.key }?.value
        }
    }
}