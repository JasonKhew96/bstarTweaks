package com.github.bstartweaks.hook

import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookReplace
import com.github.kyuubiran.ezxhelper.utils.loadClass

object AdsHook : BaseHook() {
    override fun init() {
        val tpBaseAdapterClass = loadClass("com.tradplus.ads.base.adapter.TPBaseAdapter")

        findMethod(tpBaseAdapterClass) {
            name == "loadAd"
        }.hookReplace {
            return@hookReplace null
        }
    }
}