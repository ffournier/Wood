package com.tonytangandroid.wood

import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.core.content.ContextCompat

internal class WoodColorUtil private constructor(context: Context) {
    private val mColorDefault: Int = ContextCompat.getColor(context, R.color.wood_status_default)
    private val mColorVerbose: Int = ContextCompat.getColor(context, R.color.wood_log_verbose)
    private val mColorError: Int = ContextCompat.getColor(context, R.color.wood_log_error)
    private val mColorAssert: Int = ContextCompat.getColor(context, R.color.wood_log_assert)
    private val mColorInfo: Int = ContextCompat.getColor(context, R.color.wood_log_info)
    private val mColorWarning: Int = ContextCompat.getColor(context, R.color.wood_log_warning)
    private val mColorDebug: Int = ContextCompat.getColor(context, R.color.wood_log_debug)

    fun getTransactionColor(transaction: Leaf): Int {
        return getTransactionColor(transaction.priority)
    }

    fun getTransactionColor(priority: Int): Int = when(priority) {
        Log.VERBOSE -> mColorVerbose
        Log.DEBUG -> mColorDebug
        Log.INFO -> mColorInfo
        Log.WARN -> mColorWarning
        Log.ERROR -> mColorError
        Log.ASSERT -> mColorAssert
        else -> mColorDefault
    }

    companion object {
        val SEARCHED_HIGHLIGHT_BACKGROUND_COLOR: Int = Color.parseColor("#FD953F")
        val HIGHLIGHT_BACKGROUND_COLOR: Int = Color.parseColor("#FFFD38")
        const val HIGHLIGHT_TEXT_COLOR: Int = 0
        const val HIGHLIGHT_UNDERLINE: Boolean = false
        private var TRANSACTION_COLOR_UTIL_INSTANCE: WoodColorUtil? = null

        fun getInstance(context: Context): WoodColorUtil = TRANSACTION_COLOR_UTIL_INSTANCE ?: WoodColorUtil(context).also {
            TRANSACTION_COLOR_UTIL_INSTANCE = it
        }
    }
}
