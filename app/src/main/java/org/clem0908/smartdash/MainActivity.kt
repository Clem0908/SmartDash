package org.clem0908.smartdash

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.math.roundToInt

// Variable to know if a socket is running
var alreadyConnected = 0

class MainActivity : Activity() {

    /* Function to convert the speed to integer 
     *
     * @param: b0, b1, b2, b3: 4 bytes
     * @return: Int: speed in km/h
     * */
    private fun fromFloatBytesToInt(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Int {

        val speedBytes = byteArrayOf(b0, b1, b2, b3)
        val speedBytesBuffer =
            ByteBuffer.wrap(speedBytes).order(ByteOrder.LITTLE_ENDIAN)
        val speed = speedBytesBuffer.float
        return (speed * 3.6).toInt()
    }

    /* Function to convert the RPMs to integer 
     *
     * @param: b0, b1, b2, b3: 4 bytes
     * @return: Int: RPMs
     * */
    private fun fromFloatBytesToIntRpm(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Int {

        val speedBytes = byteArrayOf(b0, b1, b2, b3)
        val speedBytesBuffer =
            ByteBuffer.wrap(speedBytes).order(ByteOrder.LITTLE_ENDIAN)
        val speed = speedBytesBuffer.float
        return speed.toInt()
    }

    /* Function to convert the fuel to a integer percentage 
     *
     * @param: b0, b1, b2, b3: 4 bytes
     * @return: Int: percentage of fuel
     * */
    private fun fromFloatBytesToFuel(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Int {

        val speedBytes = byteArrayOf(b0, b1, b2, b3)
        val speedBytesBuffer =
            ByteBuffer.wrap(speedBytes).order(ByteOrder.LITTLE_ENDIAN)
        return (speedBytesBuffer.float * 100).roundToInt()
    }

    /* Function to get a positional bit  
     *
     * @param: value, position
     * @return: Int: position
     * */
    private fun getBit(value: Int, position: Int): Int {
        return (value shr position) and 1;
    }

    /* Function to display a Toast
     *
     * @param: String
     * */
    private fun displayToast(param: String) {
        Toast.makeText(this, param, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        actionBar?.hide();
        setContentView(R.layout.mainactivityview)
        super.onStart()
        super.onResume()

	// Button to enable Immersive Mode
        findViewById<Button>(R.id.immersiveMode).setOnClickListener {
            val windowInsetsController =
                WindowCompat.getInsetsController(window, window.decorView)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        }

	// Button to go to the Settings activity
        findViewById<Button>(R.id.settings).setOnClickListener {

            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }

        val sharedPref = getSharedPreferences("userSettings", Context.MODE_PRIVATE)
        val userIP = sharedPref.getString("userIP", "127.0.0.1")
        val userPort = sharedPref.getInt("userPort", 60009)

        val rpmText = findViewById<TextView>(R.id.rpmValue)
        val currentGearText = findViewById<TextView>(R.id.currentGear)
        val speedValueText = findViewById<TextView>(R.id.speedValue)
        val fuelText = findViewById<TextView>(R.id.fuelValue)

	// Connection button that start the main routine
        findViewById<Button>(R.id.connect).setOnClickListener {

            // If a socket has been launched, do not create a new one
            if(alreadyConnected == 1) { return@setOnClickListener }

            GlobalScope.launch {

                var ds = DatagramSocket(userPort)

                alreadyConnected = 1

                while (true) {

                    try {

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

                        //Current gear display
                        val currentGear = data[10]
                        if (currentGear.toInt() == 0) {
                            runOnUiThread { currentGearText.text = "R" }
                        } else {
                            if (currentGear.toInt() == 1) {
                                runOnUiThread { currentGearText.text = "N" }
                            } else {
                                runOnUiThread {
                                    currentGearText.text =
                                        (currentGear.toInt() - 1).toString()
                                }
                            }
                        }

                        //Current speed display
                        val speed =
                            fromFloatBytesToInt(data[12], data[13], data[14], data[15])
                        runOnUiThread { speedValueText.text = speed.toString() }

                        //Current RPMs display
                        val rpm = fromFloatBytesToIntRpm(
                            data[16],
                            data[17],
                            data[18],
                            data[19]
                        )
                        //val rpmText = findViewById<TextView>(R.id.rpmValue)
                        runOnUiThread { rpmText.text = rpm.toString() }

                        //Current fuel percentage
                        val fuel =
                            fromFloatBytesToFuel(data[28], data[29], data[30], data[31])
                        runOnUiThread { fuelText.text = fuel.toString() }
                        if (fuel < 5) {
                            val lowFuelImage = findViewById<ImageView>(R.id.lowFuel)
                            runOnUiThread { lowFuelImage.setImageResource(R.drawable.low_fuel) }
                        } else {
                            val lowFuelImage = findViewById<ImageView>(R.id.lowFuel)
                            runOnUiThread { lowFuelImage.setImageResource(R.drawable.transparent) }
                        }

                        //Shift light
                        val shiftLight = getBit(data[44].toInt(), 0)
                        val shiftLightImage = findViewById<ImageView>(R.id.currentShift)
                        if (shiftLight == 1) {
                            runOnUiThread { shiftLightImage.setImageResource(R.drawable.shift) }
                        } else {
                            runOnUiThread { shiftLightImage.setImageResource(R.drawable.transparent) }
                        }

                        //Beam light
                        val beamLight = getBit(data[44].toInt(), 1)
                        val beamLightImage = findViewById<ImageView>(R.id.beam)
                        if (beamLight == 1) {
                            runOnUiThread { beamLightImage.setImageResource(R.drawable.beam) }
                        } else {
                            runOnUiThread { beamLightImage.setImageResource(R.drawable.transparent) }
                        }

                        //Handbrake light
                        val handbrakeLight = getBit(data[44].toInt(), 2)
                        val handbrakeLightImage =
                            findViewById<ImageView>(R.id.currentHandbrake)
                        if (handbrakeLight == 1) {
                            runOnUiThread { handbrakeLightImage.setImageResource(R.drawable.handbrake) }
                        } else {
                            runOnUiThread { handbrakeLightImage.setImageResource(R.drawable.transparent) }
                        }

                        //Limiter light
                        val limiterLight = getBit(data[44].toInt(), 3)
                        val limiterLightImage =
                            findViewById<ImageView>(R.id.currentLimiter)
                        if (limiterLight == 1) {
                            runOnUiThread { limiterLightImage.setImageResource(R.drawable.limiter) }
                        } else {
                            runOnUiThread { limiterLightImage.setImageResource(R.drawable.transparent) }
                        }

                        //ESP light
                        val ESPLight = getBit(data[44].toInt(), 4)
                        val ESPLightImage = findViewById<ImageView>(R.id.currentESP)
                        if (ESPLight == 1) {
                            runOnUiThread { ESPLightImage.setImageResource(R.drawable.esp) }
                        } else {
                            runOnUiThread { ESPLightImage.setImageResource(R.drawable.transparent) }
                        }

                        //Indicators
                        val rightLight = getBit(data[44].toInt(), 6)
                        val leftLight = getBit(data[44].toInt(), 5)
                        val rightLightImage = findViewById<ImageView>(R.id.currentRight)
                        val leftLightImage = findViewById<ImageView>(R.id.currentLeft)
                        val warningLightImage = findViewById<ImageView>(R.id.warning)

                        if (rightLight == 1) {
                            runOnUiThread { rightLightImage.setImageResource(R.drawable.right) }
                        } else {
                            runOnUiThread { rightLightImage.setImageResource(R.drawable.transparent) }
                        }
                        if (leftLight == 1) {
                            runOnUiThread { leftLightImage.setImageResource(R.drawable.left) }
                        } else {
                            runOnUiThread { leftLightImage.setImageResource(R.drawable.transparent) }
                        }
                        if (rightLight == 1 && leftLight == 1) {
                            runOnUiThread { rightLightImage.setImageResource(R.drawable.right) }
                            runOnUiThread { leftLightImage.setImageResource(R.drawable.left) }
                            runOnUiThread { warningLightImage.setImageResource(R.drawable.warning) }
                        }
                        if (rightLight == 0 && leftLight == 0) {
                            runOnUiThread { rightLightImage.setImageResource(R.drawable.transparent) }
                            runOnUiThread { leftLightImage.setImageResource(R.drawable.transparent) }
                            runOnUiThread { warningLightImage.setImageResource(R.drawable.transparent) }
                        }

                        //Oil light
                        val oilLight = getBit(data[45].toInt(), 0)
                        val oilLightImage = findViewById<ImageView>(R.id.oil)
                        if (oilLight == 1) {
                            runOnUiThread { oilLightImage.setImageResource(R.drawable.oil) }
                        } else {
                            runOnUiThread { oilLightImage.setImageResource(R.drawable.transparent) }
                        }

                        //Battery light
                        val batteryLight = getBit(data[45].toInt(), 1)
                        val batteryLightImage = findViewById<ImageView>(R.id.battery)
                        if (batteryLight == 1) {
                            runOnUiThread { batteryLightImage.setImageResource(R.drawable.battery) }
                        } else {
                            runOnUiThread { batteryLightImage.setImageResource(R.drawable.transparent) }
                        }

                        //ABS light
                        val absLight = getBit(data[45].toInt(), 2)
                        val absLightImage = findViewById<ImageView>(R.id.abs)
                        if (absLight == 1) {
                            runOnUiThread { absLightImage.setImageResource(R.drawable.abs) }
                        } else {
                            runOnUiThread { absLightImage.setImageResource(R.drawable.transparent) }
                        }

                        //Engine damage light
                        val engineDamageLight = getBit(data[45].toInt(), 3)
                        //val engineDamageLightImage = findViewById<ImageView>(R.id.abs)
                        if (engineDamageLight == 1) {
                            runOnUiThread { displayToast("Engine damaged")}//absLightImage.setImageResource(R.drawable.abs) }
                        } else {
                            runOnUiThread { }//absLightImage.setImageResource(R.drawable.transparent) }
                        }
                        //Engine damage light
                        val rearFogLight = getBit(data[45].toInt(), 4)
                        //val engineDamageLightImage = findViewById<ImageView>(R.id.abs)
                        if (rearFogLight == 1) {
                            runOnUiThread { displayToast("rearfoglight ")}//absLightImage.setImageResource(R.drawable.abs) }
                        } else {
                            runOnUiThread { }//absLightImage.setImageResource(R.drawable.transparent) }
                        }
                        //Engine damage light
                        val frontFogLight = getBit(data[45].toInt(), 5)
                        //val engineDamageLightImage = findViewById<ImageView>(R.id.abs)
                        if (frontFogLight == 1) {
                            runOnUiThread { displayToast("frontfoglight ")}//absLightImage.setImageResource(R.drawable.abs) }
                        } else {
                            runOnUiThread { }//absLightImage.setImageResource(R.drawable.transparent) }
                        }
                        //Engine damage light
                        val dippedLight = getBit(data[45].toInt(), 6)
                        //val engineDamageLightImage = findViewById<ImageView>(R.id.abs)
                        if (dippedLight == 1) {
                            runOnUiThread { displayToast("dippedlight ")}//absLightImage.setImageResource(R.drawable.abs) }
                        } else {
                            runOnUiThread { }//absLightImage.setImageResource(R.drawable.transparent) }
                        }

                        //Low fuel light
                        val lowFuelLight = getBit(data[45].toInt(), 7)
                        val lowFuelLightImage = findViewById<ImageView>(R.id.lowFuel)
                        if (lowFuelLight == 1) {
                            runOnUiThread { lowFuelLightImage.setImageResource(R.drawable.low_fuel) }
                        } else {
                            runOnUiThread { lowFuelLightImage.setImageResource(R.drawable.transparent) }
                        }

                        //Engine damage light
                        val daylight = getBit(data[45].toInt(), 5)
                        //val engineDamageLightImage = findViewById<ImageView>(R.id.abs)
                        if (daylight == 1) {
                            runOnUiThread { displayToast("veilleuses ")}//absLightImage.setImageResource(R.drawable.abs) }
                        } else {
                            runOnUiThread { }//absLightImage.setImageResource(R.drawable.transparent) }
                        }


                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } //while
            } //Scope coroutine
        } //Connect onClickListener()
    }
}
