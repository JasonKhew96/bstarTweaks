package com.github.bstartweaks.ui

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.github.bstartweaks.XposedInit
import com.github.bstartweaks.Utils
import de.robv.android.xposed.XposedHelpers

class ForegroundRelativeLayout(context: Context, leftText: String, rightText: String) {
    companion object {
        val relativeLayoutClazz = XposedInit.classLoader.loadClass("tv.danmaku.bili.widget.ForegroundRelativeLayout")!!
    }
    private val relativeLayout: RelativeLayout = XposedHelpers.newInstance(
        relativeLayoutClazz,
        context
    ) as RelativeLayout

    init {
        relativeLayout.setPadding(
            Utils.dpToPx(12),
            0,
            Utils.dpToPx(12),
            0
        )
        relativeLayout.minimumHeight = Utils.dpToPx(44)
//        relativeLayout.setBackgroundColor(Color.BLACK)

        val tvlp = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        tvlp.addRule(
            RelativeLayout.CENTER_VERTICAL,
            RelativeLayout.TRUE
        )

        val textView = TextView(context)
        textView.text = leftText
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.setTextAppearance(
                context.resources.getIdentifier(
                    "TextAppearance.App.Title",
                    "style",
                    context.packageName
                )
            )
        } else {
            @Suppress("DEPRECATION")
            textView.setTextAppearance(
                context,
                context.resources.getIdentifier(
                    "TextAppearance.App.Title",
                    "style",
                    context.packageName
                )
            )
        }
//        textView.setTextColor(Color.YELLOW)
        val tv2lp = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        tv2lp.addRule(
            RelativeLayout.CENTER_VERTICAL,
            RelativeLayout.TRUE
        )
        tv2lp.addRule(
            RelativeLayout.ALIGN_PARENT_RIGHT,
            RelativeLayout.TRUE
        )
        tv2lp.addRule(
            RelativeLayout.ALIGN_PARENT_END,
            RelativeLayout.TRUE
        )
        val textView2 = TextView(context)
        textView2.text = rightText
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView2.setTextAppearance(
                context.resources.getIdentifier(
                    "TextAppearance.App.Title",
                    "style",
                    context.packageName
                )
            )
        } else {
            @Suppress("DEPRECATION")
            textView2.setTextAppearance(
                context,
                context.resources.getIdentifier(
                    "TextAppearance.App.Title",
                    "style",
                    context.packageName
                )
            )
        }
        textView2.setTextColor(Color.GRAY)
        relativeLayout.addView(textView, tvlp)
        relativeLayout.addView(textView2, tv2lp)
    }

    fun setOnClickListener(onClickListener: View.OnClickListener) {
        relativeLayout.setOnClickListener(onClickListener)
    }

    fun build(): RelativeLayout {
        return relativeLayout
    }
}