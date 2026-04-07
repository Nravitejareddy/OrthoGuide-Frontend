package com.yourname.orthoguide

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
            val userId = sharedPref.getString("USER_ID", null)
            val userRole = sharedPref.getString("USER_ROLE", null)

            val intent = if (userId != null && userRole != null) {
                // User already logged in, go to their dashboard
                when (userRole) {
                    "patient" -> Intent(this, DashboardActivity::class.java)
                    "clinician" -> Intent(this, ClinicianDashboardActivity::class.java)
                    "admin" -> Intent(this, AdminDashboardActivity::class.java)
                    else -> Intent(this, UnifiedLoginActivity::class.java)
                }
            } else {
                Intent(this, UnifiedLoginActivity::class.java)
            }

            startActivity(intent)
            finish()
        }, 2000)
    }
}
