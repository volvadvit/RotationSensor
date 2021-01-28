package com.volvadvit.rotationsensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var sensorMgr: SensorManager
    private lateinit var sensorListener: SensorEventListener
    private lateinit var rotationMatrix: FloatArray
    private lateinit var orientationData: FloatArray
    private lateinit var accelData: FloatArray
    private lateinit var vectorData: FloatArray
    private lateinit var magnetData: FloatArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorsInit()
        sensorListenerInit()
    }

    override fun onResume() {
        super.onResume()
        registrationListener()
    }

    override fun onPause() {
        super.onPause()
        sensorMgr.unregisterListener(sensorListener)
    }

    private fun sensorsInit() {
        sensorMgr = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        rotationMatrix = FloatArray(16)
        orientationData = FloatArray(3)
        accelData = FloatArray(3)
        magnetData = FloatArray(3)
        vectorData = FloatArray(3)
    }

    private fun sensorListenerInit() {
        sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                getSensorData(event)
                setDegree(event)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // not yet implemented
            }

        }
    }

    private fun registrationListener() {
        sensorMgr.registerListener(
            sensorListener,
            sensorMgr.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_FASTEST)

        sensorMgr.registerListener(
            sensorListener,
            sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_FASTEST)

        sensorMgr.registerListener(
            sensorListener,
            sensorMgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_FASTEST
        )
    }

    private fun setDegree(event: SensorEvent?) {
        event?.let {
            when (event.sensor.type ) {
                Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_ACCELEROMETER -> xyzSetData()
                Sensor.TYPE_ROTATION_VECTOR -> setRotationData(event)
            }
        }
    }

    private fun setRotationData(event: SensorEvent) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, vectorData)
        val remappedRotationMatrix = FloatArray(16)
        SensorManager.remapCoordinateSystem(rotationMatrix,
            SensorManager.AXIS_X,
            SensorManager.AXIS_Z,
            remappedRotationMatrix)

    // convert to orientations
        val orientations= FloatArray(3)
        SensorManager.getOrientation(remappedRotationMatrix, orientations)
        OX.text = Math.toDegrees(orientations[0].toDouble()).roundToInt().toString()
        OY.text = Math.toDegrees(orientations[1].toDouble()).roundToInt().toString()
        OZ.text = Math.toDegrees(orientations[2].toDouble()).roundToInt().toString()

        imageView.rotation = Math.toDegrees(orientations[2].toDouble()).toFloat()

    }

    private fun xyzSetData() {
        SensorManager.getRotationMatrix(rotationMatrix, null, accelData, magnetData)
        SensorManager.getOrientation(rotationMatrix, orientationData)

        XYValue.text = Math.toDegrees(orientationData[0].toDouble()).roundToInt().toString()
        YZValue.text = Math.toDegrees(orientationData[1].toDouble()).roundToInt().toString()
        ZXValue.text = Math.toDegrees(orientationData[2].toDouble()).roundToInt().toString()
    }

    private fun getSensorData(event: SensorEvent?) {
        event?.let {
                when (event.sensor.type ) {
                Sensor.TYPE_MAGNETIC_FIELD -> magnetData = event.values.clone()
                Sensor.TYPE_ACCELEROMETER -> accelData = event.values.clone()
                Sensor.TYPE_ROTATION_VECTOR -> vectorData = event.values.clone()
            }
        }
    }
}