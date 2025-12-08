package com.tonytangandroid.wood.sample

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

class GeneratingLogActivity : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keep_generating_log)
    }

    override fun onResume() {
        super.onResume()
        generate()
    }

    private fun generate() {
        handler.postDelayed(Runnable { this.log() }, 1000)
    }

    private fun log() {
        (0..9).forEach { i ->
            count++
            Timber.v("generate log :%s", count)
        }
        generate()
    }

    override fun onPause() {
        super.onPause()
        stop()
    }

    private fun stop() {
        handler.removeCallbacksAndMessages(null)
    }
}
