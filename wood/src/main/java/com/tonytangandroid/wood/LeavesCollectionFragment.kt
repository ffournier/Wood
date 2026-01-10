package com.tonytangandroid.wood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tonytangandroid.wood.NotificationHelper.Companion.clearBuffer
import com.tonytangandroid.wood.WoodTree.Companion.autoScroll

class LeavesCollectionFragment : Fragment(), LeafAdapter.Listener, SearchView.OnQueryTextListener {
    private var adapter: LeafAdapter? = null
    private var listDiffUtil: ListDiffUtil? = null
    private lateinit var recyclerView: RecyclerView
    private var viewModel: LeafListViewModel? = null
    private var currentSubscription: LiveData<PagedList<Leaf>>? = null
    private var filterLevels: MutableList<Int> = LogLevel.entries.map { it.level }.toMutableList()

    // 100 mills delay. batch all changes in 100 mills and emit last item at the end of 100 mills
    private val transactionSampler = Sampler<TransactionListWithSearchKeyModel?>(
        100,
        object : Callback<TransactionListWithSearchKeyModel?> {
            override fun onEmit(event: TransactionListWithSearchKeyModel?) {
                listDiffUtil?.setSearchKey(event?.searchKey)
                adapter?.setSearchKey(event?.searchKey)?.submitList(event?.pagedList)
            }
        })

    // 300 mills delay min. Max no limit
    private val searchDebounce = Debouncer<String?>(
        300,
        object : Callback<String?> {
            override fun onEmit(event: String?) {
                loadResults(event, filterLevels, viewModel?.getTransactions(event, filterLevels))

            }
        })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.wood_fragment_leaves_collection, container, false)
        bindView(rootView)
        return rootView
    }

    private fun bindView(rootView: View) {
        recyclerView = rootView.findViewById<RecyclerView>(R.id.wood_transaction_list)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = LeafAdapter(requireContext(), ListDiffUtil().also { listDiffUtil = it }, this)
        recyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recyclerView.setAdapter(adapter)

        viewModel = ViewModelProvider(this).get<LeafListViewModel>(LeafListViewModel::class.java)
        loadResults(null, filterLevels, viewModel?.getTransactions(null, filterLevels))
    }

    private fun loadResults(searchKey: String?, filter: List<Int>, pagedListLiveData: LiveData<PagedList<Leaf>>?) {
        currentSubscription?.takeIf { it.hasObservers() }?.run {
            removeObservers(this@LeavesCollectionFragment)
        }
        currentSubscription = pagedListLiveData
        currentSubscription?.observe(
            getViewLifecycleOwner(),
            Observer { list: PagedList<Leaf>? -> consume(list, searchKey) })
    }

    private fun consume(transactionPagedList: PagedList<Leaf>?, searchKey: String?) {
        transactionSampler.consume(
            TransactionListWithSearchKeyModel(searchKey, transactionPagedList)
        )
    }


    override fun onTransactionClicked(transaction: Leaf) {
        LeafDetailsActivity.start(requireContext(), transaction.id, transaction.priority)
    }

    override fun onItemsInserted(firstInsertedItemPosition: Int) {
        if (autoScroll(requireContext())) {
            recyclerView.smoothScrollToPosition(firstInsertedItemPosition)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.wood_list_menu, menu)
        val searchMenuItem = menu.findItem(R.id.search)
        (searchMenuItem.actionView as? SearchView)?.run {
            setOnQueryTextListener(this@LeavesCollectionFragment)
            setIconifiedByDefault(true)
        }
    }

    private fun showFilter() {
        val filters = LogLevel.entries.map {
            it.level in filterLevels
        }.toBooleanArray()
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.wood_filter)
            .setMultiChoiceItems(LogLevel.entries.map {
                it.name
            }.toTypedArray(), filters) { p0, which, isChecked ->
                filters[which] = isChecked
            }
            .setOnDismissListener {
                filterLevels = filters.mapIndexed { pos, value ->
                    LogLevel.entries[pos].takeIf { value }?.level
                }.mapNotNull { it }.toMutableList()
                loadResults(adapter?.get(), filterLevels,  viewModel?.getTransactions(null, filterLevels))
            }
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filter -> {
                showFilter()
                return true
            }
            R.id.clear -> {
                viewModel?.clearAll()
                clearBuffer()
                return true
            }
            R.id.browse_sql -> {
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        searchDebounce.consume(newText)
        return true
    }

    internal class TransactionListWithSearchKeyModel(
        val searchKey: String?,
        val pagedList: PagedList<Leaf>?
    )
}
