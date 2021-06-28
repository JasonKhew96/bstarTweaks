@file:Suppress("DEPRECATION")

package com.github.bstartweaks

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import com.github.bstartweaks.hook.*
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
//import com.bilibili.droid.x


class XposedInit : IXposedHookLoadPackage {
    companion object {

        private const val TAG = "bstarTweaks"
        lateinit var classLoader: ClassLoader
        lateinit var processName: String
        var versionCode: Int = 0
        var versionCodeStr: String = ""

        fun handleBstar() {
            log("hook bstar")

            XposedHelpers.findAndHookMethod(
                Application::class.java,
                "attach",
                Context::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(appParam: MethodHookParam) {
                        super.afterHookedMethod(appParam)

                        val currentContext = appParam.args[0] as Context
                        versionCode = getAppVersionCode(currentContext)
                        versionCodeStr = versionCode.toString()

                        val prefs = currentContext.getSharedPreferences(
                            "bstar_tweaks",
                            Context.MODE_PRIVATE
                        )

//                        try {
//                            x.b(currentContext, "TESTTESTTEST")
//                        } catch (e: Throwable) {
//                            log(e)
//                        }

                        startHook(SettingsHook(classLoader))
                        startHook(InfoHook(classLoader))
                        startHook(SimHook(classLoader))
                        startHook(LocaleHook(classLoader))
                        startHook(PrivacyHook(classLoader))

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

                        val privacyMode = prefs.getBoolean("privacy_mode", false)
                        if (privacyMode) {
                            startHook(PrivacyHook(classLoader))
                        }
                    }

                })
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

        fun startHook(hooker: BaseHook) {
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
        classLoader = lpparam.classLoader
        processName = lpparam.processName
        when (lpparam.packageName) {
            Constants.PACKAGE_NAME -> handleBstar()
        }
    }


}