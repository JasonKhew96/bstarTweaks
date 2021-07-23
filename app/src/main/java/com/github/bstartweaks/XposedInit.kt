@file:Suppress("DEPRECATION")

package com.github.bstartweaks

import android.app.Application
import android.app.Instrumentation
import android.content.Context
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