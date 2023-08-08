package org.clem0908.smartdash

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.EditText

class Settings : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.settingsview)
        super.onStart()
        super.onResume()

        findViewById<Button>(R.id.save).setOnClickListener() {

            val sharedPreferences = getSharedPreferences("userSettings", Context.MODE_PRIVATE)
            val userAddr = findViewById<EditText>(R.id.userIPSetting)
            val userPort = findViewById<EditText>(R.id.userPortSetting)
            val sharedPreferencesEditor = sharedPreferences.edit()
            sharedPreferencesEditor.putString("userIP",userAddr.text.toString())
            sharedPreferencesEditor.putInt("userPort",userPort.text.toString().toInt())
            sharedPreferencesEditor.apply()
        }
    }

}