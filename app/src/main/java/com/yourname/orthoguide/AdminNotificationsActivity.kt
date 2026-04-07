package com.yourname.orthoguide

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yourname.orthoguide.adapters.ReactivationRequestAdapter
import com.yourname.orthoguide.util.DialogUtils
import com.yourname.orthoguide.network.GenericResponse
import com.yourname.orthoguide.network.ReactivationRequestItem
import com.yourname.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminNotificationsActivity : AppCompatActivity() {

    private lateinit var rvRequests: RecyclerView
    private lateinit var adapter: ReactivationRequestAdapter
    private lateinit var pbLoading: ProgressBar
    private lateinit var llEmptyState: View
    private var lastInteractionTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_notifications)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val root = findViewById<View>(R.id.cl_admin_notif_root)
        ViewCompat.setOnApplyWindowInsetsListener(root ?: findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        rvRequests = findViewById(R.id.rv_reactivation_requests)
        pbLoading = findViewById(R.id.pb_admin_notif)
        llEmptyState = findViewById(R.id.ll_empty_state)

        rvRequests.layoutManager = LinearLayoutManager(this)
        adapter = ReactivationRequestAdapter(emptyList()) { request ->
            // Mark as read on server
            if (!request.isRead) {
                RetrofitClient.service.markAdminReactivationRead(request.id).enqueue(object : Callback<GenericResponse> {
                    override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                        if (response.isSuccessful) {
                            // Instant update in UI
                            request.isRead = true
                            adapter.notifyDataSetChanged()
                        }
                    }
                    override fun onFailure(call: Call<GenericResponse>, t: Throwable) {}
                })
            }

            val targetActivity = if (request.userRole.lowercase() == "clinician") {
                ManageCliniciansActivity::class.java
            } else {
                ManagePatientsActivity::class.java
            }
            val intent = Intent(this, targetActivity)
            intent.putExtra("SEARCH_QUERY", request.patientId)
            startActivity(intent)
        }
        rvRequests.adapter = adapter

        findViewById<View>(R.id.iv_back).setOnClickListener { finish() }

        findViewById<View>(R.id.tv_mark_all_read)?.setOnClickListener {
            markAllRead()
        }



        setupBottomNavigation()
        fetchRequests()
    }

    private fun setupBottomNavigation() {
        findViewById<View>(R.id.admin_nav_dashboard)?.setOnClickListener {
            if (System.currentTimeMillis() - lastInteractionTime < 500) return@setOnClickListener
            val intent = Intent(this, AdminDashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.admin_nav_clinicians)?.setOnClickListener {
            if (System.currentTimeMillis() - lastInteractionTime < 500) return@setOnClickListener
            val intent = Intent(this, ManageCliniciansActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.admin_nav_patients)?.setOnClickListener {
            if (System.currentTimeMillis() - lastInteractionTime < 500) return@setOnClickListener
            val intent = Intent(this, ManagePatientsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.admin_nav_settings)?.setOnClickListener {
            if (System.currentTimeMillis() - lastInteractionTime < 500) return@setOnClickListener
            val intent = Intent(this, AdminSettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    private fun fetchRequests() {
        pbLoading.visibility = View.VISIBLE
        llEmptyState.visibility = View.GONE
        
        RetrofitClient.service.getReactivationRequests().enqueue(object : Callback<List<ReactivationRequestItem>> {
            override fun onResponse(call: Call<List<ReactivationRequestItem>>, response: Response<List<ReactivationRequestItem>>) {
                pbLoading.visibility = View.GONE
                if (response.isSuccessful) {
                    val requests = response.body() ?: emptyList()
                    adapter.updateData(requests)
                    
                    if (requests.isEmpty()) {
                        llEmptyState.visibility = View.VISIBLE
                    }
                } else {
                    DialogUtils.showError(findViewById(android.R.id.content), "Failed to fetch notifications")
                }
            }

            override fun onFailure(call: Call<List<ReactivationRequestItem>>, t: Throwable) {
                pbLoading.visibility = View.GONE
                DialogUtils.showError(findViewById(android.R.id.content), "Network error")
            }
        })
    }

    private fun markAllRead() {
        val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", "") ?: ""
        if (userId.isEmpty()) return

        val body = mapOf(
            "user_id" to userId,
            "role" to "admin"
        )

        pbLoading.visibility = View.VISIBLE
        RetrofitClient.service.markAllNotificationsRead(body).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                pbLoading.visibility = View.GONE
                if (response.isSuccessful) {
                    // Refresh data from server to get updated is_read flags
                    fetchRequests()
                    DialogUtils.showSuccess(findViewById(android.R.id.content), "All marked as read")
                }
            }
            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                pbLoading.visibility = View.GONE
                DialogUtils.showError(findViewById(android.R.id.content), "Failed to mark all read")
            }
        })
    }
}
