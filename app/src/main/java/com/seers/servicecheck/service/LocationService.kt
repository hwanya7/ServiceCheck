package com.seers.servicecheck.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.seers.servicecheck.*
import com.seers.servicecheck.R
import com.seers.servicecheck.data.LocationData
import com.seers.servicecheck.data.LocationDataListWrapper
import com.seers.servicecheck.receiver.RestartBackgroundService
import java.util.*


class LocationService: Service() {

    private val TAG = javaClass.simpleName
    private var currentLocation: Location? = null

    private var isFirstLoading = true

    private var isLocationCall = false

    override fun onCreate() {
        super.onCreate()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel() else startForeground(
            ConstantData.NOTI_SERVICE_ID,
            Notification()
        )

        requestLocationUpdates()

    }


    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(){
        val NOTIFICATION_CHANNEL_ID = "com.seers.servicecheck"
        val channelName = "Background Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(chan)

        val remoteViews = RemoteViews(packageName, R.layout.notification_service)

        var mainIntent = Intent(applicationContext, MainActivity::class.java)
        var pendingIntent = PendingIntent.getActivity(applicationContext, 0, mainIntent, 0)
        val notificationBuilder = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)

        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("title")
            .setContentText("App is running")
            .setCategory(Notification.CATEGORY_SERVICE)
            .setCustomContentView(remoteViews)
            .setContentIntent(pendingIntent)
            .build()

