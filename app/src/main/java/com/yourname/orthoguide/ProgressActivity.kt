package com.yourname.orthoguide

import android.content.Intent
import android.os.Bundle
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

class ProgressActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_progress)
        androidx.core.view.WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        // Back button
        findViewById<View>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val progressGoal = 33
        
        val pbOverallProgress = findViewById<ProgressBar>(R.id.pb_overall_progress_circle)
        val tvOverallProgressPct = findViewById<TextView>(R.id.tv_overall_progress_pct_circle)
        
        if (pbOverallProgress != null && tvOverallProgressPct != null) {
            pbOverallProgress.progress = 0
            val progressBarAnimator = ObjectAnimator.ofInt(pbOverallProgress, "progress", 0, progressGoal)
            progressBarAnimator.duration = 1000
            progressBarAnimator.interpolator = DecelerateInterpolator()
            progressBarAnimator.start()

            val textAnimator = ValueAnimator.ofInt(0, progressGoal)
            textAnimator.duration = 1000
            textAnimator.interpolator = DecelerateInterpolator()
            textAnimator.addUpdateListener { animation ->
                tvOverallProgressPct.text = "${animation.animatedValue}%"
            }
            textAnimator.start()
        }

        // Bottom Navigation
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
}
