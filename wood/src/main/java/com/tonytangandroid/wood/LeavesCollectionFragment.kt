package com.tonytangandroid.wood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tonytangandroid.wood.LeavesCollectionFragment.TransactionListWithSearchKeyModel
import com.tonytangandroid.wood.NotificationHelper.Companion.clearBuffer
import com.tonytangandroid.wood.WoodTree.Companion.autoScroll

class LeavesCollectionFragment : Fragment(), LeafAdapter.Listener, SearchView.OnQueryTextListener {
    private var adapter: LeafAdapter? = null
    private var listDiffUtil: ListDiffUtil? = null
    private lateinit var recyclerView: RecyclerView
    private var viewModel: LeafListViewModel? = null
    private var currentSubscription: LiveData<PagedList<Leaf>>? = null

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
                loadResults(event, viewModel?.getTransactions(event))

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
        loadResults(null, viewModel?.getTransactions(null))
    }

    private fun loadResults(searchKey: String?, pagedListLiveData: LiveData<PagedList<Leaf>>?) {
        currentSubscription?.takeIf { it.hasObservers() == true }?.run {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.clear) {
            viewModel?.clearAll()
            clearBuffer()
            return true
        } else if (item.itemId == R.id.browse_sql) {
            return true
        } else {
            return super.onOptionsItemSelected(item)
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
