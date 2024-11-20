package com.tonytangandroid.wood

import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object FormatUtils {

    fun formatTextHighlight(text: String, searchKey: String): CharSequence {
        if (TextUtil.isNullOrWhiteSpace(text) || TextUtil.isNullOrWhiteSpace(searchKey)) {
            return text
        } else {
            val startIndexes = indexOf(text, searchKey)
            val spannableString = SpannableString(text)
            applyHighlightSpan(spannableString, startIndexes, searchKey.length)
            return spannableString
        }
    }

    fun indexOf(charSequence: CharSequence, criteria: String): List<Int> {
        val criteriaLowerCase = criteria.lowercase(Locale.getDefault())
        val text = charSequence.toString().lowercase(Locale.getDefault())

        val startPositions: MutableList<Int> = ArrayList()
        var index = text.indexOf(criteriaLowerCase)
        while (index >= 0) {
            startPositions.add(index)
            index = text.indexOf(criteriaLowerCase, index + 1)
        }
        return startPositions
    }

    fun applyHighlightSpan(
        spannableString: Spannable, indexes: List<Int>, length: Int
    ) {
        for (position in indexes) {
            spannableString.setSpan(
                HighlightSpan(
                    WoodColorUtil.HIGHLIGHT_BACKGROUND_COLOR,
                    WoodColorUtil.HIGHLIGHT_TEXT_COLOR,
                    WoodColorUtil.HIGHLIGHT_UNDERLINE
                ),
                position,
                position + length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    fun getShareText(transaction: Leaf): CharSequence {
        return transaction.body().orEmpty()
    }

    fun highlightSearchKeyword(textView: TextView, searchKey: String?): List<Int> {
        val body = textView.text
        if (body is Spannable) {
            // remove old HighlightSpans
            val spansToRemove = body.getSpans(0, body.length - 1, HighlightSpan::class.java)
            for (span in spansToRemove) {
                body.removeSpan(span)
            }
            // add spans only if searchKey size is > 0
            if (!searchKey.isNullOrEmpty()) {
                // get indices of new search
                val startIndexes = indexOf(body.toString(), searchKey)
                // add spans
                applyHighlightSpan(body, startIndexes, searchKey.length)
                return startIndexes
            }
        }

        return ArrayList(0)
    }

    fun timeDesc(nowInMilliseconds: Long): String {
        val date = Date(nowInMilliseconds)
        val formatter = SimpleDateFormat("HH:mm:ss.SSS MMM-dd", Locale.US)
        return formatter.format(date)
    }
}
