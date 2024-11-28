package com.tonytangandroid.wood.sample

import android.app.Application
import com.tonytangandroid.wood.sample.HomeActivity.Companion.logInBackground
import com.tonytangandroid.wood.sample.WoodIntegrationUtil.initWood

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initWood(this)
        logInBackground()
    }
}
