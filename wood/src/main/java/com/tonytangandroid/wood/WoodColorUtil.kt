package com.tonytangandroid.wood

import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.core.content.ContextCompat

internal class WoodColorUtil private constructor(context: Context) {
    private val colorDefault: Int = ContextCompat.getColor(context, R.color.wood_status_default)
    private val colorVerbose: Int = ContextCompat.getColor(context, R.color.wood_log_verbose)
    private val colorError: Int = ContextCompat.getColor(context, R.color.wood_log_error)
    private val colorAssert: Int = ContextCompat.getColor(context, R.color.wood_log_assert)
    private val colorInfo: Int = ContextCompat.getColor(context, R.color.wood_log_info)
    private val colorWarning: Int = ContextCompat.getColor(context, R.color.wood_log_warning)
    private val colorDebug: Int = ContextCompat.getColor(context, R.color.wood_log_debug)

    fun getTransactionColor(transaction: Leaf): Int = getTransactionColor(transaction.priority)

    fun getTransactionColor(priority: Int): Int = when(priority) {
        Log.VERBOSE -> colorVerbose
        Log.DEBUG -> colorDebug
        Log.INFO -> colorInfo
        Log.WARN -> colorWarning
        Log.ERROR -> colorError
        Log.ASSERT -> colorAssert
        else -> colorDefault
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
