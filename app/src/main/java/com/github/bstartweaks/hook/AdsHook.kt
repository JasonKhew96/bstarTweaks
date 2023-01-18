package com.github.bstartweaks.hook

import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder

object AdsHook : BaseHook() {
    override fun init() {
        val tpBaseAdapterClass = loadClass("com.tradplus.ads.base.adapter.TPBaseAdapter")

        MethodFinder.fromClass(tpBaseAdapterClass).filterByName("loadAd").first().createHook {
            replace {
                returnConstant(null)
            }
        }
    }
}