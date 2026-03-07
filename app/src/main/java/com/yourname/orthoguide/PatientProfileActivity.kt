package com.yourname.orthoguide

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class PatientProfileActivity : AppCompatActivity() {

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
        val patientName = intent.getStringExtra("patientName") ?: "James Wilson"
        val patientId = intent.getStringExtra("patientId") ?: "ID: 100982100"

        val tvName = findViewById<android.widget.TextView>(R.id.tv_patient_name)
        val tvId = findViewById<android.widget.TextView>(R.id.tv_patient_id)
        val tvInitials = findViewById<android.widget.TextView>(R.id.tv_avatar_initials)

        tvName?.text = patientName
        tvId?.text = patientId

        // Generate initials (e.g., "James Wilson" -> "JW")
        val initials = patientName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
        tvInitials?.text = initials

        // Setup Treatment Overview Action
        val prefs = getSharedPreferences("OrthoGuidePrefs", android.content.Context.MODE_PRIVATE)
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
            val popup = android.widget.PopupMenu(this, view)
            val stages = listOf(
                "Pre-Treatment", 
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

        // Initial state from Prefs
        val savedStatus = prefs.getString("status_$patientId", "On Track")
        val savedStage = prefs.getString("stage_$patientId", "Alignment Phase")
        
        tvStageSelected?.text = savedStage
        when (savedStatus) {
            "On Track" -> updateStatusUI(btnStatusOnTrack)
            "Attention" -> updateStatusUI(btnStatusAttention)
            "Critical" -> updateStatusUI(btnStatusCritical)
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
            
            // Visual feedback
            android.widget.Toast.makeText(this, "Changes saved successfully", android.widget.Toast.LENGTH_SHORT).show()
            finish()
        }

        // Appointments Logic
        val llHasAppointment = findViewById<View>(R.id.ll_has_appointment)
        val llNoAppointment = findViewById<View>(R.id.ll_no_appointment)
        
        val hasAppointment = if (prefs.contains("appt_$patientId")) {
            prefs.getBoolean("appt_$patientId", true)
        } else {
            intent.getBooleanExtra("hasAppointment", true)
        }
        
        if (hasAppointment) {
            llHasAppointment?.visibility = View.VISIBLE
            llNoAppointment?.visibility = View.GONE
            
            val savedDate = prefs.getString("appt_date_$patientId", "")
            val savedTime = prefs.getString("appt_time_$patientId", "")
            val savedType = prefs.getString("appt_type_$patientId", "Regular Checkup")
            val isTbd = prefs.getBoolean("appt_tbd_$patientId", false)
            
            findViewById<android.widget.TextView>(R.id.tv_appt_title)?.text = savedType
            findViewById<android.widget.TextView>(R.id.tv_appt_type)?.text = savedType
            
            val savedNotes = prefs.getString("appt_notes_$patientId", "")
            val etNotesProfile = findViewById<android.widget.EditText>(R.id.et_patient_notes)
            if (!savedNotes.isNullOrEmpty()) {
                etNotesProfile?.setText(savedNotes)
            }
            
            if (!savedDate.isNullOrEmpty() && savedDate != "Select Date") {
                if (isTbd || savedTime == "TBD" || savedTime.isNullOrEmpty() || savedTime == "Select Time") {
                    findViewById<android.widget.TextView>(R.id.tv_appt_date)?.text = savedDate
                } else {
                    findViewById<android.widget.TextView>(R.id.tv_appt_date)?.text = "$savedDate • $savedTime"
                }
            }
        } else {
            llHasAppointment?.visibility = View.GONE
            llNoAppointment?.visibility = View.VISIBLE
        }

        fun showScheduleBottomSheet() {
            val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottom_sheet_schedule_appointment, null)
            bottomSheetDialog.setContentView(view)
            
            val tvSelectedDate = view.findViewById<android.widget.TextView>(R.id.tv_selected_date)
            val tvSelectedTime = view.findViewById<android.widget.TextView>(R.id.tv_selected_time)
            val tvSelectedType = view.findViewById<android.widget.TextView>(R.id.tv_selected_type)
            val switchTimeTbd = view.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switch_time_tbd)
            val llTimePicker = view.findViewById<android.view.View>(R.id.ll_time_picker)
            val etNotes = view.findViewById<android.widget.EditText>(R.id.et_notes)
            
            // Initialize with saved values if any
            val savedDate = prefs.getString("appt_date_$patientId", "")
            val savedTime = prefs.getString("appt_time_$patientId", "")
            val savedType = prefs.getString("appt_type_$patientId", "Regular Checkup")
            val isTbd = prefs.getBoolean("appt_tbd_$patientId", false)
            val savedNoteVal = prefs.getString("appt_notes_$patientId", "")
            
            etNotes?.setText(savedNoteVal)
            
            if (!savedDate.isNullOrEmpty() && savedDate != "Select Date") {
                tvSelectedDate?.text = savedDate
                tvSelectedDate?.setTextColor(android.graphics.Color.parseColor("#1E293B"))
            }
            
            if (isTbd || savedTime == "TBD") {
                switchTimeTbd?.isChecked = true
                llTimePicker?.alpha = 0.5f
                llTimePicker?.isClickable = false
                tvSelectedTime?.text = "Time TBD"
            } else if (!savedTime.isNullOrEmpty() && savedTime != "Select Time") {
                tvSelectedTime?.text = savedTime
                tvSelectedTime?.setTextColor(android.graphics.Color.parseColor("#1E293B"))
            }
            
            tvSelectedType?.text = savedType
            
            // Time TBD Logic
            switchTimeTbd?.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    llTimePicker?.alpha = 0.5f
                    llTimePicker?.isClickable = false
                    tvSelectedTime?.text = "Time TBD"
                } else {
                    llTimePicker?.alpha = 1.0f
                    llTimePicker?.isClickable = true
                    // Only reset if it's currently TBD text
                    if (tvSelectedTime?.text == "Time TBD") {
                        tvSelectedTime?.text = "Select Time"
                        tvSelectedTime?.setTextColor(android.graphics.Color.parseColor("#94A3B8"))
                    }
                }
            }
            
            view.findViewById<View>(R.id.ll_date_picker)?.setOnClickListener {
                val calendar = java.util.Calendar.getInstance()
                android.app.DatePickerDialog(this, { _, year, month, day ->
                    val dateStr = "${month + 1}/$day/$year"
                    tvSelectedDate?.text = dateStr
                    tvSelectedDate?.setTextColor(android.graphics.Color.parseColor("#1E293B"))
                }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
            }
            
            llTimePicker?.setOnClickListener {
                val calendar = java.util.Calendar.getInstance()
                android.app.TimePickerDialog(this, { _, hour, minute ->
                    val amPm = if (hour >= 12) "PM" else "AM"
                    val hr = if (hour % 12 == 0) 12 else hour % 12
                    val timeStr = String.format("%02d:%02d %s", hr, minute, amPm)
                    tvSelectedTime?.text = timeStr
                    tvSelectedTime?.setTextColor(android.graphics.Color.parseColor("#1E293B"))
                }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), false).show()
            }
            
            // Appointment Type Dropdown
            view.findViewById<View>(R.id.ll_type_dropdown)?.setOnClickListener { dropdownView ->
                val popup = android.widget.PopupMenu(this, dropdownView)
                val types = listOf("Regular Checkup", "Emergency", "Fitting", "Consultation")
                types.forEachIndexed { index, type ->
                    popup.menu.add(0, index, 0, type)
                }
                popup.setOnMenuItemClickListener { menuItem ->
                    tvSelectedType?.text = menuItem.title
                    true
                }
                popup.show()
            }
            
            val btnConfirm = view.findViewById<View>(R.id.btn_confirm_appointment)
            btnConfirm?.setOnClickListener {
                bottomSheetDialog.dismiss()
                llNoAppointment?.visibility = View.GONE
                llHasAppointment?.visibility = View.VISIBLE
                
                val dateText = tvSelectedDate?.text.toString()
                val timeText = tvSelectedTime?.text.toString()
                val typeText = tvSelectedType?.text.toString()
                val notesText = etNotes?.text.toString()
                
                // Update dynamic UI in profile
                findViewById<android.widget.TextView>(R.id.tv_appt_title)?.text = typeText
                if (dateText != "Select Date") {
                    if (switchTimeTbd?.isChecked == true) {
                        findViewById<android.widget.TextView>(R.id.tv_appt_date)?.text = dateText
                    } else {
                        findViewById<android.widget.TextView>(R.id.tv_appt_date)?.text = "$dateText • $timeText"
                    }
                }
                findViewById<android.widget.TextView>(R.id.tv_appt_type)?.text = typeText
                
                if (!notesText.isNullOrEmpty()) {
                    findViewById<android.widget.EditText>(R.id.et_patient_notes)?.setText(notesText)
                }
                
                prefs.edit().apply {
                    putBoolean("appt_$patientId", true)
                    putString("appt_date_$patientId", dateText)
                    putString("appt_time_$patientId", if (switchTimeTbd?.isChecked == true) "TBD" else timeText)
                    putString("appt_type_$patientId", typeText)
                    putBoolean("appt_tbd_$patientId", switchTimeTbd?.isChecked == true)
                    putString("appt_notes_$patientId", notesText)
                    apply()
                }
                
                // Sync with global state for dashboard
                getSharedPreferences("OrthoGuidePrefs", android.content.Context.MODE_PRIVATE)
                    .edit().putBoolean("appt_$patientId", true).apply()
            }
            
            bottomSheetDialog.show()
        }

        findViewById<View>(R.id.btn_schedule_appt_empty)?.setOnClickListener { showScheduleBottomSheet() }
        findViewById<View>(R.id.btn_reschedule_appt)?.setOnClickListener { showScheduleBottomSheet() }

        findViewById<View>(R.id.btn_cancel_appt)?.setOnClickListener {
            // Simulate cancellation
            llHasAppointment?.visibility = View.GONE
            llNoAppointment?.visibility = View.VISIBLE
            
            prefs.edit().putBoolean("appt_$patientId", false).apply()
            
            // Sync with global state for dashboard and schedule
            getSharedPreferences("OrthoGuidePrefs", android.content.Context.MODE_PRIVATE)
                .edit().putBoolean("appt_$patientId", false).apply()
        }
    }
}
