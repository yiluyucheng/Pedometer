package com.fyspring.stepcounter.ui.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.*
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fyspring.stepcounter.HttpUtils
import com.fyspring.stepcounter.R
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
    SeekBarHint.OnSeekBarHintProgressChangeListener {
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

    /**
     * ????????????
     */
    private var timerTask: TimerTask? = null
    private var timer: Timer? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initData() {
        GifSpUtils.putValue(this, GifSpUtils.start_time, TimeUtil.getCurrTimers())
        mSeekBar = findViewById<View>(R.id.seekbar) as SeekBarHint
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
    private fun commSubmit() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Hint").setIcon(R.mipmap.ic_launcher)
            .setMessage("Are you sure to submit?")
         .setPositiveButton("Confirm") { _, _ -> // TODO Auto-generated method stub
             val beforeDateListByNow = TimeUtil.getBeforeDateListByNow()
             getYesData(beforeDateListByNow[6])
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

    override fun initListener() {
        calenderView!!.setOnBoaCalenderClickListener(object :
            BeforeOrAfterCalendarView.BoaCalenderClickListener {
            override fun onClickToRefresh(position: Int, curDate: String) {
                //???????????????????????????
                curSelDate = curDate
                //????????????????????????
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
                    //?????????????????????????????????????????????
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
         * ??????????????????????????????????????????
         */
        if (StepCountCheckUtil.isSupportStepCountSensor(this)) {
            getRecordList()
            is_support_tv.visibility = View.GONE
            Submit.isClickable=true
            setDatas()
            setupService()
        } else {
            movement_total_steps_tv.text = "0"
            is_support_tv!!.visibility = View.VISIBLE
            Submit.isClickable=false
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
                    Submit.isClickable=false
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
     * ??????????????????
     */
    private fun setupService() {
        val intent = Intent(this, StepService::class.java)
        isBind = bindService(intent, conn, Context.BIND_AUTO_CREATE)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) startForegroundService(intent)
        else startService(intent)
    }

    /**
     * ???????????????????????????application Service?????????????????????interface???
     * ??????????????????????????????Service ??? context.bindService()???????????????
     * ?????????????????????????????????????????????ServiceConnection????????????????????????????????????????????????
     */
    private val conn = object : ServiceConnection {
        /**
         * ???????????????Service???????????????????????????????????????Android?????????IBind?????????????????????????????????
         * @param name ?????????????????????Service????????????
         * @param service ????????????????????????IBind???????????????Service??????????????????
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
         * ??????Service???????????????????????????????????????????????????
         * ???????????????????????????Service??????????????????????????????Kill??????????????????
         * ????????????????????????Service????????????????????????????????????????????????????????? onServiceConnected()???
         * @param name ???????????????????????????
         */
        override fun onServiceDisconnected(name: ComponentName) {

        }
    }

    /**
     * ??????????????????
     */
    private fun setDatas() {
        val stepEntity = stepDataDao!!.getCurDataByDate(curSelDate)

        if (stepEntity != null) {
            val steps = stepEntity.steps?.let { Integer.parseInt(it) }
            //?????????????????????
            movement_total_steps_tv.text = steps.toString()
            //??????????????????
            movement_total_km_tv.text = steps?.let { countTotalKM(it) }
        } else {
            //?????????????????????
            movement_total_steps_tv.text = "0"
            //??????????????????
            movement_total_km_tv.text = "0"
        }

        //????????????
        val time = TimeUtil.getWeekStr(curSelDate)
        movement_total_km_time_tv.text = time
        movement_total_steps_time_tv.text = time

    }

    fun loaderZuiYou(activity: Activity?, id: String, date: String, step: Int, start: String, end: String, distance: String, pain: Int) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("ids", id)
            jsonObject.put("Step", step)
            jsonObject.put("Pain", pain)
            jsonObject.put("Date", date)
            jsonObject.put("Start", start)
            jsonObject.put("End", end)
            jsonObject.put("Distance", distance)
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
        HttpUtils.getClient().post(activity, "https://pedometer.ngrok.io/step/", entity, "application/json",
            object :
                AsyncHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<Header>, responseBody: ByteArray) {
                    try {
                        val result = JSONObject(String(responseBody))
                      //  is_succtext.text = Html.fromHtml(String(responseBody))
                        is_succtext.text =result.optString("feedback")
                        activity?.let {
                            GifSpUtils.putValue(it, GifSpUtils.start_text, result.optString("feedback"))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(statusCode: Int, headers: Array<Header>, responseBody: ByteArray, error: Throwable) {
                    error.printStackTrace()
                    is_succtext.text = "Unable to connect the server!"
                }
            })
    }

    private fun getYesData(time: String) {
        if (progressNum==0){
            Toast.makeText(
                this,
                "Please select a value between 1-100. 1: very good, do not feel pain; 100: extremely pain.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        //?????????????????????
        val stepEntity = stepDataDao!!.getCurDataByDate(time)
        if (stepEntity != null) {
            val steps = stepEntity.steps?.let { Integer.parseInt(it) }
            //?????????????????????
            val toString = steps.toString()
            val start_time = GifSpUtils.getValue(this, GifSpUtils.start_time, "")
            val start_step = GifSpUtils.getValue(this, GifSpUtils.start_step, 0)
            val step = steps?.minus(start_step)

            GifSpUtils.putValue(this, GifSpUtils.start_step, steps!!)
            val let = step?.let { countTotalKM(it) }
            //????????????
            loaderZuiYou(
                this,
                TimeUtil.getPhoneSign(this)!!,
                TimeUtil.changeFormatDate(time)!!,
                step!!,
                start_time!!,
                TimeUtil.getCurrTimers()!!,
                let!!,
                progressNum
            )
        } else {
            Toast.makeText(this, "Today's data is unavailable", Toast.LENGTH_SHORT).show()

        }

    }

    /**
     * ?????????????????????????????????????????????0.7???
     *
     * @param steps ??????????????????
     * @return
     */
    private fun countTotalKM(steps: Int): String {
        val totalMeters = steps * 0.7
        //????????????????????????
        return df.format(totalMeters / 1000)
    }

    /**
     * ??????????????????????????????
     */
    private fun getRecordList() {
        //???????????????
        stepDataDao = StepDataDao(this)
        stepEntityList.clear()
        stepEntityList.addAll(stepDataDao!!.getAllDatas())
        if (stepEntityList.size > 7) {
            //???????????????????????????????????????????????????7??????????????????????????????????????????????????????
            for (entity in stepEntityList) {
                if (TimeUtil.isDateOutDate(entity.curDate!!)) {
                    stepDataDao?.deleteCurData(entity.curDate!!)
                }
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            //?????????????????????Service???????????????
            ConstantData.MSG_FROM_SERVER ->
                //??????????????????????????????
                if (curSelDate == TimeUtil.getCurrentDate()) {
                    //??????????????????
                    val steps = msg.data.getInt("steps")
                    //???????????????
                    movement_total_steps_tv.text = steps.toString()
                    //??????????????????
                    movement_total_km_tv.text = countTotalKM(steps)
                }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        //????????????Service?????????????????????Service?????????
        if (isBind) this.unbindService(conn)
    }

    override fun onHintTextChanged(seekBarHint: SeekBarHint?, progress: Int): String {
        progressNum=progress
        poptext.text = "pain: $progress"
        return "p: $progress"
    }
}
