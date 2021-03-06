package com.zkhy.fenggang.community.view.main.bm

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.sinothk.comm.utils.IntentUtil
import com.sinothk.comm.utils.NetUtil
import com.sinothk.comm.utils.StringUtil
import com.sinothk.comm.utils.ToastUtil
import com.sinothk.dialog.loading.LoadingDialog
import com.zkhy.fenggang.community.R
import com.zkhy.fenggang.community.model.api.BaseData
import com.zkhy.fenggang.community.model.bean.*
import com.zkhy.fenggang.community.model.cache.DataCache
import com.zkhy.fenggang.community.presenter.BizHandlePresenter
import com.zkhy.fenggang.community.view.comm.AddressStreetListActivity
import com.zkhy.library.base.activity.TitleBarBaseActivity
import com.zkhy.library.mvp.AndroidExt2View
import com.zkhy.library.utils.StringUtilExt
import kotlinx.android.synthetic.main.activity_child_cxjs_submit.*

/**
 * <pre>
 *  创建:  梁玉涛 2019/5/24 on 14:09
 *  项目: WuMinAndroid
 *  描述: 诚信计生证明
 *  更新:
 * <pre>
 */
class ChildCxjsSubmit : TitleBarBaseActivity(), View.OnClickListener, AndroidExt2View {
    override fun load2Complete(resultData: Any?) {
    }

    var id = ""
    var areaCode = ""

    private var presenter: BizHandlePresenter? = null

    override fun getLayout(): Int = R.layout.activity_child_cxjs_submit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitleBar("诚信计生证明")
        initView()
        setListener()

