package com.tonytangandroid.wood

import android.app.IntentService
import android.content.Intent

class DismissNotificationService : IntentService("Wood-DismissNotificationService") {

    override fun onHandleIntent(intent: Intent?) {
        val notificationHelper = NotificationHelper(this)
        notificationHelper.dismiss()
    }
}
