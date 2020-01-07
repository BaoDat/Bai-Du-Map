package com.example.baidumapexample

import android.os.Bundle
import com.baidu.mapapi.model.LatLng

import com.baidu.location.BDLocation
import com.baidu.location.BDLocationListener
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View

import android.widget.Toast
import com.baidu.mapapi.CoordType

import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.map.*
import com.baidu.mapapi.map.MarkerOptions


class MainActivity : AppCompatActivity() {

    private lateinit var mLocClient: LocationClient
    var myListener = MyLocationListenner()
    private var mCurrentMode: LocationMode? = null
    private var mSensorManager: SensorManager? = null
    private var update: MapStatusUpdate? = null

    internal var mMapView: MapView? = null
    internal lateinit var mBaiduMap: BaiduMap


    internal var isFirstLoc = true
    private var locData: MyLocationData? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SDKInitializer.initialize(applicationContext)
        SDKInitializer.setCoordType(CoordType.GCJ02)
        setContentView(R.layout.activity_main)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mCurrentMode = LocationMode.NORMAL




        mMapView = findViewById<View>(R.id.map_view) as MapView
        mBaiduMap = mMapView!!.map

        mBaiduMap.isMyLocationEnabled = true

        val latLng = LatLng(39.963175, 116.400244)
        val markerOptions = MarkerOptions()
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.bakery_marker))
            .position(latLng)
            .visible(true)

        mBaiduMap.addOverlay(markerOptions)                         // add marker



        mBaiduMap.setOnMarkerClickListener {
            Toast.makeText(
                this, "MARKER CLICK",
                Toast.LENGTH_LONG
            ).show()
            true
        }

        mBaiduMap.setOnMapClickListener(object : BaiduMap.OnMapClickListener {
            override fun onMapClick(latLng: LatLng) {
                Log.d("onMapClick", "$latLng")
            }

            override fun onMapPoiClick(mapPoi: MapPoi) {
                return
            }
        })

        mLocClient = LocationClient(this)
        mLocClient.registerLocationListener(myListener)
        initLocation()

        mLocClient.start()

    }


    inner class MyLocationListenner : BDLocationListener {

        override fun onReceiveLocation(location: BDLocation?) {

            if (location == null || mMapView == null) {
                return
            }

            locData = MyLocationData.Builder().accuracy(location.radius)
                .direction(100F).latitude(location.latitude).longitude(location.longitude)
                .build()
            mBaiduMap.setMyLocationData(locData)

            if (isFirstLoc) {
                val ll = LatLng(location.latitude, location.longitude)
                update = MapStatusUpdateFactory.newLatLngZoom(ll, 16F)
                mBaiduMap.animateMapStatus(update)
                isFirstLoc = false
                Toast.makeText(applicationContext, location.addrStr, Toast.LENGTH_SHORT).show()

            }
        }

    }

    private fun initLocation() {
        val option = LocationClientOption()
        option.locationMode =
            LocationClientOption.LocationMode.Hight_Accuracy//Optional, default high precision, setting location mode, high precision, low power consumption, only equipment
        option.setCoorType("gcj02")//Optional, default gcj02, set the coordinate system of the returned location result
        val span = 1000
        option.setScanSpan(span)//Optional, default0，That is to say, locate only once, and set the interval between requests to initiate locations to be greater than or equal to1000ms That's what works.
        option.setIsNeedAddress(true)//Optional. Set whether address information is required. No address information is required by default.
        option.isOpenGps = true//Optional, defaultfalse,Set whether to use gps
        option.isLocationNotify = true//Optional, defaultfalse，Whether the settings are appropriate or not gps When valid, according to1S1 Subfrequency output GPS Result
        option.setIsNeedLocationDescribe(true)//Optional, defaultfalse，Set whether location semantics results are needed, and you can BDLocation.getLocationDescribe The result is similar to "near Tian'anmen in Beijing".
        option.setIsNeedLocationPoiList(true)//Optional, defaultfalse，Set whether you need to POI As a result, the BDLocation.getPoiList Get inside
        option.setIgnoreKillProcess(false)//Optional, defaulttrue，Location SDK Inside is a SERVICE，And put it in a separate process, set whether or not it is stop When killing this process, default does not kill
        option.SetIgnoreCacheException(false)//Optional, defaultfalse，Set whether to collect CRASH Information, default collection
        option.setEnableSimulateGps(false)//Optional, defaultfalse，Set whether filtering is required gps Simulation results, default requirements
        option.openGps = true
        mLocClient.locOption = option
    }

    override fun onPause() {
        mMapView!!.onPause()
        super.onPause()
    }

    override fun onResume() {
        mMapView!!.onResume()
        super.onResume()

    }

    override fun onDestroy() {
        mLocClient.unRegisterLocationListener(myListener)
        mLocClient.stop()
        mBaiduMap.isMyLocationEnabled = false
        mMapView!!.onDestroy()
        mMapView = null
        super.onDestroy()
    }

}