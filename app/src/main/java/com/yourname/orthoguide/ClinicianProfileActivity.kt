package com.yourname.orthoguide

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class ClinicianProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("OrthoGuide", "ClinicianProfileActivity created")
        enableEdgeToEdge()
        setContentView(R.layout.activity_clinician_profile)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val rootView = findViewById<View>(android.R.id.content)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, 0, systemBars.right, 0)
                insets
            }
        }

        findViewById<ImageView>(R.id.iv_back)?.setOnClickListener {
            finish()
        }

        // Logic for menu items
        findViewById<LinearLayout>(R.id.ll_edit_profile)?.setOnClickListener {
            startActivity(Intent(this, ClinicianEditProfileActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.ll_notification_prefs)?.setOnClickListener {
            val intent = Intent(this, NotificationPreferencesActivity::class.java)
            intent.putExtra("isClinician", true)
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.ll_manage_account)?.setOnClickListener {
            val intent = Intent(this, ManageAccountActivity::class.java)
            intent.putExtra("isClinician", true)
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.ll_help_support)?.setOnClickListener {
            val intent = Intent(this, HelpSupportActivity::class.java)
            intent.putExtra("isClinician", true)
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.ll_logout)?.setOnClickListener {
            val intent = Intent(this, ClinicianLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        // Dashboard
        findViewById<View>(R.id.nav_dashboard)?.setOnClickListener {
            val intent = Intent(this, ClinicianDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        // Patients
        findViewById<View>(R.id.nav_patients)?.setOnClickListener {
            val intent = Intent(this, PatientsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        // Schedule
        findViewById<View>(R.id.nav_schedule)?.setOnClickListener {
            val intent = Intent(this, ScheduleActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }
}
