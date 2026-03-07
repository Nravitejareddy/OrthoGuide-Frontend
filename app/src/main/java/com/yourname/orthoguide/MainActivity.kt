package com.yourname.orthoguide

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        val patientCard = findViewById<View>(R.id.card_user)
        patientCard?.setOnClickListener {
            Log.d("OrthoGuide", "Patient Login card clicked")
            try {
                val intent = Intent(this@MainActivity, PatientLoginActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("OrthoGuide", "Failed to start PatientLoginActivity", e)
            }
        }

        val clinicianCard = findViewById<View>(R.id.card_developer)
        clinicianCard?.setOnClickListener {
            Log.d("OrthoGuide", "Clinician Login card clicked")
            try {
                val intent = Intent(this@MainActivity, ClinicianLoginActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("OrthoGuide", "Failed to start ClinicianLoginActivity", e)
            }
        }
    }
}
