package com.tonytangandroid.wood

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.LongSparseArray
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.tony.tang.safe.pending.intent.sdk.SafePendingIntent
import com.tonytangandroid.wood.Wood.getLaunchIntent
import com.tonytangandroid.wood.WoodColorUtil.Companion.getInstance

internal class NotificationHelper(context: Context) {
    private val mContext: Context = context
    private val mNotificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val mColorUtil: WoodColorUtil = getInstance(context)

    init {
        setUpChannelIfNecessary()
    }

    private fun setUpChannelIfNecessary() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    mContext.getString(R.string.wood_notification_category),
                    NotificationManager.IMPORTANCE_LOW
                )
            channel.setShowBadge(false)

            mNotificationManager.createNotificationChannel(channel)
        }
    }

    @Synchronized
    fun show(transaction: Leaf, stickyNotification: Boolean) {
        addToBuffer(transaction)
        val builder =
            NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentIntent(
                    SafePendingIntent.getActivity(mContext, 0, getLaunchIntent(mContext), 0)
                )
                .setLocalOnly(true)
                .setSmallIcon(R.drawable.wood_icon)
                .setColor(ContextCompat.getColor(mContext, R.color.wood_colorPrimary))
                .setOngoing(stickyNotification)
                .setContentTitle(mContext.getString(R.string.wood_notification_title))
        val inboxStyle = NotificationCompat.InboxStyle()

        var count = 0
        for (i in TRANSACTION_BUFFER.size() - 1 downTo 0) {
            if (count < BUFFER_SIZE) {
                if (count == 0) {
                    builder.setContentText(getNotificationText(TRANSACTION_BUFFER.valueAt(i)!!))
                }
                inboxStyle.addLine(getNotificationText(TRANSACTION_BUFFER.valueAt(i)!!))
            }
            count++
        }
        builder.setAutoCancel(true)
        builder.setStyle(inboxStyle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setSubText(TRANSACTION_COUNT.toString())
        } else {
            builder.setNumber(TRANSACTION_COUNT)
        }
        builder.addAction(getDismissAction())
        builder.addAction(getClearAction())
        mNotificationManager.notify(CHANNEL_ID.hashCode(), builder.build())
    }

    private fun getNotificationText(transaction: Leaf): CharSequence {
        val color = mColorUtil.getTransactionColor(transaction)
        val text = transaction.body()
        // Simple span no Truss required
        val spannableString = SpannableString(text)
        spannableString.setSpan(
            ForegroundColorSpan(color), 0, text!!.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }

    private fun getClearAction(): NotificationCompat.Action {
        val clearTitle: CharSequence = mContext.getString(R.string.wood_clear)
        val deleteIntent = Intent(mContext, ClearTransactionsService::class.java)
        val intent =
            SafePendingIntent.getService(mContext, 11, deleteIntent, PendingIntent.FLAG_ONE_SHOT)
        return NotificationCompat.Action(R.drawable.wood_ic_delete_white_24dp, clearTitle, intent)
    }

    private fun getDismissAction(): NotificationCompat.Action {
        val dismissTitle: CharSequence = mContext.getString(R.string.wood_dismiss)
        val dismissIntent = Intent(mContext, DismissNotificationService::class.java)
        val intent =
            SafePendingIntent.getService(mContext, 12, dismissIntent, PendingIntent.FLAG_ONE_SHOT)
        return NotificationCompat.Action(0, dismissTitle, intent)
    }

    fun dismiss() {
        mNotificationManager.cancel(CHANNEL_ID.hashCode())
    }

    companion object {
        private const val CHANNEL_ID = "wood_notification_log_channel"
        private const val BUFFER_SIZE = 10

        private val TRANSACTION_BUFFER = LongSparseArray<Leaf?>()
        private var TRANSACTION_COUNT = 0

        @JvmStatic
        @Synchronized
        fun clearBuffer() {
            TRANSACTION_BUFFER.clear()
            TRANSACTION_COUNT = 0
        }

        @Synchronized
        private fun addToBuffer(transaction: Leaf) {
            TRANSACTION_COUNT++
            TRANSACTION_BUFFER.put(transaction.id, transaction)
            if (TRANSACTION_BUFFER.size() > BUFFER_SIZE) {
                TRANSACTION_BUFFER.removeAt(0)
            }
        }
    }
}
