@file:Suppress("DEPRECATION")

package com.github.bstartweaks

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import com.github.bstartweaks.hook.*
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.findMethodByCondition
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage


class XposedInit : IXposedHookLoadPackage {
    companion object {

        private const val TAG = "bstarTweaks"
        lateinit var classLoader: ClassLoader
        lateinit var processName: String
        var versionCode: Int = 0
        var versionCodeStr: String = ""

        fun handleBstar() {
            log("hook bstar")

            findMethodByCondition(Application::class.java) {
                it.name == "attach"
            }.also { m ->
                m.hookAfter { param ->
                    val currentContext = param.args[0] as Context
                    versionCode = getAppVersionCode(currentContext)
                    versionCodeStr = versionCode.toString()

                    startHook(SettingsHook(classLoader))
                    startHook(InfoHook(classLoader))
                    startHook(SimHook(classLoader))
                    startHook(LocaleHook(classLoader))

                    val prefs = currentContext.getSharedPreferences(
                        "bstar_tweaks",
                        Context.MODE_PRIVATE
                    )

                    val forceAllowDownload = prefs.getBoolean("force_allow_download", false)
                    if (forceAllowDownload) {
                        startHook(DownloadHook(classLoader))
                    }

                    val forceMobileNetwork = prefs.getBoolean("force_mobile_network", false)
                    if (forceMobileNetwork) {
                        startHook(NetworkHook(classLoader))
                    }

                    val cleanShareUrl = prefs.getBoolean("clean_share_url", false)
                    if (cleanShareUrl) {
                        startHook(ShareHook(classLoader))
                    }
                }
            }
        }

        fun getMajorVersionCode(): Int {
            return Integer.parseInt(versionCodeStr.substring(0, 4))
        }

        private fun getAppVersionCode(context: Context): Int {
            return try {
                val packageInfo: PackageInfo =
                    context.packageManager.getPackageInfo(Constants.PACKAGE_NAME, 0)
                packageInfo.versionCode
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                0
            }
        }

        private fun startHook(hooker: BaseHook) {
            try {
                hooker.startHook()
            } catch (e: Throwable) {
                log(e)
            }
        }

        fun log(e: Any?) {
            Log.d(TAG, "$processName: $e")
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != lpparam.processName) return
        EzXHelperInit.initHandleLoadPackage(lpparam)
        classLoader = lpparam.classLoader
        processName = lpparam.processName
        when (lpparam.packageName) {
            Constants.PACKAGE_NAME -> handleBstar()
        }
    }


}