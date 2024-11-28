package com.tonytangandroid.wood

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class LeafListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wood_activity_leaf_list)
        val toolbar = findViewById<Toolbar>(R.id.wood_toolbar)
        setSupportActionBar(toolbar)
        toolbar.setSubtitle(getApplicationName())
    }

    private fun getApplicationName(): String {
        val applicationInfo = getApplicationInfo()
        val stringId = applicationInfo.labelRes
        return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else getString(stringId)
    }
}
