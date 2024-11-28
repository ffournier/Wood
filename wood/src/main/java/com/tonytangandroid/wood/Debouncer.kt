package com.tonytangandroid.wood

import android.os.Handler
import android.os.Looper

internal class Debouncer<T>(private val interval: Int, private val callback: Callback<T>) {
    private val handler = Handler(Looper.getMainLooper())

    fun consume(event: T) {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed(Counter(event, callback), interval.toLong())
    }

    class Counter<T> internal constructor(
        private val event: T,
        private val callback: Callback<T>
    ) : Runnable {
        override fun run() {
            callback.onEmit(event)
        }
    }
}
