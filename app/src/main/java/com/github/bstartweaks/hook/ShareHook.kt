package com.github.bstartweaks.hook

import com.github.bstartweaks.ClassMaps
import com.github.bstartweaks.XposedInit
import com.github.kyuubiran.ezxhelper.utils.findMethodByCondition
import com.github.kyuubiran.ezxhelper.utils.hookReplace

class ShareHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        XposedInit.log("startHook: ShareHook")
        val mapData = findMap() ?: throw NoClassDefFoundError("startHook: ShareHook failed")
        val refClazz = mClassLoader.loadClass(mapData.first)

        findMethodByCondition(refClazz) {
            it.name == mapData.second && it.parameterTypes.size == 2 &&
                    it.parameterTypes[0] == String::class.java &&
                    it.parameterTypes[1] == String::class.java
        }.also { m ->
            m.hookReplace { param ->
                param.args[1]
            }
        }
    }

    companion object {
        fun findMap(): Pair<String, String>? {
            if (ClassMaps.share.containsKey(XposedInit.getMajorVersionCode())) {
                return ClassMaps.share[XposedInit.getMajorVersionCode()]
            }
            return ClassMaps.share.maxByOrNull { p -> p.key }?.value
        }
    }
}