        presenter = BizHandlePresenter(this)
    }

    private fun initView() {
        val currUser = DataCache.getUserInfo()
        areaCode = currUser.areaCode
        userNameValueEt.setText(StringUtil.getNotNullValue(currUser.name, currUser.account))
        userPhoneValueEt.setText(StringUtil.getNotNullValue(currUser.phone, "暂无"))
        userIDValueEt.setText(StringUtil.getNotNullValue(currUser.idcard, "暂无"))

        areaValueTv.text = StringUtil.getNotNullValue(currUser.areaFullName, "暂无")
        userAddressValueEt.setText(StringUtil.getNotNullValue(currUser.addrDetail, "暂无"))
    }

    private fun setListener() {
        userAreaItem.setOnClickListener(this)
        submitBtn.setOnClickListener(this)

        // 文件部分
        file01Item.setOnClickListener(this)
        file02Item.setOnClickListener(this)
        file03Item.setOnClickListener(this)
    }

    private val REQUEST_CODE_ADDRESS: Int = 201

    private val file01Code: Int = 301
    private val file02Code: Int = 302
    private val file03Code: Int = 303

    override fun onClick(v: View?) {
        when (v) {
            userAreaItem -> {
                IntentUtil.openActivity(this, AddressStreetListActivity::class.java)
                    .putStringExtra("id", "525629318659833921")
                    .requestCode(REQUEST_CODE_ADDRESS)
                    .start()
            }
            submitBtn -> {
                submit()
            }

            file01Item -> {
                val fileEntity = BmFileEntity()
                fileEntity.bizId = id
                fileEntity.titleStr = "身份证或户口薄"
                fileEntity.tip = "请上传身份证正、反面或户口薄户主页、本人页图片"
                fileEntity.bizType = BizType.WU_BM_CX_JS_ZM2
                fileEntity.fileList = if (file01List == null) {// 本次已选附件或重新提交已存附件回显
                    ArrayList()
                } else {
                    file01List
                }

                IntentUtil.openActivity(
                    this@ChildCxjsSubmit, FileUploadCommActivity::class.java
                )
                    .putSerializableExtra("fileEntity", fileEntity)
                    .requestCode(file01Code)
                    .start()
            }
            file02Item -> {
                val fileEntity = BmFileEntity()
                fileEntity.bizId = id
                fileEntity.titleStr = "计划生育情况证明"
                fileEntity.tip = "请上传村(社区)出具的计划生育情况证明图片"
                fileEntity.bizType = BizType.WU_BM_CX_JS_ZM3
                fileEntity.fileList = if (file02List == null) {// 本次已选附件或重新提交已存附件回显
                    ArrayList()
                } else {
                    file02List
                }

                IntentUtil.openActivity(
                    this@ChildCxjsSubmit, FileUploadCommActivity::class.java
                )
                    .putSerializableExtra("fileEntity", fileEntity)
                    .requestCode(file02Code)
                    .start()
            }
            file03Item -> {
                val fileEntity = BmFileEntity()
                fileEntity.bizId = id
                fileEntity.titleStr = "结婚证"
                fileEntity.tip = "请上传结婚证信息图片"
                fileEntity.bizType = BizType.WU_BM_CX_JS_ZM4
                fileEntity.fileList = if (file03List == null) {// 本次已选附件或重新提交已存附件回显
                    ArrayList()
                } else {
                    file03List
                }

                IntentUtil.openActivity(
                    this@ChildCxjsSubmit, FileUploadCommActivity::class.java
                )
                    .putSerializableExtra("fileEntity", fileEntity)
                    .requestCode(file03Code)
                    .start()
            }
        }
    }

    private var file01List: java.util.ArrayList<ImgSelectEntity>? = null
    private var file02List: java.util.ArrayList<ImgSelectEntity>? = null
    private var file03List: java.util.ArrayList<ImgSelectEntity>? = null

    private var file01IsUpload = false
    private var file02IsUpload = false
    private var file03IsUpload = false

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null && resultCode == 200 && REQUEST_CODE_ADDRESS == requestCode) {
            val streetName = data.getStringExtra("streetName")
            val areaName = data.getStringExtra("areaName")
            areaValueTv.text = "$streetName$areaName"
            areaCode = data.getStringExtra("areaCode")
            return
        }

        if (resultCode != 200 || data == null) {
            return
        }

        when (requestCode) {
            file01Code -> {
                // 附件处理
                file01List = data.getSerializableExtra("fileList") as ArrayList<ImgSelectEntity>?
                if (file01List != null && file01List!!.size > 0) {
                    id = file01List!![0].bizId
                    file01ValueTv.text = "已上传"
                } else {
                    file01ValueTv.text = "未上传"
                }
            }
            file02Code -> {
                // 附件处理
                file02List = data.getSerializableExtra("fileList") as ArrayList<ImgSelectEntity>?
                if (file02List != null && file02List!!.size > 0) {
                    id = file02List!![0].bizId
                    file02ValueTv.text = "已上传"
                } else {
                    file02ValueTv.text = "未上传"
                }
            }
            file03Code -> {
                // 附件处理
                file03List = data.getSerializableExtra("fileList") as ArrayList<ImgSelectEntity>?
                if (file03List != null && file03List!!.size > 0) {
                    id = file03List!![0].bizId
                    file03ValueTv.text = "已上传"
                } else {
                    file03ValueTv.text = "未上传"
                }
            }
        }
    }

    private fun submit() {
        val vo = PersonWorkVo()
        val userName = userNameValueEt.text.toString()
        val userID = userIDValueEt.text.toString()
        val userPhone = userPhoneValueEt.text.toString()
        val userAddress = userAddressValueEt.text.toString()

        if (StringUtil.isEmpty(userName)) {
            ToastUtil.show("请填写申请人姓名")
            return
        } else {
            vo.name = userName
        }

        if (StringUtil.isEmpty(userID)) {
            ToastUtil.show("请填写申请人身份证号")
            return
        } else {
            if (!StringUtilExt.isIDNumber(userID)) {
                ToastUtil.show("身份证号格式不正确")
                return
            }
            vo.idcard = userID
        }

        if (StringUtil.isEmpty(userPhone)) {
            ToastUtil.show("请填写申请人电话")
            return
        } else {
            if (!StringUtilExt.isMobile(userPhone) && !StringUtilExt.isPhone(userPhone)) {
                ToastUtil.show("电话格式不正确")
                return
            }
            vo.phone = userPhone
        }

        if (StringUtil.isEmpty(areaCode)) {
            ToastUtil.show("请填写社区信息")
            return
        } else {
            vo.areaCode = areaCode
        }

        if (StringUtil.isEmpty(userAddress)) {
            ToastUtil.show("请填写详细地址")
            return
        } else {
            vo.addrDetail = userAddress
        }

        // 非填写部分
        vo.id = id

        vo.flowId = BizType.FlowType.FLOW_20
        vo.handleType = 1

        val userInfo: WmUser = DataCache.getUserInfo()
        vo.applyUserId = userInfo.userId
        vo.apprUserId = userInfo.userId

        vo.cityId = 851
        vo.county = 5222
        vo.provinceId = 54
        vo.town = 123

        // ==========================================
        if (!NetUtil.isAvailable(this)) {
            ToastUtil.show("网络不可用")
            return
        }

        if (TextUtils.isEmpty(id)) {
            ToastUtil.show("请先上传资料")
            return
        }

        if (file01List == null || file01List!!.size == 0) {
            ToastUtil.show("请上传身份证或户口薄图片")
            return
        }

        if (file02List == null || file02List!!.size == 0) {
            ToastUtil.show("请上传计划生育情况证明图片")
            return
        }

        if (file03List == null || file03List!!.size == 0) {
            ToastUtil.show("请上传结婚证信息图片")
            return
        }

        presenter?.submitAllowance(vo)
    }

    override fun loadingShow() {
        LoadingDialog.show(this, "正在提交")
    }

    override fun loadingDismiss() {
        LoadingDialog.hidden()
    }

    override fun showTip(msg: String?) {
        ToastUtil.show(msg)
    }

    override fun loadComplete(resultData: Any?) {
        val result = resultData as BaseData<PersonWorkEntity>
        if (result.errcode == 0) {
            showTip("申请已提交")
            finish()
        } else {
            showTip(result.errmsg)
        }
    }
}