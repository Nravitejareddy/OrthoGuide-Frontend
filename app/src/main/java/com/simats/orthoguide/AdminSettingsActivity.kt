package com.simats.orthoguide

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class AdminSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_settings)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val rootView = findViewById<View>(R.id.admin_settings_root)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, 0, systemBars.right, 0)
                insets
            }
        }

        findViewById<View>(R.id.iv_back)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<View>(R.id.btn_logout)?.setOnClickListener {
            getSharedPreferences("OrthoPref", MODE_PRIVATE).edit().clear().apply()
            val intent = Intent(this, UnifiedLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.ll_edit_profile)?.setOnClickListener {
            val intent = Intent(this, AdminEditProfileActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.ll_manage_account)?.setOnClickListener {
            val intent = Intent(this, ManageAccountActivity::class.java)
            intent.putExtra("isAdmin", true)
            intent.putExtra("isClinician", false)
            startActivity(intent)
        }

        findViewById<View>(R.id.ll_help_support)?.setOnClickListener {
            val intent = Intent(this, HelpSupportActivity::class.java)
            intent.putExtra("isAdmin", true)
            intent.putExtra("isClinician", false)
            startActivity(intent)
        }

        // Navigation
        findViewById<View>(R.id.admin_nav_dashboard)?.setOnClickListener {
            val intent = Intent(this, AdminDashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.admin_nav_clinicians)?.setOnClickListener {
            val intent = Intent(this, ManageCliniciansActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.admin_nav_patients)?.setOnClickListener {
            val intent = Intent(this, ManagePatientsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }
    override fun onResume() {
        super.onResume()
        updateAdminProfileUI()
    }

    private fun updateAdminProfileUI() {
        val prefs = getSharedPreferences("OrthoPref", MODE_PRIVATE)
        val adminName = prefs.getString("USER_NAME", "System Admin")
        val adminEmail = prefs.getString("USER_EMAIL", "admin@orthoguide.com")
        val adminPhone = prefs.getString("USER_PHONE", "+91 98765 43210")
        val adminId = prefs.getString("USER_ID", "ADMIN001")

        findViewById<android.widget.TextView>(R.id.tv_admin_name_settings)?.text = adminName
        findViewById<android.widget.TextView>(R.id.tv_admin_email_settings)?.text = adminEmail
        findViewById<android.widget.TextView>(R.id.tv_admin_phone_settings)?.text = adminPhone
        findViewById<android.widget.TextView>(R.id.tv_admin_id_settings)?.text = "ID: $adminId"
    }
}

