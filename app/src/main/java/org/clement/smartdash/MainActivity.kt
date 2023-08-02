package org.clement.smartdash

import android.content.Context
import kotlinx.coroutines.Dispatchers
import android.Manifest
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.InvalidPropertiesFormatException


class MainActivity : ComponentActivity() {

    companion object {
        private const val INTERNET_PERMISSION_REQUEST_CODE = 123
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), INTERNET_PERMISSION_REQUEST_CODE)
        }

        //Settings button
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.settings)
            .setOnClickListener {
                    val intent = Intent(this, Settings::class.java)
                    startActivity(intent)
            }

        //Connection button
        findViewById<Button>(R.id.connection)
            .setOnClickListener {

                //Connection... Toast
                val connectionRunStr = getString(R.string.connection_running)
                showToast(connectionRunStr)
                //Default settings
                val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
                val upAddrStr = sharedPreferences.getString("userIP", "127.0.0.1")
                val port: Int = sharedPreferences.getInt("userPort", 60008)
                showToast(upAddrStr.toString()+":"+port)

                val addr: InetAddress = InetAddress.getByName(upAddrStr) as InetAddress

                showToast("Création du socket...")
                try {
                    val s = DatagramSocket(port,addr)
                    var data = ByteArray(1024)

                    val buffer = ByteArray(1024)
                    val p = DatagramPacket(buffer, buffer.size)

                        while (true) {
                            s.receive(p)

                            val sourceAddr = p.address.hostAddress
                            val sourcePort = p.port

                            showToast(sourceAddr)
                            if (upAddrStr == sourceAddr && port == sourcePort) {

                                val connectedStr = getString(R.string.connected)
                                showToast(connectedStr)

                                val data = p.data
                                val dataSize = p.length
                            } else {
                                val notConnectedStr = getString(R.string.not_connected)
                                showToast(notConnectedStr)
                            }
                    }
                } catch (t: Throwable) {
                    val errSoc = getString(R.string.socket_error)
                    showToast(errSoc)
                    t.printStackTrace()
                }
            }
    }
    private fun showToast(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }
}
