package org.clem0908.smartdash

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.roundToInt

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
        return
    }

    private var ds: DatagramSocket = DatagramSocket(null)

    private fun getDS(): DatagramSocket {
        return this.ds
    }

    private fun startDS(userPort: Int) {
        this.ds = DatagramSocket(userPort)
        return
    }

    private fun stopDS() {
        if(this.ds.isBound) {
            this.ds.close()
            return
        }
        return
    }

    private fun setBroadcast() {
        if(!this.ds.isClosed) {
            this.ds.broadcast = true
            return
        }
        return
    }

    private fun receiveDP(dp: DatagramPacket) {
        if(!this.ds.isClosed) {
            this.ds.receive(dp)
            return
        }
        return
    }

    private fun getLFSData() {

        val sharedPref = getSharedPreferences("userSettings", Context.MODE_PRIVATE)
        val userIP = sharedPref.getString("userIP", "127.0.0.1")
        val userPort = sharedPref.getInt("userPort", 60009)

        val rpmText = findViewById<TextView>(R.id.rpmValue)
        val currentGearText = findViewById<TextView>(R.id.currentGear)
        val speedValueText = findViewById<TextView>(R.id.speedValue)
        val fuelText = findViewById<TextView>(R.id.fuelValue)
        val t = Thread {

            startDS(userPort)

            while(!getDS().isClosed) {

                try {

                    setBroadcast()
                    val data = ByteArray(96)
                    val dp = DatagramPacket(
                        data,
                        data.size,
                        Inet4Address.getByName(userIP),
                        userPort
                    )

                    //LFS packet receiving
                    receiveDP(dp)

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

                    //Full beam light
                    val beamLight = getBit(data[44].toInt(), 1)
                    val beamLightImage = findViewById<ImageView>(R.id.full_beam)
                    if (beamLight == 1) {
                        runOnUiThread { beamLightImage.setImageResource(R.drawable.full_beam) }
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

                    //Engine light
                    val engineLight = getBit(data[45].toInt(), 3)
                    val engineLightImage = findViewById<ImageView>(R.id.engine)
                    if (engineLight == 1) {
                        runOnUiThread { engineLightImage.setImageResource(R.drawable.engine) }
                    } else {
                        runOnUiThread { engineLightImage.setImageResource(R.drawable.transparent) }
                    }

                    //Rear fog light
                    val rearFogLight = getBit(data[45].toInt(), 4)
                    val rearFogLightImage = findViewById<ImageView>(R.id.rear_fog_light)
                    if (rearFogLight == 1) {
                        runOnUiThread { rearFogLightImage.setImageResource(R.drawable.rear_fog_light) }
                    } else {
                        runOnUiThread { rearFogLightImage.setImageResource(R.drawable.transparent) }
                    }

                    //Front fog light
                    val frontFogLight = getBit(data[45].toInt(), 5)
                    val frontFogLightImage = findViewById<ImageView>(R.id.front_fog_light)
                    if (frontFogLight == 1) {
                        runOnUiThread { frontFogLightImage.setImageResource(R.drawable.front_fog_light) }
                    } else {
                        runOnUiThread { frontFogLightImage.setImageResource(R.drawable.transparent) }
                    }

                    //Low beam light
                    val lowBeamLight = getBit(data[45].toInt(), 6)
                    val lowBeamLightImage = findViewById<ImageView>(R.id.low_beam)
                    if (lowBeamLight == 1) {
                        runOnUiThread { lowBeamLightImage.setImageResource(R.drawable.low_beam) }
                    } else {
                        runOnUiThread { lowBeamLightImage.setImageResource(R.drawable.transparent) }
                    }

                    //Low fuel light
                    val lowFuelLight = getBit(data[45].toInt(), 7)
                    val lowFuelLightImage = findViewById<ImageView>(R.id.lowFuel)
                    if (lowFuelLight == 1) {
                        runOnUiThread { lowFuelLightImage.setImageResource(R.drawable.low_fuel) }
                    } else {
                        runOnUiThread { lowFuelLightImage.setImageResource(R.drawable.transparent) }
                    }

                    //Position light
                    val positionLight = getBit(data[46].toInt(), 0)
                    val positionLightImage = findViewById<ImageView>(R.id.position_light)
                    if (positionLight == 1) {
                        runOnUiThread { positionLightImage.setImageResource(R.drawable.position_light) }
                    } else {
                        runOnUiThread { positionLightImage.setImageResource(R.drawable.transparent) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        t.start()
        if(getDS().isClosed) {
            t.interrupt()
        }
        return
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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

        // Connection button that start the main routine
        findViewById<Button>(R.id.connect).setOnClickListener {

            it.visibility = View.GONE
            val disconnectButton = findViewById<Button>(R.id.disconnect)
            disconnectButton.visibility = View.VISIBLE
            displayToast(getString(R.string.connecting))
            getLFSData()
        }

        findViewById<Button>(R.id.disconnect).setOnClickListener {
            it.visibility = View.GONE
            val connectButton = findViewById<Button>(R.id.connect)
            connectButton.visibility = View.VISIBLE
            displayToast(getString(R.string.disconnecting))
            stopDS()
        }
        return
    }
}
