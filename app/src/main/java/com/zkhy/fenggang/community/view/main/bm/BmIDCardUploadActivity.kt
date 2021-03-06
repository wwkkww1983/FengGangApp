package com.zkhy.fenggang.community.view.main.bm

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.view.View
import com.jiangyy.easydialog.CommonDialog
import com.sinothk.comm.utils.StringUtil
import com.sinothk.comm.utils.ToastUtil
import com.sinothk.dialog.loading.LoadingDialog
import com.sinothk.helper.image.compress.ImageCompress
import com.sinothk.rxretrofit.RxRetrofit
import com.sinothk.rxretrofit.param.RetrofitParam
import com.zkhy.fenggang.community.R
import com.zkhy.fenggang.community.model.api.AllApi
import com.zkhy.fenggang.community.model.api.BaseData
import com.zkhy.fenggang.community.model.api.ServerConfig
import com.zkhy.fenggang.community.model.bean.AttachmentEntity
import com.zkhy.library.base.activity.TitleBarBaseActivity
import kotlinx.android.synthetic.main.activity_bm_handle_id_card.*
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


open class BmIDCardUploadActivity : TitleBarBaseActivity(), View.OnClickListener {

    private var isUpSide = true
    var uploadSuccess = false

    var img1Success = false
    var img2Success = false

    var img1Id = ""
    var img2Id = ""

    var img1Type = 1
    var img2Type = 2

    var id = ""
    private var tip = ""
    private var bizType = ""
    private var contentType = IDCard

    override fun getLayout(): Int = R.layout.activity_bm_handle_id_card

    private fun showView(contentType: Int) {
        when (contentType) {
            IDCard -> { // 身份证
                userID_1.visibility = View.VISIBLE
                userID_1.setImageResource(R.drawable.user_id_1)

                userID_2.visibility = View.VISIBLE
                userID_2.setImageResource(R.drawable.user_id_2)
            }
            RegisterBook -> { // 户口簿
                userID_1.visibility = View.VISIBLE
                userID_1.setImageResource(R.drawable.take_photo_comm)

                userID_2.visibility = View.VISIBLE
                userID_2.setImageResource(R.drawable.take_photo_comm)
            }

            Certificate -> {  // 子女鉴定
                userID_1.visibility = View.VISIBLE
                userID_1.setImageResource(R.drawable.take_photo_comm)

                userID_2.visibility = View.VISIBLE
                userID_2.setImageResource(R.drawable.take_photo_comm)
            }

            MarryBook -> {  // 结婚证
                userID_1.visibility = View.VISIBLE
                userID_1.setImageResource(R.drawable.take_photo_comm)

                userID_2.visibility = View.GONE
            }

            DivorceAgreement -> {  // 离婚协议
                userID_1.visibility = View.VISIBLE
                userID_1.setImageResource(R.drawable.take_photo_comm)

                userID_2.visibility = View.VISIBLE
                userID_2.setImageResource(R.drawable.take_photo_comm)
            }

            ChildrenProof -> {  // 婚前子女情况
                userID_1.visibility = View.VISIBLE
                userID_1.setImageResource(R.drawable.take_photo_comm)

                userID_2.visibility = View.VISIBLE
                userID_2.setImageResource(R.drawable.take_photo_comm)
            }

            SelfPhoto -> {  // 近期照片
                userID_1.visibility = View.VISIBLE
                userID_1.setImageResource(R.drawable.take_photo_comm)

                userID_2.visibility = View.GONE
            }

            BankCard -> {  // 银行卡照片
                userID_1.visibility = View.VISIBLE
                userID_1.setImageResource(R.drawable.take_photo_comm)

                userID_2.visibility = View.VISIBLE
                userID_2.setImageResource(R.drawable.take_photo_comm)
            }

            HouseBook -> { // 购房合同
                userID_1.visibility = View.VISIBLE
                userID_1.setImageResource(R.drawable.take_photo_comm)

                userID_2.visibility = View.VISIBLE
                userID_2.setImageResource(R.drawable.take_photo_comm)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        id = intent.getStringExtra("id")
        tip = intent.getStringExtra("tip")
        bizType = intent.getStringExtra("bizType")
        contentType = intent.getIntExtra("contentType", IDCard)

        setTitleBar(StringUtil.getNotNullValue(tip, "证件上传"))

        // 控制显示
        showView(contentType)

        setListener()

        delImg1Btn.setOnClickListener {
            dialog(img1Type, img1Id)
        }

        delImg2Btn.setOnClickListener {
            dialog(img2Type, img2Id)
        }
    }

    private fun setListener() {
        userID_1.setOnClickListener(this)
        userID_2.setOnClickListener(this)
        submitBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            userID_1 -> {
                isUpSide = true
                openSysCamera()
            }

            userID_2 -> {
                isUpSide = false
                openSysCamera()
            }

            submitBtn -> {
                if (uploadSuccess) {
                    if (contentType == IDCard) {
                        if (!(img1Success && img2Success)) {
                            ToastUtil.show("身份证必须上传正反面")
                            return
                        }
                    }

                    val intent = intent
                    intent.putExtra("id", id)
                    setResult(200, intent)
                }

                finish()
            }
        }
    }

    /**
     * 打开系统相机
     */
    internal var file: File? = null
    private var imgUriOri: Uri? = null

    private fun openSysCamera() {

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        try {
            file = createOriImageFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (file != null) {

            imgUriOri = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                Uri.fromFile(file)
            } else {
                FileProvider.getUriForFile(this, "$packageName.fileProvider", file!!)
            }
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imgUriOri)
            startActivityForResult(cameraIntent, 11)
        }
    }

