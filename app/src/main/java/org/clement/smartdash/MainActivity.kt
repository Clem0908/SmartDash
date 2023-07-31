package org.clement.smartdash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.settings)
            .setOnClickListener {
                    val intent = Intent(this, Settings::class.java)
                    startActivity(intent)
            }

        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val addrStr = sharedPreferences.getString("userIP", "127.0.0.1")
        val addr: InetAddress = InetAddress.getByName(addrStr) as InetAddress
        if(addr.isAnyLocalAddress()) {
            val port: Int = sharedPreferences.getInt("userPort", 49999)
            val s = DatagramSocket(port, addr)
            val buffer = ByteArray(128)
            val p = DatagramPacket(buffer, buffer.size)
            var data = ByteArray(128)

                val connexion = findViewById<TextView>(R.id.connexion)
                connexion.text = "$addrStr:$port"
                s.receive(p)

                Toast.makeText(this, "Connecté", Toast.LENGTH_SHORT).show()


                val taille = p.length
                data = p.data
                var car = String()
                /* for (i in 4..7) {
                car += Char(data[i].toUShort())
            }
            println("Voiture : $car")
            println("Drapaux : " + data[8] + data[9])
            println("Rapport : " + data[10])*/
                val vueRapport = findViewById<TextView>(R.id.vueRapport)
                vueRapport.text = data[10].toString()
                /*
            println("PLID : " + data[11])
            var asInt = (data[12].toInt() and 0xFF
                    or (data[13].toInt() and 0xFF shl 8)
                    or (data[14].toInt() and 0xFF shl 16)
                    or (data[15].toInt() and 0xFF shl 24))
            var asFloat = java.lang.Float.intBitsToFloat(asInt)
            println("Vitesse : " + asFloat * 3.6)
            asInt = (data[16].toInt() and 0xFF
                    or (data[17].toInt() and 0xFF shl 8)
                    or (data[18].toInt() and 0xFF shl 16)
                    or (data[19].toInt() and 0xFF shl 24))
            asFloat = java.lang.Float.intBitsToFloat(asInt)
            println("RPMs : $asFloat")
*/
        }
    }
}
