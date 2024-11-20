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

    private val tv_time: TextView = itemView.findViewById<TextView>(R.id.tv_time)
    private val tv_tag: TextView = itemView.findViewById<TextView>(R.id.tv_tag)
    private val tv_id: TextView = itemView.findViewById<TextView>(R.id.tv_id)
    private val tv_body: TextView = itemView.findViewById<TextView>(R.id.tv_body)

    fun bind(transaction: Leaf) {
        tv_tag.text = transaction.tag
        tv_time.text = timeDesc(transaction.createAt)
        tv_body.text = getHighlightedText(transaction.body().toString())
        tv_id.text = String.format(Locale.getDefault(), "%d", transaction.id)
        tv_tag.setTextColor(getInstance(context).getTransactionColor(transaction))
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
