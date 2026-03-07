package com.yourname.orthoguide

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class HelpSupportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_help_support)

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
        val isAdmin = intent.getBooleanExtra("isAdmin", false)

        if (isAdmin) {
            findViewById<android.widget.TextView>(R.id.tv_help_title)?.text = "System Support & Assistance"
            findViewById<android.widget.TextView>(R.id.tv_help_subtitle)?.text = "For system errors, account management issues, or technical support, contact the system administrator or developer."
            
            findViewById<android.widget.TextView>(R.id.tv_contact_header)?.text = "CONTACT INFORMATION"
            findViewById<android.widget.TextView>(R.id.tv_phone_title)?.text = "Technical Support Phone"
            findViewById<android.widget.TextView>(R.id.tv_phone_value)?.text = "+91 98765 43210" // Technical support sample
            findViewById<android.widget.TextView>(R.id.tv_email_title)?.text = "System Support Email"
            findViewById<android.widget.TextView>(R.id.tv_email_value)?.text = "support-admin@orthoguide.com"
            
            // Apply Admin Purple Theme
            applyTheme("#7C3AED", "#FAF5FF")
        } else if (isClinician) {
            findViewById<android.widget.TextView>(R.id.tv_help_title)?.text = "Need admin support?"
            findViewById<android.widget.TextView>(R.id.tv_help_subtitle)?.text = "For system issues or account assistance, please contact the admin directly."
            
            findViewById<android.widget.TextView>(R.id.tv_phone_title)?.text = "Admin Phone"
            findViewById<android.widget.TextView>(R.id.tv_phone_value)?.text = "+91 98765 43210"
            findViewById<android.widget.TextView>(R.id.tv_email_title)?.text = "Admin Support"
            findViewById<android.widget.TextView>(R.id.tv_email_value)?.text = "admin@orthoguide.com"
            
            // Apply Clinician Blue Theme
            applyTheme("#2196F3", "#E8F4FD")
        } else {
            // Default Patient Green Theme
            applyTheme("#059669", "#ECFDF5")
        }
    }

    private fun applyTheme(primaryColorHex: String, bgColorHex: String) {
        val primaryColor = android.graphics.Color.parseColor(primaryColorHex)
        val bgColor = android.graphics.Color.parseColor(bgColorHex)
        val primaryStateList = android.content.res.ColorStateList.valueOf(primaryColor)
        val bgStateList = android.content.res.ColorStateList.valueOf(bgColor)

        // Update Icon Backgrounds
        findViewById<android.view.View>(R.id.fl_help)?.backgroundTintList = bgStateList
        findViewById<android.view.View>(R.id.fl_phone)?.backgroundTintList = bgStateList
        findViewById<android.view.View>(R.id.fl_email)?.backgroundTintList = bgStateList

        // Update Icon Tints
        findViewById<android.widget.ImageView>(R.id.iv_help)?.imageTintList = primaryStateList
        findViewById<android.widget.ImageView>(R.id.iv_phone)?.imageTintList = primaryStateList
        findViewById<android.widget.ImageView>(R.id.iv_email)?.imageTintList = primaryStateList

        // Update Version Text
        val tvVersion = findViewById<android.widget.TextView>(R.id.tv_version)
        tvVersion?.backgroundTintList = bgStateList
        tvVersion?.setTextColor(primaryColor)
    }
}
                           