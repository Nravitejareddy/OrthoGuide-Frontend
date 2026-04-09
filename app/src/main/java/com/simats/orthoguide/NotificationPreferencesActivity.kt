package com.simats.orthoguide

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.appcompat.widget.SwitchCompat

class NotificationPreferencesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification_preferences)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.card_toolbar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        findViewById<View>(R.id.iv_back)?.setOnClickListener {
            finish()
        }
        
        val isClinician = intent.getBooleanExtra("isClinician", false)
        if (isClinician) {
            findViewById<android.widget.FrameLayout>(R.id.fl_notif_icon)?.let {
                it.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#DBEAFE")) // light blue
            }
            findViewById<android.widget.ImageView>(R.id.iv_notif_icon)?.let {
                it.setColorFilter(android.graphics.Color.parseColor("#2563EB")) // brand blue
            }
        }
        
        // Handle switch toggles if needed
        val pushSwitch = findViewById<SwitchCompat>(R.id.switch_push_notifications)
        pushSwitch.setOnCheckedChangeListener { _, isChecked ->
            // In a real app, save to preferences or backend here
        }
    }
}