    /**
     * 创建原图像保存的文件
     *
     * @return
     * @throws IOException
     */
    private var imgPathOri = ""

    @Throws(IOException::class)
    private fun createOriImageFile(): File {
        val imgNameOri = "HomePic_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val pictureDirOri = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).absolutePath + "/OriPicture")
        if (!pictureDirOri.exists()) {
            pictureDirOri.mkdirs()
        }
        val image = File.createTempFile(
            imgNameOri, /* prefix */
            ".jpg", /* suffix */
            pictureDirOri       /* directory */
        )
        imgPathOri = image.absolutePath
        return image
    }


//    internal var fileList = arrayOfNulls<File>(2)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 11 && resultCode == -1) {
            ImageCompress.execute(imgPathOri) { obj ->
                if (obj != null) {

                    val newPath: String = obj as String
                    val tempFile = File(newPath)

                    if (tempFile.length() > 0) {
                        if (isUpSide) {
                            userID_1.setImageBitmap(BitmapFactory.decodeFile(tempFile.path))
                        } else {
                            userID_2.setImageBitmap(BitmapFactory.decodeFile(tempFile.path))
                        }
                        upLoad(id, bizType, tempFile)
                    } else {
                        ToastUtil.show("压缩图片为空")
                    }
                } else {
                    ToastUtil.show("图片压缩失败")
                }
            }
        } else {
            ToastUtil.show("已取消操作")
        }
    }

    private fun upLoad(bizId: String, bizType: String, file: File) {
        // 传单文件文件和键值对
        RxRetrofit.init(ServerConfig.baseUrl, ServerConfig.getHeaderMap()).create(AllApi::class.java)
            .uploadFile(bizId, bizType, RetrofitParam.createFileParam("file", file))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Subscriber<BaseData<AttachmentEntity>>() {

                override fun onStart() {
                    super.onStart()
                    LoadingDialog.show(this@BmIDCardUploadActivity, "正在提交")
                }

                override fun onNext(result: BaseData<AttachmentEntity>?) {
                    if (result != null) {
                        if (result.errcode == 0) {
                            if (result.data != null) {
                                if (isUpSide) {
                                    img1Success = true
                                    delImg1Btn.visibility = View.VISIBLE

                                    img1Id = result.data.id.toString()
                                } else {
                                    img2Success = true
                                    delImg2Btn.visibility = View.VISIBLE

                                    img2Id = result.data.id.toString()
                                }

                                uploadSuccess = true
                                id = result.data.bizId



                                return
                            } else {
                                ToastUtil.show("无文件信息")
                            }
                        } else {
                            ToastUtil.show(result.errmsg)
                        }
                    } else {
                        ToastUtil.show("提交失败")
                    }
                    uploadSuccess = false
                    if (isUpSide) {
                        delImg1Btn.visibility = View.GONE
                        img1Success = false
                    } else {
                        delImg2Btn.visibility = View.GONE
                        img2Success = false
                    }
                }

                override fun onCompleted() {
                    LoadingDialog.hidden()
                }

                override fun onError(e: Throwable?) {
                    LoadingDialog.hidden()
                    uploadSuccess = false
                    if (isUpSide) {
                        delImg1Btn.visibility = View.GONE
                        img1Success = false
                    } else {
                        delImg2Btn.visibility = View.GONE
                        img2Success = false
                    }

                    if (e != null) {
                        ToastUtil.show("文件上传失败")
                    }
                }
            })
    }

    /*
    * Dialog对话框提示用户删除操作
    * position为删除图片位置
    */
    protected fun dialog(type: Int, imgId: String) {

        if (StringUtil.isEmpty(imgId)) {
            ToastUtil.show("文件不存在")
            return
        }

        CommonDialog.Builder(this)
            .setTitle("提示")
            .setMessage("确认移除已添加的图片吗？")
            .setPositiveButton("确认") {

                delFileById(type, imgId)

            }.setNegativeButton("取消") {

            }.show()
    }

    private fun delFileById(type: Int, imgId: String) {

        val ids: Array<String> = arrayOf(imgId)

        // 传单文件文件和键值对
        RxRetrofit.init(ServerConfig.baseUrl, ServerConfig.getHeaderMap()).create(AllApi::class.java)
            .delFileSysById(ids)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Subscriber<BaseData<Boolean>>() {

                override fun onStart() {
                    super.onStart()
                    LoadingDialog.show(this@BmIDCardUploadActivity, "正在删除")
                }

                override fun onNext(result: BaseData<Boolean>?) {
                    if (result != null) {
                        if (result.errcode == 0) {
                            if (result.data != null) {

                                if (type == img1Type) {
                                    delImg1Btn.visibility = View.GONE
                                    img1Success = false

                                    if (contentType== IDCard) {
                                        userID_1.setImageResource(R.drawable.user_id_1)
                                    }else{
                                        userID_1.setImageResource(R.drawable.take_photo_comm)
                                    }
                                } else {
                                    delImg2Btn.visibility = View.GONE
                                    img2Success = false

                                    if (contentType== IDCard) {
                                        userID_2.setImageResource(R.drawable.user_id_2)
                                    }else{
                                        userID_2.setImageResource(R.drawable.take_photo_comm)
                                    }
                                }

                                return
                            } else {
                                ToastUtil.show(result.errmsg)
                            }

                        } else {
                            ToastUtil.show(result.errmsg)
                        }
                    } else {
                        ToastUtil.show("删除失败")
                    }
                }

                override fun onCompleted() {
                    LoadingDialog.hidden()
                }

                override fun onError(e: Throwable?) {
                    LoadingDialog.hidden()
                    if (e != null) {
                        ToastUtil.show("文件删除失败")
                    }
                }
            })
    }

    companion object {
        const val IDCard = 1
        const val RegisterBook = 2 // 户口簿
        const val Certificate = 3 // 子女鉴定
        const val MarryBook = 4 // 结婚证书
        const val DivorceAgreement = 5 // 离婚证书
        const val ChildrenProof = 6

        const val SelfPhoto = 7
        const val BankCard = 8

        const val HouseBook = 9 // 房产证或购房合同
    }
}
