package com.github.bstartweaks.hook

import android.content.Context
import android.os.Build
import com.github.bstartweaks.ClassMaps
import com.github.bstartweaks.XposedInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import java.util.*

class LocaleHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        XposedInit.log("startHook: LocaleHook")
        val mapData = findMap() ?: throw NoClassDefFoundError("startHook: LocaleHook failed")
        val localeClazz =
            mClassLoader.loadClass(mapData.first)
        XposedHelpers.findAndHookMethod(
            localeClazz,
            mapData.second,
            Context::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    super.afterHookedMethod(param)
                    val locale = param.result as Locale
                    if (locale.country == "CN" || locale.country == "TW") {
                        val newLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Locale.Builder().setLanguage("zh")
//                                .setScript("Hans")
                                .setRegion("SG").build()
                        } else {
                            Locale("zh", "SG")
                        }
                        Locale.setDefault(newLocale)
                        param.result = newLocale
                    }
                }
            }
        )
    }
    companion object {
        fun findMap(): Pair<String, String>? {
            if (ClassMaps.locale.containsKey(XposedInit.versionCodeStr)) {
                return ClassMaps.locale[XposedInit.versionCodeStr]
            }
            return ClassMaps.locale["fall"]
        }
    }
}