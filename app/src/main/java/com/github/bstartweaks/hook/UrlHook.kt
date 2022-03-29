package com.github.bstartweaks.hook

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import com.github.bstartweaks.modulePrefs
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookBefore


object UrlHook : BaseHook() {
    private fun String.isBstarUrl(): Boolean {
        return this.startsWith("https://www.bilibili.tv/")
    }

    private fun String.hasExtraParam(): Boolean {
        return this.startsWith("from")
    }

    private fun clearExtraParams(url: String): String {
        val oldUri = Uri.parse(url)
        val newUri = oldUri.buildUpon().clearQuery()
        oldUri.queryParameterNames.forEach {
            if (it.hasExtraParam()) {
                return@forEach
            }
            newUri.appendQueryParameter(it, oldUri.getQueryParameter(it))
        }
        return newUri.build().toString()
    }

    override fun init() {
        if (!modulePrefs.getBoolean("clean_urls", true)) return
        findAllMethods(Intent::class.java) { name == "createChooser" }.hookBefore { param ->
            val intent = param.args[0] as Intent
            val extraText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return@hookBefore
            if (!extraText.isBstarUrl()) {
                return@hookBefore
            }
            intent.putExtra(Intent.EXTRA_TEXT, clearExtraParams(extraText))
        }
        findAllMethods(ClipData::class.java) { name == "newPlainText" }.hookBefore { param ->
            val text = (param.args[1] as CharSequence).toString()
            if (!text.isBstarUrl()) {
                return@hookBefore
            }
            param.args[1] = clearExtraParams(text)
        }
    }
}
