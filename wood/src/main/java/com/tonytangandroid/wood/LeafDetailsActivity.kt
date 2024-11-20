package com.tonytangandroid.wood

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.tonytangandroid.wood.WoodColorUtil.Companion.getInstance

class LeafDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wood_activity_leaf_details)

        val id = intent.getLongExtra(ARG_TRANSACTION_ID, 0)
        val priority = intent.getIntExtra(ARG_PRIORITY, Log.VERBOSE)

        val colorUtil = getInstance(this)
        val appBarLayout = findViewById<AppBarLayout>(R.id.wood_details_appbar)
        appBarLayout.setBackgroundColor(colorUtil.getTransactionColor(priority))
        val toolbar = findViewById<Toolbar?>(R.id.wood_details_toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        bindFragment(LeafDetailFragment.newInstance(id))
    }

    private fun bindFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fl_fragment_holder, fragment)
            .commit()
    }

    companion object {
        private const val ARG_TRANSACTION_ID = "arg_transaction_id"
        private const val ARG_PRIORITY = "arg_priority"

        fun start(context: Context, id: Long, priority: Int) {
            val intent = Intent(context, LeafDetailsActivity::class.java)
            intent.putExtra(ARG_TRANSACTION_ID, id)
            intent.putExtra(ARG_PRIORITY, priority)
            context.startActivity(intent)
        }
    }
}
