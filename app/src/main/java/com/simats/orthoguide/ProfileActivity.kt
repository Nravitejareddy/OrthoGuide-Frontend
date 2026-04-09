package com.simats.orthoguide

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.simats.orthoguide.network.ProfileResponse
import com.simats.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.card_toolbar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        findViewById<View>(R.id.iv_back).setOnClickListener {
            finish()
        }

        val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
        val userRole = sharedPref.getString("USER_ROLE", "patient") ?: "patient"
        val userId = sharedPref.getString("USER_ID", "") ?: ""

        // Quick update with locally stored name
        val localName = sharedPref.getString("USER_NAME", "User") ?: "User"
        findViewById<TextView>(R.id.tv_profile_name).text = localName
        findViewById<TextView>(R.id.tv_profile_initials).text = getInitials(localName)
        findViewById<TextView>(R.id.tv_profile_role_id).text = "${userRole.replaceFirstChar { it.uppercase() }} • ID: $userId"

        // Navigation
        findViewById<View>(R.id.ll_edit_profile).setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.ll_manage_account).setOnClickListener {
            val intent = Intent(this, ManageAccountActivity::class.java)
            intent.putExtra("isClinician", userRole == "clinician")
            intent.putExtra("isAdmin", userRole == "admin")
            startActivity(intent)
        }

        findViewById<View>(R.id.ll_help_support).setOnClickListener {
            startActivity(Intent(this, HelpSupportActivity::class.java))
        }

        findViewById<View>(R.id.ll_logout).setOnClickListener {
            sharedPref.edit().clear().apply()
            val intent = Intent(this, UnifiedLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
        val userRole = sharedPref.getString("USER_ROLE", "patient") ?: "patient"
        val userId = sharedPref.getString("USER_ID", "") ?: ""
        fetchProfileData(userRole, userId)
    }

    private fun fetchProfileData(role: String, id: String) {
        if (id.isEmpty()) return

        val call = if (role == "patient") {
            RetrofitClient.service.getPatientProfile(id)
        } else {
            RetrofitClient.service.getClinicianProfile(id)
        }

        call.enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (response.isSuccessful) {
                    val profile = response.body() ?: return
                    
                    findViewById<TextView>(R.id.tv_profile_name).text = profile.name
                    findViewById<TextView>(R.id.tv_profile_initials).text = getInitials(profile.name)
                    findViewById<TextView>(R.id.tv_profile_email).text = profile.email ?: "No email set"
                    findViewById<TextView>(R.id.tv_profile_phone).text = profile.phone ?: "No phone set"
                    
                    // Update shared prefs with latest name
                    getSharedPreferences("OrthoPref", Context.MODE_PRIVATE).edit()
                        .putString("USER_NAME", profile.name)
                        .apply()
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Log.e("OrthoGuide", "Profile fetch failed", t)
            }
        })
    }

    private fun setupBottomNavigation() {
        findViewById<View>(R.id.tab_home).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.tab_chat).setOnClickListener {
            startActivity(Intent(this, ChatBotActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.tab_progress).setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
        
        findViewById<View>(R.id.tab_profile).setOnClickListener { /* Already here */ }
    }

    private fun getInitials(name: String): String {
        if (name.isBlank()) return "U"
        val parts = name.trim().split("\\s+".toRegex())
        return if (parts.size >= 2) {
            (parts[0].substring(0, 1) + parts[parts.size - 1].substring(0, 1)).uppercase()
        } else if (parts[0].isNotEmpty()) {
            parts[0].substring(0, 1).uppercase()
        } else {
            "U"
        }
    }
}

