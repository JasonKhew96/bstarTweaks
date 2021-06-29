package com.github.bstartweaks.hook

import android.content.Context
import com.github.bstartweaks.ClassMaps
import com.github.bstartweaks.XposedInit
import com.github.kyuubiran.ezxhelper.utils.findMethodByCondition
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import java.util.*

class LocaleHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        XposedInit.log("startHook: LocaleHook")
        val mapData = findMap() ?: throw NoClassDefFoundError("startHook: LocaleHook failed")
        val localeClazz =
            mClassLoader.loadClass(mapData.first)

        findMethodByCondition(localeClazz) {
            it.name == mapData.second /*&& it.typeParameters.size == 1 &&
                    it.typeParameters[0] == Context::class.java*/
        }.also { m ->
            m.hookAfter { param ->
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