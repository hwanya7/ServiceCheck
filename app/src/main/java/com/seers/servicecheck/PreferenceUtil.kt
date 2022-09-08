package com.seers.servicecheck

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class PreferenceUtil(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("location_service", Context.MODE_PRIVATE)

    //String
    fun getString(key: String, defValue: String): String{
        return prefs.getString(key, defValue).toString()
    }

    fun setString(key: String, stringValue: String){
        prefs.edit().putString(key, stringValue).apply()
    }


    //Boolean
    fun getBoolean(key: String, defValue: Boolean): Boolean{
        return prefs.getBoolean(key, defValue)
    }

    fun setBoolean(key: String, booleanValue: Boolean){
        prefs.edit().putBoolean(key, booleanValue).apply()
    }


    //Float
    fun getFloat(key: String, defValue: Float): Float{
        return prefs.getFloat(key, defValue)
    }

    fun setFloat(key: String, floatValue: Float){
        prefs.edit().putFloat(key, floatValue).apply()
    }


    //Long
    fun getLong(key: String, defValue: Long): Long{
        return prefs.getLong(key, defValue)
    }

    fun setLong(key: String, longValue: Long){
        prefs.edit().putLong(key, longValue).apply()
    }


    //Int
    fun getInt(key: String, defValue: Int): Int{
        return prefs.getInt(key, defValue)
    }

    fun setInt(key: String, intValue: Int){
        prefs.edit().putInt(key, intValue).apply()
    }

    //object
    fun getObject(key: String, defValue: String): String{
        return prefs.getString(key, defValue).toString()
    }

    fun setObject(key: String, objValue: Any){
        var gson = Gson()
        var json = gson.toJson(objValue)
        prefs.edit().putString(key, json).apply()
    }





}