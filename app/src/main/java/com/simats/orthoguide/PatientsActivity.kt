package com.simats.orthoguide

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.orthoguide.adapters.ClinicianPatientAdapter
import com.simats.orthoguide.network.ClinicianPatientItem
import com.simats.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientsActivity : AppCompatActivity() {
    private lateinit var rvPatients: RecyclerView
    private lateinit var adapter: ClinicianPatientAdapter
    private var allPatients: List<ClinicianPatientItem> = emptyList()
    private var filteredPatients: List<ClinicianPatientItem> = emptyList()
    
    private var currentFilterId = R.id.filter_all
    private var currentSearchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("OrthoGuide", "PatientsActivity created")
        enableEdgeToEdge()
        setContentView(R.layout.activity_patients)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val rootView = findViewById<View>(R.id.patients_root)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, 0, systemBars.right, 0)
                insets
            }
        }

        findViewById<View>(R.id.iv_back)?.setOnClickListener { finish() }

        setupRecyclerView()
        setupBottomNavigation()
        setupFiltersAndSearch()
        
        findViewById<View>(R.id.btn_add_patient)?.setOnClickListener {
            val intent = Intent(this, CreatePatientActivity::class.java)
            intent.putExtra("isEditMode", false)
            intent.putExtra("userRole", "clinician")
            startActivity(intent)
        }

        fetchPatients()
    }

    private fun setupRecyclerView() {
        rvPatients = findViewById(R.id.rv_patients)
        rvPatients.layoutManager = LinearLayoutManager(this)
        adapter = ClinicianPatientAdapter(emptyList()) { patient ->
            val intent = Intent(this, PatientProfileActivity::class.java)
            intent.putExtra("patientName", patient.name)
            intent.putExtra("patientId", patient.patientId)
            intent.putExtra("patientStatus", patient.status)
            intent.putExtra("patientStage", patient.treatmentStage)
            startActivity(intent)
        }
        rvPatients.adapter = adapter
    }

    private fun setupBottomNavigation() {
        findViewById<View>(R.id.nav_dashboard)?.setOnClickListener {
            val intent = Intent(this, ClinicianDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
        findViewById<View>(R.id.nav_schedule)?.setOnClickListener {
            val intent = Intent(this, ScheduleActivity::class.java)
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

    private fun setupFiltersAndSearch() {
        val etSearch = findViewById<EditText>(R.id.et_search_patients)
        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString() ?: ""
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        findViewById<View>(R.id.filter_all)?.setOnClickListener { updateFilterUI(R.id.filter_all) }
        findViewById<View>(R.id.filter_critical)?.setOnClickListener { updateFilterUI(R.id.filter_critical) }
        findViewById<View>(R.id.filter_attention)?.setOnClickListener { updateFilterUI(R.id.filter_attention) }
        findViewById<View>(R.id.filter_on_track)?.setOnClickListener { updateFilterUI(R.id.filter_on_track) }
        findViewById<View>(R.id.filter_unscheduled)?.setOnClickListener { updateFilterUI(R.id.filter_unscheduled) }
    }

    private fun fetchPatients() {
        val sharedPrefs = getSharedPreferences("OrthoPref", MODE_PRIVATE)
        val clinicianId = sharedPrefs.getString("USER_ID", "") ?: return
        
        if (clinicianId.isEmpty()) return

        RetrofitClient.service.getClinicianPatients(clinicianId).enqueue(object : Callback<List<ClinicianPatientItem>> {
            override fun onResponse(call: Call<List<ClinicianPatientItem>>, response: Response<List<ClinicianPatientItem>>) {
                if (response.isSuccessful) {
                    allPatients = response.body() ?: emptyList()
                    applyFilters()
                } else {
                    com.simats.orthoguide.util.DialogUtils.showErrorSnackbar(findViewById(android.R.id.content), "Failed to fetch patients")
                }
            }

            override fun onFailure(call: Call<List<ClinicianPatientItem>>, t: Throwable) {
                com.simats.orthoguide.util.DialogUtils.showErrorSnackbar(findViewById(android.R.id.content), "Error: ${t.message}")
            }
        })
    }

    private fun applyFilters() {
        filteredPatients = allPatients.filter { patient ->
            // Filter by Status
            val matchesFilter = when (currentFilterId) {
                R.id.filter_all -> true
                R.id.filter_critical -> patient.status?.lowercase() == "critical"
                R.id.filter_attention -> patient.status?.lowercase() == "attention"
                R.id.filter_on_track -> patient.status?.lowercase() == "on track" || patient.status?.lowercase() == "active"
                R.id.filter_unscheduled -> patient.hasAppointment == false
                else -> true
            }

            // Filter by Search Query
            val matchesSearch = if (currentSearchQuery.isEmpty()) {
                true
            } else {
                (patient.name ?: "").contains(currentSearchQuery, ignoreCase = true) || 
                (patient.patientId ?: "").contains(currentSearchQuery, ignoreCase = true)
            }

            matchesFilter && matchesSearch
        }
        
        val rv = findViewById<RecyclerView>(R.id.rv_patients)
        val emptyState = findViewById<View>(R.id.ll_empty_state)
        val tvEmpty = findViewById<TextView>(R.id.tv_empty_state_text)
        
        if (filteredPatients.isEmpty()) {
            rv?.visibility = View.GONE
            emptyState?.visibility = View.VISIBLE
            if (currentSearchQuery.isNotEmpty()) {
                tvEmpty?.text = "No patients found for \"$currentSearchQuery\""
            } else {
                tvEmpty?.text = "No patients found"
            }
        } else {
            rv?.visibility = View.VISIBLE
            emptyState?.visibility = View.GONE
        }
        
        adapter.updateData(filteredPatients)
    }

    private fun updateFilterUI(selectedId: Int) {
        currentFilterId = selectedId
        val filterButtons = listOf(
            findViewById<TextView>(R.id.filter_all),
            findViewById<TextView>(R.id.filter_critical),
            findViewById<TextView>(R.id.filter_attention),
            findViewById<TextView>(R.id.filter_on_track),
            findViewById<TextView>(R.id.filter_unscheduled)
        )

        filterButtons.forEach { it?.setBackgroundResource(R.drawable.bg_segment_unselected) }
        filterButtons.forEach { it?.setTextColor(Color.parseColor("#64748B")) }
        filterButtons.forEach { it?.setTypeface(null, android.graphics.Typeface.NORMAL) }

        val selected = findViewById<TextView>(selectedId)
        selected?.setBackgroundResource(R.drawable.bg_segment_selected_blue)
        selected?.setTextColor(Color.parseColor("#2563EB"))
        selected?.setTypeface(null, android.graphics.Typeface.BOLD)

        applyFilters()
    }

    override fun onResume() {
        super.onResume()
        // Refresh patients in case something changed
        fetchPatients()
    }
}

