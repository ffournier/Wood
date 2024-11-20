package com.tonytangandroid.wood

import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance
import androidx.annotation.ColorInt

internal class HighlightSpan(
    private val mBackgroundColor: Int,
    @param:ColorInt private val mTextColor: Int,
    private val mUnderLineText: Boolean
) : CharacterStyle(), UpdateAppearance {
    private val mApplyBackgroundColor = mBackgroundColor != 0
    private val mApplyTextColor = mTextColor != 0

    override fun updateDrawState(ds: TextPaint) {
        if (mApplyTextColor) ds.color = mTextColor
        if (mApplyBackgroundColor) ds.bgColor = mBackgroundColor
        ds.isUnderlineText = mUnderLineText
    }
}
