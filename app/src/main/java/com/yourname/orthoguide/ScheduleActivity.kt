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
    private val calendar = java.util.Calendar.getInstance()

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
        prefs = getSharedPreferences("OrthoPref", android.content.Context.MODE_PRIVATE)

        val tvDay = findViewById<android.widget.TextView>(R.id.tv_schedule_day)
        val tvDate = findViewById<android.widget.TextView>(R.id.tv_schedule_date)
        val dateFormat = java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.getDefault())
        val dayFormat = java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault())

        fun updateDateDisplay(fetchData: Boolean = true) {
            tvDay.text = dayFormat.format(calendar.time)
            tvDate.text = dateFormat.format(calendar.time)
            if (fetchData) loadAppointments()
        }

        findViewById<ImageView>(R.id.iv_prev_day)?.setOnClickListener {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
            updateDateDisplay()
        }

        findViewById<ImageView>(R.id.iv_next_day)?.setOnClickListener {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
            updateDateDisplay()
        }
        
        updateDateDisplay(fetchData = false)
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        loadAppointments()
    }


    private fun loadAppointments() {
        if (!::timeslotsContainer.isInitialized) return
        
        val teacherId = prefs.getString("USER_ID", "") ?: ""
        if (teacherId.isEmpty()) return

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val dateStr = sdf.format(calendar.time)

        com.yourname.orthoguide.network.RetrofitClient.service.getClinicianSchedule(teacherId, dateStr)
            .enqueue(object : retrofit2.Callback<List<com.yourname.orthoguide.network.ScheduleItem>> {
                override fun onResponse(
                    call: retrofit2.Call<List<com.yourname.orthoguide.network.ScheduleItem>>,
                    response: retrofit2.Response<List<com.yourname.orthoguide.network.ScheduleItem>>
                ) {
                    if (response.isSuccessful) {
                        timeslotsContainer.removeAllViews()
                        val schedule = response.body() ?: emptyList()
                        if (schedule.isEmpty()) {
                            showEmptyState()
                        } else {
                            for (item in schedule) {
                                addApptView(item)
                            }
                        }
                    } else {
                        timeslotsContainer.removeAllViews()
                        showEmptyState("Error loading schedule")
                    }
                }

                override fun onFailure(call: retrofit2.Call<List<com.yourname.orthoguide.network.ScheduleItem>>, t: Throwable) {
                    timeslotsContainer.removeAllViews()
                    showEmptyState("Network error")
                }
            })
    }

    private fun showEmptyState(msg: String = "No appointments scheduled for this day.") {
        val tvNoAppt = android.widget.TextView(this).apply {
            text = msg
            gravity = android.view.Gravity.CENTER
            setPadding(0, 100, 0, 0)
            setTextColor(Color.parseColor("#64748B"))
        }
        timeslotsContainer.addView(tvNoAppt)
    }

    private fun addApptView(item: com.yourname.orthoguide.network.ScheduleItem) {
        val view = layoutInflater.inflate(R.layout.item_schedule_timeslot, timeslotsContainer, false)

        val tvTime = view.findViewById<android.widget.TextView>(R.id.tv_time)
        val tvAmPm = view.findViewById<android.widget.TextView>(R.id.tv_ampm)
        val tvName = view.findViewById<android.widget.TextView>(R.id.tv_patient_name)
        val tvId = view.findViewById<android.widget.TextView>(R.id.tv_patient_id)
        val tvStage = view.findViewById<android.widget.TextView>(R.id.tv_patient_stage)
        val tvType = view.findViewById<android.widget.TextView>(R.id.tv_appt_type)
        val tvStatus = view.findViewById<android.widget.TextView>(R.id.tv_status_badge)
        val ivComplete = view.findViewById<ImageView>(R.id.iv_complete)
        val ivDelete = view.findViewById<ImageView>(R.id.iv_delete)
        val card = view.findViewById<View>(R.id.card_appt)

        val fullTime = item.appointmentTime ?: item.time ?: "12:00 PM"
        try {
            val parts = fullTime.split(" ")
            tvTime.text = parts.getOrNull(0) ?: fullTime
            tvAmPm.text = parts.getOrNull(1) ?: ""
            tvAmPm.visibility = if (parts.size > 1) View.VISIBLE else View.GONE
        } catch (e: Exception) {
            tvTime.text = fullTime
            tvAmPm.visibility = View.GONE
        }

        tvName.text = item.patientName
        tvId.text = "ID: ${item.patientId}"
        tvStage.text = item.patientStage ?: "Initial Consultation"
        tvType.text = item.appointmentType ?: item.type ?: "Regular Checkup"
        
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val currentViewDate = (calendar.clone() as java.util.Calendar).apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val isPast = currentViewDate.before(today)

        if (isPast) {
            view.alpha = 0.5f
            card.setOnClickListener(null) // Disable profile click for past appointments
            
            // Faded grey status badge
            tvStatus.setTextColor(Color.parseColor("#64748B"))
            tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#F1F5F9"))
        } else {
            val status = item.patientStatus?.lowercase() ?: "on track"
            tvStatus.text = status.uppercase()
            when (status) {
                "critical" -> {
                    tvStatus.setTextColor(Color.parseColor("#991B1B"))
                    tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FEE2E2"))
                }
                "attention" -> {
                    tvStatus.setTextColor(Color.parseColor("#B45309"))
                    tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FEF3C7"))
                }
                else -> {
                    tvStatus.setTextColor(Color.parseColor("#065F46"))
                    tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#D1FAE5"))
                }
            }
        }

        val openProfile = {
            val intent = Intent(this, PatientProfileActivity::class.java)
            intent.putExtra("patientId", item.patientId)
            intent.putExtra("patientName", item.patientName)
            intent.putExtra("hasAppointment", true)
            intent.putExtra("appointmentId", item.id)
            startActivity(intent)
        }

        card.setOnClickListener { openProfile() }
        
        ivComplete.setOnClickListener {
            val apptId = item.id ?: return@setOnClickListener
            com.yourname.orthoguide.util.DialogUtils.showConfirmDialog(
                context = this,
                title = "Complete Appointment",
                message = "Are you sure you want to mark the appointment for ${item.patientName} as completed?",
                confirmText = "Mark Completed",
                cancelText = "Not Yet",
                onConfirm = {
                    ivComplete.setImageResource(R.drawable.ic_check_filled)
                    ivComplete.imageTintList = null // Use drawable's native blue color
                    com.yourname.orthoguide.network.RetrofitClient.service.completeAppointment(apptId)
                        .enqueue(object : retrofit2.Callback<com.yourname.orthoguide.network.GenericResponse> {
                            override fun onResponse(
                                call: retrofit2.Call<com.yourname.orthoguide.network.GenericResponse>,
                                response: retrofit2.Response<com.yourname.orthoguide.network.GenericResponse>
                            ) {
                                if (response.isSuccessful) {
                                    timeslotsContainer.removeView(view)
                                    if (timeslotsContainer.childCount == 0) showEmptyState()
                                    com.yourname.orthoguide.util.DialogUtils.showSuccessSnackbar(
                                        findViewById(android.R.id.content),
                                        "Appointment marked as completed"
                                    )
                                } else {
                                    com.yourname.orthoguide.util.DialogUtils.showError(this@ScheduleActivity, "Failed to complete")
                                }
                            }

                            override fun onFailure(call: retrofit2.Call<com.yourname.orthoguide.network.GenericResponse>, t: Throwable) {
                                com.yourname.orthoguide.util.DialogUtils.showError(this@ScheduleActivity, "Network error")
                            }
                        })
                }
            )
        }

        ivDelete.setOnClickListener {
            val apptId = item.id ?: return@setOnClickListener
            com.yourname.orthoguide.util.DialogUtils.showConfirmDialog(
                context = this,
                title = "Cancel Appointment",
                message = "Are you sure you want to cancel the appointment for ${item.patientName}?",
                confirmText = "Cancel Appointment",
                cancelText = "Keep It",
                onConfirm = {
                    com.yourname.orthoguide.network.RetrofitClient.service.deleteAppointment(apptId)
                        .enqueue(object : retrofit2.Callback<com.yourname.orthoguide.network.GenericResponse> {
                            override fun onResponse(
                                call: retrofit2.Call<com.yourname.orthoguide.network.GenericResponse>,
                                response: retrofit2.Response<com.yourname.orthoguide.network.GenericResponse>
                            ) {
                                if (response.isSuccessful) {
                                    loadAppointments()
                                    com.yourname.orthoguide.util.DialogUtils.showSuccessSnackbar(findViewById(android.R.id.content), "Appointment cancelled successfully")
                                } else {
                                    com.yourname.orthoguide.util.DialogUtils.showError(this@ScheduleActivity, "Failed to cancel")
                                }
                            }

                            override fun onFailure(call: retrofit2.Call<com.yourname.orthoguide.network.GenericResponse>, t: Throwable) {
                                com.yourname.orthoguide.util.DialogUtils.showError(this@ScheduleActivity, "Network error")
                            }
                        })
                }
            )
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
