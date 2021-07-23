// Taken and modified from:
// https://github.com/yujincheng08/BiliRoaming/
package com.github.bstartweaks

import android.app.AndroidAppHelper
import android.content.Context
import android.util.Base64
import com.github.bstartweaks.utils.*
import java.io.*
import kotlin.math.max


class BilibiliPackage constructor(private val mClassLoader: ClassLoader, mContext: Context) {
    private val mHookInfo: MutableMap<String, String?> = readHookInfo(mContext)

    private val classesList by lazy { mClassLoader.allClassesList() }

    private fun findShareHelperClass(): String? {
        // b.vd0;->a 1100101 1.10.0
        return classesList.firstOrNull {
            it.startsWith("b.") &&
                    it.findClass(mClassLoader).declaredConstructors.isEmpty() &&
                    it.findClass(mClassLoader).declaredFields.size == 1 &&
                    it.findClass(mClassLoader).declaredFields[0].type == Map::class.java &&
                    it.findClass(mClassLoader).declaredMethods.size == 5
        }
    }

    private fun findShareHelperMethod() = shareHelperClass?.declaredMethods?.firstOrNull {
        it.parameterTypes.size == 2 &&
                it.parameterTypes[0] == String::class.java &&
                it.parameterTypes[1] == String::class.java
    }?.name

    val shareHelperClass by Weak { mHookInfo["class_share_helper"]?.findClassOrNull(mClassLoader) }
    fun shareHelperMethod() = mHookInfo["method_share_helper"]

    val ids by lazy {
        ObjectInputStream(
            ByteArrayInputStream(
                Base64.decode(
                    mHookInfo["map_ids"],
                    Base64.DEFAULT
                )
            )
        ).readObject() as Map<String, Int>
    }

    private fun readHookInfo(context: Context): MutableMap<String, String?> {
        try {
            val hookInfoFile = File(context.cacheDir, Constant.HOOK_INFO_FILE_NAME)
            Log.d("Reading hook info: $hookInfoFile")
            val startTime = System.currentTimeMillis()
            if (hookInfoFile.isFile && hookInfoFile.canRead()) {
                val lastUpdateTime = context.packageManager.getPackageInfo(
                    AndroidAppHelper.currentPackageName(),
                    0
                ).lastUpdateTime
                val lastModuleUpdateTime = try {
                    context.packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0)
                } catch (e: Throwable) {
                    null
                }?.lastUpdateTime ?: 0
                val stream = ObjectInputStream(FileInputStream(hookInfoFile))
                val lastHookInfoUpdateTime = stream.readLong()
                @Suppress("UNCHECKED_CAST")
                if (lastHookInfoUpdateTime >= lastUpdateTime && lastHookInfoUpdateTime >= lastModuleUpdateTime)
                    return stream.readObject() as MutableMap<String, String?>
            }
            val endTime = System.currentTimeMillis()
            Log.d("Read hook info completed: take ${endTime - startTime} ms")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return HashMap()
    }

    /**
     * @return Whether to update the serialization file.
     */
    private fun checkHookInfo(): Boolean {
        var needUpdate = false

        fun <K, V> MutableMap<K, V>.checkOrPut(
            key: K,
            checkOption: String? = null,
            defaultValue: () -> V
        ): MutableMap<K, V> {
            if (checkOption != null) {
                if (!sPrefs.getBoolean(checkOption, false)) return this
            }
            if (!containsKey(key)) {
                put(key, defaultValue())
                needUpdate = true
            }
            return this
        }

        fun <K, V> MutableMap<K, V>.checkOrPut(
            keys: Array<out K>,
            checkOption: String? = null,
            checker: (map: MutableMap<K, V>, keys: Array<out K>) -> Boolean,
            defaultValue: () -> Array<V>
        ): MutableMap<K, V> {
            if (checkOption != null) {
                if (!sPrefs.getBoolean(checkOption, false)) return this
            }
            if (!checker(this, keys)) {
                putAll(keys.zip(defaultValue()))
                needUpdate = true
            }
            return this
        }

        fun <K, V> MutableMap<K, V>.checkConjunctiveOrPut(
            vararg keys: K,
            defaultValue: () -> Array<V>
        ) =
            checkOrPut(
                keys,
                null,
                { m, ks -> ks.fold(true) { acc, k -> acc && m.containsKey(k) } },
                defaultValue
            )

        @Suppress("unused")
        fun <K, V> MutableMap<K, V>.checkDisjunctiveOrPut(
            vararg keys: K,
            defaultValue: () -> Array<V>
        ) =
            checkOrPut(
                keys,
                null,
                { m, ks -> ks.fold(false) { acc, k -> acc || m.containsKey(k) } },
                defaultValue
            )

        mHookInfo.checkOrPut("class_share_helper") {
            findShareHelperClass()
        }.checkOrPut("method_share_helper") {
            findShareHelperMethod()
        }

        Log.d(mHookInfo.filterKeys { it != "map_ids" })
        Log.d("Check hook info completed: needUpdate = $needUpdate")
        return needUpdate
    }

    private fun writeHookInfo(context: Context) {
        try {
            val hookInfoFile = File(context.cacheDir, Constant.HOOK_INFO_FILE_NAME)
            val lastUpdateTime = context.packageManager.getPackageInfo(
                AndroidAppHelper.currentPackageName(),
                0
            ).lastUpdateTime
            val lastModuleUpdateTime = try {
                context.packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0)
            } catch (e: Throwable) {
                null
            }?.lastUpdateTime ?: 0
            if (hookInfoFile.exists()) hookInfoFile.delete()
            ObjectOutputStream(FileOutputStream(hookInfoFile)).use { stream ->
                stream.writeLong(max(lastModuleUpdateTime, lastUpdateTime))
                stream.writeObject(mHookInfo)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.d("Write hook info completed")
    }

    init {
        try {
            if (checkHookInfo()) {
                writeHookInfo(mContext)
            }
        } catch (e: Throwable) {
            Log.e(e)
        }
        instance = this
    }

    companion object {
        @Volatile
        lateinit var instance: BilibiliPackage
    }
}
