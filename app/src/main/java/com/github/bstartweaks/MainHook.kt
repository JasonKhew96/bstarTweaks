package com.github.bstartweaks

import android.app.Application
import android.content.Context
import com.github.bstartweaks.hook.BaseHook
import com.github.bstartweaks.hook.JsonHook
import com.github.bstartweaks.hook.ParamHook
import com.github.bstartweaks.hook.SettingsHook
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.init.InitFields.appContext
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.Log.logexIfThrow
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage

private const val PACKAGE_NAME_HOOKED = "com.bstar.intl"
private const val TAG = "bstarTweaks"

class MainHook : IXposedHookLoadPackage, IXposedHookZygoteInit {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == PACKAGE_NAME_HOOKED) {
            // Init EzXHelper
            EzXHelperInit.initHandleLoadPackage(lpparam)

            findMethod(Application::class.java) {
                name == "attach" && parameterTypes.contentEquals(arrayOf(Context::class.java))
            }.hookAfter { param ->
                EzXHelperInit.initAppContext(param.args[0] as Context)
                EzXHelperInit.setEzClassLoader(appContext.classLoader)
                // Init hooks
                BilibiliPackage()
                initHooks(JsonHook, ParamHook, SettingsHook)
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
                it.init()
                it.isInit = true
                Log.i("Inited hook: ${it.javaClass.simpleName}")
            }.logexIfThrow("Failed init hook: ${it.javaClass.simpleName}")
        }
    }
}
