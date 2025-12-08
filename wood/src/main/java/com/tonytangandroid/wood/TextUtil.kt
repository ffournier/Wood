package com.tonytangandroid.wood

import android.os.Build
import android.text.PrecomputedText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.tonytangandroid.wood.TextUtil.AsyncTextProvider
import java.lang.Exception
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import java.util.concurrent.Executor

internal object TextUtil {
    /**
     * Pref Matters
     *
     *
     * PrecomputedText is not yet in support library, But still this is left because the callable
     * which is formatting Json, Xml will now be done in background thread
     */
    fun asyncSetText(bgExecutor: Executor, asyncTextProvider: AsyncTextProvider?) {
        val asyncTextProviderReference: Reference<AsyncTextProvider?> =
            WeakReference<AsyncTextProvider?>(asyncTextProvider)

        bgExecutor.execute(Runnable {
            try {
                 asyncTextProviderReference.get()?.run {
                     // get text from background
                     val longString = getText()
                     // pre-compute Text before setting on text view. so UI thread can be free from
                     // calculating text paint
                     var updateText: CharSequence?
                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                         val params = getTextView().textMetricsParams
                         updateText = PrecomputedText.create(longString, params)
                     } else {
                         updateText = longString
                     }
                     val updateTextFinal = updateText

                     getTextView().post(Runnable {
                             asyncTextProviderReference.get()?.run {
                                 val textView: TextView = getTextView()
                                 textView.setText(updateTextFinal, TextView.BufferType.SPANNABLE)
                             }
                         })
                 }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    fun isNullOrWhiteSpace(text: CharSequence?): Boolean {
        return text == null || text.isEmpty() || text.toString().trim { it <= ' ' }.isEmpty()
    }

    interface AsyncTextProvider {
        fun getText(): CharSequence

        fun getTextView(): AppCompatTextView
    }
}
