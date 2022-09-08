package com.seers.servicecheck

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.PermissionChecker
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.seers.servicecheck.data.LocationData
import com.seers.servicecheck.data.LocationDataListWrapper
import com.seers.servicecheck.service.LocationService
import com.seers.servicecheck.util.Util
import kotlinx.android.synthetic.main.activity_main.*
import net.daum.mf.map.api.*
import java.security.MessageDigest

class MainActivity : AppCompatActivity(), MapView.MapViewEventListener {
    private lateinit var mLocationService: LocationService
    private lateinit var mServiceIntent: Intent
    private lateinit var mActivity: Activity
    private lateinit var mapView: MapView

    private val lbm by lazy { LocalBroadcastManager.getInstance(this) }
    var filter = IntentFilter("seers.locationservice")
    private lateinit var marker: MapPOIItem
    private var isCurrentLocationMove = false
    private lateinit var mapCircle: MapCircle

    private var locationDataList: ArrayList<LocationData> = ArrayList()

    private var currLocation: MapPoint ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mActivity = this@MainActivity

        initMap()

        if(!Util.isLocationEnabledOrNot(mActivity)){
            Util.showAlertLocation(mActivity,
            getString(R.string.gps_enable),
            getString(R.string.please_turn_on_gps),
            getString(R.string.ok))
        }

        requestPermissionsSafely(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 200
        )


        txtStartService.setOnClickListener{
            mLocationService = LocationService()
            mServiceIntent = Intent(this, mLocationService.javaClass)
            if(!Util.isMyServiceRunning(mLocationService.javaClass, mActivity)){
                startService(mServiceIntent)
                Toast.makeText(mActivity, R.string.service_start_successfully, Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(mActivity, R.string.service_already_running, Toast.LENGTH_SHORT).show()
            }
        }

        btnCurrLocation.setOnClickListener {
            if(currLocation != null){
                mapView.setMapCenterPoint(currLocation, true)
            }

        }

        getAppKeyHash()

        drawCircle()

    }

    private fun initMap(){
        mapView = MapView(mActivity)
        var mapViewContainer = findViewById<ViewGroup>(R.id.map_view)
        mapViewContainer.addView(mapView)
        mapView.setMapViewEventListener(this)


        var defaultLocation = MapPoint.mapPointWithGeoCoord(37.123232, 127.1231)

        marker = MapPOIItem()
        marker.itemName = "current"
        marker.tag = 1
        marker.mapPoint = defaultLocation
        marker.markerType = MapPOIItem.MarkerType.BluePin

        mapView.addPOIItem(marker)

        /*var currMarker = MapCurrentLocationMarker()
        currMarker.radius = 100
        currMarker.directionImageId = R.drawable.ic_launcher_background
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving
        mapView.setCurrentLocationMarker(currMarker)*/



    }


    private fun startService(){
        mLocationService = LocationService()
        mServiceIntent = Intent(this, mLocationService.javaClass)
        if(!Util.isMyServiceRunning(mLocationService.javaClass, mActivity)){
            startService(mServiceIntent)
            Toast.makeText(mActivity, R.string.service_start_successfully, Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(mActivity, R.string.service_already_running, Toast.LENGTH_SHORT).show()
        }
    }


    private fun requestPermissionsSafely(permissions: Array<String>, requestCode: Int){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(permissions, requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        var isAllGrant = true
        for(i in grantResults.indices){
            if(grantResults[i] != PermissionChecker.PERMISSION_GRANTED){
                isAllGrant = false
                break
            }
        }

        if(!isAllGrant){
            Toast.makeText(mActivity, "권한이 모두 허용이 되어야 합니다. 앱을 종료합니다.", Toast.LENGTH_SHORT).show()
        }else{
            startService()
        }

    }


    //mapview event callback
    override fun onMapViewInitialized(p0: MapView?) {

    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {

    }

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {

    }


    fun getAppKeyHash() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for(i in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(i.toByteArray())

                val something = String(Base64.encode(md.digest(), 0)!!)
                Log.e("Debug key", something)
            }
        } catch(e: Exception) {
            Log.e("Not found", e.toString())
        }
    }

    override fun onResume() {
        super.onResume()

        lbm.registerReceiver(locationMessageListener, filter)

    }

    override fun onPause() {
        super.onPause()

        lbm.unregisterReceiver(locationMessageListener)
    }


    private val locationMessageListener = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, data: Intent) {

            var type = data.getIntExtra("type", -1)

            if(type == 0){
                var regLocation = App.instance.getLocationData()
                if(regLocation != null){
                    regLocation.locationDataList?.let { list ->
                        for(i in list.indices){
                            var settingLoaction = MapPoint.mapPointWithGeoCoord(list[i].latitude, list[i].longitude)

                            var mapCircle = MapCircle(
                                settingLoaction,
                                list[i].radius,
                                Color.argb(128, 255, 0, 0),
                                Color.argb(128, 255, 255, 0)
                            )
                            /*
                            mapCircle1.tag = 1234
                            mapCircle1.strokeColor = Color.BLUE
                            mapCircle1.id = 9191
                            */
                            mapView.addCircle(mapCircle)
                        }
                    }

                }
            }else if(type == 1){
                var latitude = data.getDoubleExtra("latitude",0.0)
                var longitude = data.getDoubleExtra("longitude",0.0)


                currLocation = MapPoint.mapPointWithGeoCoord(latitude, longitude)
                marker.mapPoint = currLocation

                //todo ssshin
                mapView.setMapCenterPoint(currLocation, true)


            }

        }
    }


    private fun drawCircle(){

        var regLocation = App.instance.getLocationData()
        if(regLocation != null){
            regLocation.locationDataList?.let { list ->
                for(i in list.indices){
                    var settingLoaction = MapPoint.mapPointWithGeoCoord(list[i].latitude, list[i].longitude)

                    var mapCircle = MapCircle(
                        settingLoaction,
                        list[i].radius,
                        Color.argb(128, 255, 0, 0),
                        Color.argb(128, 255, 255, 0)
                    )
                    /*
                    mapCircle1.tag = 1234
                    mapCircle1.strokeColor = Color.BLUE
                    mapCircle1.id = 9191
                    */
                    mapView.addCircle(mapCircle)
                }
            }
        }
    }
}