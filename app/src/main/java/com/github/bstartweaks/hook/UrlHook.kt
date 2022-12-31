package com.github.bstartweaks.hook

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import com.github.bstartweaks.modulePrefs
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


object UrlHook : BaseHook() {
    private fun String.isBstarShortUrl() = startsWith("https://bili.im/")

    private fun String.isBstarUrl() = startsWith("https://www.bilibili.tv/")

    private fun String.hasExtraParam() = startsWith("from")

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

    private fun getOriginalLocation(url: String): String {
        try {
            val httpURLConnection = URL(url).openConnection() as HttpURLConnection
            httpURLConnection.apply {
                connectTimeout = 5000
                readTimeout = 5000
                instanceFollowRedirects = false
                connect()
                return getHeaderField("Location") ?: url
            }
        } catch (e: IOException) {
            return url
        }
    }

    override fun init() {
        if (!modulePrefs.getBoolean("clean_urls", true)) return
        findAllMethods(Intent::class.java) { name == "createChooser" }.hookBefore { param ->
            val intent = param.args[0] as Intent
            var extraText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return@hookBefore
            if (extraText.isBstarShortUrl()) {
                extraText = getOriginalLocation(extraText)
            }
            if (extraText.isBstarUrl()) {
                intent.putExtra(Intent.EXTRA_TEXT, clearExtraParams(extraText))
            }
        }
        findAllMethods(ClipData::class.java) { name == "newPlainText" }.hookBefore { param ->
            var text = (param.args[1] as CharSequence).toString()
            if (text.isBstarShortUrl()) {
                text = getOriginalLocation(text)
            }
            if (text.isBstarUrl()) {
                param.args[1] = clearExtraParams(text)
            }
        }
    }
}
