package com.github.bstartweaks.hook

import com.github.bstartweaks.BilibiliPackage.Companion.instance
import com.github.bstartweaks.utils.*

class ShareHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        Log.d("startHook: ShareHook")

        val replacer: Replacer = { param ->
            param.args[1]
        }
        instance.shareHelperClass?.replaceMethod(
            instance.shareHelperMethod(),
            String::class.java,
            String::class.java,
            replacer = replacer
        )

    }
}