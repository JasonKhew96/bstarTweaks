package com.github.bstartweaks.hook

abstract class BaseHook {
    var isInit: Boolean = false
    abstract fun init()
}
