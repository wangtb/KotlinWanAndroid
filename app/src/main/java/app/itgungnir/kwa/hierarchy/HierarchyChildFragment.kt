package app.itgungnir.kwa.hierarchy

import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import app.itgungnir.kwa.R
import app.itgungnir.kwa.common.widget.easy_adapter.EasyAdapter
import app.itgungnir.kwa.common.widget.easy_adapter.bind
import app.itgungnir.kwa.common.widget.list_footer.ListFooter
import app.itgungnir.kwa.common.widget.status_view.StatusView
import kotlinx.android.synthetic.main.fragment_hierarchy_child.*
import my.itgungnir.rxmvvm.core.mvvm.LazyFragment
import my.itgungnir.rxmvvm.core.mvvm.buildFragmentViewModel
import org.jetbrains.anko.support.v4.toast

class HierarchyChildFragment : LazyFragment() {

    private var listAdapter: EasyAdapter? = null

    private var footer: ListFooter? = null

    private val viewModel by lazy {
        buildFragmentViewModel(
            fragment = this,
            viewModelClass = HierarchyChildViewModel::class.java
        )
    }

    private var flag: Int = -1

    companion object {
        fun newInstance(flag: Int) = HierarchyChildFragment().apply { this.flag = flag }
    }

    override fun layoutId(): Int = R.layout.fragment_hierarchy_child

    override fun initComponent() {
        hierarchyPage.apply {
            // Refresh Layout
            refreshLayout().setOnRefreshListener {
                viewModel.getArticles(flag)
            }
            // Status View
            statusView().addDelegate(StatusView.Status.SUCCEED, R.layout.status_view_list) {
                val list = it.findViewById<RecyclerView>(R.id.list)
                // Recycler View
                listAdapter = list.bind(delegate = HierarchyChildDelegate())
                // Footer
                footer = ListFooter.Builder()
                    .bindTo(list)
                    .doOnLoadMore {
                        if (!refreshLayout().isRefreshing) {
                            viewModel.loadMoreArticles(flag)
                        }
                    }
                    .build()
            }.addDelegate(StatusView.Status.EMPTY, R.layout.status_view_list_empty) {
                it.findViewById<TextView>(R.id.tip).text = "本栏目暂无文章~"
            }
        }
    }

    override fun onLazyLoad() {
        viewModel.getArticles(flag)
    }

    override fun observeVM() {

        viewModel.pick(HierarchyChildState::refreshing)
            .observe(this, Observer { refreshing ->
                refreshing?.a?.let {
                    hierarchyPage.refreshLayout().isRefreshing = it
                }
            })

        viewModel.pick(HierarchyChildState::items, HierarchyChildState::hasMore)
            .observe(this, Observer { states ->
                states?.let {
                    when (it.a.isNotEmpty()) {
                        true -> hierarchyPage.statusView().succeed { listAdapter?.update(states.a) }
                        else -> hierarchyPage.statusView().empty { }
                    }
                    footer?.onLoadSucceed(it.b)
                }
            })

        viewModel.pick(HierarchyChildState::loading)
            .observe(this, Observer { loading ->
                if (loading?.a == true) {
                    footer?.onLoading()
                }
            })

        viewModel.pick(HierarchyChildState::error)
            .observe(this, Observer { error ->
                error?.a?.message?.let {
                    toast(it)
                    footer?.onLoadFailed()
                }
            })
    }
}