package com.yourname.orthoguide

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class ScheduleActivity : AppCompatActivity() {

    private lateinit var timeslotsContainer: android.widget.LinearLayout
    private lateinit var prefs: android.content.SharedPreferences
    private val patients = listOf(
        Patient("Robert Chen", "ID: 100982300", "Debonding & Retention", "ATTENTION", "#B45309", "#FEF3C7"),
        Patient("James Wilson", "ID: 100982100", "Bonding / First Trays", "CRITICAL", "#991B1B", "#FEE2E2"),
        Patient("Sarah Anderson", "ID: 100982200", "Alignment Phase", "ON TRACK", "#065F46", "#D1FAE5"),
        Patient("Emily Davis", "ID: 100982400", "Bite Correction", "ON TRACK", "#065F46", "#D1FAE5"),
        Patient("Michael Brown", "ID: 100982500", "Finishing & Detailing", "ON TRACK", "#065F46", "#D1FAE5")
    )
    private val calendar = java.util.Calendar.getInstance()

    data class Patient(val name: String, val id: String, val stage: String, val status: String, val statusColor: String, val statusBg: String)
    data class Appointment(val patient: Patient, val time: String, val type: String, val isTbd: Boolean, val notes: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("OrthoGuide", "ScheduleActivity created")
        enableEdgeToEdge()
        setContentView(R.layout.activity_schedule)

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

        findViewById<ImageView>(R.id.iv_back)?.setOnClickListener { finish() }
        timeslotsContainer = findViewById(R.id.ll_timeslots_container)
        prefs = getSharedPreferences("OrthoGuidePrefs", android.content.Context.MODE_PRIVATE)

        val tvDay = findViewById<android.widget.TextView>(R.id.tv_schedule_day)
        val tvDate = findViewById<android.widget.TextView>(R.id.tv_schedule_date)
        val dateFormat = java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.getDefault())
        val dayFormat = java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault())

        fun updateDateDisplay() {
            tvDay.text = dayFormat.format(calendar.time)
            tvDate.text = dateFormat.format(calendar.time)
            loadAppointments()
        }

        findViewById<ImageView>(R.id.iv_prev_day)?.setOnClickListener {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
            updateDateDisplay()
        }

        findViewById<ImageView>(R.id.iv_next_day)?.setOnClickListener {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
            updateDateDisplay()
        }
        
        updateDateDisplay()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        loadAppointments()
    }

    private fun loadAppointments() {
        if (!::timeslotsContainer.isInitialized) return
        timeslotsContainer.removeAllViews()
        
        // Only show mock appointments if the selected date is today
        val today = java.util.Calendar.getInstance()
        val isToday = calendar.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                      calendar.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR)

        if (!isToday) {
            val tvNoAppt = android.widget.TextView(this).apply {
                text = "No appointments scheduled for this day."
                gravity = android.view.Gravity.CENTER
                setPadding(0, 100, 0, 0)
                setTextColor(Color.parseColor("#64748B"))
            }
            timeslotsContainer.addView(tvNoAppt)
            return
        }

        val scheduledAppts = mutableListOf<Appointment>()

        for (patient in patients) {
            val hasAppt = prefs.getBoolean("appt_${patient.id}", false)
            if (hasAppt) {
                val time = prefs.getString("appt_time_${patient.id}", "Select Time") ?: "Select Time"
                val type = prefs.getString("appt_type_${patient.id}", "Regular Checkup") ?: "Regular Checkup"
                val isTbd = prefs.getBoolean("appt_tbd_${patient.id}", false)
                val notes = prefs.getString("appt_notes_${patient.id}", "") ?: ""
                scheduledAppts.add(Appointment(patient, time, type, isTbd, notes))
            }
        }

        val finalSorted = scheduledAppts.sortedWith(compareBy<Appointment> { it.isTbd }
            .thenBy { if (it.isTbd) 0 else timeToMinutes(it.time) })

        if (finalSorted.isEmpty()) {
            val tvNoAppt = android.widget.TextView(this).apply {
                text = "No appointments scheduled for this day."
                gravity = android.view.Gravity.CENTER
                setPadding(0, 100, 0, 0)
                setTextColor(Color.parseColor("#64748B"))
            }
            timeslotsContainer.addView(tvNoAppt)
        } else {
            for (appt in finalSorted) {
                addApptView(appt)
            }
        }
    }

    private fun addApptView(appt: Appointment) {
        val view = layoutInflater.inflate(R.layout.item_schedule_timeslot, timeslotsContainer, false)
        
        val tvTime = view.findViewById<android.widget.TextView>(R.id.tv_time)
        val tvAmPm = view.findViewById<android.widget.TextView>(R.id.tv_ampm)
        val tvName = view.findViewById<android.widget.TextView>(R.id.tv_patient_name)
        val tvId = view.findViewById<android.widget.TextView>(R.id.tv_patient_id)
        val tvStage = view.findViewById<android.widget.TextView>(R.id.tv_patient_stage)
        val tvType = view.findViewById<android.widget.TextView>(R.id.tv_appt_type)
        val tvStatus = view.findViewById<android.widget.TextView>(R.id.tv_status_badge)
        val ivEdit = view.findViewById<ImageView>(R.id.iv_edit)
        val ivDelete = view.findViewById<ImageView>(R.id.iv_delete)
        val card = view.findViewById<View>(R.id.card_appt)

        if (appt.isTbd) {
            tvTime.text = "TBD"
            tvTime.setTextColor(Color.parseColor("#94A3B8"))
            tvTime.setTypeface(null, android.graphics.Typeface.ITALIC)
            tvAmPm.visibility = View.GONE
        } else {
            val parts = appt.time.split(" ")
            tvTime.text = parts.getOrNull(0) ?: appt.time
            tvAmPm.text = parts.getOrNull(1) ?: ""
            tvAmPm.visibility = View.VISIBLE
        }

        tvName.text = appt.patient.name
        tvId.text = appt.patient.id
        tvStage.text = appt.patient.stage
        tvType.text = appt.type
        tvStatus.text = appt.patient.status
        tvStatus.setTextColor(Color.parseColor(appt.patient.statusColor))
        tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(appt.patient.statusBg))

        val openProfile = {
            val intent = Intent(this, PatientProfileActivity::class.java)
            intent.putExtra("patientName", appt.patient.name)
            intent.putExtra("patientId", appt.patient.id)
            intent.putExtra("hasAppointment", true)
            startActivity(intent)
        }

        card.setOnClickListener { openProfile() }
        ivEdit.setOnClickListener { openProfile() }

        ivDelete.setOnClickListener {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Delete Appointment")
                .setMessage("Are you sure you want to delete the appointment for ${appt.patient.name}?")
                .setPositiveButton("Delete") { _, _ ->
                    prefs.edit().putBoolean("appt_${appt.patient.id}", false).apply()
                    loadAppointments()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        timeslotsContainer.addView(view)
    }

    private fun timeToMinutes(timeStr: String): Int {
        return try {
            val parts = timeStr.split(" ")
            val hms = parts[0].split(":")
            var hours = hms[0].toInt()
            val minutes = if (hms.size > 1) hms[1].toInt() else 0
            val amPm = parts.getOrNull(1)?.uppercase() ?: "AM"
            
            if (amPm == "PM" && hours < 12) hours += 12
            if (amPm == "AM" && hours == 12) hours = 0
            
            hours * 60 + minutes
        } catch (e: Exception) {
            0
        }
    }

    private fun setupBottomNavigation() {
        findViewById<View>(R.id.nav_dashboard)?.setOnClickListener {
            val intent = Intent(this, ClinicianDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.nav_patients)?.setOnClickListener {
            val intent = Intent(this, PatientsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.nav_profile)?.setOnClickListener {
            val intent = Intent(this, ClinicianProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }
}
