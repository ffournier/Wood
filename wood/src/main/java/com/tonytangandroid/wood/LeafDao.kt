package com.tonytangandroid.wood

import android.util.Log
import androidx.annotation.IntRange
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Flowable

@Dao
internal abstract class LeafDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertTransaction(leaf: Leaf?): Long

    @Delete
    abstract fun deleteTransactions(vararg leaves: Leaf?): Int

    @Query(value = "DELETE FROM Leaf WHERE createAt < :beforeDate")
    abstract fun deleteTransactionsBefore(beforeDate: Long): Int

    @Query(value = "DELETE FROM Leaf")
    abstract fun clearAll(): Int

    @get:Query(value = "SELECT * FROM Leaf ORDER BY id DESC")
    abstract val allTransactions: DataSource.Factory<Int, Leaf>?

    @Query(value = "SELECT * FROM Leaf WHERE id = :id")
    abstract fun getTransactionsWithId(id: Long): Flowable<Leaf?>?

    fun getAllTransactionsWith(
        key: String, @IntRange(from = 2, to = 7) priority: Int
    ): DataSource.Factory<Int, Leaf>? {
        val endWildCard = "$key%"
        val doubleSideWildCard = "%$key%"
        return getAllTransactionsIncludeRequestResponse(endWildCard, doubleSideWildCard, priority)
    }

    @Query(value = "SELECT id,createAt,length,priority,body FROM Leaf WHERE (tag LIKE :endWildCard OR body LIKE :doubleWildCard) AND priority >= :priority ORDER BY id DESC")
    abstract fun getAllTransactionsIncludeRequestResponse(
        endWildCard: String?, doubleWildCard: String?, priority: Int
    ): DataSource.Factory<Int, Leaf>?

    companion object {
        const val SEARCH_DEFAULT: Int = Log.VERBOSE
    }
}
