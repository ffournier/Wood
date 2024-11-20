package com.tonytangandroid.wood

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.tonytangandroid.wood.WoodTree.ErrorUtil
import timber.log.Timber.DebugTree
import java.io.PrintWriter
import java.io.StringWriter
import java.util.ArrayList
import java.util.Locale
import java.util.concurrent.Executor
import kotlin.math.min

class WoodTree @JvmOverloads constructor(context: Context, private val threadTagPrefix: String = "") : DebugTree() {
    private var contextApp: Context = context.applicationContext
    private val woodDatabase: WoodDatabase = WoodDatabase.getInstance(context)
    private val executor: Executor = JobExecutor()
    private var notificationHelper: NotificationHelper? = null
    private var retentionManager: RetentionManager
    private var maxContentLength: Int = 250000
    private var stickyNotification = false
    private var supportedTaggerList: MutableList<String> = ArrayList<String>()
    private val sharedPreferences: SharedPreferences
    private var logLevel = Log.DEBUG

    /**
     * @param context context
     * @param threadTagPrefix the extra prefix added on the logged message.
     */
    /** @param context The current Context.
     */
    init {
        this.retentionManager = RetentionManager(contextApp, DEFAULT_RETENTION)
        this.sharedPreferences =
            context.getSharedPreferences(PREF_WOOD_CONFIG, Context.MODE_PRIVATE)
    }

    /**
     * Control whether a notification is shown while Timber log is recorded.
     *
     * @param sticky true to show a sticky notification.
     * @return The [WoodTree] instance.
     */
    fun showNotification(sticky: Boolean): WoodTree {
        this.stickyNotification = sticky
        notificationHelper = NotificationHelper(contextApp)
        return this
    }

    fun limitToTheseTaggerList(supportedTaggerList: MutableList<String>): WoodTree {
        this.supportedTaggerList = supportedTaggerList
        return this
    }

    /**
     * If you want to only log warning or above, pass [Log.WARN]. By default, it
     * will log all debug log [Log.DEBUG] or above
     *
     * @param logLevel the log level value from [Log]
     * @return The [WoodTree] instance.
     */
    fun logLevel(logLevel: Int): WoodTree {
        this.logLevel = logLevel
        return this
    }

    /**
     * Set the retention period for Timber log data captured by this interceptor. The default is one
     * week.
     *
     * @param period the period for which to retain Timber log data.
     * @return The [WoodTree] instance.
     */
    fun retainDataFor(period: Period): WoodTree {
        retentionManager = RetentionManager(contextApp, period)
        return this
    }

    /**
     * Set the log should auto scroll like Android Logcat console. By default it is false.
     *
     * @param autoScroll true if you want to make the log auto scroll.
     * @return The [WoodTree] instance.
     */
    fun autoScroll(autoScroll: Boolean): WoodTree {
        sharedPreferences.edit().putBoolean(PREF_KEY_AUTO_SCROLL, autoScroll).apply()
        return this
    }

    /**
     * Set the maximum length for request and response content before it is truncated. Warning:
     * setting this value too high may cause unexpected results.
     *
     * @param max the maximum length (in bytes) for request/response content.
     * @return The [WoodTree] instance.
     */
    fun maxLength(max: Int): WoodTree {
        maxContentLength = min(max, 999999) // close to => 1 MB Max in a BLOB SQLite.
        return this
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (shouldBeLogged(priority, tag.orEmpty())) {
            val assembledMessage: String = formatThreadTag(message, this.threadTagPrefix)
            executor.execute(Runnable { doLog(priority, tag, assembledMessage, t) })
        }
    }

    private fun shouldBeLogged(priority: Int, tag: String): Boolean {
        if (priority < logLevel) {
            return false
        }

        if (hasNoTagFilter()) {
            return true
        }

        return tagIsListedCaseInsensitive(tag)
    }

    private fun tagIsListedCaseInsensitive(tag: String): Boolean {
        val toLowerCase = tag.lowercase(Locale.getDefault())
        for (supportedTagger in supportedTaggerList) {
            if (supportedTagger.lowercase(Locale.getDefault()).contains(toLowerCase)) {
                return true
            }
        }
        return false
    }

    private fun hasNoTagFilter(): Boolean {
        return supportedTaggerList.isEmpty()
    }

    private fun doLog(priority: Int, tag: String?, message: String, t: Throwable?) {
        var message = message
        val transaction = Leaf()
        transaction.priority = priority
        transaction.createAt = System.currentTimeMillis()
        transaction.tag = tag
        transaction.setLength(message.length)
        if (t != null) {
            message = message + "\n" + t.message + "\n" + ErrorUtil.asString(t)
        }
        transaction.setBody(
            message.substring(
                0,
                min(message.length, maxContentLength)
            ) + ""
        )
        create(transaction)
    }

    private fun create(transaction: Leaf) {
        val transactionId = woodDatabase.leafDao().insertTransaction(transaction)
        transaction.id = transactionId
        if (notificationHelper != null) {
            notificationHelper!!.show(transaction, stickyNotification)
        }
        retentionManager.doMaintenance()
    }

    enum class Period {
        /** Retain data for the last hour.  */
        ONE_HOUR,

        /** Retain data for the last day.  */
        ONE_DAY,

        /** Retain data for the last week.  */
        ONE_WEEK,

        /** Retain data forever.  */
        FOREVER
    }

    /** From https://stackoverflow.com/a/1149712/4068957  */
    internal object ErrorUtil {
        fun asString(throwable: Throwable): String {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            return sw.toString() // stack trace as a string
        }
    }

    companion object {
        private val DEFAULT_RETENTION = Period.ONE_WEEK
        private const val PREF_WOOD_CONFIG = "pref_wood_config"
        private const val PREF_KEY_AUTO_SCROLL = "pref_key_auto_scroll"
        @JvmStatic
        fun autoScroll(context: Context): Boolean {
            val sharedPreferences =
                context.getSharedPreferences(PREF_WOOD_CONFIG, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean(PREF_KEY_AUTO_SCROLL, true)
        }

        private fun formatThreadTag(message: String?, threadTagPrefix: String): String {
            return String.format(
                Locale.US, "[%s#%s]:%s", threadTagPrefix, Thread.currentThread().getName(), message
            )
        }
    }
}
