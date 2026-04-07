package com.yourname.orthoguide

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
import com.yourname.orthoguide.adapters.AdminUserAdapter
import com.yourname.orthoguide.util.DialogUtils
import com.yourname.orthoguide.network.AdminUserItem
import com.yourname.orthoguide.network.AdminUsersResponse
import com.yourname.orthoguide.network.GenericResponse
import com.yourname.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ManageCliniciansActivity : AppCompatActivity() {

    private lateinit var adapter: AdminUserAdapter
    private var allClinicians = mutableListOf<AdminUserItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_clinicians)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val rootView = findViewById<View>(R.id.manage_clinicians_root)
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

        fetchClinicians()

        findViewById<View>(R.id.btn_add_clinician)?.setOnClickListener {
            val intent = Intent(this, CreateClinicianActivity::class.java)
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

        findViewById<View>(R.id.admin_nav_patients)?.setOnClickListener {
            val intent = Intent(this, ManagePatientsActivity::class.java)
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
        val rv = findViewById<RecyclerView>(R.id.rv_clinicians)
        adapter = AdminUserAdapter(
            users = emptyList(),
            onUserClick = { user -> editClinician(user) },
            onResetPassword = { user -> resetPassword(user) },
            onToggleStatus = { user -> toggleStatus(user) }
        )
        rv.adapter = adapter
    }

    private fun setupSearch() {
        findViewById<EditText>(R.id.et_search)?.addTextChangedListener { text ->
            val query = text.toString().lowercase()
            val filtered = if (query.isEmpty()) {
                allClinicians
            } else {
                allClinicians.filter { 
                    it.name?.lowercase()?.contains(query) == true || 
                    it.id.lowercase().contains(query) 
                }
            }
            adapter.updateData(filtered)
        }
    }

    private fun fetchClinicians() {
        RetrofitClient.service.getAllUsers().enqueue(object : Callback<AdminUsersResponse> {
            override fun onResponse(call: Call<AdminUsersResponse>, response: Response<AdminUsersResponse>) {
                if (response.isSuccessful) {
                    allClinicians.clear()
                    response.body()?.clinicians?.let { allClinicians.addAll(it) }
                    
                    // Re-apply search filter if there's text in search bar
                    val currentQuery = findViewById<EditText>(R.id.et_search)?.text?.toString()?.lowercase() ?: ""
                    if (currentQuery.isNotEmpty()) {
                        val filtered = allClinicians.filter { 
                            it.name?.lowercase()?.contains(currentQuery) == true || 
                            it.id.lowercase().contains(currentQuery) 
                        }
                        adapter.updateData(filtered)
                    } else {
                        adapter.updateData(allClinicians)
                    }
                    
                    // Update header subtitle
                    val subtitle = findViewById<android.widget.TextView>(R.id.tv_header_subtitle)
                    subtitle?.text = "${allClinicians.size} Staff Members"
                } else {
                    Toast.makeText(this@ManageCliniciansActivity, "Failed to load clinicians", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AdminUsersResponse>, t: Throwable) {
                Toast.makeText(this@ManageCliniciansActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun editClinician(user: AdminUserItem) {
        val intent = Intent(this, CreateClinicianActivity::class.java).apply {
            putExtra("isEditMode", true)
            putExtra("clinicianId", user.id)
            putExtra("clinicianName", user.name)
            putExtra("clinicianEmail", user.email)
            putExtra("clinicianPhone", user.phoneNumber)
            putExtra("clinicianRole", user.roleType)
        }
        startActivity(intent)
    }

    private fun resetPassword(user: AdminUserItem) {
        val request = mapOf("id" to user.id, "role" to "clinician")
        RetrofitClient.service.adminResetPassword(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful) {
                    DialogUtils.showSuccessDialog(
                        this@ManageCliniciansActivity,
                        "Success",
                        response.body()?.message ?: "Password has been reset successfully."
                    )
                    } else {
                        val errorMsg = try {
                            val errorBody = response.errorBody()?.string()
                            val errorJson = com.google.gson.Gson().fromJson(errorBody, GenericResponse::class.java)
                            errorJson.error ?: errorJson.message ?: "Reset failed"
                        } catch (e: Exception) { "Reset failed" }
                        Toast.makeText(this@ManageCliniciansActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Toast.makeText(this@ManageCliniciansActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun toggleStatus(user: AdminUserItem) {
        val isActive = user.status?.lowercase() == "active"
        val newStatus = if (isActive) "Inactive" else "Active"
        
        val request = mapOf(
            "id" to user.id,
            "role" to "clinician",
            "status" to newStatus
        )

        RetrofitClient.service.adminUpdateUser(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful) {
                    DialogUtils.showSuccessDialog(
                        this@ManageCliniciansActivity,
                        "Status Updated",
                        "Account status for ${user.name} has been set to $newStatus."
                    )
                    fetchClinicians() // Refresh list
                    } else {
                        val errorMsg = try {
                            val errorBody = response.errorBody()?.string()
                            val errorJson = com.google.gson.Gson().fromJson(errorBody, GenericResponse::class.java)
                            errorJson.error ?: errorJson.message ?: "Update failed"
                        } catch (e: Exception) { "Update failed" }
                        Toast.makeText(this@ManageCliniciansActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Toast.makeText(this@ManageCliniciansActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        fetchClinicians() // Refresh when returning from edit/create
    }
}
