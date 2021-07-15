package com.github.bstartweaks.hook

import com.github.bstartweaks.ClassMaps
import com.github.bstartweaks.XposedInit
import com.github.bstartweaks.utils.Log
import com.github.bstartweaks.utils.hookAfterMethod
import java.util.*

class LocaleHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        Log.d("startHook: LocaleHook")
        val mapData = findMap() ?: throw NoClassDefFoundError("startHook: LocaleHook failed")
        val localeClazz =
            mClassLoader.loadClass(mapData.first)

        localeClazz.declaredMethods.firstOrNull {
            it.name == mapData.second /*&& it.typeParameters.size == 1 &&
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

    companion object {
        fun findMap(): Pair<String, String>? {
            if (ClassMaps.locale.containsKey(XposedInit.getMajorVersionCode())) {
                return ClassMaps.locale[XposedInit.getMajorVersionCode()]
            }
            return ClassMaps.locale.maxByOrNull { p -> p.key }?.value
        }
    }
}