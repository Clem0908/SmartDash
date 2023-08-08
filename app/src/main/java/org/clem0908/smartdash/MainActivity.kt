package org.clem0908.smartdash

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainactivityview)
        super.onStart()
        super.onResume()

        findViewById<Button>(R.id.connection).setOnClickListener {
            Log.d("SmartDash", "User clicked connection")

            GlobalScope.launch {
                try {
                    val asInt = 0
                    val asFloat = 0.0
                    val ds = DatagramSocket(60009)
                    ds.broadcast = true
                    val data = ByteArray(96)
                    val dp = DatagramPacket(
                        data,
                        data.size,
                        Inet4Address.getByName("192.168.0.10"),
                        60009
                    )
                    ds.receive(dp)
                    val speedBytes = byteArrayOf(data[12],data[13],data[14],data[15])
                    val speedBytesBuffer = ByteBuffer.wrap(speedBytes).order(ByteOrder.LITTLE_ENDIAN)
                    //val speedBytesBuffer = ByteBuffer.wrap(speedBytes)
                    var speed = speedBytesBuffer.float
                    speed = (speed * 3.6).toFloat()
                    Log.d("UDP data",speed.toString())
                    ds.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }
}