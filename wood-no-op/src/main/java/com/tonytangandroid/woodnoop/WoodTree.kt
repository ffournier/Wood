package com.tonytangandroid.woodnoop

import android.content.Context
import timber.log.Timber

class WoodTree : Timber.Tree {
    constructor(context: Context?)

    constructor(context: Context?, threadTagPrefix: String?)

    fun showNotification(sticky: Boolean): WoodTree {
        return this
    }

    fun retainDataFor(period: Period?): WoodTree {
        return this
    }

    fun maxLength(max: Int): WoodTree {
        return this
    }

    override fun log(
        priority: Int, tag: String?, message: String, t: Throwable?
    ) {
    }

    fun limitToTheseTaggerList(supportedTaggerList: List<String?>?): WoodTree {
        return this
    }

    fun logLevel(logLevel: Int): WoodTree {
        return this
    }

    fun autoScroll(autoScroll: Boolean): WoodTree {
        return this
    }

    enum class Period {
        ONE_HOUR,
        ONE_DAY,
        ONE_WEEK,
        FOREVER
    }

    companion object {
        fun autoScroll(context: Context?): Boolean {
            return false
        }
    }
}
