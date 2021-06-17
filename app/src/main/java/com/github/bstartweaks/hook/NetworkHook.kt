@file:Suppress("DEPRECATION")

package com.github.bstartweaks.hook

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.github.bstartweaks.XposedInit
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers

class NetworkHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        XposedInit.log("startHook: NetworkHook")
        XposedHelpers.findAndHookMethod(
            NetworkInfo::class.java, "getType",
            XC_MethodReplacement.returnConstant(ConnectivityManager.TYPE_MOBILE)
        )
        val ijkNetworkUtilsClazz = mClassLoader.loadClass("tv.danmaku.ijk.media.player.IjkNetworkUtils")
        XposedHelpers.findAndHookMethod(ijkNetworkUtilsClazz, "isWifiValid", Context::class.java, XC_MethodReplacement.returnConstant(false))
        XposedHelpers.findAndHookMethod(ijkNetworkUtilsClazz, "isMobileNetwork", Context::class.java, XC_MethodReplacement.returnConstant(true))
    }
}