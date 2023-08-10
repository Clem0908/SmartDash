package org.clem0908.smartdash

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class MainActivity : Activity() {

    private fun fromFloatBytesToInt(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Int {

        val speedBytes = byteArrayOf(b0, b1, b2, b3)
        val speedBytesBuffer =
            ByteBuffer.wrap(speedBytes).order(ByteOrder.LITTLE_ENDIAN)
        val speed = speedBytesBuffer.float
        return (speed * 3.6).toInt()
    }

    private fun fromFloatBytesToIntRpm(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Int {

        val speedBytes = byteArrayOf(b0, b1, b2, b3)
        val speedBytesBuffer =
            ByteBuffer.wrap(speedBytes).order(ByteOrder.LITTLE_ENDIAN)
        val speed = speedBytesBuffer.float
        return speed.toInt()
    }

    private fun fromFloatBytesToFuel(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Int {

        val speedBytes = byteArrayOf(b0, b1, b2, b3)
        val speedBytesBuffer =
            ByteBuffer.wrap(speedBytes).order(ByteOrder.LITTLE_ENDIAN)
        return (speedBytesBuffer.float * 100).roundToInt()
    }

    private fun getBit(value: Int, position: Int): Int {
        return (value shr position) and 1;
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainactivityview)
        super.onStart()
        super.onResume()

        findViewById<Button>(R.id.settings).setOnClickListener {

            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.connection).setOnClickListener {
            Log.d("SmartDash", "User clicked connection")

            val sharedPref = getSharedPreferences("userSettings", Context.MODE_PRIVATE)
            val userIP = sharedPref.getString("userIP", "127.0.0.1")
            val userPort = sharedPref.getInt("userPort", 60009)

            GlobalScope.launch {
                while(true) {
                    try {
                        val ds = DatagramSocket(userPort)
                        ds.broadcast = true
                        val data = ByteArray(96)
                        val dp = DatagramPacket(
                            data,
                            data.size,
                            Inet4Address.getByName(userIP),
                            userPort
                        )

                        //LFS packet receiving
                        ds.receive(dp)

                        //Car name display
                        val currentCar = byteArrayOf(data[4], data[5], data[6], data[7])
                        val currentCarText = findViewById<TextView>(R.id.currentCar)
                        currentCarText.text = String(currentCar)

                        //Current gear display
                        val currentGear = data[10]
                        val currentGearText = findViewById<TextView>(R.id.currentGear)
                        if(currentGear.toInt() == 0){
                            currentGearText.text = "R"
                        } else {
                            if (currentGear.toInt() == 1) {
                                currentGearText.text = "N"
                            } else {
                                currentGearText.text = (currentGear.toInt()-1).toString()
                            }
                        }

                        //Current speed display
                        val speed = fromFloatBytesToInt(data[12], data[13], data[14], data[15])
                        val speedValueText = findViewById<TextView>(R.id.speedValue)
                        speedValueText.text = speed.toString()+" km/h"

                        //Current RPMs display
                        val rpm = fromFloatBytesToIntRpm(data[16], data[17], data[18], data[19])
                        val rpmText = findViewById<TextView>(R.id.rpmValue)
                        rpmText.text = rpm.toString()

                        //Current fuel percentage
                        val fuel = fromFloatBytesToFuel(data[28], data[29], data[30], data[31])
                        val fuelText = findViewById<TextView>(R.id.fuelValue)
                        fuelText.text = fuel.toString()+" %"

                        //Shift light
                        val shiftLight = getBit(data[44].toInt(),0)
                        val shiftLightImage = findViewById<ImageView>(R.id.currentShift)
                        if(shiftLight == 1) {
                            shiftLightImage.setImageResource(R.drawable.shift)
                        } else {
                            shiftLightImage.setImageResource(R.drawable.transparent)
                        }

                        //Beam light
                        val beamLight = getBit(data[44].toInt(),1)
                        val beamLightImage = findViewById<ImageView>(R.id.beam)
                        if(beamLight == 1) {
                            beamLightImage.setImageResource(R.drawable.beam)
                        } else {
                            beamLightImage.setImageResource(R.drawable.transparent)
                        }

                        //Handbrake light
                        val handbrakeLight = getBit(data[44].toInt(),2)
                        val handbrakeLightImage = findViewById<ImageView>(R.id.currentHandbrake)
                        if(handbrakeLight == 1) {
                            handbrakeLightImage.setImageResource(R.drawable.handbrake)
                        } else {
                            handbrakeLightImage.setImageResource(R.drawable.transparent)
                        }

                        //Limiter light
                        val limiterLight = getBit(data[44].toInt(),3)
                        val limiterLightImage = findViewById<ImageView>(R.id.currentLimiter)
                        if(limiterLight == 1) {
                            limiterLightImage.setImageResource(R.drawable.limiter)
                        } else {
                            limiterLightImage.setImageResource(R.drawable.transparent)
                        }

                        //ESP light
                        val ESPLight = getBit(data[44].toInt(),4)
                        val ESPLightImage = findViewById<ImageView>(R.id.currentESP)
                        if(ESPLight == 1) {
                            ESPLightImage.setImageResource(R.drawable.esp)
                        } else {
                            ESPLightImage.setImageResource(R.drawable.transparent)
                        }

                        //Indicators
                        val rightLight = getBit(data[44].toInt(),6)
                        val leftLight = getBit(data[44].toInt(),5)
                        val rightLightImage = findViewById<ImageView>(R.id.currentRight)
                        val leftLightImage = findViewById<ImageView>(R.id.currentLeft)
                        val warningLigthImage = findViewById<ImageView>(R.id.warning)

                        if(rightLight == 1) {
                            rightLightImage.setImageResource(R.drawable.right)
                        } else {
                            rightLightImage.setImageResource(R.drawable.transparent)
                        }
                        if(leftLight == 1) {
                            leftLightImage.setImageResource(R.drawable.left)
                        } else {
                            leftLightImage.setImageResource(R.drawable.transparent)
                        }
                        if(rightLight == 1 && leftLight == 1) {
                            rightLightImage.setImageResource(R.drawable.right)
                            leftLightImage.setImageResource(R.drawable.left)
                            warningLigthImage.setImageResource(R.drawable.warning)
                        }
                        if(rightLight == 0 && leftLight == 0) {
                            rightLightImage.setImageResource(R.drawable.transparent)
                            leftLightImage.setImageResource(R.drawable.transparent)
                            warningLigthImage.setImageResource(R.drawable.transparent)
                        }

                        //Oil light
                        val oilLight = getBit(data[45].toInt(),0)
                        val oilLightImage = findViewById<ImageView>(R.id.oil)
                        if(oilLight == 1) {
                            oilLightImage.setImageResource(R.drawable.oil)
                        } else {
                            oilLightImage.setImageResource(R.drawable.transparent)
                        }

                        //Battery light
                        val batteryLight = getBit(data[45].toInt(),1)
                        val batteryLightImage = findViewById<ImageView>(R.id.battery)
                        if(batteryLight == 1) {
                            batteryLightImage.setImageResource(R.drawable.battery)
                        } else {
                            batteryLightImage.setImageResource(R.drawable.transparent)
                        }

                        //ABS light
                        val absLight = getBit(data[45].toInt(),2)
                        val absLightImage = findViewById<ImageView>(R.id.abs)
                        if(absLight == 1) {
                            absLightImage.setImageResource(R.drawable.abs)
                        } else {
                            absLightImage.setImageResource(R.drawable.transparent)
                        }

                        ds.close()
                        TimeUnit.MILLISECONDS.sleep(100L)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

    }
}