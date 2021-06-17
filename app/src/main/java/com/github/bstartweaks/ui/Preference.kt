package com.github.bstartweaks.ui

import android.content.Context
import com.github.bstartweaks.XposedInit
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Proxy

class Preference(context: Context) {
    companion object {
        val preferenceClazz = XposedInit.classLoader.loadClass("androidx.preference.Preference")!!
    }

    private val preference = XposedHelpers.newInstance(preferenceClazz, context)

    var title: CharSequence
        get() {
            return XposedHelpers.callMethod(preference, "getTitle") as CharSequence
        }
        set(title) {
            XposedHelpers.callMethod(preference, "setTitle", title)
        }

    var summary: CharSequence
        get() {
            return XposedHelpers.callMethod(preference, "getSummary") as CharSequence
        }
        set(summary) {
            XposedHelpers.callMethod(preference, "setSummary", summary)
        }

    var key: CharSequence
        get() {
            return XposedHelpers.callMethod(preference, "getKey") as CharSequence
        }
        set(key) {
            XposedHelpers.callMethod(preference, "setKey", key)
        }

    fun setOnPreferenceClickListener(onPreferenceClickListener: OnPreferenceClickListener) {
        val someInterface = XposedInit.classLoader.loadClass("androidx.preference.Preference\$OnPreferenceClickListener")
        val instance = Proxy.newProxyInstance(
            someInterface.classLoader,
            arrayOf(someInterface)
        ) { _, _, _ -> onPreferenceClickListener.onPreferenceClick(this@Preference) }
        XposedHelpers.callMethod(preference, "setOnPreferenceClickListener", instance)
    }

    fun build(): Any {
        return preference
    }

    interface OnPreferenceClickListener {
        fun onPreferenceClick(preference: Preference): Boolean
    }
}