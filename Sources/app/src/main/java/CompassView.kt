package com.example.compassapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.AttributeSet
import android.view.View
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.widget.TextView
import androidx.core.content.ContextCompat

class CompassView(context: Context, attrs: AttributeSet? = null) : View(context, attrs), SensorEventListener {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var lastLocation: Location? = null

    private var azimuth: Float = 0f
    private var pitch: Float = 0f
    private var roll: Float = 0f

    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val compassCirclePaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.FILL
    }

    private val needlePaint = Paint().apply {
        color = Color.RED
        strokeWidth = 10f
        style = Paint.Style.STROKE
    }

    private var accelerometerValues: FloatArray? = null
    private var magnetometerValues: FloatArray? = null

    private lateinit var coordinatesTextView: TextView

    init {
        if (accelerometer == null || magnetometer == null) {
            throw IllegalArgumentException("Device does not have necessary sensors")
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        } else {
            lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelerometerValues = event.values
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                magnetometerValues = event.values
            }
        }

        if (accelerometerValues != null && magnetometerValues != null) {
            val R = FloatArray(9)
            val I = FloatArray(9)
            val values = FloatArray(3)
            SensorManager.getRotationMatrix(R, I, magnetometerValues, accelerometerValues)
            SensorManager.getOrientation(R, values)
            azimuth = Math.toDegrees(values[0].toDouble()).toFloat()
            pitch = Math.toDegrees(values[1].toDouble()).toFloat()
            roll = Math.toDegrees(values[2].toDouble()).toFloat()

            invalidate()

            accelerometerValues = null
            magnetometerValues = null
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = Math.min(centerX, centerY) - 10f

        canvas.drawCircle(centerX, centerY, radius, compassCirclePaint)

        val needleLength = radius * 0.8f
        val needleStartX = centerX + needleLength * Math.sin(Math.toRadians(azimuth.toDouble())).toFloat()
        val needleStartY = centerY - needleLength * Math.cos(Math.toRadians(azimuth.toDouble())).toFloat()
        canvas.drawLine(centerX, centerY, needleStartX, needleStartY, needlePaint)

        val angleStep = 30f
        for (i in 0..11) {
            val angle = i * angleStep
            val angleRad = Math.toRadians(angle.toDouble())
            val markX = centerX + radius * 0.9f * Math.sin(angleRad).toFloat()
            val markY = centerY - radius * 0.9f * Math.cos(angleRad).toFloat()
            canvas.drawLine(markX, markY, markX, markY + radius * 0.05f, paint)
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = radius * 0.1f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("N", centerX, centerY - radius * 0.7f, textPaint)
        canvas.drawText("E", centerX + radius * 0.8f, centerY + 15f, textPaint)
        canvas.drawText("S", centerX, centerY + radius * 0.8f, textPaint)
        canvas.drawText("W", centerX - radius * 0.8f, centerY + 15f, textPaint)

        if (lastLocation != null) {
            val latitude = lastLocation!!.latitude
            val longitude = lastLocation!!.longitude
            coordinatesTextView.text = String.format("Latitude: %.2f, Longitude: %.2f", latitude, longitude)
        }
    }
}