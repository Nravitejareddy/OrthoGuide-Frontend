package com.yourname.orthoguide

import android.content.Intent
import android.os.Bundle
import android.graphics.Color
import android.view.View
import android.view.animation.AnimationUtils
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

class DashboardActivity : AppCompatActivity() {
    private var pbOverallProgress: ProgressBar? = null
    private var tvOverallProgressPct: TextView? = null
    private val progressGoal = 33

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        // Insets handling if needed for the root view, 
        // but since we have a full-screen header, we might want to apply insets to specific views.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply top padding to top bar only to keep header full bleed
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

        findViewById<View>(R.id.tab_home)?.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

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
    }


    override fun onResume() {
        super.onResume()
        animateProgress()
        
        // Show notification dot if there are "new" notifications
        val prefs = getSharedPreferences("OrthoGuidePrefs", MODE_PRIVATE)
        val notifSeen = prefs.getBoolean("notif_seen", false)
        findViewById<View>(R.id.v_notification_dot)?.visibility = if (notifSeen) View.GONE else View.VISIBLE
    }

    private fun animateProgress() {
        if (pbOverallProgress != null && tvOverallProgressPct != null) {
            pbOverallProgress?.progress = 0
            val progressBarAnimator = ObjectAnimator.ofInt(pbOverallProgress, "progress", 0, progressGoal)
            progressBarAnimator.duration = 1000
            progressBarAnimator.interpolator = DecelerateInterpolator()
            // Add slight delay to ensure activity transition is mostly complete
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
