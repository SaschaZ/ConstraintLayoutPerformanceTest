package com.krpiotrek.constraintlayoutstuff

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.RelativeLayout

class TestRelativeLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : RelativeLayout(context, attributeSet, defStyleAttr, defStyleRes) {

    var drawListener: (() -> Unit)? = null

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawListener?.invoke()
        drawListener = null
    }
}