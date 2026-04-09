package com.simats.orthoguide

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.simats.orthoguide.network.PatientIssueItem
import com.simats.orthoguide.network.ProfileResponse
import com.simats.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.simats.orthoguide.R

class PatientProfileActivity : AppCompatActivity() {
    private var patientId: String = ""
    private var sessionId: Int = -1
    private var hasSession: Boolean = false
    private var isSavingAppt: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_profile)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.patient_profile_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        findViewById<View>(R.id.iv_back)?.setOnClickListener { finish() }

        // Dynamic patient info
        val patientName = intent.getStringExtra("patientName") ?: "Patient"
        patientId = intent.getStringExtra("patientId") ?: "P001"
        hasSession = intent.getBooleanExtra("hasSession", false)
        sessionId = intent.getIntExtra("sessionId", -1)

        val tvName = findViewById<android.widget.TextView>(R.id.tv_patient_name)
        val tvId = findViewById<android.widget.TextView>(R.id.tv_patient_id)
        val tvInitials = findViewById<android.widget.TextView>(R.id.tv_avatar_initials)

        tvName?.text = patientName
        tvId?.text = "ID: $patientId"


        // Generate initials (e.g., "James Wilson" -> "JW")
        val initials = try {
            patientName.split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2)
                .joinToString("")
                .uppercase()
        } catch (e: Exception) { "?" }
        tvInitials?.text = if (initials.isEmpty()) "?" else initials

        // Setup Treatment Overview Action
        val prefs = getSharedPreferences("OrthoPref", android.content.Context.MODE_PRIVATE)
        val btnSaveChanges = findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_save_changes)
        
        val btnStatusOnTrack = findViewById<android.widget.TextView>(R.id.btn_status_on_track)
        val btnStatusAttention = findViewById<android.widget.TextView>(R.id.btn_status_attention)
        val btnStatusCritical = findViewById<android.widget.TextView>(R.id.btn_status_critical)

        fun updateStatusUI(selectedBtn: android.widget.TextView?) {
            // Reset all to unselected
            val unselectedBg = R.drawable.bg_segment_unselected
            val unselectedColor = android.graphics.Color.parseColor("#64748B")
            
            val buttons = listOf(btnStatusOnTrack, btnStatusAttention, btnStatusCritical)
            for (btn in buttons) {
                btn?.setBackgroundResource(unselectedBg)
                btn?.setTextColor(unselectedColor)
                btn?.setTypeface(null, android.graphics.Typeface.NORMAL)
            }

            // Apply selected
            selectedBtn?.setTypeface(null, android.graphics.Typeface.BOLD)
            when (selectedBtn?.id) {
                R.id.btn_status_on_track -> {
                    selectedBtn.setBackgroundResource(R.drawable.bg_segment_selected_green)
                    selectedBtn.setTextColor(android.graphics.Color.parseColor("#059669"))
                }
                R.id.btn_status_attention -> {
                    selectedBtn.setBackgroundResource(R.drawable.bg_segment_selected_orange)
                    selectedBtn.setTextColor(android.graphics.Color.parseColor("#D97706"))
                }
                R.id.btn_status_critical -> {
                    selectedBtn.setBackgroundResource(R.drawable.bg_segment_selected_red)
                    selectedBtn.setTextColor(android.graphics.Color.parseColor("#DC2626"))
                }
            }
            
            // Enable save button on change
            btnSaveChanges?.isEnabled = true
        }

        btnStatusOnTrack?.setOnClickListener { updateStatusUI(it as android.widget.TextView) }
        btnStatusAttention?.setOnClickListener { updateStatusUI(it as android.widget.TextView) }
        btnStatusCritical?.setOnClickListener { updateStatusUI(it as android.widget.TextView) }

        // Initial state
        var currentStatusId = R.id.btn_status_on_track
        
        // Stage dropdown
        val llStageDropdown = findViewById<View>(R.id.ll_stage_dropdown)
        val tvStageSelected = findViewById<android.widget.TextView>(R.id.tv_stage_selected)
        
        llStageDropdown?.setOnClickListener { view ->
            val wrapper = android.view.ContextThemeWrapper(this, R.style.WhitePopupMenuTheme)
            val popup = android.widget.PopupMenu(wrapper, view)
            val stages = listOf(
                "Initial Consultation", 
                "Bonding / First Trays", 
                "Alignment Phase", 
                "Bite Correction", 
                "Finishing & Detailing", 
                "Debonding & Retention"
            )
            stages.forEachIndexed { index, stage ->
                popup.menu.add(0, index, 0, stage)
            }
            popup.setOnMenuItemClickListener { menuItem ->
                tvStageSelected?.text = menuItem.title
                btnSaveChanges?.isEnabled = true
                true
            }
            popup.show()
        }

        // Initial state from Intent fallback to Prefs
        val intentStatus = intent.getStringExtra("patientStatus")
        val intentStage = intent.getStringExtra("patientStage")
        
        val savedStatus = if (!intentStatus.isNullOrEmpty()) intentStatus else prefs.getString("status_$patientId", "On Track")
        val savedStage = if (!intentStage.isNullOrEmpty()) intentStage else prefs.getString("stage_$patientId", "Initial Consultation")
        
        tvStageSelected?.text = savedStage
        when (savedStatus?.lowercase()) {
            "on track" -> updateStatusUI(btnStatusOnTrack)
            "attention" -> updateStatusUI(btnStatusAttention)
            "critical" -> updateStatusUI(btnStatusCritical)
            else -> updateStatusUI(btnStatusOnTrack)
        }
        
        // Reset save button enabled state after loading initial values
        btnSaveChanges?.isEnabled = true // Always enabled now as per user request to be at bottom and save everything

        btnSaveChanges?.setOnClickListener {
            val currentStatus = when {
                btnStatusOnTrack?.typeface?.isBold == true -> "On Track"
                btnStatusAttention?.typeface?.isBold == true -> "Attention"
                btnStatusCritical?.typeface?.isBold == true -> "Critical"
                else -> "On Track"
            }
            val currentStage = tvStageSelected?.text.toString()
            val currentNotes = findViewById<android.widget.EditText>(R.id.et_patient_notes)?.text.toString()
            
            prefs.edit().apply {
                putString("status_$patientId", currentStatus)
                putString("stage_$patientId", currentStage)
                putString("appt_notes_$patientId", currentNotes)
                apply()
            }
            
            // Clean the "ID: " prefix to extract actual user_id
            val cleanId = patientId.replace("ID: ", "").trim()
            val request = mapOf(
                "id" to cleanId,
                "role" to "patient",
                "status" to currentStatus,
                "treatment_stage" to currentStage,
                "notes" to currentNotes
            )
            
            btnSaveChanges?.isEnabled = false
            btnSaveChanges?.text = "Saving..."
            
            com.simats.orthoguide.network.RetrofitClient.service.adminUpdateUser(request).enqueue(object : retrofit2.Callback<com.simats.orthoguide.network.GenericResponse> {
                override fun onResponse(call: retrofit2.Call<com.simats.orthoguide.network.GenericResponse>, response: retrofit2.Response<com.simats.orthoguide.network.GenericResponse>) {
                    if (response.isSuccessful) {
                        com.simats.orthoguide.util.DialogUtils.showSuccessSnackbar(findViewById(android.R.id.content), "Changes saved successfully")
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            finish()
                        }, 1000)
                    } else {
                        btnSaveChanges?.isEnabled = true
                        btnSaveChanges?.text = "Save Changes"
                        com.simats.orthoguide.util.DialogUtils.showError(this@PatientProfileActivity, "Failed to update database")
                    }
                }
                
                override fun onFailure(call: retrofit2.Call<com.simats.orthoguide.network.GenericResponse>, t: Throwable) {
                    btnSaveChanges?.isEnabled = true
                    btnSaveChanges?.text = "Save Changes"
                    com.simats.orthoguide.util.DialogUtils.showError(this@PatientProfileActivity, "Network error")
                }
            })
        }

        // Initial fetch for synchronization
        fetchPatientProfile(patientId)
        fetchPatientIssues(patientId)

        // Appointments Logic
        val llHasAppointment = findViewById<View>(R.id.ll_has_appointment)
        val llNoAppointment = findViewById<View>(R.id.ll_no_appointment)
        
        // Immediate UI state from Intent extras
        if (hasSession) {
            llHasAppointment?.visibility = View.VISIBLE
            llNoAppointment?.visibility = View.GONE
            
            val savedDate = intent.getStringExtra("appointmentDate") ?: prefs.getString("appt_date_$patientId", "")
            val savedTime = intent.getStringExtra("appointmentTime") ?: prefs.getString("appt_time_$patientId", "")
            val savedType = intent.getStringExtra("appointmentType") ?: prefs.getString("appt_type_$patientId", "Regular Session")
            val isTbd = savedTime == "TBD"
            
            findViewById<android.widget.TextView>(R.id.tv_appt_title)?.text = savedType
            findViewById<android.widget.TextView>(R.id.tv_appt_type)?.text = savedType
            
            val savedNotes = intent.getStringExtra("appointmentNotes") ?: prefs.getString("appt_notes_$patientId", "")
            val etNotesProfile = findViewById<android.widget.EditText>(R.id.et_patient_notes)
            if (!savedNotes.isNullOrEmpty()) {
                etNotesProfile?.setText(savedNotes)
            }
            
            if (!savedDate.isNullOrEmpty() && savedDate != "Select Date") {
                findViewById<android.widget.TextView>(R.id.tv_appt_date)?.text = if (isTbd || savedTime == "TBD") savedDate else "$savedDate • $savedTime"
            }
        } else {
            llHasAppointment?.visibility = View.GONE
            llNoAppointment?.visibility = View.VISIBLE
        }
        
        fun showScheduleBottomSheet() {
            val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
            bottomSheetDialog.window?.let { window ->
                @Suppress("DEPRECATION")
                window.navigationBarColor = android.graphics.Color.WHITE
                WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = true
            }

            val view = layoutInflater.inflate(R.layout.bottom_sheet_schedule_session, null)
            bottomSheetDialog.setContentView(view)
            
            val tvSelectedDate = view.findViewById<android.widget.TextView>(R.id.tv_selected_date)
            val tvSelectedTime = view.findViewById<android.widget.TextView>(R.id.tv_selected_time)
            val tvSelectedType = view.findViewById<android.widget.TextView>(R.id.tv_selected_type)
            val llTimePicker = view.findViewById<android.view.View>(R.id.ll_time_picker)
            val etNotes = view.findViewById<android.widget.EditText>(R.id.et_notes)
            
            val calendar = java.util.Calendar.getInstance()
            val sdfDisplay = java.text.SimpleDateFormat("M/d/yyyy", java.util.Locale.US)
            
            if (sessionId != -1) {
                // Initializing with current values
                val savedDate = intent.getStringExtra("appointmentDate") ?: prefs.getString("appt_date_$patientId", "")
                val savedTime = intent.getStringExtra("appointmentTime") ?: prefs.getString("appt_time_$patientId", "")
                val savedType = intent.getStringExtra("appointmentType") ?: prefs.getString("appt_type_$patientId", "Regular Session")
                val savedNoteVal = intent.getStringExtra("appointmentNotes") ?: prefs.getString("appt_notes_$patientId", "")
                
                etNotes?.setText(savedNoteVal)
                if (!savedDate.isNullOrEmpty() && savedDate != "Select Date") {
                    tvSelectedDate?.text = savedDate
                    tvSelectedDate?.setTextColor(android.graphics.Color.parseColor("#1E293B"))
                }
                if (savedTime != "TBD" && !savedTime.isNullOrEmpty() && savedTime != "Select Time") {
                    tvSelectedTime?.text = savedTime
                    tvSelectedTime?.setTextColor(android.graphics.Color.parseColor("#1E293B"))
                }
                tvSelectedType?.text = savedType
            } else {
                val dateStr = sdfDisplay.format(calendar.time)
                tvSelectedDate?.text = dateStr
                tvSelectedDate?.setTextColor(android.graphics.Color.parseColor("#1E293B"))
                tvSelectedTime?.text = "Select Time"
                tvSelectedType?.text = "Regular Session"
            }

            fun performAutoSave(isManualConfirm: Boolean = false): Boolean {
                val dateText = tvSelectedDate?.text.toString()
                val timeText = tvSelectedTime?.text.toString()
                
                val anchor = view

                if (dateText == "Select Date" || timeText == "Select Time") {
                    if (isManualConfirm) {
                        com.simats.orthoguide.util.DialogUtils.showErrorSnackbar(anchor, "Please select both a date and a time.")
                    }
                    return false
                }

                val backendDate = try {
                    val sdfIn = java.text.SimpleDateFormat("M/d/yyyy", java.util.Locale.US)
                    val sdfOut = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    sdfOut.format(sdfIn.parse(dateText)!!)
                } catch (e: Exception) { 
                    if (dateText.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) dateText else dateText 
                }

                val clinicianId = prefs.getString("USER_ID", "") ?: ""
                val typeText = tvSelectedType?.text.toString()
                val notesText = etNotes?.text.toString()

                if (sessionId != -1) {
                    val reschReq = mapOf(
                        "appointment_id" to sessionId.toString(),
                        "date" to backendDate,
                        "time" to timeText,
                        "notes" to notesText
                    )
                    if (isSavingAppt) return false
                    isSavingAppt = true
                    view.findViewById<android.widget.Button>(R.id.btn_confirm_session)?.isEnabled = false

                    com.simats.orthoguide.network.RetrofitClient.service.rescheduleAppointment(reschReq).enqueue(object : retrofit2.Callback<com.simats.orthoguide.network.GenericResponse> {
                        override fun onResponse(call: retrofit2.Call<com.simats.orthoguide.network.GenericResponse>, response: retrofit2.Response<com.simats.orthoguide.network.GenericResponse>) {
                            isSavingAppt = false
                            view.findViewById<android.widget.Button>(R.id.btn_confirm_session)?.isEnabled = true

                            if (response.isSuccessful) {
                                val newId = response.body()?.appointmentId
                                if (newId != null) sessionId = newId

                                if (isManualConfirm) {
                                    com.simats.orthoguide.util.DialogUtils.showSuccessSnackbar(anchor, "Session saved successfully")
                                    bottomSheetDialog.dismiss()
                                }
                                updateAppointmentUI(true, typeText, dateText, timeText, notesText, "rescheduled")
                                fetchPatientProfile(patientId)
                            } else {
                                val rawJson = response.errorBody()?.string() ?: ""
                                val parsedMsg = if (rawJson.contains("\"error\":")) {
                                    try {
                                        org.json.JSONObject(rawJson).getString("error")
                                    } catch (e: Exception) { "Failed to save session" }
                                } else { "Failed to save session" }
                                com.simats.orthoguide.util.DialogUtils.showErrorSnackbar(anchor, parsedMsg)
                            }
                        }
                        override fun onFailure(call: retrofit2.Call<com.simats.orthoguide.network.GenericResponse>, t: Throwable) {
                            isSavingAppt = false
                            view.findViewById<android.widget.Button>(R.id.btn_confirm_session)?.isEnabled = true
                            com.simats.orthoguide.util.DialogUtils.showErrorSnackbar(anchor, "Network error: ${t.message}")
                        }
                    })
                } else {
                    val request = mapOf(
                        "patient_id" to patientId,
                        "clinician_id" to clinicianId,
                        "appointment_date" to backendDate,
                        "appointment_time" to timeText,
                        "appointment_type" to typeText,
                        "notes" to notesText
                    )
                    if (isSavingAppt) return false
                    isSavingAppt = true
                    view.findViewById<android.widget.Button>(R.id.btn_confirm_session)?.isEnabled = false

                    com.simats.orthoguide.network.RetrofitClient.service.clinicianAddSchedule(request).enqueue(object : retrofit2.Callback<com.simats.orthoguide.network.GenericResponse> {
                        override fun onResponse(call: retrofit2.Call<com.simats.orthoguide.network.GenericResponse>, response: retrofit2.Response<com.simats.orthoguide.network.GenericResponse>) {
                            isSavingAppt = false
                            view.findViewById<android.widget.Button>(R.id.btn_confirm_session)?.isEnabled = true

                            if (response.isSuccessful) {
                                val newId = response.body()?.appointmentId
                                if (newId != null) sessionId = newId

                                if (isManualConfirm) {
                                    com.simats.orthoguide.util.DialogUtils.showSuccessSnackbar(anchor, "Session planned successfully")
                                    bottomSheetDialog.dismiss()
                                }
                                updateAppointmentUI(true, typeText, dateText, timeText, notesText, "scheduled")
                                fetchPatientProfile(patientId)
                            } else {
                                val rawJson = response.errorBody()?.string() ?: ""
                                val parsedMsg = if (rawJson.contains("\"error\":")) {
                                    try {
                                        org.json.JSONObject(rawJson).getString("error")
                                    } catch (e: Exception) { "Failed to plan session" }
                                } else { "Failed to plan session" }
                                com.simats.orthoguide.util.DialogUtils.showErrorSnackbar(anchor, parsedMsg)
                            }
                        }
                        override fun onFailure(call: retrofit2.Call<com.simats.orthoguide.network.GenericResponse>, t: Throwable) {
                            isSavingAppt = false
                            view.findViewById<android.widget.Button>(R.id.btn_confirm_session)?.isEnabled = true
                            com.simats.orthoguide.util.DialogUtils.showErrorSnackbar(anchor, "Network error: ${t.message}")
                        }
                    })
                }
                return true
            }
            
            view.findViewById<View>(R.id.ll_date_picker)?.setOnClickListener {
                val cal = java.util.Calendar.getInstance()
                val dateDialog = android.app.DatePickerDialog(this, { _, year, month, day ->
                    val dateStr = "${month + 1}/$day/$year"
                    tvSelectedDate?.text = dateStr
                    tvSelectedDate?.setTextColor(android.graphics.Color.parseColor("#1E293B"))
                    tvSelectedTime?.text = "Select Time" // Reset time on date change for safety
                }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH))
                dateDialog.datePicker.minDate = System.currentTimeMillis() - 1000
                dateDialog.show()
            }
            
            llTimePicker?.setOnClickListener {
                val cal = java.util.Calendar.getInstance()
                android.app.TimePickerDialog(this, { _, hour, minute ->
                    val selectedCal = java.util.Calendar.getInstance()
                    val dateParts = tvSelectedDate?.text.toString().split("/")
                    if (dateParts.size == 3) {
                        selectedCal.set(java.util.Calendar.MONTH, dateParts[0].toInt() - 1)
                        selectedCal.set(java.util.Calendar.DAY_OF_MONTH, dateParts[1].toInt())
                        selectedCal.set(java.util.Calendar.YEAR, dateParts[2].toInt())
                    }
                    selectedCal.set(java.util.Calendar.HOUR_OF_DAY, hour)
                    selectedCal.set(java.util.Calendar.MINUTE, minute)

                    val amPm = if (hour >= 12) "PM" else "AM"
                    val hr = if (hour % 12 == 0) 12 else hour % 12
                    val timeStr = String.format("%02d:%02d %s", hr, minute, amPm)
                    tvSelectedTime?.text = timeStr
                    tvSelectedTime?.setTextColor(android.graphics.Color.parseColor("#1E293B"))
                    
                    performAutoSave()
                }, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), false).show()
            }
            
            view.findViewById<View>(R.id.ll_type_dropdown)?.setOnClickListener { dropdownView ->
                val wrapper = android.view.ContextThemeWrapper(this, R.style.WhitePopupMenuTheme)
                val popup = android.widget.PopupMenu(wrapper, dropdownView)
                val types = listOf("Regular Session", "Emergency", "Fitting", "Consultation")
                types.forEachIndexed { index, type -> popup.menu.add(0, index, 0, type) }
                popup.setOnMenuItemClickListener { menuItem ->
                    tvSelectedType?.text = menuItem.title
                    performAutoSave()
                    true
                }
                popup.show()
            }
            
            view.findViewById<View>(R.id.btn_confirm_session)?.setOnClickListener { 
                performAutoSave(isManualConfirm = true)
            }
            bottomSheetDialog.show()
        }

        findViewById<View>(R.id.btn_schedule_appt_empty)?.setOnClickListener { showScheduleBottomSheet() }
        findViewById<View>(R.id.btn_reschedule_appt)?.setOnClickListener { showScheduleBottomSheet() }

        findViewById<View>(R.id.btn_cancel_appt)?.setOnClickListener {
            if (sessionId == -1) {
                updateAppointmentUI(false)
                return@setOnClickListener
            }
            
            com.simats.orthoguide.util.DialogUtils.showConfirmDialog(
                context = this,
                title = "Cancel Treatment Session",
                message = "Are you sure you want to cancel the upcoming treatment session?",
                confirmText = "Cancel Session",
                cancelText = "Keep It",
                onConfirm = {
                    com.simats.orthoguide.network.RetrofitClient.service.deleteAppointment(sessionId).enqueue(object : retrofit2.Callback<com.simats.orthoguide.network.GenericResponse> {
                        override fun onResponse(call: retrofit2.Call<com.simats.orthoguide.network.GenericResponse>, response: retrofit2.Response<com.simats.orthoguide.network.GenericResponse>) {
                            if (response.isSuccessful) {
                                prefs.edit().apply {
                                    remove("appt_date_$patientId")
                                    remove("appt_time_$patientId")
                                    remove("appt_type_$patientId")
                                    remove("appt_notes_$patientId")
                                    apply()
                                }
                                com.simats.orthoguide.util.DialogUtils.showSuccessSnackbar(findViewById(android.R.id.content), "Session cancelled")
                                updateAppointmentUI(false)
                            } else {
                                com.simats.orthoguide.util.DialogUtils.showError(this@PatientProfileActivity, "Failed to cancel")
                            }
                        }
                        override fun onFailure(call: retrofit2.Call<com.simats.orthoguide.network.GenericResponse>, t: Throwable) {
                            com.simats.orthoguide.util.DialogUtils.showError(this@PatientProfileActivity, "Network error")
                        }
                    })
                }
            )
        }
    }

    private fun updateAppointmentUI(hasAppt: Boolean, typeText: String? = null, dateText: String? = null, timeText: String? = null, notesText: String? = null, status: String? = null) {
        val llHasAppointment = findViewById<View>(R.id.ll_has_appointment)
        val llNoAppointment = findViewById<View>(R.id.ll_no_appointment)
        val tvStatusBadge = findViewById<android.widget.TextView>(R.id.tv_appt_status_badge)
        
        this.hasSession = hasAppt
        if (hasAppt) {
            llHasAppointment?.visibility = View.VISIBLE
            llNoAppointment?.visibility = View.GONE
            
            if (typeText != null) {
                findViewById<android.widget.TextView>(R.id.tv_appt_title)?.text = typeText
                findViewById<android.widget.TextView>(R.id.tv_appt_type)?.text = typeText
            }
            if (dateText != null) {
                findViewById<android.widget.TextView>(R.id.tv_appt_date)?.text = if (timeText == "TBD" || timeText.isNullOrEmpty()) dateText else "$dateText • $timeText"
            }
                     // Status Badge Logic
            val statusStr = status?.lowercase() ?: "scheduled"
            tvStatusBadge?.text = when (statusStr) {
                "scheduled", "rescheduled", "confirmed" -> "CONFIRMED"
                "completed" -> "COMPLETED"
                "cancelled" -> "CANCELLED"
                "missed" -> "MISSED"
                else -> statusStr.uppercase()
            }
            
            when (statusStr) {
                "scheduled", "rescheduled", "confirmed" -> {
                    tvStatusBadge?.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E6F7F1"))
                    tvStatusBadge?.setTextColor(android.graphics.Color.parseColor("#10B981"))
                }
                "cancelled" -> {
                    tvStatusBadge?.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FEE2E2"))
                    tvStatusBadge?.setTextColor(android.graphics.Color.parseColor("#EF4444"))
                }
                "completed" -> {
                    tvStatusBadge?.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E6F7F1"))
                    tvStatusBadge?.setTextColor(android.graphics.Color.parseColor("#10B981"))
                }
                "missed" -> {
                    tvStatusBadge?.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F1F5F9"))
                    tvStatusBadge?.setTextColor(android.graphics.Color.parseColor("#94A3B8"))
                }
            }
        } else {
            this.sessionId = -1
            llHasAppointment?.visibility = View.GONE
            llNoAppointment?.visibility = View.VISIBLE
        }
    }

    private fun fetchPatientProfile(patientId: String) {
        com.simats.orthoguide.network.RetrofitClient.service.getPatientProfileById(patientId)
            .enqueue(object : retrofit2.Callback<com.simats.orthoguide.network.ProfileResponse> {
                override fun onResponse(
                    call: retrofit2.Call<com.simats.orthoguide.network.ProfileResponse>,
                    response: retrofit2.Response<com.simats.orthoguide.network.ProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body() ?: return
                        
                        // Sync notes if available in profile
                        if (!body.notes.isNullOrEmpty()) {
                            findViewById<android.widget.EditText>(R.id.et_patient_notes)?.setText(body.notes)
                        }

                        val appt = body.nextAppointment
                        if (appt != null) {
                            sessionId = appt.id ?: -1
                            val dateText = try {
                                val sdfIn = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                val sdfOut = java.text.SimpleDateFormat("M/d/yyyy", java.util.Locale.getDefault())
                                sdfOut.format(sdfIn.parse(appt.date!!)!!)
                            } catch (e: Exception) { appt.date }
                            updateAppointmentUI(true, appt.type, dateText, appt.time, appt.notes, appt.status)
                        } else {
                            sessionId = -1
                            updateAppointmentUI(false)
                        }
                    }
                }
                override fun onFailure(call: retrofit2.Call<com.simats.orthoguide.network.ProfileResponse>, t: Throwable) {
                    android.util.Log.e("OrthoGuide", "Failed to fetch patient profile", t)
                }
            })
    }

    private fun decodeBase64ToBitmap(base64Str: String): android.graphics.Bitmap? {
        return try {
            val decodedBytes = android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT)
            android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchPatientIssues(patientId: String) {
        val cleanId = patientId.replace("ID: ", "").trim()
        
        val llContent = findViewById<android.view.View>(R.id.ll_attachment_content)
        val llEmpty = findViewById<android.view.View>(R.id.ll_attachment_empty)
        val ivAttachment = findViewById<android.widget.ImageView>(R.id.iv_patient_attachment)
        val tvType = findViewById<android.widget.TextView>(R.id.tv_attachment_type)
        val tvDesc = findViewById<android.widget.TextView>(R.id.tv_attachment_description)
        val tvDate = findViewById<android.widget.TextView>(R.id.tv_attachment_date)

        com.simats.orthoguide.network.RetrofitClient.service.getPatientIssues(cleanId).enqueue(object : retrofit2.Callback<List<com.simats.orthoguide.network.PatientIssueItem>> {
            override fun onResponse(call: retrofit2.Call<List<com.simats.orthoguide.network.PatientIssueItem>>, response: retrofit2.Response<List<com.simats.orthoguide.network.PatientIssueItem>>) {
                if (response.isSuccessful) {
                    val issues = response.body()
                    if (!issues.isNullOrEmpty()) {
                        // Find the latest issue (prioritize ones with photos)
                        val latestIssue = issues.sortedByDescending { it.id }.firstOrNull()
                        
                        if (latestIssue != null) {
                            llContent?.visibility = android.view.View.VISIBLE
                            llEmpty?.visibility = android.view.View.GONE
                            
                            tvType?.text = latestIssue.issueType ?: "Reported Issue"
                            tvDesc?.text = latestIssue.description ?: "No description provided."
                            tvDate?.text = "Reported on ${latestIssue.createdAt}"

                            if (!latestIssue.photoUrl.isNullOrEmpty()) {
                                val bitmap = decodeBase64ToBitmap(latestIssue.photoUrl!!)
                                if (bitmap != null) {
                                    ivAttachment?.visibility = android.view.View.VISIBLE
                                    ivAttachment?.setImageBitmap(bitmap)
                                } else {
                                    ivAttachment?.visibility = android.view.View.GONE
                                }
                            } else {
                                ivAttachment?.visibility = android.view.View.GONE
                            }
                        } else {
                            llContent?.visibility = android.view.View.GONE
                            llEmpty?.visibility = android.view.View.VISIBLE
                        }
                    } else {
                        llContent?.visibility = android.view.View.GONE
                        llEmpty?.visibility = android.view.View.VISIBLE
                    }
                } else {
                    llContent?.visibility = android.view.View.GONE
                    llEmpty?.visibility = android.view.View.VISIBLE
                }
            }
            override fun onFailure(call: retrofit2.Call<List<com.simats.orthoguide.network.PatientIssueItem>>, t: Throwable) {
                android.util.Log.e("OrthoGuide", "Failed to fetch issues", t)
                llContent?.visibility = android.view.View.GONE
                llEmpty?.visibility = android.view.View.VISIBLE
            }
        })
    }
}

