package com.yourname.orthoguide

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class CareRemindersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        setContentView(R.layout.activity_care_reminders)

        findViewById<View>(R.id.iv_back)?.setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btn_save)?.setOnClickListener {
            Toast.makeText(this, "Preferences Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
