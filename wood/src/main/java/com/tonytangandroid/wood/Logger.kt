package com.tonytangandroid.wood

import android.annotation.SuppressLint
import android.util.Log
import java.lang.Exception

@SuppressLint("LogNotTimber")
internal object Logger {
    private const val LOG_TAG = "WoodTree"

    fun i(message: String) {
        Log.i(LOG_TAG, message)
    }

    fun w(message: String) {
        Log.w(LOG_TAG, message)
    }

    fun e(message: String?, e: Exception?) {
        Log.e(LOG_TAG, message, e)
    }
}
