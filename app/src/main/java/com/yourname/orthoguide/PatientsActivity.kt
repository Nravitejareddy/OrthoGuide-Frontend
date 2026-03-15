package com.yourname.orthoguide

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class PatientsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("OrthoGuide", "PatientsActivity created")
        enableEdgeToEdge()
        setContentView(R.layout.activity_patients)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val rootView = findViewById<View>(R.id.patients_root)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, 0, systemBars.right, 0)
                insets
            }
        }

        findViewById<View>(R.id.iv_back)?.setOnClickListener {
            finish()
        }



        // Handle Bottom Navigation clicks
        findViewById<View>(R.id.nav_dashboard)?.setOnClickListener {
            val intent = Intent(this, ClinicianDashboardActivity::class.java)
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
        
        // Profile
        findViewById<View>(R.id.nav_profile)?.setOnClickListener {
            val intent = Intent(this, ClinicianProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        // Open Patient Profile
        val openProfile = View.OnClickListener { view ->
            val intent = Intent(this, PatientProfileActivity::class.java)
            val prefs = getSharedPreferences("OrthoGuidePrefs", android.content.Context.MODE_PRIVATE)
            
            val pId = when (view.id) {
                R.id.card_patient_1 -> "ID: 100982300"
                R.id.card_patient_2 -> "ID: 100982100"
                R.id.card_patient_3 -> "ID: 100982200"
                R.id.card_patient_4 -> "ID: 100982400"
                R.id.card_patient_5 -> "ID: 100982500"
                else -> ""
            }
            
            val pName = when (view.id) {
                R.id.card_patient_1 -> "Robert Chen"
                R.id.card_patient_2 -> "James Wilson"
                R.id.card_patient_3 -> "Sarah Anderson"
                R.id.card_patient_4 -> "Emily Davis"
                R.id.card_patient_5 -> "Michael Brown"
                else -> ""
            }

            intent.putExtra("patientName", pName)
            intent.putExtra("patientId", pId)
            intent.putExtra("hasAppointment", prefs.getBoolean("appt_$pId", false))
            
            startActivity(intent)
        }
        
        findViewById<View>(R.id.btn_add_patient)?.setOnClickListener {
            val intent = Intent(this, CreatePatientActivity::class.java)
            intent.putExtra("isEditMode", false)
            intent.putExtra("userRole", "clinician")
            startActivity(intent)
        }
        
        val cards = listOf(
            findViewById<View>(R.id.card_patient_1), // Attention
            findViewById<View>(R.id.card_patient_2), // Critical
            findViewById<View>(R.id.card_patient_3), // On Track
            findViewById<View>(R.id.card_patient_4), // On Track
            findViewById<View>(R.id.card_patient_5)  // On Track
        )
        
        cards.forEach { it?.setOnClickListener(openProfile) }

        // Search and Filter Logic
        val etSearch = findViewById<android.widget.EditText>(R.id.et_search_patients)
        var currentFilterId = R.id.filter_all
        var currentSearchQuery = ""

        val patientData = listOf(
            Pair("Robert Chen", "100982300"),
            Pair("James Wilson", "100982100"),
            Pair("Sarah Anderson", "100982200"),
            Pair("Emily Davis", "100982400"),
            Pair("Michael Brown", "100982500")
        )

        fun applyFilters() {
            cards.forEachIndexed { index, card ->
                if (card == null) return@forEachIndexed
                
                // 1. Check Segment Filter
                val matchesFilter = when (currentFilterId) {
                    R.id.filter_all -> true
                    R.id.filter_critical -> index == 1
                    R.id.filter_attention -> index == 0
                    R.id.filter_on_track -> index >= 2
                    else -> true
                }

                // 2. Check Search Query
                val (name, id) = patientData[index]
                val matchesSearch = if (currentSearchQuery.isEmpty()) {
                    true
                } else {
                    name.contains(currentSearchQuery, ignoreCase = true) || 
                    id.contains(currentSearchQuery, ignoreCase = true)
                }

                card.visibility = if (matchesFilter && matchesSearch) View.VISIBLE else View.GONE
            }
        }

        fun updateFilterUI(selectedId: Int) {
            currentFilterId = selectedId
            val filterButtons = listOf(
                findViewById<TextView>(R.id.filter_all),
                findViewById<TextView>(R.id.filter_critical),
                findViewById<TextView>(R.id.filter_attention),
                findViewById<TextView>(R.id.filter_on_track)
            )

            filterButtons.forEach { it?.setBackgroundResource(R.drawable.bg_segment_unselected) }
            filterButtons.forEach { it?.setTextColor(Color.parseColor("#64748B")) }
            filterButtons.forEach { it?.setTypeface(null, android.graphics.Typeface.NORMAL) }

            val selected = findViewById<TextView>(selectedId)
            selected?.setBackgroundResource(R.drawable.bg_segment_selected_blue)
            selected?.setTextColor(Color.parseColor("#2563EB"))
            selected?.setTypeface(null, android.graphics.Typeface.BOLD)

            applyFilters()
        }

        etSearch?.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString() ?: ""
                applyFilters()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        findViewById<View>(R.id.filter_all)?.setOnClickListener { updateFilterUI(R.id.filter_all) }
        findViewById<View>(R.id.filter_critical)?.setOnClickListener { updateFilterUI(R.id.filter_critical) }
        findViewById<View>(R.id.filter_attention)?.setOnClickListener { updateFilterUI(R.id.filter_attention) }
        findViewById<View>(R.id.filter_on_track)?.setOnClickListener { updateFilterUI(R.id.filter_on_track) }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("OrthoGuidePrefs", android.content.Context.MODE_PRIVATE)
        
        val patientsList = listOf(
            Triple("ID: 100982300", R.id.ll_appt_status_1,Pair(R.id.iv_appt_icon_1, R.id.tv_appt_status_1)),
            Triple("ID: 100982100", R.id.ll_appt_status_2,Pair(R.id.iv_appt_icon_2, R.id.tv_appt_status_2)),
            Triple("ID: 100982200", R.id.ll_appt_status_3,Pair(R.id.iv_appt_icon_3, R.id.tv_appt_status_3)),
            Triple("ID: 100982400", R.id.ll_appt_status_4,Pair(R.id.iv_appt_icon_4, R.id.tv_appt_status_4)),
            Triple("ID: 100982500", R.id.ll_appt_status_5,Pair(R.id.iv_appt_icon_5, R.id.tv_appt_status_5))
        )
        
        patientsList.forEach { (pId, layoutId, views) ->
            val hasAppt = prefs.getBoolean("appt_$pId", false)
            val layout = findViewById<View>(layoutId)
            val icon = findViewById<ImageView>(views.first)
            val text = findViewById<TextView>(views.second)
            
            if (hasAppt) {
                layout?.setBackgroundResource(R.drawable.bg_tag_green_soft)
                icon?.setImageResource(R.drawable.ic_check_green)
                icon?.imageTintList = null // No tint needed for green check
                text?.text = "Appointment scheduled"
                text?.setTextColor(Color.parseColor("#059669"))
            } else {
                layout?.setBackgroundResource(R.drawable.bg_tag_yellow)
                icon?.setImageResource(R.drawable.ic_clock_outline)
                icon?.imageTintList = ColorStateList.valueOf(Color.parseColor("#D97706"))
                text?.text = "No appointment scheduled"
                text?.setTextColor(Color.parseColor("#D97706"))
            }
        }
    }
}
