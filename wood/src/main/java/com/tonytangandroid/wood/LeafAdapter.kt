package com.tonytangandroid.wood

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import javax.inject.Provider

internal class LeafAdapter(
    private val context: Context,
    listDiffUtil: ListDiffUtil,
    private val listener: Listener
) : PagedListAdapter<Leaf, LeafViewHolder>(listDiffUtil), Provider<String?> {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(this.context)

    private var searchKey: String? = null

    init {
        registerAdapterDataObserver(
            object : AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    // in the database inserts only occur at the top
                    listener.onItemsInserted(positionStart)
                }
            })
    }

    fun setSearchKey(searchKey: String?): LeafAdapter {
        this.searchKey = searchKey
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeafViewHolder {
        val itemView = layoutInflater.inflate(R.layout.wood_list_item_leaf, parent, false)
        return LeafViewHolder(itemView, context, this, listener)
    }

    override fun onBindViewHolder(viewHolder: LeafViewHolder, position: Int) {
        val transaction = getItem(position)
        if (transaction != null) {
            viewHolder.bind(transaction)
        }
    }

    override fun get(): String? {
        return searchKey
    }

    internal interface Listener {
        fun onTransactionClicked(leaf: Leaf)

        fun onItemsInserted(firstInsertedItemPosition: Int)
    }
}
