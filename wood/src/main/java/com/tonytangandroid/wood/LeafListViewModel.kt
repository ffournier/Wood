package com.tonytangandroid.wood

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.paging.PagedList.Config
import com.tonytangandroid.wood.LeafListViewModel.ClearAsyncTask
import com.tonytangandroid.wood.LeafListViewModel.DeleteAsyncTask
import com.tonytangandroid.wood.WoodDatabase.Companion.getInstance

@Suppress("deprecation")
class LeafListViewModel(application: Application) : AndroidViewModel(application) {
    private var leafDao: LeafDao = getInstance(application).leafDao()

    fun getTransactions(key: String?, filter: List<Int>): LiveData<PagedList<Leaf>>? {
        return if (key == null || key.trim { it <= ' ' }.isEmpty()) {
            leafDao.getAllTransactionsWith( filter)?.run {
                LivePagedListBuilder<Int, Leaf>(this, config).build()
            }
        } else {
            leafDao.getAllTransactionsWith(key, filter)?.run {
                LivePagedListBuilder<Int, Leaf>(this, config).build()
            }
        }
    }

    fun deleteItem(transaction: Leaf) {
        DeleteAsyncTask(leafDao).execute(transaction)
    }

    fun clearAll() {
        ClearAsyncTask(leafDao).execute()
    }

    private class DeleteAsyncTask(private val leafDao: LeafDao) : AsyncTask<Leaf, Void?, Int?>() {

        override fun doInBackground(vararg params: Leaf): Int {
            return leafDao.deleteTransactions(*params)
        }
    }

    private class ClearAsyncTask(private val leafDao: LeafDao) : AsyncTask<Leaf, Void?, Int?>() {

        override fun doInBackground(vararg params: Leaf): Int {
            return leafDao.clearAll()
        }
    }

    companion object {
        private val config: Config = Config.Builder()
            .setPageSize(15) // page size
            .setInitialLoadSizeHint(30) // items to fetch on first load
            .setPrefetchDistance(10) // trigger when to fetch a page
            .setEnablePlaceholders(true)
            .build()
    }
}
