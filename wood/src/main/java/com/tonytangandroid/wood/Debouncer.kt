package com.tonytangandroid.wood

import android.os.Handler

internal class Debouncer<T>(private val mInterval: Int, private val mCallback: Callback<T>) {
    private val mHandler = Handler()

    fun consume(event: T) {
        mHandler.removeCallbacksAndMessages(null)
        mHandler.postDelayed(Counter(event, mCallback), mInterval.toLong())
    }

    class Counter<T> internal constructor(
        private val mEvent: T,
        private val mCallback: Callback<T>
    ) : Runnable {
        override fun run() {
            mCallback.onEmit(mEvent)
        }
    }
}
