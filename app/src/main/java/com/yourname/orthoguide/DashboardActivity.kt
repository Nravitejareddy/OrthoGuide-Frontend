package com.yourname.orthoguide

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.graphics.Color
import android.util.Log
import android.view.View
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.widget.ProgressBar
import android.widget.TextView
import android.view.animation.DecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.yourname.orthoguide.network.DashboardResponse
import com.yourname.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {
    private var pbOverallProgress: ProgressBar? = null
    private var tvOverallProgressPct: TextView? = null
    private var progressGoal = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<View>(R.id.ll_top_bar).setPadding(
                systemBars.left + dpToPx(24), 
                systemBars.top + dpToPx(24), 
                systemBars.right + dpToPx(24), 
                dpToPx(24)
            )
            insets
        }
        
        // Initialize views
        pbOverallProgress = findViewById(R.id.pb_overall_progress)
        tvOverallProgressPct = findViewById(R.id.tv_overall_progress_pct)

        // Set greeting based on time of day
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good Morning,"
            hour < 17 -> "Good Afternoon,"
            else -> "Good Evening,"
        }
        findViewById<TextView>(R.id.tv_greeting)?.text = greeting

        // Set user name from shared prefs immediately
        val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
        val userName = sharedPref.getString("USER_NAME", "Patient") ?: "Patient"
        findViewById<TextView>(R.id.tv_user_name)?.text = userName

        // Navigation
        findViewById<View>(R.id.iv_notification_bell)?.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        findViewById<View>(R.id.card_ai_assistant)?.setOnClickListener {
            startActivity(Intent(this, ChatBotActivity::class.java))
        }

        findViewById<View>(R.id.card_reminders)?.setOnClickListener {
            startActivity(Intent(this, CareRemindersActivity::class.java))
        }

        findViewById<View>(R.id.card_report_issue)?.setOnClickListener {
            startActivity(Intent(this, ReportIssueActivity::class.java))
        }

        findViewById<View>(R.id.card_care_guide)?.setOnClickListener {
            startActivity(Intent(this, CareGuideActivity::class.java))
        }

        findViewById<View>(R.id.card_appointment)?.setOnClickListener {
            startActivity(Intent(this, AppointmentsActivity::class.java))
        }

        // Bottom Navigation
        findViewById<View>(R.id.tab_home)?.setOnClickListener { /* Already here */ }

        findViewById<View>(R.id.tab_chat)?.setOnClickListener {
            startActivity(Intent(this, ChatBotActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.tab_progress)?.setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
        
        findViewById<View>(R.id.tab_profile)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        // Fetch real dashboard data - moved to onResume
    }


    override fun onResume() {
        super.onResume()
        
        // Hide badge instantly if already marked as seen locally
        val prefs = getSharedPreferences("OrthoPref", MODE_PRIVATE)
        if (prefs.getBoolean("notif_seen", false)) {
            findViewById<View>(R.id.tv_notification_count)?.visibility = View.GONE
        }

        fetchUnreadCount()
        animateProgress()
        fetchDashboardData()
    }

    private fun fetchUnreadCount() {
        val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", "") ?: ""
        if (userId.isEmpty()) return

        RetrofitClient.service.getUnreadNotificationsCount(userId, "patient").enqueue(object : Callback<com.yourname.orthoguide.network.UnreadCountResponse> {
            override fun onResponse(call: Call<com.yourname.orthoguide.network.UnreadCountResponse>, response: Response<com.yourname.orthoguide.network.UnreadCountResponse>) {
                if (response.isSuccessful) {
                    val currentUnreadCount = response.body()?.unreadCount ?: 0
                    // Store current count for next time comparison
                    sharedPref.edit().putInt("LAST_KNOWN_UNREAD_COUNT", currentUnreadCount).apply()

                    val badge = findViewById<TextView>(R.id.tv_notification_count)
                    if (currentUnreadCount > 0) {
                        badge?.visibility = View.VISIBLE
                        badge?.text = if (currentUnreadCount > 9) "9+" else currentUnreadCount.toString()
                    } else {
                        badge?.visibility = View.GONE
                    }
                }
            }
            override fun onFailure(call: Call<com.yourname.orthoguide.network.UnreadCountResponse>, t: Throwable) {
                Log.e("OrthoGuide", "Failed to fetch unread count", t)
            }
        })
    }

    private fun fetchDashboardData() {
        val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", "") ?: ""
        if (userId.isEmpty()) return

        RetrofitClient.service.getPatientDashboard(userId).enqueue(object : Callback<DashboardResponse> {
            override fun onResponse(call: Call<DashboardResponse>, response: Response<DashboardResponse>) {
                if (response.isSuccessful) {
                    val data = response.body() ?: return

                    // Update user name
                    data.name?.let { 
                        findViewById<TextView>(R.id.tv_user_name)?.text = it 
                    }

                    // Update treatment stage
                    val currentStage = data.treatmentPhase ?: data.treatmentStage
                    currentStage?.let {
                        findViewById<TextView>(R.id.tv_current_stage)?.text = it
                        findViewById<TextView>(R.id.tv_treatment_status)?.text = 
                            if (data.status == "on track") "Treatment in Progress" else data.status ?: "Treatment in Progress"
                    }

                    // Update progress from API
                    progressGoal = data.progressPercent ?: 0
                    animateProgress()

                    // Update next appointment
                    val appt = data.nextAppointment
                    if (appt != null && appt.date != null) {
                        try {
                            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val date = inputFormat.parse(appt.date)
                            if (date != null) {
                                val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
                                val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
                                findViewById<TextView>(R.id.tv_appt_month)?.text = monthFormat.format(date).uppercase()
                                findViewById<TextView>(R.id.tv_appt_day)?.text = dayFormat.format(date)
                            }
                        } catch (e: Exception) {
                            Log.e("OrthoGuide", "Date parse error", e)
                        }
                        
                        findViewById<TextView>(R.id.tv_appt_title)?.text = appt.type ?: "Regular Checkup"
                        val clinician = appt.clinicianName ?: data.doctorName ?: "Doctor"
                        val clinicianWithPrefix = if (clinician.startsWith("Dr.")) clinician else "Dr. $clinician"
                        val details = "${appt.time ?: "TBD"} • $clinicianWithPrefix"
                        findViewById<TextView>(R.id.tv_appt_details)?.text = details
                    } else {
                        findViewById<TextView>(R.id.tv_appt_title)?.text = "No Upcoming"
                        findViewById<TextView>(R.id.tv_appt_details)?.text = "No appointments scheduled"
                        
                        val today = Calendar.getInstance().time
                        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
                        val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
                        findViewById<TextView>(R.id.tv_appt_month)?.text = monthFormat.format(today).uppercase()
                        findViewById<TextView>(R.id.tv_appt_day)?.text = dayFormat.format(today)
                    }

                    // Update daily tip if available
                    data.dailyTip?.let { tip ->
                        findViewById<TextView>(R.id.tv_daily_tip)?.text = tip
                    }

                    // Update doctor name
                    data.doctorName?.let { doctor ->
                        val doctorWithPrefix = if (doctor.startsWith("Dr.")) doctor else "Dr. $doctor"
                        findViewById<TextView>(R.id.tv_doctor_name)?.text = doctorWithPrefix
                    }
                } else {
                    // Fallback to default progress
                    progressGoal = 33
                    animateProgress()
                }
            }

            override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {
                Log.e("OrthoGuide", "Dashboard API failed", t)
                progressGoal = 33
                animateProgress()
            }
        })
    }

    private fun animateProgress() {
        if (pbOverallProgress != null && tvOverallProgressPct != null) {
            pbOverallProgress?.progress = 0
            val progressBarAnimator = ObjectAnimator.ofInt(pbOverallProgress, "progress", 0, progressGoal)
            progressBarAnimator.duration = 1000
            progressBarAnimator.interpolator = DecelerateInterpolator()
            progressBarAnimator.startDelay = 300
            progressBarAnimator.start()

            val textAnimator = ValueAnimator.ofInt(0, progressGoal)
            textAnimator.duration = 1000
            textAnimator.interpolator = DecelerateInterpolator()
            textAnimator.startDelay = 300
            textAnimator.addUpdateListener { animation ->
                tvOverallProgressPct?.text = "${animation.animatedValue}%"
            }
            textAnimator.start()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
