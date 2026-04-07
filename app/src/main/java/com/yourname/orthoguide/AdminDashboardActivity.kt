package com.yourname.orthoguide

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Typeface
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.yourname.orthoguide.network.AnalyticsOverviewResponse
import com.yourname.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import android.widget.Toast
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import com.yourname.orthoguide.network.AdminUsersResponse
import com.yourname.orthoguide.network.UnreadCountResponse
import com.google.android.material.button.MaterialButton
import android.text.SpannableString
import android.text.style.*
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import android.view.View

// Explicitly import the generated R class to resolve reference issues
import com.yourname.orthoguide.R

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

        // Navigation Actions
        val openClinicians = {
            startActivity(Intent(this, ManageCliniciansActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        val openPatients = {
            startActivity(Intent(this, ManagePatientsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.card_active_patients)?.setOnClickListener { openPatients() }
        findViewById<View>(R.id.card_total_patients)?.setOnClickListener { openPatients() }
        findViewById<View>(R.id.card_active_clinicians)?.setOnClickListener { openClinicians() }
        findViewById<View>(R.id.card_total_clinicians)?.setOnClickListener { openClinicians() }

        findViewById<View>(R.id.admin_nav_clinicians)?.setOnClickListener {
            startActivity(Intent(this, ManageCliniciansActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.admin_nav_patients)?.setOnClickListener {
            startActivity(Intent(this, ManagePatientsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.admin_nav_settings)?.setOnClickListener {
            startActivity(Intent(this, AdminSettingsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.iv_admin_notifications_bell)?.setOnClickListener {
            getSharedPreferences("OrthoPref", MODE_PRIVATE).edit().putBoolean("admin_notif_seen", true).apply()
            startActivity(Intent(this, AdminNotificationsActivity::class.java))
        }

        updateNotificationDot()
        fetchAnalytics()
        setupExportButton()
    }

    private fun setupExportButton() {
        findViewById<MaterialButton>(R.id.btn_generate_csv)?.setOnClickListener {
            exportSystemData()
        }
    }

    private fun exportSystemData() {
        RetrofitClient.service.getAllUsers().enqueue(object : Callback<AdminUsersResponse> {
            override fun onResponse(call: Call<AdminUsersResponse>, response: Response<AdminUsersResponse>) {
                if (response.isSuccessful) {
                    val users = response.body() ?: return
                    val csv = generateCsv(users)
                    shareCsv(csv)
                } else {
                    Toast.makeText(this@AdminDashboardActivity, "Failed to fetch user data for export", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AdminUsersResponse>, t: Throwable) {
                Toast.makeText(this@AdminDashboardActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun generateCsv(users: AdminUsersResponse): String {
        val builder = StringBuilder()
        builder.append("ID,Name,Email,Phone,Role,Status,Created At\n")
        
        users.patients?.forEach { 
            builder.append("${it.id},${it.name},${it.email},${it.phoneNumber},Patient,${it.status},${it.createdAt}\n")
        }
        users.clinicians?.forEach { 
            builder.append("${it.id},${it.name},${it.email},${it.phoneNumber},Clinician,${it.status},${it.createdAt}\n")
        }
        users.admins?.forEach { 
            builder.append("${it.id},${it.name},${it.email},${it.phoneNumber},Admin,${it.status},${it.createdAt}\n")
        }
        
        return builder.toString()
    }

    private fun shareCsv(csv: String) {
        try {
            val fileName = "system_export_${System.currentTimeMillis()}.csv"
            val file = File(cacheDir, fileName)
            file.writeText(csv)
            
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
            
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/csv"
            intent.putExtra(Intent.EXTRA_SUBJECT, "OrthoGuide System Data Export")
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            
            startActivity(Intent.createChooser(intent, "Export Data"))
        } catch (e: Exception) {
            Log.e("OrthoGuide", "Export failed", e)
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateNotificationDot() {
        val prefs = getSharedPreferences("OrthoPref", MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", "") ?: ""
        
        RetrofitClient.service.getUnreadNotificationsCount(userId, "admin").enqueue(object : Callback<UnreadCountResponse> {
            override fun onResponse(call: Call<UnreadCountResponse>, response: Response<UnreadCountResponse>) {
                if (response.isSuccessful) {
                    val count = response.body()?.unreadCount ?: 0
                    val badge = findViewById<TextView>(R.id.tv_notif_count)
                    if (count > 0) {
                        badge?.visibility = View.VISIBLE
                        badge?.text = if (count > 9) "9+" else count.toString()
                    } else {
                        badge?.visibility = View.GONE
                    }
                }
            }
            override fun onFailure(call: Call<UnreadCountResponse>, t: Throwable) {
                // Ignore failure
            }
        })
    }

    override fun onResume() {
        super.onResume()
        updateHeader()
        updateNotificationDot()
        fetchAnalytics()
    }

    private fun fetchAnalytics() {
        RetrofitClient.service.getAnalyticsOverview().enqueue(object : Callback<AnalyticsOverviewResponse> {
            override fun onResponse(call: Call<AnalyticsOverviewResponse>, response: Response<AnalyticsOverviewResponse>) {
                if (response.isSuccessful) {
                    val data = response.body() ?: return
                    findViewById<TextView>(R.id.tv_total_patients_count)?.text = (data.totalPatients ?: 0).toString()
                    findViewById<TextView>(R.id.tv_total_clinicians_count)?.text = (data.totalClinicians ?: 0).toString()
                    findViewById<TextView>(R.id.tv_active_patients_count)?.text = (data.activePatients ?: 0).toString()
                    findViewById<TextView>(R.id.tv_active_clinicians_count)?.text = (data.activeClinicians ?: 0).toString()

                    setupDistributionChart(data)
                    updateCustomLegend(data)
                } else {
                    Log.e("OrthoGuide", "Analytics fetch failed")
                }
            }

            override fun onFailure(call: Call<AnalyticsOverviewResponse>, t: Throwable) {
                Log.e("OrthoGuide", "Analytics API failed", t)
            }
        })
    }

    private fun setupDistributionChart(data: AnalyticsOverviewResponse) {
        val chart = findViewById<PieChart>(R.id.chart_distribution) ?: return
        val entries = mutableListOf<PieEntry>()
        entries.add(PieEntry((data.totalPatients ?: 0).toFloat(), "Patients"))
        entries.add(PieEntry((data.totalClinicians ?: 0).toFloat(), "Clinicians"))
        entries.add(PieEntry((data.totalAdmins ?: 0).toFloat(), "Admins"))

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#9333ea"),
                Color.parseColor("#3b82f6"),
                Color.parseColor("#bfdbfe")
            )
            sliceSpace = 4f
            setDrawValues(false)
        }

        chart.apply {
            this.data = PieData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            setDrawEntryLabels(false)
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            transparentCircleRadius = 74f
            holeRadius = 68f
            
            val totalUsers = data.totalUsers ?: 0
            val centerText = SpannableString("${totalUsers}\nTOTAL USERS")
            centerText.setSpan(RelativeSizeSpan(3.5f), 0, totalUsers.toString().length, 0)
            centerText.setSpan(StyleSpan(Typeface.BOLD), 0, totalUsers.toString().length, 0)
            centerText.setSpan(ForegroundColorSpan(Color.parseColor("#0f172a")), 0, totalUsers.toString().length, 0)
            val subtitleStart = totalUsers.toString().length + 1
            centerText.setSpan(RelativeSizeSpan(0.8f), subtitleStart, centerText.length, 0)
            centerText.setSpan(StyleSpan(Typeface.BOLD), subtitleStart, centerText.length, 0)
            centerText.setSpan(ForegroundColorSpan(Color.parseColor("#94a3b8")), subtitleStart, centerText.length, 0)
            
            this.centerText = centerText
            this.marker = CustomChartMarker(this@AdminDashboardActivity, R.layout.layout_chart_marker)
            animateY(1200)
            invalidate()
        }
    }

    private fun updateCustomLegend(data: AnalyticsOverviewResponse) {
        val total = (data.totalUsers ?: 1).toFloat()
        val patientsPct = ((data.totalPatients ?: 0) / total * 100).toInt()
        val cliniciansPct = ((data.totalClinicians ?: 0) / total * 100).toInt()
        val adminsPct = ((data.totalAdmins ?: 0) / total * 100).toInt()

        findViewById<TextView>(R.id.tv_patients_percentage)?.text = "${patientsPct}%"
        findViewById<TextView>(R.id.tv_clinicians_percentage)?.text = "${cliniciansPct}%"
        findViewById<TextView>(R.id.tv_admins_percentage)?.text = "${adminsPct}%"
    }

    class CustomChartMarker(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {
        private val tvContent: TextView = findViewById(R.id.tv_marker_content)
        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            if (e is PieEntry) {
                tvContent.text = "${e.label} : ${e.value.toInt()}"
            }
            super.refreshContent(e, highlight)
        }
        override fun getOffset(): MPPointF = MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }

    private fun updateHeader() {
        val tvDate = findViewById<TextView>(R.id.tv_today_date)
        val tvName = findViewById<TextView>(R.id.tv_admin_name_display)
        val tvGreeting = findViewById<TextView>(R.id.tv_greeting)
        val sdf = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        tvDate?.text = sdf.format(Date())
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        tvGreeting?.text = when {
            hour < 12 -> "Good Morning,"
            hour < 17 -> "Good Afternoon,"
            else -> "Good Evening,"
        }
        val adminName = getSharedPreferences("OrthoPref", MODE_PRIVATE).getString("USER_NAME", "System Admin")
        tvName?.text = adminName
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}
