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

        // Set initial name from shared prefs
        val prefs = getSharedPreferences("OrthoPref", MODE_PRIVATE)
        val userName = prefs.getString("USER_NAME", "Doctor")
        val userId = prefs.getString("USER_ID", "")
        
        findViewById<android.widget.TextView>(R.id.tv_profile_name)?.text = "Dr. $userName"
        findViewById<android.widget.TextView>(R.id.tv_profile_id)?.text = "ID: $userId"
        
        // Update initials
        val initials = (userName ?: "D").split(" ")
            .filter { it.isNotEmpty() }
            .map { it[0].uppercaseChar() }
            .take(2)
            .joinToString("")
        findViewById<android.widget.TextView>(R.id.tv_initials)?.text = if (initials.isNotEmpty()) initials else "D"

        // Fetch profile data from API
        fetchProfileData(userId ?: "")

        // Logic for menu items
        findViewById<LinearLayout>(R.id.ll_edit_profile)?.setOnClickListener {
            val intent = Intent(this, ClinicianEditProfileActivity::class.java)
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
            getSharedPreferences("OrthoPref", MODE_PRIVATE).edit().clear().apply()
            val intent = Intent(this, UnifiedLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("OrthoPref", MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", "")
        fetchProfileData(userId ?: "")
    }

    private fun fetchProfileData(clinicianId: String) {
        if (clinicianId.isEmpty()) return
        
        com.yourname.orthoguide.network.RetrofitClient.service.getClinicianProfile(clinicianId)
            .enqueue(object : retrofit2.Callback<com.yourname.orthoguide.network.ProfileResponse> {
                override fun onResponse(
                    call: retrofit2.Call<com.yourname.orthoguide.network.ProfileResponse>,
                    response: retrofit2.Response<com.yourname.orthoguide.network.ProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        val profile = response.body() ?: return
                        
                        findViewById<android.widget.TextView>(R.id.tv_profile_name)?.text = "Dr. ${profile.name}"
                        findViewById<android.widget.TextView>(R.id.tv_profile_email)?.text = profile.email ?: "No email set"
                        findViewById<android.widget.TextView>(R.id.tv_profile_phone)?.text = profile.phone ?: profile.phoneNumber ?: "No phone set"
                        findViewById<android.widget.TextView>(R.id.tv_profile_role)?.text = profile.role ?: "Orthodontist"
                        findViewById<android.widget.TextView>(R.id.tv_profile_id)?.text = "ID: ${profile.clinicianId}"
                        
                        // Update initials
                        val initials = (profile.name ?: "").split(" ")
                            .filter { it.isNotEmpty() }
                            .map { it[0].uppercaseChar() }
                            .take(2)
                            .joinToString("")
                        findViewById<android.widget.TextView>(R.id.tv_initials)?.text = if (initials.isNotEmpty()) initials else "--"

                        // Update shared prefs
                        getSharedPreferences("OrthoPref", MODE_PRIVATE).edit()
                            .putString("USER_NAME", profile.name)
                            .apply()
                    }
                }
                
                override fun onFailure(
                    call: retrofit2.Call<com.yourname.orthoguide.network.ProfileResponse>,
                    t: Throwable
                ) {
                    Log.e("OrthoGuide", "Clinician profile fetch failed", t)
                }
            })
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
