package com.fyspring.stepcounter.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.*
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fyspring.stepcounter.HttpUtils
import com.fyspring.stepcounter.OnSuccesListener
import com.fyspring.stepcounter.R
import com.fyspring.stepcounter.WindowUtils
import com.fyspring.stepcounter.base.BaseActivity
import com.fyspring.stepcounter.bean.StepEntity
import com.fyspring.stepcounter.constant.ConstantData
import com.fyspring.stepcounter.dao.StepDataDao
import com.fyspring.stepcounter.service.StepService
import com.fyspring.stepcounter.ui.view.BeforeOrAfterCalendarView
import com.fyspring.stepcounter.ui.view.SeekBarHint
import com.fyspring.stepcounter.utils.GifSpUtils
import com.fyspring.stepcounter.utils.StepCountCheckUtil
import com.fyspring.stepcounter.utils.TimeUtil
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.entity.ByteArrayEntity
import cz.msebera.android.httpclient.message.BasicHeader
import cz.msebera.android.httpclient.protocol.HTTP
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : BaseActivity(), Handler.Callback,
    SeekBarHint.OnSeekBarHintProgressChangeListener, OnSuccesListener {
    private var calenderView: BeforeOrAfterCalendarView? = null
    private var curSelDate: String = ""
    var progressNum: Int = 50
    private val df = DecimalFormat("#.##")
    private val stepEntityList: MutableList<StepEntity> = ArrayList()
    private var stepDataDao: StepDataDao? = null
    private var isBind = false
    private val mGetReplyMessenger = Messenger(Handler(this))
    private var messenger: Messenger? = null
    private var mSeekBar: SeekBarHint? = null
    private var tijiaoTime: String = ""
    private var height: String? = null
    private var weight: String? = null
    private var age: String? = null
    private var gender: String? = null

    /**
     * 定时任务
     */
    private var timerTask: TimerTask? = null
    private var timer: Timer? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    private fun initView() {
        GifSpUtils.putValue(this, GifSpUtils.start_time, TimeUtil.getCurrTimers())
        curSelDate = TimeUtil.getCurrentDate()
        calenderView = BeforeOrAfterCalendarView(this)
        movement_records_calender_ll!!.addView(calenderView)
        requestPermission()
        val start_text = GifSpUtils.getValue(this, GifSpUtils.start_text, "")
        is_succtext.text = start_text
        Submit.setOnClickListener {
            commSubmit()
        }
    }

    override fun initData() {
        mSeekBar = findViewById<View>(R.id.seekbar) as SeekBarHint
        val frist_app = GifSpUtils.getValue(this, GifSpUtils.frist_app, true)
        if (frist_app) {
            WindowUtils.showQuXiaoDialog(this, this)
        } else {
            this.height =GifSpUtils.getValue(this, GifSpUtils.height, "")
            this.weight =GifSpUtils.getValue(this, GifSpUtils.weight, "")
            this.age = GifSpUtils.getValue(this, GifSpUtils.age, "")
            this.gender =GifSpUtils.getValue(this, GifSpUtils.gender, "")
            initView()
            initListener()
        }
    }

    private fun commSubmit() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Hint").setIcon(R.mipmap.ic_launcher)
            .setMessage("Are you sure to submit?")
            .setPositiveButton("Confirm") { _, _ -> // TODO Auto-generated method stub
                val beforeDateListByNow = TimeUtil.getBeforeDateListByNow()
                tijiaoTime = beforeDateListByNow[6]
                getYesData(tijiaoTime!!)
            }.setNeutralButton("Cancel") { _, _ -> // TODO Auto-generated method stub
            }
        builder.create().show()
    }

    override fun onStart() {
        // TODO Auto-generated method stub
        super.onStart()
        mSeekBar!!.setLeftText(0)
        mSeekBar!!.setProgressText(-1)
        mSeekBar!!.setRightText(100)
        mSeekBar!!.setOnProgressChangeListener(this)
        mSeekBar!!.post {
            mSeekBar!!.initShow()
        }
    }

    private fun initListener() {
        calenderView!!.setOnBoaCalenderClickListener(object :
            BeforeOrAfterCalendarView.BoaCalenderClickListener {
            override fun onClickToRefresh(position: Int, curDate: String) {
                //获取当前选中的时间
                curSelDate = curDate
                //根据日期去取数据
                setDatas()
            }
        })
    }


    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    1
                )
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACTIVITY_RECOGNITION
                    )
                ) {
                    //此处需要弹窗通知用户去设置权限
                    Toast.makeText(
                        this,
                        "Please allow access to fitness information, otherwise we will not be able to count steps for you~",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                startStepService()
            }
        } else {
            startStepService()
        }
    }


    private fun startStepService() {
        /**
         * 这里判断当前设备是否支持计步
         */
        if (StepCountCheckUtil.isSupportStepCountSensor(this)) {
            getRecordList()
            is_support_tv.visibility = View.GONE
            Submit.isClickable = true
            setDatas()
            setupService()
        } else {
            movement_total_steps_tv.text = "0"
            is_support_tv!!.visibility = View.VISIBLE
            Submit.isClickable = false
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    startStepService()
                } else {
                    Submit.isClickable = false
                    Toast.makeText(
                        this,
                        "Please allow access to fitness information, otherwise we will not be able to count steps for you~",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * 开启计步服务
     */
    private fun setupService() {
        val intent = Intent(this, StepService::class.java)
        isBind = bindService(intent, conn, Context.BIND_AUTO_CREATE)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) startForegroundService(intent)
        else startService(intent)
    }

    /**
     * 用于查询应用服务（application Service）的状态的一种interface，
     * 更详细的信息可以参考Service 和 context.bindService()中的描述，
     * 和许多来自系统的回调方式一样，ServiceConnection的方法都是进程的主线程中调用的。
     */
    private val conn = object : ServiceConnection {
        /**
         * 在建立起于Service的连接时会调用该方法，目前Android是通过IBind机制实现与服务的连接。
         * @param name 实际所连接到的Service组件名称
         * @param service 服务的通信信道的IBind，可以通过Service访问对应服务
         */
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            timerTask = object : TimerTask() {
                override fun run() {
                    try {
                        messenger = Messenger(service)
                        val msg = Message.obtain(null, ConstantData.MSG_FROM_CLIENT)
                        msg.replyTo = mGetReplyMessenger
                        messenger!!.send(msg)
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                    }
                }
            }
            timer = Timer()
            timer!!.schedule(timerTask, 0, 500)
        }

        /**
         * 当与Service之间的连接丢失的时候会调用该方法，
         * 这种情况经常发生在Service所在的进程崩溃或者被Kill的时候调用，
         * 此方法不会移除与Service的连接，当服务重新启动的时候仍然会调用 onServiceConnected()。
         * @param name 丢失连接的组件名称
         */
        override fun onServiceDisconnected(name: ComponentName) {

        }
    }

    /**
     * 设置记录数据
     */
    private fun setDatas() {
        val stepEntity = stepDataDao!!.getCurDataByDate(curSelDate)

        if (stepEntity != null) {
            val steps = stepEntity.steps?.let { Integer.parseInt(it) }
            //获取全局的步数
            movement_total_steps_tv.text = steps.toString()
            //计算总公里数
            movement_total_km_tv.text = steps?.let { countTotalKM(it, height!!.toInt()) }

            val beforeDateListByNow = TimeUtil.getBeforeDateListByNow()
            tijiaoTime = beforeDateListByNow[6]
            val start_step = GifSpUtils.getValue(this, tijiaoTime!!, 0)
            val step = steps?.minus(start_step)
            tijiao_total_steps_tv.text = step.toString()

        } else {
            //获取全局的步数
            movement_total_steps_tv.text = "0"
            //计算总公里数
            movement_total_km_tv.text = "0"
            tijiao_total_steps_tv.text = "0"
        }

        //设置时间
        val time = TimeUtil.getWeekStr(curSelDate)
        movement_total_km_time_tv.text = time
        movement_total_steps_time_tv.text = time
        tijiao_total_steps_time_tv.text = time
    }
    fun hasNetWork(context: Context): Boolean {
        val activeNetworkInfo = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivity = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity != null) {
            val info: NetworkInfo? = connectivity.activeNetworkInfo
            if (info != null && info.isConnected()) {
                if (info.getState() === NetworkInfo.State.CONNECTED) {
                    return true
                }
            }
        }
        return false
    }
    fun loaderZuiYou(
        activity: Activity?,
        id: String,
        date: String,
        step: Int,
        start: String,
        end: String,
        distance: String,
        pain: Int,
        tijiaoTime: String
    ) {
        if (!hasNetWork(this)) {
            Toast.makeText(
                activity,
                "Unable to connect the server! Please check your network.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        val jsonObject = JSONObject()
        try {
            jsonObject.put("ids", id)
            jsonObject.put("Step", step)
            jsonObject.put("Pain", pain)
            jsonObject.put("Date", date)
            jsonObject.put("Start", start)
            jsonObject.put("End", end)
            jsonObject.put("Distance", distance)
            jsonObject.put("Height", height)
            jsonObject.put("Weight", weight)
            jsonObject.put("Sex", gender)
            jsonObject.put("Age", age)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        var entity: ByteArrayEntity? = null
        try {
            entity = ByteArrayEntity(jsonObject.toString().toByteArray(charset("UTF-8")))
            entity.contentType = BasicHeader(HTTP.CONTENT_TYPE, "application/json")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        HttpUtils.getClient()
            .post(activity, "https://pedometer.ngrok.io/step/", entity, "application/json",
                object :
                    AsyncHttpResponseHandler() {
                    override fun onSuccess(
                        statusCode: Int,
                        headers: Array<Header>,
                        responseBody: ByteArray
                    ) {
                        try {
                            val result = JSONObject(String(responseBody))
                            val jsonObject = result.optJSONObject("data")
                            //  is_succtext.text = Html.fromHtml(String(responseBody))
                            is_succtext.text = jsonObject.optString("feedback")
                            activity?.let {
                                GifSpUtils.putValue(
                                    it,
                                    GifSpUtils.start_text,
                                    jsonObject.optString("feedback")
                                )
                                val value = GifSpUtils.getValue(it, tijiaoTime, 0)
                                val mainstep = value + step
                                GifSpUtils.putValue(it, tijiaoTime, mainstep!!)
                                tijiao_total_steps_tv.text = "0"
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                        statusCode: Int,
                        headers: Array<Header>,
                        responseBody: ByteArray,
                        error: Throwable
                    ) {
                        error.printStackTrace()
                        is_succtext.text = "Unable to connect the server!"
                    }
                })
    }

    private fun getYesData(tijiaoTime: String) {
        if (progressNum == 0) {
            Toast.makeText(
                this,
                "Please select a value between 1-100. 1: very good, do not feel pain; 100: extremely pain.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        //查询昨天的数据
        val stepEntity = stepDataDao!!.getCurDataByDate(tijiaoTime)
        if (stepEntity != null) {
            val steps = stepEntity.steps?.let { Integer.parseInt(it) }
            //获取全局的步数
            val toString = steps.toString()
            val start_time = GifSpUtils.getValue(this, GifSpUtils.start_time, "")
            val start_step = GifSpUtils.getValue(this, tijiaoTime, 0)
            val step = steps?.minus(start_step)

            val let = step?.let { countTotalKM(it, height!!.toInt()) }
            //上传数据
            loaderZuiYou(
                this,
                TimeUtil.getPhoneSign(this)!!,
                TimeUtil.changeFormatDate(tijiaoTime)!!,
                step!!,
                start_time!!,
                TimeUtil.getCurrTimers()!!,
                let!!,
                progressNum, tijiaoTime
            )
        } else {
            Toast.makeText(this, "Today's data is unavailable", Toast.LENGTH_SHORT).show()

        }

    }

    /**
     * 简易计算公里数，假设一步大约有0.7米
     *
     * @param steps 用户当前步数
     * @return
     */
    private fun countTotalKM(steps: Int, height: Int): String {
        val totalMeters = steps * height * 0.004
        //保留两位有效数字
        return df.format(totalMeters / 1000)
    }

    /**
     * 获取全部运动历史纪录
     */
    private fun getRecordList() {
        //获取数据库
        stepDataDao = StepDataDao(this)
        stepEntityList.clear()
        stepEntityList.addAll(stepDataDao!!.getAllDatas())
        if (stepEntityList.size > 7) {
            //在这里获取历史记录条数，当条数达到7条以上时，就开始删除第七天之前的数据
            for (entity in stepEntityList) {
                if (TimeUtil.isDateOutDate(entity.curDate!!)) {
                    stepDataDao?.deleteCurData(entity.curDate!!)
                }
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            //这里用来获取到Service发来的数据
            ConstantData.MSG_FROM_SERVER ->
                //如果是今天则更新数据
                if (curSelDate == TimeUtil.getCurrentDate()) {
                    //记录运动步数
                    val steps = msg.data.getInt("steps")
                    //设置的步数
                    movement_total_steps_tv.text = steps.toString()
                    //计算总公里数
                    movement_total_km_tv.text = countTotalKM(steps, height!!.toInt())


                    val beforeDateListByNow = TimeUtil.getBeforeDateListByNow()
                    tijiaoTime = beforeDateListByNow[6]
                    val start_step = GifSpUtils.getValue(this, tijiaoTime!!, 0)
                    val step = steps?.minus(start_step)
                    tijiao_total_steps_tv.text = step.toString()
                }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        //记得解绑Service，不然多次绑定Service会异常
        if (isBind) this.unbindService(conn)
    }

    override fun onHintTextChanged(seekBarHint: SeekBarHint?, progress: Int): String {
        progressNum = progress
        poptext.text = "pain: $progress"
        return "p: $progress"
    }

    override fun onSucces(height: String?, weight: String?, age: String?, gender: String?) {
        GifSpUtils.putValue(this, GifSpUtils.frist_app, false)
        GifSpUtils.putValue(this, GifSpUtils.height, height)
        GifSpUtils.putValue(this, GifSpUtils.weight, weight)
        GifSpUtils.putValue(this, GifSpUtils.age, age)
        GifSpUtils.putValue(this, GifSpUtils.gender, gender)
        this.height = height
        this.weight = weight
        this.age = age
        this.gender = gender
        initView()
        initListener()
    }
}
