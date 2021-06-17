package com.github.bstartweaks.hook

abstract class BaseHook(val mClassLoader: ClassLoader) {
    abstract fun startHook()
}