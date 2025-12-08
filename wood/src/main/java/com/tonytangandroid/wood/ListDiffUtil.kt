package com.tonytangandroid.wood

import androidx.recyclerview.widget.DiffUtil

internal class ListDiffUtil : DiffUtil.ItemCallback<Leaf>() {
    private var mSearchKey: String? = null

    fun setSearchKey(searchKey: String?) {
        this.mSearchKey = searchKey
    }

    override fun areItemsTheSame(oldItem: Leaf, newItem: Leaf): Boolean {
        // might not work always due to async nature of Adapter fails in very rare race conditions but
        // increases pref.
        newItem.searchKey = mSearchKey
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Leaf, newItem: Leaf): Boolean {
        // both will non null. because of areItemsTheSame logic only non nulls come here
        // comparing only items shown in the list
        return areEqual(oldItem.searchKey, newItem.searchKey)
    }

    companion object {
        private fun areEqual(oldItem: Any?, newItem: Any?): Boolean {
            if (oldItem == null && newItem == null) {
                // both are null
                return true
            } else if (oldItem == null || newItem == null) {
                // only one is null => return false
                return false
            }
            return oldItem == newItem
        }
    }
}
