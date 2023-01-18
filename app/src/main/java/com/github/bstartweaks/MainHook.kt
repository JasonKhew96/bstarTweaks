package com.github.bstartweaks

import android.app.Application
import android.content.Context
import com.github.bstartweaks.hook.AdsHook
import com.github.bstartweaks.hook.BaseHook
import com.github.bstartweaks.hook.DebugHook
import com.github.bstartweaks.hook.JsonHook
import com.github.bstartweaks.hook.ParamHook
import com.github.bstartweaks.hook.SettingsHook
import com.github.bstartweaks.hook.UrlHook
import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.EzXHelper.appContext
import com.github.kyuubiran.ezxhelper.EzXHelper.modulePath
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.Log.logexIfThrow
import com.github.kyuubiran.ezxhelper.finders.MethodFinder
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.luckypray.dexkit.DexKitBridge
import java.io.File

private const val PACKAGE_NAME_HOOKED = "com.bstar.intl"
private const val TAG = "bstarTweaks"

class MainHook : IXposedHookLoadPackage, IXposedHookZygoteInit {

    companion object {
        lateinit var dexKit: DexKitBridge
        var isDexKitNeeded = false

        fun initDexKit(path: String) {
            if (::dexKit.isInitialized) return

            val moduleLastModify = File(modulePath).lastModified()
            @Suppress("DEPRECATION") val hostLastModify =
                appContext.packageManager.getPackageInfo(appContext.packageName, 0).lastUpdateTime
            val hookLastModify = modulePrefs.getLong("hook_last_modify", 0)
            if (hookLastModify < hostLastModify || hookLastModify < moduleLastModify) {
                isDexKitNeeded = true
            } else {
                return
            }

            try {
                System.loadLibrary("dexkit")
            } catch (e: Throwable) {
                Log.e("load dexkit library failed", e)
            }
            dexKit = DexKitBridge.create(path) ?: return
        }

        fun closeDexKit() {
            if (::dexKit.isInitialized) {
                dexKit.close()
                modulePrefs.edit().putLong("hook_last_modify", System.currentTimeMillis()).apply()
            }
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == PACKAGE_NAME_HOOKED) {
            // Init EzXHelper
            EzXHelper.initHandleLoadPackage(lpparam)
            EzXHelper.setLogTag(TAG)
            EzXHelper.setToastTag(TAG)

            MethodFinder.fromClass(Application::class.java).filterByName("attach")
                .filterByParamTypes(Context::class.java).first().createHook {
                    before { param ->
                        val context = param.args[0] as Context
                        EzXHelper.initAppContext(context)

                        // Init hooks
                        initHooks(JsonHook, ParamHook, SettingsHook, UrlHook, DebugHook, AdsHook)
                    }
                }
        }
    }

    // Optional
    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelper.initZygote(startupParam)
    }

    private fun initHooks(vararg hook: BaseHook) {
        initDexKit(appContext.applicationInfo.sourceDir)

        hook.forEach {
            runCatching {
                if (it.isInit) return@forEach
                val startMs = System.currentTimeMillis()
                it.init()
                it.isInit = true
                Log.i("Inited hook in ${System.currentTimeMillis() - startMs}ms: ${it.javaClass.simpleName}")
            }.logexIfThrow("Failed init hook: ${it.javaClass.simpleName}")
        }

        closeDexKit()
    }
}
