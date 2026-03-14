package com.yourname.orthoguide

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class ClinicianLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_clinician_login)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val rootView = findViewById<View>(R.id.clinician_login_root)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        findViewById<ImageView>(R.id.iv_back)?.setOnClickListener {
            if (isTaskRoot) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            finish()
        }

        // Login button navigates to Dashboard based on credentials
        findViewById<View>(R.id.btn_login)?.setOnClickListener {
            val providerId = findViewById<android.widget.EditText>(R.id.et_provider_id)?.text?.toString()?.trim()
            val password = findViewById<android.widget.EditText>(R.id.et_password)?.text?.toString()?.trim()

            if (providerId == "admin" && password == "admin@123") {
                val intent = Intent(this, AdminDashboardActivity::class.java)
                startActivity(intent)
                @Suppress("DEPRECATION")
                overridePendingTransition(0, 0)
            } else {
                val intent = Intent(this, ClinicianDashboardActivity::class.java)
                startActivity(intent)
                @Suppress("DEPRECATION")
                overridePendingTransition(0, 0)
            }
        }
    }
}
