package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.content.pm.PackageManager
import android.util.Log

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var coordinatesTextView: TextView

    private val locationPermissionRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        coordinatesTextView = findViewById(R.id.coordinates_text_view)

        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionRequestCode
            )
        } else {
            getLastLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                coordinatesTextView.text = "Широта: $latitude\nДолгота: $longitude"
            } else {
                coordinatesTextView.text = "Не удалось получить координаты"
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode) {
            Log.d("Location", "onRequestPermissionsResult: RequestCode $requestCode")
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Location", "Разрешение получено")
                getLastLocation()
            } else {
                Log.d("Location", "Разрешение не получено")
                coordinatesTextView.text = "Разрешение на геолокацию не получено"
            }
        }
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST = 1
    }
}