        //notification.contentIntent = pendingIntent
        startForeground(ConstantData.NOTI_SERVICE_ID, notification)


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startTimerTask()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimerTask()
        val broadcastIntent = Intent()
        broadcastIntent.action = "android.intent.action.BOOT_COMPLETED"
        broadcastIntent.setClass(this, RestartBackgroundService::class.java)
        this.sendBroadcast(broadcastIntent)
    }

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private fun startTimerTask(){
        timer = Timer()
        timerTask = object : TimerTask(){
            override fun run() {
                /*if(!isLocationCall){
                }else{
                    if(currentLocation != null){
                        val lbm = LocalBroadcastManager.getInstance(applicationContext)
                        val dataIntent = Intent("seers.locationservice")
                        dataIntent.putExtra("type", 1)  //0: ?????? ?????? ??????, 1: ?????????
                        dataIntent.putExtra("latitude", currentLocation!!.latitude)
                        dataIntent.putExtra("longitude", currentLocation!!.longitude)
                        lbm.sendBroadcast(dataIntent)

                        showNotification(ConstantData.NOTI_LOCATION_ID, "location:"+currentLocation!!.latitude + ">>"+currentLocation!!.longitude)

                        checkLocation()

                    }
                }*/
                if(currentLocation != null){
                    val lbm = LocalBroadcastManager.getInstance(applicationContext)
                    val dataIntent = Intent("seers.locationservice")
                    dataIntent.putExtra("type", 1)  //0: ?????? ?????? ??????, 1: ?????????
                    dataIntent.putExtra("latitude", currentLocation!!.latitude)
                    dataIntent.putExtra("longitude", currentLocation!!.longitude)
                    lbm.sendBroadcast(dataIntent)

                    showNotification(ConstantData.NOTI_LOCATION_ID, "location:"+currentLocation!!.latitude + ">>"+currentLocation!!.longitude)

                    checkLocation()

                }

            }
        }
        timer!!.schedule(timerTask, 0, 1000)
    }

    private fun stopTimerTask(){
        if(timer != null){
            timer!!.cancel()
            timer = null
        }
    }

    private fun requestLocationUpdates(){
        var request = LocationRequest()
        request.interval = 10000
        request.fastestInterval = 5000
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if(permission == PackageManager.PERMISSION_GRANTED){
            Log.e("ssshin", "PERMISSION_GRANTED")
            client.requestLocationUpdates(request, object : LocationCallback(){
                override fun onLocationResult(locationResult: LocationResult?) {
                    Log.e("ssshin", "locationResult:"+locationResult)
                    locationResult?.let {
                        isLocationCall = true
                        val location = it.lastLocation
                        if(location != null){
                            currentLocation = location

                            if(isFirstLoading){
                                tempAddLocationData()
                                isFirstLoading = false
                            }
                            Log.d("Locatioin Service", "loaction update: $location")

                            //todo ssshin timer??? ??????
                            /*if(currentLocation != null){
                                val lbm = LocalBroadcastManager.getInstance(applicationContext)
                                val dataIntent = Intent("seers.locationservice")
                                dataIntent.putExtra("type", 1)  //0: ?????? ?????? ??????, 1: ?????????
                                dataIntent.putExtra("latitude", currentLocation!!.latitude)
                                dataIntent.putExtra("longitude", currentLocation!!.longitude)
                                lbm.sendBroadcast(dataIntent)

                                showNotification(ConstantData.NOTI_LOCATION_ID, "location:"+currentLocation!!.latitude + ">>"+currentLocation!!.longitude)

                                checkLocation()

                            }*/
                        }
                    }
                }


            }, null)
        }
    }

    private fun checkLocation(){
        var regLocation = App.instance.getLocationData()
        if(regLocation != null){
            regLocation.locationDataList?.let { list ->
                var nearSettingName: String ?= null
                var nearSettingDistance: Float ?= null
                for(i in list.indices){
                    var currentIsInside = checkInRange(list[i])
                    Log.e("checkLocation", "currentIsInside:"+currentIsInside+">>save:"+list[i].isInside)
                    if(list[i].isInside != currentIsInside){
                        if(list[i].isInside){   //in -> out
                            showNotification(ConstantData.NOTI_ALERT_ID, list[i].name + "?????? ??????????????????.")
                        }else{  //out -> in
                            showNotification(ConstantData.NOTI_ALERT_ID, list[i].name + "?????? ????????????????????????.")
                        }
                    }

                    list[i].isInside = currentIsInside

                    var distance = checkDistance(list[i])
                    if(nearSettingName == null){
                        nearSettingName = list[i].name
                        nearSettingDistance = distance
                    }else{
                        if (distance != null) {
                            if(distance < nearSettingDistance!!){
                                nearSettingName = list[i].name
                                nearSettingDistance = distance
                            }
                        }
                    }
                }

                if(nearSettingName != null){
                    showNotification(ConstantData.NOTI_ALERT_DISTANCE_ID, nearSettingName + "?????? ??????:"+nearSettingDistance+"m")
                }

            }

            var pref = PreferenceUtil(this)
            pref.setObject(ConstantData.LOCATION_DATA, regLocation)

        }
    }

    private fun checkInRange(settingLocation: LocationData): Boolean{
        //1. diff?????? ??????????????? ?????? ??? in?????? ??????
        //2. ?????? inOutType ????????? ?????? ??????????????? ????????? noti??????
        //3. ?????? ???????????? ????????? ??????
        //4. ?????? ?????????????????? ???????????? ??? ????????? ?????? ??? ?????? ????????? ???????????? ?????? ???????????? ????????? ???
        //5. in ?????? inOutType -> 1??? out ?????? inOutType -> 0 ?????? ??????
        if(currentLocation != null){
            var diff = checkDistance(settingLocation)

            return if(diff != null){
                settingLocation.radius > diff
            }else{
                false
            }
        }
        return false
    }

    private fun checkDistance(settingLocation: LocationData): Float?{

        if(currentLocation != null){

            var settingLocationTemp = Location(""+System.currentTimeMillis())
            settingLocationTemp.latitude = settingLocation.latitude
            settingLocationTemp.longitude = settingLocation.longitude
            var diff = currentLocation!!.distanceTo(settingLocationTemp)

            return diff
        }
        return null
    }


    //load setting location
    private fun tempAddLocationData(){
        var locationDataList = ArrayList<LocationData>()

        var companyLocationData = LocationData(0, "????????????????????????", 37.365987, 127.106882, 50)
        companyLocationData.isInside = checkInRange(companyLocationData)
        locationDataList.add(companyLocationData)

        var houseLocationData = LocationData(1, "???", 37.2904927, 127.0283961, 30)
        houseLocationData.isInside = checkInRange(houseLocationData)
        locationDataList.add(houseLocationData)

        var locationData = LocationDataListWrapper(locationDataList)

        var pref = PreferenceUtil(this)
        pref.setObject(ConstantData.LOCATION_DATA, locationData)


        val lbm = LocalBroadcastManager.getInstance(applicationContext)
        val dataIntent = Intent("seers.locationservice")
        dataIntent.putExtra("type", 0)  //0: ?????? ?????? ??????, 1: ?????????
        lbm.sendBroadcast(dataIntent)

    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    fun showNotification(notiId: Int, content: String){
        val channelId = "com.seers.servicecheck.noti"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder: Notification.Builder
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationBuilder = Notification.Builder(this, channelId)
            val channelName = "??????"
            val chan = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_MIN
            )
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            manager.createNotificationChannel(chan)
        }else{
            notificationBuilder = Notification.Builder(this)
        }
        var mainIntent = Intent(applicationContext, MainActivity::class.java)
        var pendingIntent = PendingIntent.getActivity(applicationContext, 0, mainIntent, 0)
        val notification: Notification = notificationBuilder
            .setContentTitle("TestApp")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
        manager.notify(notiId, notification)
    }


}