@file:Suppress("DEPRECATION")

package com.github.bstartweaks.hook

import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.github.bstartweaks.XposedInit
import com.github.kyuubiran.ezxhelper.utils.findMethodByCondition
import com.github.kyuubiran.ezxhelper.utils.hookReplace

class NetworkHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        XposedInit.log("startHook: NetworkHook")

        findMethodByCondition(NetworkInfo::class.java) {
            it.name == "getType"
        }.also { m ->
            m.hookReplace {
                ConnectivityManager.TYPE_MOBILE
            }
        }

        val ijkNetworkUtilsClazz =
            mClassLoader.loadClass("tv.danmaku.ijk.media.player.IjkNetworkUtils")

        findMethodByCondition(ijkNetworkUtilsClazz) {
            it.name == "isWifiValid"
        }.also { m ->
            m.hookReplace {
                false
            }
        }

        findMethodByCondition(ijkNetworkUtilsClazz) {
            it.name == "isMobileNetwork"
        }.also { m ->
            m.hookReplace {
                true
            }
        }

    }
}