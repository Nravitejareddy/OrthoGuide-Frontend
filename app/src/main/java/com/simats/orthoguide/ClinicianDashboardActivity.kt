package com.simats.orthoguide

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.simats.orthoguide.network.ClinicianDashboardResponse
import com.simats.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

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
                v.setPadding(systemBars.left, 0, systemBars.right, 0)
                val topBar = findViewById<View>(R.id.ll_top_bar)
                topBar?.setPadding(
                    topBar.paddingLeft,
                    systemBars.top + dpToPx(12),
                    topBar.paddingRight,
                    topBar.paddingBottom
                )
                insets
            }
        }

        // Set greeting
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good Morning,"
            hour < 17 -> "Good Afternoon,"
            else -> "Good Evening,"
        }
        findViewById<TextView>(R.id.tv_greeting)?.text = greeting

        // Set user name from prefs
        val prefs = getSharedPreferences("OrthoPref", MODE_PRIVATE)
        val userName = prefs.getString("USER_NAME", "Doctor")
        findViewById<TextView>(R.id.tv_doctor_name)?.text = "Dr. $userName"

        // Bottom Navigation
        findViewById<View>(R.id.patients_nav_item)?.setOnClickListener {
            startActivity(Intent(this, PatientsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.nav_schedule)?.setOnClickListener {
            startActivity(Intent(this, ScheduleActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.nav_profile)?.setOnClickListener {
            startActivity(Intent(this, ClinicianProfileActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        // View All Patients
        findViewById<View>(R.id.tv_view_all_patients)?.setOnClickListener {
            startActivity(Intent(this, PatientsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        // Set current date
        val tvDashboardDate = findViewById<TextView>(R.id.tv_dashboard_date)
        val dateFormat = java.text.SimpleDateFormat("EEEE, MMM d", java.util.Locale.getDefault())
        tvDashboardDate?.text = dateFormat.format(java.util.Date())

        fetchDashboardData()
    }

    override fun onResume() {
        super.onResume()
        fetchDashboardData()
    }
    
    private fun fetchDashboardData() {
        Log.d("OrthoGuide", "fetchDashboardData called")
        val prefs = getSharedPreferences("OrthoPref", MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", "") ?: ""
        if (userId.isEmpty()) {
            Log.e("OrthoGuide", "No USER_ID found in prefs")
            return
        }

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val dateStr = sdf.format(java.util.Date())

        RetrofitClient.service.getClinicianDashboard(userId, dateStr).enqueue(object : Callback<ClinicianDashboardResponse> {
            override fun onResponse(call: Call<ClinicianDashboardResponse>, response: Response<ClinicianDashboardResponse>) {
                if (response.isSuccessful) {
                    val data = response.body() ?: return

                    // Update stat cards
                    findViewById<TextView>(R.id.tv_total_patients)?.text = (data.totalPatients ?: 0).toString()
                    findViewById<TextView>(R.id.tv_appointments_today)?.text = (data.appointmentsToday ?: 0).toString()
                    findViewById<TextView>(R.id.tv_priority_count)?.text = (data.needAttention ?: 0).toString()

                    // Update recent patients list
                    val patientsContainer = findViewById<LinearLayout>(R.id.ll_patients_list)
                    if (patientsContainer != null && data.recentPatients != null) {
                        patientsContainer.removeAllViews()
                        
                        if (data.recentPatients.isEmpty()) {
                            val emptyView = TextView(this@ClinicianDashboardActivity)
                            emptyView.text = "No patients assigned yet."
                            emptyView.setTextColor(Color.parseColor("#9CA3AF"))
                            emptyView.textSize = 14f
                            emptyView.setPadding(dpToPx(16), dpToPx(24), dpToPx(16), dpToPx(24))
                            patientsContainer.addView(emptyView)
                        } else {
                            data.recentPatients.take(3).forEach { patient ->
                                val patientView = LayoutInflater.from(this@ClinicianDashboardActivity)
                                    .inflate(R.layout.item_priority_patient, patientsContainer, false)
                                
                                patientView.findViewById<TextView>(R.id.tv_patient_name)?.text = patient.name
                                patientView.findViewById<TextView>(R.id.tv_patient_id)?.text = "ID: ${patient.patientId}"
                                patientView.findViewById<TextView>(R.id.tv_patient_stage)?.text = patient.treatmentStage ?: "Initial Consultation"
                                
                                val statusView = patientView.findViewById<TextView>(R.id.tv_patient_status)
                                val ribbonView = patientView.findViewById<View>(R.id.v_ribbon)
                                
                                if (statusView != null && ribbonView != null) {
                                    statusView.text = patient.status?.uppercase()
                                    if (patient.status?.lowercase() == "critical") {
                                        ribbonView.setBackgroundResource(R.drawable.bg_ribbon_red)
                                        statusView.setBackgroundResource(R.drawable.bg_tag_red)
                                        statusView.setTextColor(Color.parseColor("#991B1B"))
                                    } else {
                                        ribbonView.setBackgroundResource(R.drawable.bg_ribbon_orange)
                                        statusView.setBackgroundResource(R.drawable.bg_tag_attention_yellow)
                                        statusView.setTextColor(Color.parseColor("#B45309"))
                                    }
                                }
                                
                                patientView.setOnClickListener {
                                    val intent = Intent(this@ClinicianDashboardActivity, PatientProfileActivity::class.java)
                                    intent.putExtra("patientName", patient.name)
                                    intent.putExtra("patientId", patient.patientId)
                                    startActivity(intent)
                                }
                                
                                patientsContainer.addView(patientView)
                            }
                        }
                    }

                    // Update today's schedule
                    val scheduleContainer = findViewById<LinearLayout>(R.id.ll_schedule_list)
                    if (scheduleContainer != null && data.todaySchedule != null) {
                        scheduleContainer.removeAllViews()
                        
                        if (data.todaySchedule.isEmpty()) {
                            val emptyView = TextView(this@ClinicianDashboardActivity)
                            emptyView.text = "No scheduled visits today."
                            emptyView.setTextColor(Color.parseColor("#9CA3AF"))
                            emptyView.textSize = 14f
                            emptyView.setPadding(dpToPx(16), dpToPx(24), dpToPx(16), dpToPx(8))
                            scheduleContainer.addView(emptyView)

                            // Show Next Appointment if available
                            if (data.nextAppointment != null) {
                                val nextHeader = TextView(this@ClinicianDashboardActivity)
                                val dateParts = data.nextAppointment.appointmentDate?.split("-")
                                val dateDisplay = if (dateParts?.size == 3) {
                                    "${dateParts[2]}/${dateParts[1]}"
                                } else data.nextAppointment.appointmentDate ?: ""
                                
                                nextHeader.text = "NEXT APPOINTMENT: $dateDisplay"
                                nextHeader.setTextColor(Color.parseColor("#2563EB"))
                                nextHeader.textSize = 12f
                                nextHeader.setTypeface(null, android.graphics.Typeface.BOLD)
                                nextHeader.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8))
                                scheduleContainer.addView(nextHeader)

                                val nextView = LayoutInflater.from(this@ClinicianDashboardActivity)
                                    .inflate(R.layout.item_schedule_entry, scheduleContainer, false)
                                
                                nextView.findViewById<TextView>(R.id.tv_schedule_time)?.text = data.nextAppointment.appointmentTime ?: "TBD"
                                nextView.findViewById<TextView>(R.id.tv_schedule_patient)?.text = data.nextAppointment.patientName ?: "Patient"
                                nextView.findViewById<TextView>(R.id.tv_schedule_type)?.text = data.nextAppointment.appointmentType ?: "Checkup"
                                
                                nextView.setOnClickListener {
                                    val intent = Intent(this@ClinicianDashboardActivity, ScheduleActivity::class.java)
                                    // Could pass date to ScheduleActivity here if it supported it
                                    startActivity(intent)
                                }
                                scheduleContainer.addView(nextView)
                            }
                        } else {
                            data.todaySchedule.forEach { item ->
                                val scheduleView = LayoutInflater.from(this@ClinicianDashboardActivity)
                                    .inflate(R.layout.item_schedule_entry, scheduleContainer, false)
                                
                                scheduleView.findViewById<TextView>(R.id.tv_schedule_time)?.text = item.time ?: item.appointmentTime ?: "TBD"
                                scheduleView.findViewById<TextView>(R.id.tv_schedule_patient)?.text = item.patientName ?: "Patient"
                                scheduleView.findViewById<TextView>(R.id.tv_schedule_type)?.text = item.type ?: item.appointmentType ?: "Checkup"
                                
                                scheduleContainer.addView(scheduleView)
                            }
                        }
                    }

                } else {
                    Log.e("OrthoGuide", "Clinician dashboard fetch failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ClinicianDashboardResponse>, t: Throwable) {
                Log.e("OrthoGuide", "Clinician Dashboard API failed", t)
            }
        })
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}

