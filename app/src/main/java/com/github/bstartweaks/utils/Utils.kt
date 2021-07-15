// Taken and modified from:
// https://github.com/yujincheng08/BiliRoaming/
package com.github.bstartweaks.utils

import android.app.AndroidAppHelper
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.github.bstartweaks.BilibiliPackage.Companion.instance
import com.github.bstartweaks.XposedInit
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.URL
import kotlin.reflect.KProperty

class Weak(val initializer: () -> Class<*>?) {
    private var weakReference: WeakReference<Class<*>?>? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = weakReference?.get() ?: let {
        weakReference = WeakReference(initializer())
        weakReference
    }?.get()

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Class<*>) {
        weakReference = WeakReference(value)
    }
}

val systemContext: Context
    get() {
        val activityThread = "android.app.ActivityThread".findClassOrNull(null)
            ?.callStaticMethod("currentActivityThread")!!
        return activityThread.callMethodAs("getSystemContext")
    }

fun getPackageVersion(packageName: String) = try {
    systemContext.packageManager.getPackageInfo(packageName, 0).run {
        @Suppress("DEPRECATION")
        String.format("${packageName}@%s(%s)", versionName, getVersionCode(packageName))
    }
} catch (e: Throwable) {
    Log.e(e)
    "(unknown)"
}


fun getVersionCode(packageName: String) = try {
    @Suppress("DEPRECATION")
    systemContext.packageManager.getPackageInfo(packageName, 0).versionCode
} catch (e: Throwable) {
    Log.e(e)
    null
} ?: 6080000

val currentContext by lazy { AndroidAppHelper.currentApplication() as Context }

val packageName: String by lazy { currentContext.packageName }

val isBuiltIn
    get() = XposedInit.modulePath.endsWith("so")

val is64
    get() = currentContext.applicationInfo.nativeLibraryDir.contains("64")

val logFile by lazy { File(currentContext.externalCacheDir, "log.txt") }

@Suppress("DEPRECATION")
val sPrefs
    get() = currentContext.getSharedPreferences("bstar_tweaks", Context.MODE_MULTI_PROCESS)!!

@Suppress("DEPRECATION")
val sCaches
    get() = currentContext.getSharedPreferences("bstar_tweaks_cache", Context.MODE_MULTI_PROCESS)!!

fun getId(name: String) = instance.ids[name]
    ?: currentContext.resources.getIdentifier(name, "id", currentContext.packageName)

fun getBitmapFromURL(src: String?, callback: (Bitmap?) -> Unit) {
    Thread {
        callback(try {
            src?.let {
                val bytes = URL(it).readBytes()
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        } catch (e: IOException) {
            Log.e(e)
            null
        })
    }.start()
}

fun String?.toJSONObject() = JSONObject(this.orEmpty())

@Suppress("UNCHECKED_CAST")
fun <T> JSONArray.asSequence() = (0 until length()).asSequence().map { get(it) as T }

operator fun JSONArray.iterator(): Iterator<JSONObject> =
    (0 until length()).asSequence().map { get(it) as JSONObject }.iterator()

fun JSONArray?.orEmpty() = this ?: JSONArray()

fun getStreamContent(input: InputStream) = try {
    input.bufferedReader().use {
        it.readText()
    }
} catch (e: Throwable) {
    Log.e(e)
    null
}
