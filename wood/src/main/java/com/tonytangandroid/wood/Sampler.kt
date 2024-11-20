package com.tonytangandroid.wood

import android.os.Handler
import android.os.Looper

internal class Sampler<T>(intervalInMills: Int, callback: Callback<T?>) {
    private val interval: Int = intervalInMills
    private val callback: Callback<T?>
    private val handler: Handler

    private var currentRunnable: Counter<T?>? = null

    init {
        this.callback = callback
        handler = Handler(Looper.getMainLooper())
    }

    fun consume(event: T?) {
        val runnable = currentRunnable
        if (runnable == null) {
            // first runnable
            Counter<T?>(event, callback).also {
                currentRunnable = it
                handler.postDelayed(it, interval.toLong())
            }
        } else {
            if (runnable.state == Counter.Companion.STATE_CREATED
                || runnable.state == Counter.Companion.STATE_QUEUED
            ) {
                //  yet to emit (with in an interval)
                runnable.updateEvent(event)
            } else if (runnable.state == Counter.Companion.STATE_RUNNING
                || runnable.state == Counter.Companion.STATE_FINISHED
            ) {
                // interval finished. open new batch
                Counter<T?>(event, callback).also {
                    currentRunnable = it
                    handler.postDelayed(it, interval.toLong())
                }
            }
        }
    }

    class Counter<T> internal constructor(event: T?, callback: Callback<T?>) : Runnable {
        private val callback: Callback<T?>
        var state: Int
        private var event: T?

        init {
            this.event = event
            this.callback = callback
            state = STATE_CREATED
        }

        fun updateEvent(deliverable: T?) {
            this.event = deliverable
        }

        override fun run() {
            state = STATE_RUNNING
            callback.onEmit(event)
            state = STATE_FINISHED
        }

        companion object {
            const val STATE_CREATED: Int = 1
            const val STATE_QUEUED: Int = 2
            const val STATE_RUNNING: Int = 3
            const val STATE_FINISHED: Int = 4
        }
    }
}
