package com.yourname.orthoguide

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class AdminDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_dashboard)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        val rootView = findViewById<View>(R.id.admin_dashboard_root)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, 0, systemBars.right, 0)
                
                val topBar = findViewById<View>(R.id.ll_admin_top_bar)
                topBar?.setPadding(
                    topBar.paddingLeft,
                    systemBars.top + dpToPx(12),
                    topBar.paddingRight,
                    topBar.paddingBottom
                )
                insets
            }
        }

        findViewById<View>(R.id.card_clinicians)?.setOnClickListener {
            val intent = Intent(this, ManageCliniciansActivity::class.java)
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

        findViewById<View>(R.id.admin_nav_settings)?.setOnClickListener {
            val intent = Intent(this, AdminSettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    override fun onResume() {
        super.onResume()
        updateHeader()
    }

    private fun updateHeader() {
        val tvDate = findViewById<android.widget.TextView>(R.id.tv_today_date)
        val tvName = findViewById<android.widget.TextView>(R.id.tv_admin_name_display)

        // Set Date
        val sdf = java.text.SimpleDateFormat("EEEE, MMM d", java.util.Locale.getDefault())
        tvDate?.text = sdf.format(java.util.Date())

        // Set Name from Prefs
        val prefs = getSharedPreferences("OrthoGuidePrefs", MODE_PRIVATE)
        val adminName = prefs.getString("admin_name", "System Admin")
        tvName?.text = adminName
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
