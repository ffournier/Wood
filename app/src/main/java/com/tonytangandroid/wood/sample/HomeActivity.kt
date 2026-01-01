package com.tonytangandroid.wood.sample

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.tonytangandroid.wood.Wood.addAppShortcut
import com.tonytangandroid.wood.Wood.getLaunchIntent
import timber.log.Timber

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        findViewById<View>(R.id.btn_generate_log).setOnClickListener { _: View -> generateTimberLog() }
        findViewById<View>(R.id.btn_test_extreme_log).setOnClickListener { _: View -> keepGenerateLog() }
        findViewById<View>(R.id.launch_wood_directly).setOnClickListener { _: View -> launchWoodDirectly() }
        addAppShortcut(this)
    }

    private fun keepGenerateLog() {
        startActivity(Intent(this, GeneratingLogActivity::class.java))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.sample_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_menu_github) {
            val url = "https://github.com/TonyTangAndroid/Wood"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = url.toUri()
            startActivity(i)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun launchWoodDirectly() {
        startActivity(getLaunchIntent(this))
    }

    private fun generateTimberLog() {
        Timber.v("This is a VERBOSE message.")
        Timber.d("This is an DEBUG message.")
        Timber.i("This is an INFO message.")
        Timber.w("This is an WARNING message.")
        Timber.e("This is an ERROR message.")
        logError()
    }

    private fun logError() {
        try {
            val shortSrc = ""
            val substring = shortSrc.substring(10)
            Timber.v("$substring$substring$substring")
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    companion object {
        
        fun logInBackground() {
            Thread { Timber.i("This is an INFO message triggered in background thread.") }.start()
        }
    }
}
