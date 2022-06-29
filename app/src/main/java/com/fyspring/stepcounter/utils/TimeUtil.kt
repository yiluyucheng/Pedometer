package com.fyspring.stepcounter.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.telephony.TelephonyManager
import android.text.TextUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by fySpring
 * Date: 2020/4/21
 * To do:
 */
class TimeUtil {

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy年MM月dd日")
        private val mCalendar = Calendar.getInstance()
        private val weekStrings = arrayOf("日", "一", "二", "三", "四", "五", "六")
        private val rWeekStrings = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")


        /**
         * 改变日期格式
         * @param date  2017年02月09日
         * @return 2017-02-09
         */
        fun changeFormatDate(date: String): String? {
            val dFormat = SimpleDateFormat("yyyy-MM-dd")
            var curDate: String? = null
            try {
                val dt = dateFormat.parse(date)
                curDate = dFormat.format(dt)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            return curDate
        }

        /**
         * 判断日期是否与当前日期差7天
         * @param date  2020年04月22日
         * @return true
         */
        fun isDateOutDate(date: String): Boolean {
            try {
                if ((Date().time - dateFormat.parse(date).time) > 7 * 24 * 60 * 60 * 1000) return true

            } catch (e: ParseException) {
                e.printStackTrace()
            }

            return false
        }

        /**
         * 获取手机唯一标示
         *
         * @param context
         * @return
         */
        fun getPhoneInfor(context: Context): String? {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return tm.deviceId
        }
        fun getPhoneSign(context: Context): String? {
            val deviceId = StringBuilder()
            // 渠道标志
            deviceId.append("a")
            try {
                //IMEI（imei）
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val imei = tm.deviceId
                if (!TextUtils.isEmpty(imei)) {
                    deviceId.append("imei")
                    deviceId.append(imei)
                    return deviceId.toString()
                }
                //序列号（sn）
                val sn = tm.simSerialNumber
                if (!TextUtils.isEmpty(sn)) {
                    deviceId.append("sn")
                    deviceId.append(sn)
                    return deviceId.toString()
                }
                //如果上面都没有， 则生成一个id：随机码
                val uuid = getUUID(context)
                if (!TextUtils.isEmpty(uuid)) {
                    deviceId.append("id")
                    deviceId.append(uuid)
                    return deviceId.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                deviceId.append("id").append(getUUID(context))
            }
            return deviceId.toString()
        }

        /**
         * 得到全局唯一UUID
         */
        private var uuid: String? = null
        fun getUUID(context: Context): String? {
            val mShare: SharedPreferences =context. getSharedPreferences("uuid", MODE_PRIVATE)
            if (mShare != null) {
                uuid = mShare.getString("uuid", "")
            }
            if (TextUtils.isEmpty(uuid)) {
                uuid = UUID.randomUUID().toString()
                mShare.edit().putString("uuid", uuid).commit()
            }
            return uuid
        }
        /**
         * 返回当前的时间
         * @return  今天 09:48
         */
        private fun getCurTime(): String {
            val dFormat = SimpleDateFormat("HH:mm")
            return "today " + dFormat.format(System.currentTimeMillis())
        }

        /**
         * 获取运动记录是周几，今天则返回具体时间，其他则返回具体周几
         * @param dateStr
         * @return
         */
        fun getWeekStr(dateStr: String): String {

            val todayStr = dateFormat.format(mCalendar.time)

            if (todayStr == dateStr) {
                return getCurTime()
            }

            val preCalendar = Calendar.getInstance()
            preCalendar.add(Calendar.DATE, -1)
            val yesterdayStr = dateFormat.format(preCalendar.time)
            if (yesterdayStr == dateStr) {
                return "昨天"
            }

            var w = 0
            try {
                val date = dateFormat.parse(dateStr)
                val calendar = Calendar.getInstance()
                calendar.time = date
                w = calendar.get(Calendar.DAY_OF_WEEK) - 1
                if (w < 0) {
                    w = 0
                }

            } catch (e: ParseException) {
                e.printStackTrace()
            }

            return rWeekStrings[w]
        }


        /**
         * 获取是几号
         *
         * @return dd
         */
        public fun getCurrentDay(): Int {
            return mCalendar.get(Calendar.DATE)
        }

        /**
         * 获取当前的日期
         *
         * @return yyyy年MM月dd日
         */
        fun getCurrentDate(): String {
            return dateFormat.format(mCalendar.time)
        }


        /**
         * 根据date列表获取day列表
         *
         * @param dateList
         * @return
         */
        fun dateListToDayList(dateList: List<String>): List<Int> {
            val calendar = Calendar.getInstance()
            val dayList: MutableList<Int> = ArrayList()
            for (date in dateList) {
                try {
                    calendar.setTime(dateFormat.parse(date))
                    val day = calendar.get(Calendar.DATE)
                    dayList.add(day)
                } catch (e: ParseException) {
                    e.printStackTrace()
                }

            }
            return dayList
        }


        /**
         * 根据当前日期获取以含当天的前一周日期
         * @return [2017年02月21日, 2017年02月22日, 2017年02月23日, 2017年02月24日, 2017年02月25日, 2017年02月26日, 2017年02月27日]
         */
        fun getBeforeDateListByNow(): List<String> {
            val weekList: MutableList<String> = ArrayList()

            for (i in -6..0) {
                //以周日为一周的第一天
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DATE, i)
                val date = dateFormat.format(calendar.time)
                weekList.add(date)
            }
            return weekList
        }

        /**
         * 判断当前日期是周几
         * @param curDate
         * @return
         */
        fun getCurWeekDay(curDate: String): String {
            var w = 0
            try {
                val date = dateFormat.parse(curDate)
                val calendar = Calendar.getInstance()
                calendar.time = date
                w = calendar.get(Calendar.DAY_OF_WEEK) - 1
                if (w < 0) {
                    w = 0
                }

            } catch (e: ParseException) {
                e.printStackTrace()
            }

            return weekStrings[w]
        }
    }

}