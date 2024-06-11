package com.bargarapp.testks

import android.content.Context

object SharedPreferencesHelper {
    fun writeToSP(value: String, keyValue: String, context: Context) {
        val sharedPreferences = context.getSharedPreferences("sPName_", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(keyValue, value)
        editor.apply()
    }

    fun readFromSP(keyValue: String, context: Context, defaultValue: String = "0.0.0.0"): String {
        val sharedPreferences = context.getSharedPreferences("sPName_", Context.MODE_PRIVATE)
        return sharedPreferences.getString(keyValue, defaultValue) ?: defaultValue
    }
}