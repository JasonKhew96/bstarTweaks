@file:Suppress("DEPRECATION")

package com.github.bstartweaks.hook

import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.github.bstartweaks.utils.Log
import com.github.bstartweaks.utils.replaceMethod

class NetworkHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        Log.d("startHook: NetworkHook")

        NetworkInfo::class.java.declaredMethods.firstOrNull {
            it.name == "getType"
        }.also { m ->
            m?.replaceMethod {
                ConnectivityManager.TYPE_MOBILE
            }
        }

        val ijkNetworkUtilsClazz =
            mClassLoader.loadClass("tv.danmaku.ijk.media.player.IjkNetworkUtils")

        ijkNetworkUtilsClazz.declaredMethods.firstOrNull {
            it.name == "isWifiValid"
        }.also { m ->
            m?.replaceMethod {
                false
            }
        }

        ijkNetworkUtilsClazz.declaredMethods.firstOrNull {
            it.name == "isMobileNetwork"
        }.also { m ->
            m?.replaceMethod {
                true
            }
        }
    }
}