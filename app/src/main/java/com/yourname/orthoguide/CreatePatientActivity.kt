package com.yourname.orthoguide

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class CreatePatientActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_patient)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val rootView = findViewById<View>(R.id.create_patient_root)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, 0, systemBars.right, 0)
                insets
            }
        }

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }

        val btnSave = findViewById<MaterialButton>(R.id.btn_save)
        val tvHeaderTitle = findViewById<TextView>(R.id.tv_header_title)
        
        val etId = findViewById<android.widget.EditText>(R.id.et_patient_id)
        val etName = findViewById<android.widget.EditText>(R.id.et_patient_name)
        val etEmail = findViewById<android.widget.EditText>(R.id.et_patient_email)
        val etPhone = findViewById<android.widget.EditText>(R.id.et_phone)
        val actvTreatmentStage = findViewById<android.widget.AutoCompleteTextView>(R.id.actv_treatment_stage)

        val stages = arrayOf(
            "Initial Consultation",
            "Bonding / First Trays",
            "Alignment Phase",
            "Bite Correction",
            "Finishing & Detailing",
            "Debonding & Retention"
        )
        val adapter = android.widget.ArrayAdapter(this, R.layout.item_dropdown, stages)
        actvTreatmentStage?.setAdapter(adapter)

        val isEditMode = intent.getBooleanExtra("isEditMode", false)

        if (isEditMode) {
            tvHeaderTitle.text = "Update Patient"
            btnSave.text = "Update Patient"

            etId?.setText(intent.getStringExtra("patientId") ?: "")
            etName?.setText(intent.getStringExtra("patientName") ?: "")
            etEmail?.setText(intent.getStringExtra("patientEmail") ?: "")
            etPhone?.setText(intent.getStringExtra("patientPhone") ?: "+91 98765 43210")
            actvTreatmentStage?.setText(intent.getStringExtra("treatmentStage") ?: "Initial Consultation", false)
        } else {
            tvHeaderTitle.text = "Create Patient Account"
            btnSave.text = "Create Account"
            etPhone?.setText("+91 ")
        }

        applyTheme()
        
        btnSave.setOnClickListener {
            // In a real app, we would save the data here
            finish()
        }

        // Dropdown selection handling
        actvTreatmentStage?.setOnClickListener {
            actvTreatmentStage.showDropDown()
            android.util.Log.d("OrthoGuide", "Showing dropdown")
        }
        
        actvTreatmentStage?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                actvTreatmentStage.showDropDown()
            }
        }
    }

    private fun applyTheme() {
        val role = intent.getStringExtra("userRole") ?: "admin"
        val btnSave = findViewById<MaterialButton>(R.id.btn_save)
        val mcvInfo = findViewById<com.google.android.material.card.MaterialCardView>(R.id.mcv_info_banner)
        val ivInfoIcon = findViewById<ImageView>(R.id.iv_info_icon)
        val tvInfoText = findViewById<TextView>(R.id.tv_info_text)

        if (role == "clinician") {
            // Blue Theme
            btnSave?.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#2563EB"))
            mcvInfo?.setCardBackgroundColor(Color.parseColor("#EFF6FF"))
            mcvInfo?.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#DBEAFE")))
            ivInfoIcon?.setImageResource(R.drawable.ic_warning_blue)
            ivInfoIcon?.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#3B82F6"))
            tvInfoText?.setTextColor(Color.parseColor("#2563EB"))
        } else {
            // Admin Purple Theme (Already default in XML, but ensuring consistency)
            btnSave?.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#7C3AED"))
            mcvInfo?.setCardBackgroundColor(Color.parseColor("#FAF5FF"))
            mcvInfo?.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#F3E8FF")))
            ivInfoIcon?.setImageResource(R.drawable.ic_warning_purple)
            ivInfoIcon?.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#A855F7"))
            tvInfoText?.setTextColor(Color.parseColor("#9333EA"))
        }
    }
}
