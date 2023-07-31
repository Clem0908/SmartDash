package org.clement.smartdash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.settings)
            .setOnClickListener {
                    val intent = Intent(this, Settings::class.java)
                    startActivity(intent)
            }
    }
}
