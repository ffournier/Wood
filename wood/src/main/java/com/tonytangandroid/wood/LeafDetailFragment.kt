package com.tonytangandroid.wood

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tonytangandroid.wood.FormatUtils.timeDesc
import com.tonytangandroid.wood.TextUtil.AsyncTextProvider
import com.tonytangandroid.wood.TextUtil.asyncSetText
import com.tonytangandroid.wood.TextUtil.isNullOrWhiteSpace
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.FlowableSubscribeProxy
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LeafDetailFragment : Fragment(), View.OnClickListener, AsyncTextProvider, TextWatcher {
    private val colorSpan =
        BackgroundColorSpan(WoodColorUtil.SEARCHED_HIGHLIGHT_BACKGROUND_COLOR)
    private var id: Long = 0
    private var searchKey: String? = null
    private var colorUtil: WoodColorUtil? = null
    private var currentSearchIndex = 0
    private var leaf: Leaf? = null
    private var searchIndexList: List<Int> = listOf(0)
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var search_bar: View
    private lateinit var et_key_word: EditText
    private lateinit var tv_search_count: TextView
    private lateinit var tv_body: AppCompatTextView
    private val searchDebounce = Debouncer<String>(400, object: Callback<String> {
        override fun onEmit(event: String) {
            onSearchKeyEmitted(event)
        }
    })
    private lateinit var nested_scroll_view: NestedScrollView
    private lateinit var floating_action_button: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        id = arguments?.getLong(ARG_ID) ?: 0L
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        colorUtil = WoodColorUtil.getInstance(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.wood_fragment_leaf_detail, container, false)
        bindView(rootView)
        return rootView
    }

    private fun bindView(rootView: View) {
        tv_body = rootView.findViewById<AppCompatTextView>(R.id.wood_details_body)
        nested_scroll_view =
            rootView.findViewById<NestedScrollView>(R.id.wood_details_scroll_parent)
        floating_action_button =
            rootView.findViewById<FloatingActionButton>(R.id.wood_details_search_fab)
        search_bar = rootView.findViewById<View>(R.id.wood_details_search_bar)
        val searchBarPrev = rootView.findViewById<View>(R.id.wood_details_search_prev)
        val searchBarNext = rootView.findViewById<View>(R.id.wood_details_search_next)
        val searchBarClose = rootView.findViewById<View>(R.id.wood_details_search_close)
        et_key_word = rootView.findViewById<EditText>(R.id.wood_details_search)
        tv_search_count = rootView.findViewById<TextView>(R.id.wood_details_search_count)
        floating_action_button.setOnClickListener(this)
        searchBarPrev.setOnClickListener(this)
        searchBarNext.setOnClickListener(this)
        searchBarClose.setOnClickListener(this)
        et_key_word.addTextChangedListener(this)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        searchDebounce.consume(s.toString())
    }

    override fun afterTextChanged(s: Editable?) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe()
    }

    private fun observe() {
        val viewModel = create()
        viewModel
            .getTransactionWithId(id)?.run {
                subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`<FlowableSubscribeProxy<Leaf?>?>(
                    AutoDispose.autoDisposable<Leaf?>(
                        AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())
                    )
                )
                .subscribe(Consumer { transaction: Leaf? -> transaction?.let { transactionUpdated(it) }})
            }
    }

    private fun create(): LeafDetailViewModel {
        return ViewModelProvider(requireActivity()).get<LeafDetailViewModel>(LeafDetailViewModel::class.java)
    }

    private fun transactionUpdated(transaction: Leaf) {
        this.leaf = transaction
        populateUI()
    }

    private fun populateUI() {
        val l = leaf
        val color = if (l != null) {
            colorUtil?.getTransactionColor(l) ?: ResourcesCompat.getColor(resources, R.color.wood_status_default, floating_action_button.context.theme)
        } else {
            resources.getColor(R.color.wood_status_default)
        }
        floating_action_button.backgroundTintList = colorStateList(color)
        search_bar.setBackgroundColor(color)
        et_key_word.setHint(R.string.wood_search_hint)
        populateBody()
    }

    private fun colorStateList(color: Int): ColorStateList {
        return ColorStateList(arrayOf<IntArray?>(intArrayOf(0)), intArrayOf(color))
    }

    private fun onSearchKeyEmitted(searchKey: String) {
        this.searchKey = searchKey
        updateUI()
    }

    private fun updateUI() {
        searchIndexList = FormatUtils.highlightSearchKeyword(tv_body, searchKey)
        updateSearch(1, searchKey)
    }

    private fun populateBody() {
        val actionBar: ActionBar = checkNotNull((requireActivity() as AppCompatActivity).supportActionBar)
        actionBar.title = leaf?.tag.orEmpty()
        actionBar.subtitle = leaf?.let { timeDesc(it) }.orEmpty()
        asyncSetText(executor, this)
    }

    private fun timeDesc(leaf: Leaf): String {
        return timeDesc(leaf.createAt)
    }

    override fun getText(): CharSequence {
        val body: CharSequence = leaf?.body().orEmpty()
        if (isNullOrWhiteSpace(body) || isNullOrWhiteSpace(searchKey)) {
            return body
        } else {
            val indexList: List<Int> = searchKey?.let { FormatUtils.indexOf(body, it) } ?: listOf()
            val spannableBody = SpannableString(body)
            FormatUtils.applyHighlightSpan(spannableBody, indexList, searchKey?.length ?: 0)
            searchIndexList = indexList
            return spannableBody
        }
    }

    override fun getTextView(): AppCompatTextView {
        return tv_body
    }

    private fun updateSearch(targetIndex: Int, searchKey: String?) {
        var targetIndex = targetIndex
        val list = searchIndexList
        val size = list.size
        targetIndex = adjustTargetIndex(targetIndex, size)
        tv_search_count.text = "$targetIndex/$size"
        (tv_body.getText() as Spannable).removeSpan(colorSpan)
        if (targetIndex > 0) {
            updateSpan(targetIndex, searchKey.orEmpty(), list)
        }
        currentSearchIndex = targetIndex
    }

    private fun updateSpan(targetIndex: Int, searchKey: String, list: List<Int>) {
        val begin: Int = list[targetIndex - 1]
        val end = begin + searchKey.length
        val lineNumber = tv_body.layout.getLineForOffset(begin)
        (tv_body.getText() as Spannable).setSpan(
            colorSpan,
            begin,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val scrollToY = tv_body.layout.getLineTop(lineNumber)
        nested_scroll_view.scrollTo(0, scrollToY)
    }

    private fun adjustTargetIndex(targetIndex: Int, size: Int): Int {
        var targetIndex = targetIndex
        if (size == 0) {
            targetIndex = 0
        } else {
            if (targetIndex > size) {
                targetIndex = 1
            } else if (targetIndex <= 0) {
                targetIndex = size
            }
        }
        return targetIndex
    }

    private fun showKeyboard() {
        et_key_word.requestFocus()
        val imm = inputMethodManager()
        imm?.showSoftInput(et_key_word, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun inputMethodManager(): InputMethodManager? {
        return requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    }

    private fun hideKeyboard() {
        val imm = inputMethodManager()
        imm?.hideSoftInputFromWindow(et_key_word.windowToken, 0)
    }

    @Suppress("deprecation")
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (!isVisibleToUser) {
            hideKeyboard()
        }
        super.setUserVisibleHint(isVisibleToUser)
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.wood_details_search_fab) {
            showSearch()
        } else if (id == R.id.wood_details_search_close) {
            clearSearch()
        } else if (id == R.id.wood_details_search_prev) {
            updateSearch(currentSearchIndex - 1, searchKey)
        } else if (id == R.id.wood_details_search_next) {
            updateSearch(currentSearchIndex + 1, searchKey)
        }
    }

    private fun clearSearch() {
        if (isNullOrWhiteSpace(searchKey)) {
            floating_action_button.show()
            search_bar.visibility = View.GONE
            nested_scroll_view.setPadding(0, 0, 0, nested_scroll_view.bottom)
            hideKeyboard()
        } else {
            et_key_word.setText("")
        }
    }

    private fun showSearch() {
        floating_action_button.hide()
        search_bar.visibility = View.VISIBLE
        nested_scroll_view.setPadding(
            0,
            getResources().getDimensionPixelSize(R.dimen.wood_search_bar_height),
            0,
            nested_scroll_view.bottom
        )
        showKeyboard()
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.wood_details_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.share_text) {
            leaf?.let { share(FormatUtils.getShareText(it)) }
            return true
        } else if (itemId == R.id.copy) {
            leaf?.let { copy(FormatUtils.getShareText(it)) }
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    private fun copy(text: CharSequence?) {
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("log", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Copied", Toast.LENGTH_SHORT).show()
    }

    private fun share(content: CharSequence?) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, content)
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, null))
    }

    companion object {
        private const val ARG_ID = "arg_id"
        fun newInstance(id: Long): LeafDetailFragment {
            val fragment = LeafDetailFragment()
            val b = Bundle()
            b.putLong(ARG_ID, id)
            fragment.setArguments(b)
            return fragment
        }
    }
}
