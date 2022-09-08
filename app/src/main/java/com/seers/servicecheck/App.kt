package com.seers.servicecheck

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.seers.servicecheck.data.LocationDataListWrapper

class App: Application() {

    companion object{
        lateinit var instance: App
        lateinit var context: Context
        lateinit var pref: PreferenceUtil
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        context = applicationContext
        pref = PreferenceUtil(context)
    }

    fun getLocationData(): LocationDataListWrapper?{
        var objData = pref.getObject(ConstantData.LOCATION_DATA, "")
        var gson = Gson()

        if(objData.isEmpty()){
            return null
        }else{
            return gson.fromJson(objData, LocationDataListWrapper::class.java)
        }
    }

    fun showNotification(notiId: Int, content: String){
        val channelId = "com.seers.servicecheck.noti"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder: Notification.Builder
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationBuilder = Notification.Builder(this, channelId)
            val channelName = "알림"
            val chan = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_NONE
            )
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            manager.createNotificationChannel(chan)
        }else{
            notificationBuilder = Notification.Builder(this)
        }
        val notification: Notification = notificationBuilder
            .setContentTitle("TestApp")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        manager.notify(notiId, notification)
    }


}