package com.github.bstartweaks.hook

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.bstartweaks.BuildConfig
import com.github.bstartweaks.MainHook
import com.github.bstartweaks.MainHook.Companion.dexKit
import com.github.bstartweaks.modulePrefs
import com.github.bstartweaks.ui.Preference
import com.github.bstartweaks.ui.SettingsDialog
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClassOrNull
import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.finders.FieldFinder
import com.github.kyuubiran.ezxhelper.finders.MethodFinder
import de.robv.android.xposed.XposedHelpers
import io.luckypray.dexkit.annotations.DexKitExperimentalApi
import java.lang.reflect.Field
import java.lang.reflect.Method

object SettingsHook : BaseHook() {
    private lateinit var biliAccountClassName: String

    private lateinit var biliPassportClassName: String

    private lateinit var passportControllerClassName: String

    private lateinit var accessTokenClassName: String
    private lateinit var accessTokenFieldName: String
    private lateinit var refreshTokenFieldName: String
    private lateinit var expiresFieldName: String

    private const val HOOK_BILI_ACCOUNT_CLASS = "hook_bili_account_class"

    private const val HOOK_BILI_PASSPORT_CLASS = "hook_bili_passport_class"

    private const val HOOK_PASSPORT_CONTROLLER_CLASS = "hook_passport_controller_class"

    private const val HOOK_ACCESS_TOKEN_CLASS = "hook_access_token_class"
    private const val HOOK_ACCESS_TOKEN_FIELD = "hook_access_token_field"
    private const val HOOK_REFRESH_TOKEN_FIELD = "hook_refresh_token_field"
    private const val HOOK_EXPIRES_FIELD = "hook_expires_field"

    @OptIn(DexKitExperimentalApi::class)
    private fun searchHook() {
        val biliAccountClass = dexKit.findMethodUsingString {
            usingString = "^BiliAccount$"
            methodReturnType = Void.TYPE.name
            methodParamTypes = emptyArray()
        }.firstNotNullOfOrNull { loadClassOrNull(it.declaringClassName) }
            ?: throw ClassNotFoundException()
        val biliPassportClass = dexKit.findMethodUsingString {
            usingString = "^BiliPassport$"
            methodParamTypes = emptyArray()
        }.firstNotNullOfOrNull { loadClassOrNull(it.declaringClassName) }
            ?: throw ClassNotFoundException()
        val passportControllerClass = dexKit.findMethodUsingString {
            usingString = "^PassportController$"
        }.firstNotNullOfOrNull { loadClassOrNull(it.declaringClassName) }
            ?: throw ClassNotFoundException()
        val accessTokenClass = dexKit.findMethodUsingString {
            usingString = "^AccessToken{mExpiresIn=$"
            methodName = "toString"
            methodReturnType = String::class.java.name
        }.firstNotNullOfOrNull { loadClassOrNull(it.declaringClassName) }
            ?: throw ClassNotFoundException()

        biliAccountClassName = biliAccountClass.name
        biliPassportClassName = biliPassportClass.name
        passportControllerClassName = passportControllerClass.name
        accessTokenClassName = accessTokenClass.name

        val jsonFieldClass = loadClass("com.alibaba.fastjson.annotation.JSONField")
        val accessTokenField = dexKit.findFieldUsingAnnotation {
            annotationClass = jsonFieldClass.name
            annotationUsingString = "access_token"
            fieldDeclareClass = accessTokenClass.name
            fieldType = String::class.java.name
        }.firstNotNullOfOrNull { it.getFieldInstance(EzXHelper.classLoader) as Field }
            ?: throw NoSuchFieldError()
        val refreshTokenField = dexKit.findFieldUsingAnnotation {
            annotationClass = jsonFieldClass.name
            annotationUsingString = "refresh_token"
            fieldDeclareClass = accessTokenClass.name
            fieldType = String::class.java.name
        }.firstNotNullOfOrNull { it.getFieldInstance(EzXHelper.classLoader) as Field }
            ?: throw NoSuchFieldError()
        val expiresField = dexKit.findFieldUsingAnnotation {
            annotationClass = jsonFieldClass.name
            annotationUsingString = "expires"
            fieldDeclareClass = accessTokenClass.name
            fieldType = Long::class.java.name
        }.lastOrNull()?.let {
            it.getFieldInstance(EzXHelper.classLoader) as Field
        } ?: throw NoSuchFieldError()

        accessTokenFieldName = accessTokenField.name
        refreshTokenFieldName = refreshTokenField.name
        expiresFieldName = expiresField.name

        modulePrefs.edit().apply {
            putString(HOOK_BILI_ACCOUNT_CLASS, biliAccountClassName)
            putString(HOOK_BILI_PASSPORT_CLASS, biliPassportClassName)
            putString(HOOK_PASSPORT_CONTROLLER_CLASS, passportControllerClassName)
            putString(HOOK_ACCESS_TOKEN_CLASS, accessTokenClassName)
            putString(HOOK_ACCESS_TOKEN_FIELD, accessTokenFieldName)
            putString(HOOK_REFRESH_TOKEN_FIELD, refreshTokenFieldName)
            putString(HOOK_EXPIRES_FIELD, expiresFieldName)
        }.apply()
    }

