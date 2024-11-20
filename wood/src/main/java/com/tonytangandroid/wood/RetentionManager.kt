package com.tonytangandroid.wood

import android.content.Context
import android.content.SharedPreferences
import com.tonytangandroid.wood.WoodDatabase.Companion.getInstance
import java.util.Date
import java.util.concurrent.TimeUnit

internal class RetentionManager(context: Context, retentionPeriod: WoodTree.Period) {
    private val woodDatabase: WoodDatabase = getInstance(context)
    private val period: Long
    private val cleanupFrequency: Long
    private val prefs: SharedPreferences

    init {
        period = toMillis(retentionPeriod)
        prefs = context.getSharedPreferences(PREFS_NAME, 0)
        cleanupFrequency =
            if ((retentionPeriod == WoodTree.Period.ONE_HOUR))
                TimeUnit.MINUTES.toMillis(30)
            else
                TimeUnit.HOURS.toMillis(2)
    }

    @Synchronized
    fun doMaintenance() {
        if (period > 0) {
            val now = Date().getTime()
            if (isCleanupDue(now)) {
                Logger.i("Performing data retention maintenance...")
                deleteSince(getThreshold(now))
                updateLastCleanup(now)
            }
        }
    }

    private fun getLastCleanup(fallback: Long): Long {
        if (LAST_CLEAN_UP == 0L) {
            LAST_CLEAN_UP = prefs.getLong(KEY_LAST_CLEANUP, fallback)
        }
        return LAST_CLEAN_UP
    }

    private fun updateLastCleanup(time: Long) {
        LAST_CLEAN_UP = time
        prefs.edit().putLong(KEY_LAST_CLEANUP, time).apply()
    }

    private fun deleteSince(threshold: Long) {
        val rows = woodDatabase.leafDao()!!.deleteTransactionsBefore(threshold).toLong()
        Logger.i("$rows transactions deleted")
    }

    private fun isCleanupDue(now: Long): Boolean {
        return (now - getLastCleanup(now)) > cleanupFrequency
    }

    private fun getThreshold(now: Long): Long {
        return if ((period == 0L)) now else now - period
    }

    private fun toMillis(period: WoodTree.Period): Long {
        when (period) {
            WoodTree.Period.ONE_HOUR -> return TimeUnit.HOURS.toMillis(1)
            WoodTree.Period.ONE_DAY -> return TimeUnit.DAYS.toMillis(1)
            WoodTree.Period.ONE_WEEK -> return TimeUnit.DAYS.toMillis(7)
            else -> return 0
        }
    }

    companion object {
        private const val PREFS_NAME = "wood_preferences"
        private const val KEY_LAST_CLEANUP = "last_cleanup"

        private var LAST_CLEAN_UP: Long = 0
    }
}
