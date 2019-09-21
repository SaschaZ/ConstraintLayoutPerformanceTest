package com.krpiotrek.constraintlayoutstuff

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout

class TestConstraintLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attributeSet, defStyleAttr, defStyleRes) {

    var drawListener: (() -> Unit)? = null

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawListener?.invoke()
        drawListener = null
    }
}