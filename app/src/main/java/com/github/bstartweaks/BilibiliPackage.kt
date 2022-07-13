package com.github.bstartweaks

import com.github.kyuubiran.ezxhelper.init.InitFields
import com.github.kyuubiran.ezxhelper.utils.loadClassOrNull
import me.iacn.biliroaming.utils.DexHelper

class BilibiliPackage {
    init {
        dexHelper = DexHelper(InitFields.ezXClassLoader)
        instance = this
    }

    companion object {
        @Volatile
        lateinit var instance: BilibiliPackage
        lateinit var dexHelper: DexHelper
    }

    // json hook
    val fastJsonFeatureClass by lazy { loadClassOrNull("com.alibaba.fastjson.parser.Feature[]") }
    val generalResponseClass by lazy { loadClassOrNull("com.bilibili.okretro.GeneralResponse") }
    val bangumiApiResponseClass by lazy { loadClassOrNull("com.bilibili.bangumi.data.common.api.BangumiApiResponse") }
    val bangumiUniformSeason by lazy { loadClassOrNull("com.bilibili.bangumi.data.page.detail.entity.BangumiUniformSeason") }
    val fastJsonClass by lazy { loadClassOrNull("com.alibaba.fastjson.JSON") }

    // info hook
    val accountHelperClass by lazy { loadClassOrNull("com.bilibili.lib.account.e") }

    val helpFragmentClass by lazy { loadClassOrNull("com.bilibili.app.preferences.fragment.HelpFragment") }
    val preferenceClass by lazy { loadClassOrNull("androidx.preference.Preference") }
    val onPreferenceClickListenerClass by lazy { loadClassOrNull("androidx.preference.Preference\$OnPreferenceClickListener") }

}
