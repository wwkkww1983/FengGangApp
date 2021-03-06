package com.zkhy.fenggang.community.view.main.dj

import android.os.Bundle
import android.view.View
import com.sinothk.comm.utils.IntentUtil
import com.sinothk.comm.utils.NetUtil
import com.sinothk.comm.utils.ToastUtil
import com.sinothk.view.status.statusViews.StatusView
import com.sinothk.widget.loadingRecyclerView.listeners.ItemClickCallBack
import com.zkhy.fenggang.community.R
import com.zkhy.fenggang.comm.base.StatusViewRecycleViewBaseActivity
import com.zkhy.fenggang.community.model.api.BaseData
import com.zkhy.fenggang.community.model.api.CommReq
import com.zkhy.fenggang.community.model.api.PageData
import com.zkhy.fenggang.community.model.api.PageReq
import com.zkhy.fenggang.community.model.bean.ThreeMeetEntity
import com.zkhy.fenggang.community.model.cache.DataCache
import com.zkhy.fenggang.community.presenter.MeetClassPresenter
import com.zkhy.fenggang.community.view.main.dj.adapters.MeetClassListAdapter
import com.zkhy.library.mvp.AndroidExt2View
import com.zkhy.library.widget.TitleBarViewCreator
import kotlinx.android.synthetic.main.activity_meet_class_list.*

class MeetClassForAllActivity : StatusViewRecycleViewBaseActivity(), AndroidExt2View {

    private var pageNo: Int = 0
    private var adapter: MeetClassListAdapter? = null
    private var presenter: MeetClassPresenter? = null

    override fun getTitleBarView(): View = TitleBarViewCreator.createTitleLC(this, "会议")

    override fun getContentLayoutId(): Int = R.layout.activity_meet_class_list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initRecycleLinearView(loadingRecyclerView)

        adapter = MeetClassListAdapter(this, ArrayList(), true)
        loadingRecyclerView.adapter = adapter
        adapter!!.setClickCallBack(ItemClickCallBack<ThreeMeetEntity> { _, entity ->
            if (entity == null) {
                return@ItemClickCallBack
            }
            IntentUtil.openActivity(this@MeetClassForAllActivity, MeetingDetailsActivity::class.java)
                .putStringExtra("id", entity.id.toString())
                .start()
        })
        // ================================
        presenter = MeetClassPresenter(this)
        refreshData()
    }

    override fun refreshData() {
        loadType = LoadType.REFRESH
        loadData(1)
    }

    private fun loadData(pageNo: Int) {

        if (!NetUtil.isAvailable(this)) {
            ToastUtil.show(R.string.net_error)
            return
        }

        val userId: String = DataCache.getUserId()

        val pageParam = PageReq<CommReq>()
        pageParam.pageNo = pageNo
        pageParam.pageSize = 20

        val comm = CommReq()
        comm.meetStatus = -1
        comm.orgId = -1
        comm.userId = userId

        pageParam.data = comm
        presenter!!.loadMeetForAllList(pageParam)
    }

    override fun loadMoreData() {
        loadType = LoadType.LOAD_MORE
        loadData(pageNo)
    }

    override fun loadingShow() {
        if (loadType == LoadType.REFRESH) {
            StatusView.showLoading("正在获取数据...")
        }
    }

    override fun loadingDismiss() {
    }

    override fun showTip(msg: String?) {
        // 设置：通用
        stopLoading(loadingRecyclerView, loadType)
        if (loadType == LoadType.REFRESH) {
            StatusView.showError(msg)
        } else {
            ToastUtil.show(msg)
        }
    }

    override fun loadComplete(resultData: Any?) {
        // 设置：通用
        stopLoading(loadingRecyclerView, loadType)

        val result: BaseData<PageData<ThreeMeetEntity>> = resultData as BaseData<PageData<ThreeMeetEntity>>

        if (result.errcode == 0) {
            if (result.data == null || result.data.list == null || result.data.list.size == 0) {
                showTip("暂无数据")
            } else {
                // 页
                val pageData = result.data
                // 数据
                val listData = pageData.list as ArrayList<ThreeMeetEntity>

                if (loadType == LoadType.REFRESH) {
                    adapter?.setData(listData)
                } else {
                    adapter?.addData(listData)
                }
                StatusView.showContent()

                // 设置：通用
                pageNo = pageData.nextPage
                if (pageData.isHasNextPage) {
                    loadingRecyclerView.setLoadingMoreEnabled(true)
                } else {
                    loadingRecyclerView.setLoadingMoreEnabled(false)
                }
            }
        } else {
            showTip(result.errmsg)
        }
    }

    override fun load2Complete(resultData: Any?) {
    }
}
