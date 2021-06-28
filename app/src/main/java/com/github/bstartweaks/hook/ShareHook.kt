package com.github.bstartweaks.hook

import com.github.bstartweaks.ClassMaps
import com.github.bstartweaks.XposedInit
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers

class ShareHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        XposedInit.log("startHook: ShareHook")
        val mapData = findMap() ?: throw NoClassDefFoundError("startHook: ShareHook failed")
        val refClazz = mClassLoader.loadClass(mapData.first)
        XposedHelpers.findAndHookMethod(refClazz, mapData.second, String::class.java, String::class.java, object : XC_MethodReplacement() {
            override fun replaceHookedMethod(param: MethodHookParam): Any {
                return param.args[1]
            }
        })
    }

    companion object {
        fun findMap(): Pair<String, String>? {
            if (ClassMaps.share.containsKey(XposedInit.versionCodeStr)) {
                return ClassMaps.share[XposedInit.versionCodeStr]
            }
            return ClassMaps.share["fall"]
        }
    }
}