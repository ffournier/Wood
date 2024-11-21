package com.tonytangandroid.wood

import android.os.Handler
import android.os.Looper

internal class Sampler<T>(intervalInMills: Int, val callback: Callback<T?>) {
    private val interval: Int = intervalInMills
    private val handler: Handler = Handler(Looper.getMainLooper())
    private var currentRunnable: Counter<T?>? = null

    fun consume(event: T?) {
        val runnable = currentRunnable
        if (runnable == null) {
            // first runnable
            Counter<T?>(event, callback).also {
                currentRunnable = it
                handler.postDelayed(it, interval.toLong())
            }
        } else {
            if (runnable.state == Counter.STATE_CREATED
                || runnable.state == Counter.STATE_QUEUED
            ) {
                //  yet to emit (with in an interval)
                runnable.updateEvent(event)
            } else if (runnable.state == Counter.STATE_RUNNING
                || runnable.state == Counter.STATE_FINISHED
            ) {
                // interval finished. open new batch
                Counter<T?>(event, callback).also {
                    currentRunnable = it
                    handler.postDelayed(it, interval.toLong())
                }
            }
        }
    }

    class Counter<T> internal constructor(event: T?, val callback: Callback<T?>) : Runnable {
        var state: Int
        private var event: T?

        init {
            this.event = event
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
