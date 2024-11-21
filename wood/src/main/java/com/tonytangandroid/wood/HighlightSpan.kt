package com.tonytangandroid.wood

import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance
import androidx.annotation.ColorInt

internal class HighlightSpan(
    private val backgroundColor: Int,
    @param:ColorInt private val textColor: Int,
    private val underLineText: Boolean
) : CharacterStyle(), UpdateAppearance {

    private val applyBackgroundColor = backgroundColor != 0
    private val applyTextColor = textColor != 0

    override fun updateDrawState(ds: TextPaint) {
        if (applyTextColor) ds.color = textColor
        if (applyBackgroundColor) ds.bgColor = backgroundColor
        ds.isUnderlineText = underLineText
    }
}
