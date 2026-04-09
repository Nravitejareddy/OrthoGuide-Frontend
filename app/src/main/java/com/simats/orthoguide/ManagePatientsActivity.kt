package com.simats.orthoguide

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.simats.orthoguide.adapters.AdminUserAdapter
import com.simats.orthoguide.util.DialogUtils
import com.simats.orthoguide.network.AdminUserItem
import com.simats.orthoguide.network.AdminUsersResponse
import com.simats.orthoguide.network.GenericResponse
import com.simats.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ManagePatientsActivity : AppCompatActivity() {

    private lateinit var adapter: AdminUserAdapter
    private var allPatients = mutableListOf<AdminUserItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_patients)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val rootView = findViewById<View>(R.id.manage_patients_root)
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

        setupRecyclerView()
        setupSearch()
        
        // Handle smart navigation from notifications
        val searchQuery = intent.getStringExtra("SEARCH_QUERY")
        if (!searchQuery.isNullOrBlank()) {
            findViewById<EditText>(R.id.et_search)?.setText(searchQuery)
        }

        fetchPatients()

        findViewById<View>(R.id.btn_add_patient)?.setOnClickListener {
            val intent = Intent(this, CreatePatientActivity::class.java)
            intent.putExtra("isEditMode", false)
            startActivity(intent)
        }

        // Navigation
        findViewById<View>(R.id.admin_nav_dashboard)?.setOnClickListener {
            val intent = Intent(this, AdminDashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.admin_nav_clinicians)?.setOnClickListener {
            val intent = Intent(this, ManageCliniciansActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.admin_nav_settings)?.setOnClickListener {
            val intent = Intent(this, AdminSettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    private fun setupRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.rv_patients)
        adapter = AdminUserAdapter(
            users = emptyList(),
            onUserClick = { user -> editPatient(user) },
            onResetPassword = { user -> resetPassword(user) },
            onToggleStatus = { user -> toggleStatus(user) }
        )
        rv.adapter = adapter
    }

    private fun setupSearch() {
        findViewById<EditText>(R.id.et_search)?.addTextChangedListener { text ->
            val query = text.toString().lowercase()
            val filtered = if (query.isEmpty()) {
                allPatients
            } else {
                allPatients.filter { 
                    it.name?.lowercase()?.contains(query) == true || 
                    it.id.lowercase().contains(query) 
                }
            }
            adapter.updateData(filtered)
        }
    }

    private fun fetchPatients() {
        RetrofitClient.service.getAllUsers().enqueue(object : Callback<AdminUsersResponse> {
            override fun onResponse(call: Call<AdminUsersResponse>, response: Response<AdminUsersResponse>) {
                if (response.isSuccessful) {
                    allPatients.clear()
                    response.body()?.patients?.let { allPatients.addAll(it) }
                    
                    // Re-apply search filter if there's text in search bar
                    val currentQuery = findViewById<EditText>(R.id.et_search)?.text?.toString()?.lowercase() ?: ""
                    if (currentQuery.isNotEmpty()) {
                        val filtered = allPatients.filter { 
                            it.name?.lowercase()?.contains(currentQuery) == true || 
                            it.id.lowercase().contains(currentQuery) 
                        }
                        adapter.updateData(filtered)
                    } else {
                        adapter.updateData(allPatients)
                    }
                    
                    // Update header subtitle
                    val subtitle = findViewById<android.widget.TextView>(R.id.tv_header_subtitle)
                    subtitle?.text = "${allPatients.size} Registered Patients"
                } else {
                    Toast.makeText(this@ManagePatientsActivity, "Failed to load patients", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AdminUsersResponse>, t: Throwable) {
                Toast.makeText(this@ManagePatientsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun editPatient(user: AdminUserItem) {
        val intent = Intent(this, CreatePatientActivity::class.java).apply {
            putExtra("isEditMode", true)
            putExtra("patientId", user.id)
            putExtra("patientName", user.name)
            putExtra("patientEmail", user.email)
            putExtra("patientPhone", user.phoneNumber)
            putExtra("treatmentStage", user.treatmentStage)
        }
        startActivity(intent)
    }

    private fun resetPassword(user: AdminUserItem) {
        val request = mapOf("id" to user.id, "role" to "patient")
        RetrofitClient.service.adminResetPassword(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful) {
                    DialogUtils.showSuccessDialog(
                        this@ManagePatientsActivity,
                        "Success",
                        response.body()?.message ?: "Password has been reset successfully."
                    )
                    } else {
                        val errorMsg = try {
                            val errorBody = response.errorBody()?.string()
                            val errorJson = com.google.gson.Gson().fromJson(errorBody, GenericResponse::class.java)
                            errorJson.error ?: errorJson.message ?: "Reset failed"
                        } catch (e: Exception) { "Reset failed" }
                        Toast.makeText(this@ManagePatientsActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Toast.makeText(this@ManagePatientsActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun toggleStatus(user: AdminUserItem) {
        val isActive = user.status?.lowercase() == "active"
        val newStatus = if (isActive) "Inactive" else "Active"
        
        val request = mapOf(
            "id" to user.id,
            "role" to "patient",
            "status" to newStatus
        )

        RetrofitClient.service.adminUpdateUser(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful) {
                    DialogUtils.showSuccessDialog(
                        this@ManagePatientsActivity,
                        "Status Updated",
                        "Account status for ${user.name} has been set to $newStatus."
                    )
                    fetchPatients() // Refresh list
                    } else {
                        val errorMsg = try {
                            val errorBody = response.errorBody()?.string()
                            val errorJson = com.google.gson.Gson().fromJson(errorBody, GenericResponse::class.java)
                            errorJson.error ?: errorJson.message ?: "Update failed"
                        } catch (e: Exception) { "Update failed" }
                        Toast.makeText(this@ManagePatientsActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Toast.makeText(this@ManagePatientsActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        fetchPatients()
    }
}

