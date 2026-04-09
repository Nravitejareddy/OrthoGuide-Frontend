package com.simats.orthoguide

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.simats.orthoguide.network.DashboardResponse
import com.simats.orthoguide.network.RetrofitClient
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.orthoguide.adapters.TimelineAdapter

class ProgressActivity : AppCompatActivity() {

    private var pbProgress: ProgressBar? = null
    private var tvProgressPct: TextView? = null
    private var progressGoal = 0
    private lateinit var timelineAdapter: TimelineAdapter
    private lateinit var rvTimeline: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_progress)

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.card_toolbar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        findViewById<View>(R.id.iv_back).setOnClickListener {
            finish()
        }

        pbProgress = findViewById(R.id.pb_overall_progress_circle)
        tvProgressPct = findViewById(R.id.tv_overall_progress_pct_circle)
        
        // Initialize RecyclerView
        rvTimeline = findViewById(R.id.rv_treatment_journey)
        rvTimeline.layoutManager = LinearLayoutManager(this)
        timelineAdapter = TimelineAdapter(emptyList())
        rvTimeline.adapter = timelineAdapter

        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        animateProgress() // Show animation starting from 0 (or previous) immediately
        fetchProgressData()
    }

    private fun fetchProgressData() {
        val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", "") ?: ""
        if (userId.isEmpty()) return

        RetrofitClient.service.getPatientDashboard(userId).enqueue(object : Callback<DashboardResponse> {
            override fun onResponse(call: Call<DashboardResponse>, response: Response<DashboardResponse>) {
                if (response.isSuccessful) {
                    val data = response.body() ?: return
                    
                    // Update progress from API
                    progressGoal = data.progressPercent ?: 0
                    animateProgress()
                    
                    // Update timeline
                    data.timeline?.let {
                        timelineAdapter.updateData(it)
                    }
                    
                    findViewById<TextView>(R.id.tv_treatment_stage_summary)?.text = 
                        "${data.treatmentPhase ?: data.treatmentStage ?: "Active Treatment"} (Stage ${data.currentStageNumber ?: 1} of ${data.totalStages ?: 6})"
                }
            }

            override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {
                Log.e("OrthoGuide", "Progress fetch failed", t)
                progressGoal = 33
                animateProgress()
            }
        })
    }

    private fun animateProgress() {
        pbProgress?.let { pb ->
            pb.progress = 0
            val animator = ObjectAnimator.ofInt(pb, "progress", 0, progressGoal)
            animator.duration = 1000
            animator.interpolator = DecelerateInterpolator()
            animator.start()
        }

        tvProgressPct?.let { tv ->
            val animator = ValueAnimator.ofInt(0, progressGoal)
            animator.duration = 1000
            animator.addUpdateListener { animation ->
                tv.text = "${animation.animatedValue}%"
            }
            animator.start()
        }
    }

    private fun setupBottomNavigation() {
        findViewById<View>(R.id.tab_home).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.tab_chat).setOnClickListener {
            startActivity(Intent(this, ChatBotActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.tab_progress).setOnClickListener { /* Already here */ }
        
        findViewById<View>(R.id.tab_profile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }
}

