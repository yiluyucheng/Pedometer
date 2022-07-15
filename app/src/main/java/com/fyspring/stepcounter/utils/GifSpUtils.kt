package com.fyspring.stepcounter.utils

import android.content.Context

class GifSpUtils {

    companion object {

        private val SharedPreferences = "GifSpUtils"
        val start_time = "start_time"
        val start_text = "start_text"
        val start_step = "start_step"
        fun removeValue(context: Context, key: String) {
            val sp = context.getSharedPreferences(SharedPreferences, Context.MODE_PRIVATE).edit()
            sp.remove(key)
            sp.commit()
        }
        fun putValue(context: Context, key: String, value: Long) {
            val sp = context.getSharedPreferences(SharedPreferences, Context.MODE_PRIVATE).edit()
            sp.putLong(key, value)
            sp.commit()
        }
        fun putValue(context: Context, key: String, value: Int) {
            val sp = context.getSharedPreferences(SharedPreferences, Context.MODE_PRIVATE).edit()
            sp.putInt(key, value)
            sp.commit()
        }


        fun putValue(context: Context, key: String?, value: Boolean) {
            val sp = context.getSharedPreferences(SharedPreferences, Context.MODE_PRIVATE).edit()
            sp.putBoolean(key, value)
            sp.commit()
        }

        fun putValue(context: Context, key: String, value: String?) {
            val sp = context.getSharedPreferences(SharedPreferences, Context.MODE_PRIVATE).edit()
            sp.putString(key, value)
            sp.commit()
        }
        fun getValue(context: Context, key: String, defValue: Long): Long {
            val sp = context.getSharedPreferences(SharedPreferences, Context.MODE_PRIVATE)
            return sp.getLong(key, defValue)
        }
        fun getValue(context: Context, key: String, defValue: Int): Int {
            val sp = context.getSharedPreferences(SharedPreferences, Context.MODE_PRIVATE)
            return sp.getInt(key, defValue)
        }

        fun getValue(context: Context, key: String, defValue: Boolean): Boolean {
            val sp = context.getSharedPreferences(SharedPreferences, Context.MODE_PRIVATE)
            return sp.getBoolean(key, defValue)
        }

        fun getValue(context: Context, key: String, defValue: String): String? {
            val sp = context.getSharedPreferences(SharedPreferences, Context.MODE_PRIVATE)
            return sp.getString(key, defValue)
        }
    }


}