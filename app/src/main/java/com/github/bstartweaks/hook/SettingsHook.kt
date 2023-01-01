package com.github.bstartweaks.hook

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.bstartweaks.BuildConfig
import com.github.bstartweaks.MainHook
import com.github.bstartweaks.ui.Preference
import com.github.bstartweaks.ui.SettingsDialog
import com.github.kyuubiran.ezxhelper.init.InitFields
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.invokeMethod
import com.github.kyuubiran.ezxhelper.utils.invokeMethodAs
import com.github.kyuubiran.ezxhelper.utils.invokeMethodAuto
import com.github.kyuubiran.ezxhelper.utils.isStatic
import com.github.kyuubiran.ezxhelper.utils.loadClassOrNull
import java.lang.reflect.Field
import java.lang.reflect.Method

object SettingsHook : BaseHook() {

    override fun init() {
        val helpFragmentClass =
            loadClassOrNull("com.bilibili.app.preferences.fragment.HelpFragment")
                ?: throw ClassNotFoundException()


        var isTokenClassLoaded = false

        var biliAccountClassMethod: Method? = null

        var biliPassportClassField: Field? = null
        var passportControllerClassField: Field? = null
        var accessTokenClassField: Field? = null

        var accessTokenField: Field? = null
        var refreshTokenField: Field? = null
        var expiresField: Field? = null

        try {
            val biliAccountClass = MainHook.dexKit.findMethodUsingString(
                usingString = "^BiliAccount$",
                methodReturnType = Void.TYPE.name,
                methodParamTypes = emptyArray(),
            ).firstNotNullOfOrNull { loadClassOrNull(it.declaringClassName) }
                ?: throw ClassNotFoundException()
            val biliPassportClass = MainHook.dexKit.findMethodUsingString(
                usingString = "^BiliPassport$",
                methodParamTypes = emptyArray(),
            ).firstNotNullOfOrNull { loadClassOrNull(it.declaringClassName) }
                ?: throw ClassNotFoundException()
            val passportControllerClass = MainHook.dexKit.findMethodUsingString(
                usingString = "^PassportController$",
            ).firstNotNullOfOrNull { loadClassOrNull(it.declaringClassName) }
                ?: throw ClassNotFoundException()
            val accessTokenClass = MainHook.dexKit.findMethodUsingString(
                usingString = "^AccessToken{mExpiresIn=$",
                methodName = "toString",
                methodReturnType = String::class.java.name,
            ).firstNotNullOfOrNull { loadClassOrNull(it.declaringClassName) }
                ?: throw ClassNotFoundException()

            val jsonFieldClass = loadClassOrNull("com.alibaba.fastjson.annotation.JSONField")
                ?: throw ClassNotFoundException()
            accessTokenField = MainHook.dexKit.findFieldUsingAnnotation(
                annotationClass = jsonFieldClass.name,
                annotationUsingString = "access_token",
                fieldDeclareClass = accessTokenClass.name,
                fieldType = String::class.java.name,
            ).firstNotNullOfOrNull { it.getFieldInstance(InitFields.ezXClassLoader) as Field }
                ?: throw NoSuchFieldError()
            refreshTokenField = MainHook.dexKit.findFieldUsingAnnotation(
                annotationClass = jsonFieldClass.name,
                annotationUsingString = "refresh_token",
                fieldDeclareClass = accessTokenClass.name,
                fieldType = String::class.java.name,
            ).firstNotNullOfOrNull { it.getFieldInstance(InitFields.ezXClassLoader) as Field }
                ?: throw NoSuchFieldError()
            expiresField = MainHook.dexKit.findFieldUsingAnnotation(
                annotationClass = jsonFieldClass.name,
                annotationUsingString = "expires",
                fieldDeclareClass = accessTokenClass.name,
                fieldType = Long::class.java.name,
            ).lastOrNull()?.let {
                it.getFieldInstance(InitFields.ezXClassLoader) as Field
            } ?: throw NoSuchFieldError()

            biliAccountClassMethod = biliAccountClass.declaredMethods.firstOrNull {
                it.isStatic && it.parameterTypes.size == 1 && it.parameterTypes[0] == Context::class.java && it.returnType == it.declaringClass
            } ?: throw Throwable("biliAccountClassMethod not found")

            biliPassportClassField = biliAccountClass.declaredFields.firstOrNull {
                it.type == biliPassportClass
            } ?: throw Throwable("biliPassportClassField not found")

            passportControllerClassField = biliPassportClass.declaredFields.firstOrNull {
                it.type == passportControllerClass
            } ?: throw Throwable("passportControllerClassField not found")

            accessTokenClassField = passportControllerClass.declaredFields.firstOrNull {
                it.type == accessTokenClass
            } ?: throw Throwable("accessTokenClassField not found")

            isTokenClassLoaded = true
        } catch (e: Throwable) {
            Log.d(e)
        }

        findMethod(helpFragmentClass) {
            name == "onCreateView" && parameterTypes.size == 3 && parameterTypes[0] == LayoutInflater::class.java && parameterTypes[1] == ViewGroup::class.java && parameterTypes[2] == Bundle::class.java
        }.hookAfter { param ->
            val activity = param.thisObject.invokeMethodAs<Activity>("getActivity")

            val preferenceScreen = param.thisObject.invokeMethod("getPreferenceScreen")

            if (preferenceScreen?.invokeMethodAuto(
                    "findPreference", "bstar_tweaks"
                ) != null
            ) {
                return@hookAfter
            }

            val hookPreference = Preference(activity as Context).apply {
                title = "bstar 工具箱 ${BuildConfig.VERSION_NAME}"
                summary = "@JasonKhew96"
                key = "bstar_tweaks"
                setOnPreferenceClickListener(object : Preference.OnPreferenceClickListener {
                    override fun onPreferenceClick(preference: Preference): Boolean {

                        var accessToken = ""
                        var refreshToken = ""
                        var expires = 0L
                        if (isTokenClassLoaded) {
                            val biliAccountObj = biliAccountClassMethod!!.invoke(null, activity)
                            val biliPassportObj = biliPassportClassField!!.get(biliAccountObj)
                            val passportControllerObj =
                                passportControllerClassField!!.get(biliPassportObj)
                            val accessTokenObj = accessTokenClassField!!.get(passportControllerObj)

                            accessToken = accessTokenField!!.get(accessTokenObj) as String
                            refreshToken = refreshTokenField!!.get(accessTokenObj) as String
                            expires = expiresField!!.get(accessTokenObj) as Long

                            Log.d("accessTokenObj: $accessTokenObj")
                        }

                        SettingsDialog(activity, accessToken, refreshToken, expires)
                        return true
                    }
                })
            }

            preferenceScreen?.invokeMethodAuto("addPreference", hookPreference.build())
        }

    }
}
