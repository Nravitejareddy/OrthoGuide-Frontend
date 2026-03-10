package com.yourname.orthoguide

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class ClinicianDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("OrthoGuide", "ClinicianDashboardActivity created")
        enableEdgeToEdge()
        setContentView(R.layout.activity_clinician_dashboard)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val rootView = findViewById<View>(R.id.clinician_dashboard_root)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                // Only padding left, right. Top padding handled manually, bottom is full bleed.
                v.setPadding(systemBars.left, 0, systemBars.right, 0)
                // Add top padding dynamically to the top bar
                val topBar = findViewById<View>(R.id.ll_top_bar)
                topBar?.setPadding(
                    topBar.paddingLeft,
                    systemBars.top + dpToPx(12), // Match Image 2 tightness
                    topBar.paddingRight,
                    topBar.paddingBottom
                )
                insets
            }
        }
        
        // Handle Bottom Navigation clicks manually for now
        findViewById<View>(R.id.bottom_nav)?.let { bottomNav ->
            // Dashboard (Already here)
            // Patients
            findViewById<View>(R.id.patients_nav_item)?.setOnClickListener {
                startActivity(android.content.Intent(this, PatientsActivity::class.java).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                })
                @Suppress("DEPRECATION")
                overridePendingTransition(0, 0)
            }
            // Schedule
            findViewById<View>(R.id.nav_schedule)?.setOnClickListener {
                startActivity(android.content.Intent(this, ScheduleActivity::class.java).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                })
                @Suppress("DEPRECATION")
                overridePendingTransition(0, 0)
            }
            // Profile
            findViewById<View>(R.id.nav_profile)?.setOnClickListener {
                startActivity(android.content.Intent(this, ClinicianProfileActivity::class.java).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                })
                @Suppress("DEPRECATION")
                overridePendingTransition(0, 0)
            }
        }

        // View All Patients
        findViewById<View>(R.id.tv_view_all_patients)?.setOnClickListener {
            startActivity(android.content.Intent(this, PatientsActivity::class.java).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        // Open Patient Profile
        val openProfile = View.OnClickListener { view ->
            val intent = android.content.Intent(this, PatientProfileActivity::class.java)
            val prefs = getSharedPreferences("OrthoGuidePrefs", android.content.Context.MODE_PRIVATE)
            
            when (view.id) {
                R.id.card_patient_1 -> {
                    intent.putExtra("patientName", "James Wilson")
                    intent.putExtra("patientId", "ID: 100982100")
                    intent.putExtra("hasAppointment", prefs.getBoolean("appt_ID: 100982100", false))
                }
                R.id.card_patient_2 -> {
                    intent.putExtra("patientName", "Robert Chen")
                    intent.putExtra("patientId", "ID: 100982300")
                    intent.putExtra("hasAppointment", prefs.getBoolean("appt_ID: 100982300", false)) 
                }
                // card_patient_3 is hidden on dashboard, but keeping intent data just in case
                R.id.card_patient_3 -> {
                    intent.putExtra("patientName", "Emily Davis")
                    intent.putExtra("patientId", "ID: 100982500")
                    intent.putExtra("hasAppointment", prefs.getBoolean("appt_ID: 100982500", true))
                }
            }
            startActivity(intent)
        }
        findViewById<View>(R.id.card_patient_1)?.setOnClickListener(openProfile)
        findViewById<View>(R.id.card_patient_2)?.setOnClickListener(openProfile)
        
        // Hide On Track patients from Dashboard (e.g., Emily Davis)
        findViewById<View>(R.id.card_patient_3)?.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("OrthoGuidePrefs", android.content.Context.MODE_PRIVATE)
        
        val patientsList = listOf(
            Triple("ID: 100982100", "James Wilson", R.id.card_patient_1),
            Triple("ID: 100982300", "Robert Chen", R.id.card_patient_2),
            Triple("ID: 100982200", "Sarah Anderson", 0),
            Triple("ID: 100982400", "Emily Davis", R.id.card_patient_3),
            Triple("ID: 100982500", "Michael Brown", 0)
        )
        
        var priorityCount = 0
        
        patientsList.forEach { (pId, name, cardId) ->
            val hasAppt = prefs.getBoolean("appt_$pId", false)
            val status = prefs.getString("status_$pId", if (pId == "ID: 100982100") "Critical" else if (pId == "ID: 100982300") "Attention" else "On Track")
            
            val isPriority = (status == "Critical" || status == "Attention") && !hasAppt
            
            if (isPriority) {
                priorityCount++
            }
            
            if (cardId != 0) {
                val card = findViewById<View>(cardId)
                card?.visibility = if (isPriority) View.VISIBLE else View.GONE
                
                // Update text if needed (e.g. status tag)
                // Note: In a real app we'd find the status TextView inside the card
            }
        }
        
        // Update priority count
        findViewById<android.widget.TextView>(R.id.tv_priority_count)?.text = priorityCount.toString()
        
        // Update Active count (example, assuming active = total)
        // findViewById<android.widget.TextView>(R.id.tv_active_count)?.text = "5"
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
