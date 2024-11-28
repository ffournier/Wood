package com.tonytangandroid.woodnoop

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build

object Wood {
    /**
     * Get an Intent to launch the Wood UI directly.
     *
     * @param context A Context.
     * @return An Intent for the main Wood Activity that can be started with [Context.startActivity].
     */
    fun getLaunchIntent(context: Context?): Intent {
        return Intent(context, LeafListActivity::class.java)
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    fun addAppShortcut(context: Context?): String? {
        return null
    }
}
