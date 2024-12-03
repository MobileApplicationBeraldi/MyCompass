package com.example.mycompass

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.graphics.withMatrix
import kotlin.math.PI
import kotlin.math.atan2


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(MyView(this))
    }
}


class MyView(context: Context): View(context),SensorEventListener2 {

    var mLastRotationVector = FloatArray(3) //The last value of the rotation vector
    var mOrientation = FloatArray(3)
    var mRotationMatrix = FloatArray(9)
    var yaw = 0f
    var a = 0.001f //Low-band pass filter
    var UPS = 0 //Update Per Second
    var FPS = 0 //Frames Per Second

    var timeFPS = 0L
    var timeUPS = 0L

    var fps =""
    var ups = ""

    var  paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED // Set the arrow color to red
        style = Paint.Style.STROKE // Set the style to stroke (outline)
        strokeWidth = 18f // Set the stroke width to 8 pixels
        strokeCap = Paint.Cap.ROUND // Set the stroke cap to round
         }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK // Set text color to black
        textSize = 32f // Set text size to 32 pixels
    }
    var M = Matrix() //Matrix as change-of-basis
    var M2=Matrix() //Matrix used to rotate head compass

    init {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(
            this,  //use this since MyView implements the listener interface
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        mLastRotationVector = event?.values?.clone()!! //Get last rotation vector
        SensorManager.getRotationMatrixFromVector(mRotationMatrix,mLastRotationVector)
        //Calculate the yaw angle, see slides of the lesson——

        yaw = a*yaw+(1-a)* atan2(mRotationMatrix[1],mRotationMatrix[4]) *180f/ PI.toFloat()

        //Alternative way using available methods
        //SensorManager.getOrientation(mRotationMatrix,mOrientation)
        //yaw = mOrientation[0]*180f/PI.toFloat()

        if (UPS%10==0){
            ups=(1000*10/(System.currentTimeMillis()-timeUPS)).toString()
            timeUPS=System.currentTimeMillis()
            UPS=0
        }
        UPS++
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onFlushCompleted(sensor: Sensor?) {
        //TODO("Not yet implemented")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        M.setScale(1f,-1f)
        M.preConcat(Matrix().apply { setTranslate(w/2f,-h/2f) })
        // View's width and height are now available in w and h

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (FPS%10==0){
            fps=(10000/(System.currentTimeMillis()-timeFPS)).toString()
            timeFPS=System.currentTimeMillis()
            FPS=0
        }
        FPS++
        with(canvas) {
            drawText("YAW: "+yaw.toString(),100f,40f,textPaint)
            drawText("FPS: "+fps,100f,80f,textPaint)
            drawText("UPS: "+ups,100f,120f,textPaint)
            withMatrix(M) {
                drawLine(0f,0f,0f,canvas.width/3f,paint)

                withMatrix(M2.apply {
                    setRotate(yaw,0f,0f)}) {
                    drawLine(0f,0f,0f,canvas.width/3f,paint)
                }
                //
            }
        }
        invalidate()
    }
}