    private fun loadCachedHook() {
        modulePrefs.apply {
            biliAccountClassName = getString(HOOK_BILI_ACCOUNT_CLASS, "")!!
            biliPassportClassName = getString(HOOK_BILI_PASSPORT_CLASS, "")!!
            passportControllerClassName = getString(HOOK_PASSPORT_CONTROLLER_CLASS, "")!!
            accessTokenClassName = getString(HOOK_ACCESS_TOKEN_CLASS, "")!!
            accessTokenFieldName = getString(HOOK_ACCESS_TOKEN_FIELD, "")!!
            refreshTokenFieldName = getString(HOOK_REFRESH_TOKEN_FIELD, "")!!
            expiresFieldName = getString(HOOK_EXPIRES_FIELD, "")!!
        }
    }

    override fun init() {
        if (MainHook.isDexKitNeeded) {
            searchHook()
        } else {
            loadCachedHook()
        }

        val helpFragmentClass = loadClass("com.bilibili.app.preferences.fragment.HelpFragment")

        var isTokenClassLoaded = false

        var biliAccountClassMethod: Method? = null

        var biliPassportClassField: Field? = null
        var passportControllerClassField: Field? = null
        var accessTokenClassField: Field? = null

        try {
            val biliAccountClass = loadClass(biliAccountClassName)
            val biliPassportClass = loadClass(biliPassportClassName)
            val passportControllerClass = loadClass(passportControllerClassName)
            val accessTokenClass = loadClass(accessTokenClassName)


            biliAccountClassMethod = MethodFinder.fromClass(biliAccountClass).filterStatic()
                .filterByParamTypes(Context::class.java).filterByReturnType(biliAccountClass)
                .first()

            biliPassportClassField =
                FieldFinder.fromClass(biliAccountClass).filterByType(biliPassportClass).first()

            passportControllerClassField =
                FieldFinder.fromClass(biliPassportClass).filterByType(passportControllerClass)
                    .first()

            accessTokenClassField =
                FieldFinder.fromClass(passportControllerClass).filterByType(accessTokenClass)
                    .first()

            isTokenClassLoaded = true
        } catch (e: Throwable) {
            Log.d(e)
        }

        MethodFinder.fromClass(helpFragmentClass).filterByName("onCreateView").filterByParamTypes(
            LayoutInflater::class.java, ViewGroup::class.java, Bundle::class.java
        ).first().createHook {
            after { param ->
                val activity = XposedHelpers.callMethod(param.thisObject, "getActivity") as Activity

                val preferenceScreen =
                    XposedHelpers.callMethod(param.thisObject, "getPreferenceScreen")

                if (XposedHelpers.callMethod(
                        preferenceScreen, "findPreference", "bstar_tweaks"
                    ) != null
                ) {
                    return@after
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
                                val biliAccountObj = biliAccountClassMethod?.invoke(null, activity)
                                val biliPassportObj = biliPassportClassField?.get(biliAccountObj)
                                val passportControllerObj =
                                    passportControllerClassField?.get(biliPassportObj)
                                val accessTokenObj =
                                    accessTokenClassField?.get(passportControllerObj)

                                Log.d("accessTokenObj: $accessTokenObj")

                                XposedHelpers.getObjectField(accessTokenObj, accessTokenFieldName)
                                    ?.let {
                                        accessToken = it as String
                                    }
                                XposedHelpers.getObjectField(accessTokenObj, refreshTokenFieldName)
                                    ?.let {
                                        refreshToken = it as String
                                    }
                                XposedHelpers.getObjectField(accessTokenObj, expiresFieldName)
                                    ?.let {
                                        expires = it as Long
                                    }
                            }

                            SettingsDialog(activity, accessToken, refreshToken, expires)
                            return true
                        }
                    })
                }

                XposedHelpers.callMethod(preferenceScreen, "addPreference", hookPreference.build())
            }
        }
    }
}
