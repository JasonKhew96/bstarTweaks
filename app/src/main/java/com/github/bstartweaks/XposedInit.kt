@file:Suppress("DEPRECATION")

package com.github.bstartweaks

import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.content.res.XModuleResources
import com.github.bstartweaks.hook.*
import com.github.bstartweaks.utils.Log
import com.github.bstartweaks.utils.hookBeforeMethod
import com.github.bstartweaks.utils.sPrefs
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage


class XposedInit : IXposedHookLoadPackage, IXposedHookZygoteInit {
    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        modulePath = startupParam.modulePath
        moduleRes = getModuleRes(modulePath)
    }

    companion object {
        lateinit var classLoader: ClassLoader
        lateinit var modulePath: String
        lateinit var moduleRes: Resources

        lateinit var processName: String
        private var versionCode: Int = 0
        private var versionCodeStr: String = ""

        fun getModuleRes(path: String): Resources {
            return XModuleResources.createInstance(path, null)
        }

        fun handleBstar(mClassLoader: ClassLoader) {
            Log.d("hook bstar")

            Instrumentation::class.java.hookBeforeMethod(
                "callApplicationOnCreate",
                Application::class.java
            ) { param ->
                BilibiliPackage(mClassLoader, param.args[0] as Context)

                val currentContext = param.args[0] as Context
                versionCode = getAppVersionCode(currentContext)
                versionCodeStr = versionCode.toString()

                startHook(SettingsHook(mClassLoader))
                startHook(InfoHook(mClassLoader))
                startHook(SimHook(mClassLoader))
                startHook(LocaleHook(mClassLoader))

                val forceAllowDownload = sPrefs.getBoolean("force_allow_download", false)
                if (forceAllowDownload) {
                    startHook(DownloadHook(mClassLoader))
                }

                val forceMobileNetwork = sPrefs.getBoolean("force_mobile_network", false)
                if (forceMobileNetwork) {
                    startHook(NetworkHook(mClassLoader))
                }

                val cleanShareUrl = sPrefs.getBoolean("clean_share_url", false)
                if (cleanShareUrl) {
                    startHook(ShareHook(mClassLoader))
                }
            }
        }

        fun getMajorVersionCode(): Int {
            return Integer.parseInt(versionCodeStr.substring(0, 4))
        }

        private fun getAppVersionCode(context: Context): Int {
            return try {
                val packageInfo: PackageInfo =
                    context.packageManager.getPackageInfo(Constant.PACKAGE_NAME, 0)
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
                Log.e(e)
            }
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != lpparam.processName) return
        classLoader = lpparam.classLoader
        processName = lpparam.processName
        when (lpparam.packageName) {
            Constant.PACKAGE_NAME -> handleBstar(lpparam.classLoader)
        }
    }


}