package org.clem0908.smartdash

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and

class MainActivity : Activity() {

    private fun fromFloatBytesToInt(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Int {

        val speedBytes = byteArrayOf(b0, b1, b2, b3)
        val speedBytesBuffer =
            ByteBuffer.wrap(speedBytes).order(ByteOrder.LITTLE_ENDIAN)
        val speed = speedBytesBuffer.float
        return (speed * 3.6).toInt()
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
                        ds.receive(dp)
                        val speed = fromFloatBytesToInt(data[12], data[13], data[14], data[15])
                        val speedValueText = findViewById<TextView>(R.id.speedValue)
                        speedValueText.setText(speed.toString()+" km/h")
                        ds.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

    }
}