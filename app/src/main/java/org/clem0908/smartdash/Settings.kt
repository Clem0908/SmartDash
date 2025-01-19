package org.clem0908.smartdash

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class Settings : Activity() {

    private fun displayToast(param: String) {
        Toast.makeText(this, param, Toast.LENGTH_SHORT).show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_ACTION_BAR);
        actionBar?.hide();
        setContentView(R.layout.settingsview)
        super.onStart()
        super.onResume()

        findViewById<Button>(R.id.save).setOnClickListener() {

            val sharedPreferences = getSharedPreferences("userSettings", Context.MODE_PRIVATE)
            val userAddr = findViewById<EditText>(R.id.userIPSetting)
            val userPort = findViewById<EditText>(R.id.userPortSetting)

            if(userAddr.text.toString() != "" && userPort.text.toString() != "") {

                val sharedPreferencesEditor = sharedPreferences.edit()
                sharedPreferencesEditor.putString("userIP", userAddr.text.toString())
                sharedPreferencesEditor.putInt("userPort", userPort.text.toString().toInt())
                sharedPreferencesEditor.apply()

                displayToast(getString(R.string.saved))

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                displayToast(getString(R.string.errorsettings))
            }
        }
    }
}