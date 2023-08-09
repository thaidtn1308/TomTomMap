package com.example.tomtommap

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.tomtommap.databinding.ActivityMainBinding
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.location.android.AndroidLocationProvider
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.style.LoadingStyleFailure
import com.tomtom.sdk.map.display.style.StandardStyles
import com.tomtom.sdk.map.display.style.StyleLoadingCallback
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.map.display.ui.logo.LogoView

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val apiKey = "QIXpUTuBCxyXh7cV3hO4SQKpAF3vea23"

    lateinit var mapFragment: MapFragment
    private var tomTomMap: TomTomMap? = null
    private lateinit var locationProvider: AndroidLocationProvider
    private lateinit var onLocationUpdateListener: OnLocationUpdateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initMap()

    }

    private fun initMap() {
        val mapOptions = MapOptions(mapKey = apiKey)
        mapFragment = MapFragment.newInstance(mapOptions)
        supportFragmentManager.beginTransaction()
            .replace(binding.mapContainer.id, mapFragment)
            .commit()
        mapFragment.getMapAsync { map ->
            tomTomMap = map
            initLocationProvider()
            enableUserLocation()
        }
    }

    private fun initLocationProvider() {
        locationProvider = AndroidLocationProvider(context = this)
    }

    private fun showUserLocation() {
        locationProvider.enable()
        // zoom to current location at city level
        onLocationUpdateListener = OnLocationUpdateListener { location ->
            tomTomMap?.moveCamera(CameraOptions(location.position, zoom = 15.0))
            locationProvider.removeOnLocationUpdateListener(onLocationUpdateListener)
        }
        locationProvider.addOnLocationUpdateListener(onLocationUpdateListener)
        tomTomMap?.setLocationProvider(locationProvider)
        val locationMarker = LocationMarkerOptions(type = LocationMarkerOptions.Type.Pointer)
        tomTomMap?.enableLocationMarker(locationMarker)
        tomTomMap?.loadStyle(StandardStyles.SATELLITE, object : StyleLoadingCallback {
            override fun onFailure(failure: LoadingStyleFailure) {}
            override fun onSuccess() {}
        })
        mapFragment.logoView.visibilityPolicy = LogoView.VisibilityPolicy.Invisible
    }

    private fun areLocationPermissionsGranted() = ContextCompat.checkSelfPermission(
        this,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        this,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true &&
            permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            showUserLocation()
        } else {
            Toast.makeText(
                this,
                "Location permission denied",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun enableUserLocation() {
        if (areLocationPermissionsGranted()) {
            showUserLocation()
        } else {
            requestLocationPermission()
        }
    }

}