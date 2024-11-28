package com.tonytangandroid.wood

import android.app.IntentService
import android.content.Intent

class ClearTransactionsService : IntentService("Wood-ClearTransactionsService") {

    override fun onHandleIntent(intent: Intent?) {
        val deletedTransactionCount = WoodDatabase.getInstance(this).leafDao().clearAll()
        Logger.i("$deletedTransactionCount transactions deleted")
        val notificationHelper = NotificationHelper(this)
        notificationHelper.dismiss()
    }

}
