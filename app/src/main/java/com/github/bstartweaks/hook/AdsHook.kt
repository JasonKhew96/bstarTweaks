package com.github.bstartweaks.hook

import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookReplace
import com.github.kyuubiran.ezxhelper.utils.loadClassOrNull

object AdsHook : BaseHook() {
    override fun init() {
        val tpBaseAdapterClass = loadClassOrNull("com.tradplus.ads.base.adapter.TPBaseAdapter")
            ?: throw ClassNotFoundException()

        findMethod(tpBaseAdapterClass) {
            name == "loadAd"
        }.hookReplace {
            return@hookReplace null
        }
    }
}