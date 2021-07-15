package com.github.bstartweaks

class ClassMaps {
    companion object {
        // 1052001
        // first four digit
        val locale: HashMap<Int, Pair<String, String>> = hashMapOf(
            1100 to ("com.bilibili.lib.ui.util.f" to "b"),
        )
        val copy: HashMap<Int, Pair<String, String>> = hashMapOf(
            1100 to ("com.bilibili.droid.f" to "a"),
        )
        val toast: HashMap<Int, Pair<String, String>> = hashMapOf(
            1100 to ("com.bilibili.droid.x" to "b"),
        )
    }
}
