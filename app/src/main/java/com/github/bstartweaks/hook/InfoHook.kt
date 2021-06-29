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
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import java.text.DateFormat
import java.util.*

class InfoHook(mClassLoader: ClassLoader) : BaseHook(mClassLoader) {
    override fun startHook() {
        XposedInit.log("startHook: InfoHook")
        val copyMapData = findCopyMap() ?: throw NoClassDefFoundError("startHook: InfoHook failed")
        val toastMapData = findToastMap() ?: throw NoClassDefFoundError("startHook: InfoHook failed")
        val personInfoFragmentClazz =
            mClassLoader.loadClass("tv.danmaku.bili.ui.personinfo.PersonInfoFragment")
        XposedHelpers.findAndHookMethod(
            personInfoFragmentClazz,
            "onCreateView",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Bundle::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    super.afterHookedMethod(param)
                    val activity = XposedHelpers.callMethod(
                        param.thisObject,
                        "getActivity"
                    )
                    val context = XposedHelpers.callMethod(
                        activity,
                        "getApplicationContext"
                    ) as Context
                    val accountHelperClazz =
                        mClassLoader.loadClass("com.bilibili.lib.account.e")

                    // Lcom/bilibili/lib/account/e;
                    val accountHelper = XposedHelpers.callStaticMethod(
                        accountHelperClazz,
                        "a",
                        context
                    )
//                                        val accessKey =
//                                            XposedHelpers.callMethod(accountHelper, "f") as String

                    // Lcom/bilibili/lib/passport/c;
                    val cObj = XposedHelpers.getObjectField(accountHelper, "c")
                    // Lcom/bilibili/lib/passport/f;
                    val fObj = XposedHelpers.getObjectField(cObj, "a")
                    // Lcom/bilibili/lib/passport/a;
                    val aObj = XposedHelpers.callMethod(fObj, "d")

                    // Lcom/bilibili/lib/passport/a;
//                                        val expiresIn =
//                                            XposedHelpers.getObjectField(aObj, "a") as Long
//                                        val mid = XposedHelpers.getObjectField(aObj, "b") as Long
                    val accessToken =
                        XposedHelpers.getObjectField(aObj, "c") as String
                    val refreshToken =
                        XposedHelpers.getObjectField(aObj, "d") as String
                    val expires =
                        XposedHelpers.getObjectField(aObj, "e") as Long

//                                        log("\nexpiresIn: $expiresIn\nmid: $mid\naccessToken: $accessToken\nrefreshToken: $refreshToken\nexpires: $expires")

                    val view = param.result as View

                    val resources =
                        XposedHelpers.callMethod(activity, "getResources")
                    val uidLayoutId = XposedHelpers.callMethod(
                        resources, "getIdentifier",
                        "uid_layout",
                        "id",
                        XposedHelpers.callMethod(activity, "getPackageName")
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
                    accessTokenLayout.setOnClickListener {
                        val clipboardHelperClazz =
                            mClassLoader.loadClass(copyMapData.first)
                        XposedHelpers.callStaticMethod(
                            clipboardHelperClazz,
                            copyMapData.second,
                            context,
                            accessToken
                        )
                        val toastHelperClazz =
                            mClassLoader.loadClass(toastMapData.first)
                        XposedHelpers.callStaticMethod(
                            toastHelperClazz,
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
                        val clipboardHelperClazz =
                            mClassLoader.loadClass(copyMapData.first)
                        XposedHelpers.callStaticMethod(
                            clipboardHelperClazz,
                            copyMapData.second,
                            context,
                            refreshToken
                        )
                        val toastHelperClazz =
                            mClassLoader.loadClass(toastMapData.first)
                        XposedHelpers.callStaticMethod(
                            toastHelperClazz,
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
            })
    }

    companion object {
        fun findCopyMap(): Pair<String, String>? {
            if (ClassMaps.copy.containsKey(XposedInit.getMajorVersionCode())) {
                return ClassMaps.copy[XposedInit.getMajorVersionCode()]
            }
            return ClassMaps.copy.maxByOrNull { p-> p.key }?.value
        }
        fun findToastMap(): Pair<String, String>? {
            if (ClassMaps.toast.containsKey(XposedInit.getMajorVersionCode())) {
                return ClassMaps.toast[XposedInit.getMajorVersionCode()]
            }
            return ClassMaps.toast.maxByOrNull { p-> p.key }?.value
        }
    }
}