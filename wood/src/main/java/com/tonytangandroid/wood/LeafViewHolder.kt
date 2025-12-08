package com.tonytangandroid.wood

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tonytangandroid.wood.FormatUtils.timeDesc
import com.tonytangandroid.wood.WoodColorUtil.Companion.getInstance
import java.util.Locale
import javax.inject.Provider

internal class LeafViewHolder(
    itemView: View,
    private val context: Context,
    private val searchKey: Provider<String?>,
    private val listener: LeafAdapter.Listener
) : RecyclerView.ViewHolder(itemView) {

    private val tvTime: TextView = itemView.findViewById<TextView>(R.id.tv_time)
    private val tvTag: TextView = itemView.findViewById<TextView>(R.id.tv_tag)
    private val tvId: TextView = itemView.findViewById<TextView>(R.id.tv_id)
    private val tvBody: TextView = itemView.findViewById<TextView>(R.id.tv_body)

    fun bind(transaction: Leaf) {
        tvTag.text = transaction.tag
        tvTime.text = timeDesc(transaction.createAt)
        tvBody.text = getHighlightedText(transaction.body.toString())
        tvId.text = String.format(Locale.getDefault(), "%d", transaction.id)
        tvTag.setTextColor(getInstance(context).getTransactionColor(transaction))
        itemView.setOnClickListener(View.OnClickListener { v: View? ->
            listener.onTransactionClicked(
                transaction
            )
        })
    }

    private fun getHighlightedText(text: String): CharSequence {
        return FormatUtils.formatTextHighlight(text, searchKey.get().orEmpty())
    }
}
