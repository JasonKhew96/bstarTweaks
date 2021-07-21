package com.github.bstartweaks.hook

import com.github.bstartweaks.utils.Log
import com.github.bstartweaks.utils.hookAfterMethod
import java.util.*

class LocaleHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        Log.d("startHook: LocaleHook")
        val localeClazz =
            mClassLoader.loadClass("com.bilibili.lib.ui.util.f")

        localeClazz.declaredMethods.firstOrNull {
            it.name == "b" /*&& it.typeParameters.size == 1 &&
                    it.typeParameters[0] == Context::class.java*/
        }.also { m ->
            m?.hookAfterMethod { param ->
                val locale = param.result as Locale
                if (locale.country == "CN" || locale.country == "TW") {
//                    val newLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        Locale.Builder().setLanguage("zh")
//                            .setRegion("SG").build()
//                    } else {
//                        Locale("zh", "SG")
//                    }
                    val newLocale = Locale("zh", "SG")
                    Locale.setDefault(newLocale)
                    param.result = newLocale
                }
            }
        }

    }
}