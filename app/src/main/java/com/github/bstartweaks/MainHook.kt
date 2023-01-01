package com.github.bstartweaks

import android.app.Application
import android.content.Context
import com.github.bstartweaks.hook.BaseHook
import com.github.bstartweaks.hook.DebugHook
import com.github.bstartweaks.hook.JsonHook
import com.github.bstartweaks.hook.ParamHook
import com.github.bstartweaks.hook.SettingsHook
import com.github.bstartweaks.hook.UrlHook
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.init.InitFields.appContext
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.Log.logexIfThrow
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.luckypray.dexkit.DexKitBridge

private const val PACKAGE_NAME_HOOKED = "com.bstar.intl"
private const val TAG = "bstarTweaks"

class MainHook : IXposedHookLoadPackage, IXposedHookZygoteInit {

    companion object {
        lateinit var dexKit: DexKitBridge

        fun initDexKit(path: String) {
            if (::dexKit.isInitialized) return
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
            }
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == PACKAGE_NAME_HOOKED) {
            // Init EzXHelper
            EzXHelperInit.initHandleLoadPackage(lpparam)

            findMethod(Application::class.java) {
                name == "attach" && parameterTypes.contentEquals(arrayOf(Context::class.java))
            }.hookAfter { param ->
                val context = param.args[0] as Context
                EzXHelperInit.initAppContext(context)
                EzXHelperInit.setEzClassLoader(appContext.classLoader)

                initDexKit(context.applicationInfo.sourceDir)
                // Init hooks
                initHooks(JsonHook, ParamHook, SettingsHook, UrlHook, DebugHook)
                closeDexKit()
            }
        }
    }

    // Optional
    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)
        EzXHelperInit.setLogTag(TAG)
        EzXHelperInit.setToastTag(TAG)
    }

    private fun initHooks(vararg hook: BaseHook) {
        hook.forEach {
            runCatching {
                if (it.isInit) return@forEach
                val startMs = System.currentTimeMillis()
                it.init()
                it.isInit = true
                Log.i("Inited hook in ${System.currentTimeMillis() - startMs}ms: ${it.javaClass.simpleName}")
            }.logexIfThrow("Failed init hook: ${it.javaClass.simpleName}")
        }
    }
}
