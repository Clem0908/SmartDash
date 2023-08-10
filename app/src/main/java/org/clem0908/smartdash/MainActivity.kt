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

    private fun fromFloatBytesToFuel(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Float {

        val speedBytes = byteArrayOf(b0, b1, b2, b3)
        val speedBytesBuffer =
            ByteBuffer.wrap(speedBytes).order(ByteOrder.LITTLE_ENDIAN)
        return speedBytesBuffer.float * 100
    }

    fun getBit(value: Int, position: Int): Int {
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

                        //Handbrake light
                        val handbrakeLight = getBit(data[44].toInt(),2)
                        val handbrakeLightImage: ImageView = findViewById(R.id.currentHandbrake)
                        if(handbrakeLight == 1) {
                            handbrakeLightImage.visibility = View.VISIBLE
                        } else {
                            handbrakeLightImage.visibility = View.GONE
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