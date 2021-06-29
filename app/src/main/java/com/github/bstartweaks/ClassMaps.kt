package com.github.bstartweaks

class ClassMaps {
    companion object {
        // 1052001
        // first four digit
        val share: HashMap<Int, Pair<String, String>> = hashMapOf(
            1052 to ("b.hf0" to "a"),
            1060 to ("b.fi0" to "a"),
            1061 to ("b.ze0" to "a"),
            1070 to ("b.de0" to "a"),
            1080 to ("b.be0" to "a"),
            1081 to ("b.be0" to "a"),
        )
        val locale: HashMap<Int, Pair<String, String>> = hashMapOf(
            1081 to ("com.bilibili.lib.ui.util.f" to "b"),
        )
        val copy: HashMap<Int, Pair<String, String>> = hashMapOf(
            1081 to ("com.bilibili.droid.f" to "a"),
        )
        val toast: HashMap<Int, Pair<String, String>> = hashMapOf(
            1081 to ("com.bilibili.droid.x" to "b"),
        )
        val download: HashMap<Int, Pair<String, String>> = hashMapOf(
            1052 to ("b.ti" to "E"),
            1060 to ("b.ll" to "E"),
            1061 to ("b.oi" to "z"),
            1070 to ("b.fi" to "x"),
            1080 to ("b.gi" to "x"),
            1081 to ("b.gi" to "x"),
        )
    }
}