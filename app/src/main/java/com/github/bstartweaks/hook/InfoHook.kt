package com.github.bstartweaks.hook

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.github.bstartweaks.ClassMaps
import com.github.bstartweaks.XposedInit
import com.github.bstartweaks.ui.ForegroundRelativeLayout
import com.github.bstartweaks.utils.*
import java.text.DateFormat
import java.util.*

class InfoHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        Log.d("startHook: InfoHook")
        val copyMapData = findCopyMap() ?: throw NoClassDefFoundError("startHook: InfoHook failed")
        val toastMapData =
            findToastMap() ?: throw NoClassDefFoundError("startHook: InfoHook failed")
        val personInfoFragmentClazz =
            mClassLoader.loadClass("tv.danmaku.bili.ui.personinfo.PersonInfoFragment")

        personInfoFragmentClazz.declaredMethods.firstOrNull {
            it.name == "onCreateView" && it.parameterTypes.size == 3 &&
                    it.parameterTypes[0] == LayoutInflater::class.java &&
                    it.parameterTypes[1] == ViewGroup::class.java &&
                    it.parameterTypes[2] == Bundle::class.java
        }.also { m ->
            m?.hookAfterMethod { param ->
                val activity = param.thisObject.callMethod("getActivity")
                val context = activity?.callMethod("getApplicationContext") as Context
                val accountHelperClazz = mClassLoader.loadClass("com.bilibili.lib.account.e")

                // Lcom/bilibili/lib/account/e;
                val accountHelper = accountHelperClazz.callStaticMethod("a", context)

                // Lcom/bilibili/lib/passport/c;
                val cObj = accountHelper?.getObjectField("c")
                // Lcom/bilibili/lib/passport/f;
                val fObj = cObj?.getObjectField("a")
                // Lcom/bilibili/lib/passport/a;
                val aObj = fObj?.getObjectField("d")

                // Lcom/bilibili/lib/passport/a;
                val accessToken = aObj?.getObjectField("c") as String
                val refreshToken = aObj.getObjectField("d") as String
                val expires = aObj.getLongField("e")

                val view = param.result as View

                val resources = activity.callMethod("getResources")
                val uidLayoutId = resources?.callMethod(
                    "getIdentifier",
                    "uid_layout",
                    "id",
                    activity.callMethod("getPackageName")
                ) as Int
                val uidLayout = view.findViewById<View>(uidLayoutId)
                val linearLayout = uidLayout.parent as LinearLayout

                val rllp = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )

                val accessTokenLayout = ForegroundRelativeLayout(
                    activity as Context,
                    "Access Token",
                    accessToken
                )
                val clipboardHelperClazz =
                    mClassLoader.loadClass(copyMapData.first)
                val toastHelperClazz =
                    mClassLoader.loadClass(toastMapData.first)
                accessTokenLayout.setOnClickListener {
                    clipboardHelperClazz.callStaticMethod(
                        copyMapData.second,
                        context,
                        accessToken
                    )
                    toastHelperClazz.callStaticMethod(
                        toastMapData.second,
                        context,
                        "已复制 Access Token"
                    )
                }
                linearLayout.addView(accessTokenLayout.build(), rllp)

                val refreshTokenLayout = ForegroundRelativeLayout(
                    activity,
                    "Refresh Token",
                    refreshToken
                )
                refreshTokenLayout.setOnClickListener {
                    clipboardHelperClazz.callStaticMethod(
                        copyMapData.second,
                        context,
                        refreshToken
                    )
                    toastHelperClazz.callStaticMethod(
                        toastMapData.second,
                        context,
                        "已复制 Refresh Token"
                    )
                }
                linearLayout.addView(refreshTokenLayout.build(), rllp)

                val expiresStr = DateFormat.getDateTimeInstance()
                    .format(Date(expires * 1000))
                val expiresLayout =
                    ForegroundRelativeLayout(
                        activity,
                        "Expires",
                        expiresStr
                    )
                linearLayout.addView(expiresLayout.build(), rllp)
            }
        }
    }

    companion object {
        fun findCopyMap(): Pair<String, String>? {
            if (ClassMaps.copy.containsKey(XposedInit.getMajorVersionCode())) {
                return ClassMaps.copy[XposedInit.getMajorVersionCode()]
            }
            return ClassMaps.copy.maxByOrNull { p -> p.key }?.value
        }

        fun findToastMap(): Pair<String, String>? {
            if (ClassMaps.toast.containsKey(XposedInit.getMajorVersionCode())) {
                return ClassMaps.toast[XposedInit.getMajorVersionCode()]
            }
            return ClassMaps.toast.maxByOrNull { p -> p.key }?.value
        }
    }
}