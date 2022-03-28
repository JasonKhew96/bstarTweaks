package com.github.bstartweaks.ui

import android.content.Context
import com.github.bstartweaks.BilibiliPackage.Companion.instance
import com.github.kyuubiran.ezxhelper.utils.argTypes
import com.github.kyuubiran.ezxhelper.utils.args
import com.github.kyuubiran.ezxhelper.utils.invokeMethodAuto
import com.github.kyuubiran.ezxhelper.utils.newInstance
import java.lang.reflect.Proxy

class Preference(context: Context) {
    private val preference =
        instance.preferenceClass?.newInstance(args(context), argTypes(Context::class.java))

    var title: CharSequence
        get() {
            return preference?.invokeMethodAuto("getTitle") as CharSequence
        }
        set(title) {
            preference?.invokeMethodAuto("setTitle", title)
        }

    var summary: CharSequence
        get() {
            return preference?.invokeMethodAuto("getSummary") as CharSequence
        }
        set(summary) {
            preference?.invokeMethodAuto("setSummary", summary)
        }

    var key: CharSequence
        get() {
            return preference?.invokeMethodAuto("getKey") as CharSequence
        }
        set(key) {
            preference?.invokeMethodAuto("setKey", key)
        }

    fun setOnPreferenceClickListener(onPreferenceClickListener: OnPreferenceClickListener) {
        val someInterface = instance.onPreferenceClickListenerClass
        val instance = Proxy.newProxyInstance(
            someInterface?.classLoader, arrayOf(someInterface)
        ) { _, _, _ -> onPreferenceClickListener.onPreferenceClick(this@Preference) }
        preference?.invokeMethodAuto("setOnPreferenceClickListener", instance)
    }

    fun build(): Any? {
        return preference
    }

    interface OnPreferenceClickListener {
        fun onPreferenceClick(preference: Preference): Boolean
    }
}
