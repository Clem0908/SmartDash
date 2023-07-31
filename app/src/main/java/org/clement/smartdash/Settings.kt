package org.clement.smartdash

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity

class Settings : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)


        val saveButton = findViewById<Button>(R.id.savebutton)
        saveButton.setOnClickListener {
            val IP = findViewById<EditText>(R.id.Settings_IP_IP)
            val userIP = IP.text.toString()
            val port = findViewById<EditText>(R.id.Settings_port_port)
            val userPort = port.text.toString()

            try {

                val userPortInt: Int = userPort.toInt()
                val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("userIP", userIP)
                editor.putInt("userPort", userPortInt)
                editor.apply()
                val savedString = getString(R.string.saved)
                Toast.makeText(this, savedString, Toast.LENGTH_SHORT).show()

            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid input. Please enter a valid integer.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}