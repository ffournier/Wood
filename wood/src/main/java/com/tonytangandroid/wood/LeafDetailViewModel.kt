package com.tonytangandroid.wood

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.tonytangandroid.wood.WoodDatabase.Companion.getInstance
import io.reactivex.Flowable

class LeafDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val leafDao: LeafDao = getInstance(application).leafDao()

    fun getTransactionWithId(id: Long): Flowable<Leaf?>? {
        return leafDao.getTransactionsWithId(id)
    }
}
