package com.seers.servicecheck.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.seers.servicecheck.service.LocationService

class RestartBackgroundService: BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("Broadcast Listened", "Service tried to stop:Intent>>${intent} >> ${context}")
        //Toast.makeText(context, "Service restarted!!!!!!!!", Toast.LENGTH_LONG).show()
        var rebootIntent = Intent(context, LocationService::class.java)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            try{
                Log.e("startForegroundService", "call")
                context!!.startForegroundService(rebootIntent)

            }catch (e: Exception){
                e.printStackTrace()
                Log.e("startForegroundService", "erroe:"+e.toString())
            }

        }else{
            context!!.startService(rebootIntent)
        }
        /*if(intent != null){
            if("restartservice" == intent.action){
                var rebootIntent = Intent(context, LocationService::class.java)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    context!!.startForegroundService(rebootIntent)
                }else{
                    context!!.startService(rebootIntent)
                }
            }
            var rebootIntent = Intent(context, LocationService::class.java)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                context!!.startForegroundService(rebootIntent)
            }else{
                context!!.startService(rebootIntent)
            }
        }*/

       /* var mLocationService = LocationService()
        var mServiceIntent = Intent(context, mLocationService.javaClass)
        context!!.startService(mServiceIntent)*/
    }
}