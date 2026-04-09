package com.simats.orthoguide

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.simats.orthoguide.util.DialogUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.button.MaterialButton
import com.simats.orthoguide.network.GenericResponse
import com.simats.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreatePatientActivity : AppCompatActivity() {

    private var isEditMode = false

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
        
        val etId = findViewById<EditText>(R.id.et_patient_id)
        val etName = findViewById<EditText>(R.id.et_patient_name)
        val etEmail = findViewById<EditText>(R.id.et_patient_email)
        val etPhone = findViewById<EditText>(R.id.et_phone_number)
        val actvTreatmentStage = findViewById<AutoCompleteTextView>(R.id.actv_treatment_stage)

        val stages = arrayOf(
            "Initial Consultation",
            "Bonding / First Trays",
            "Alignment Phase",
            "Bite Correction",
            "Finishing & Detailing",
            "Debonding & Retention"
        )
        val adapter = ArrayAdapter(this, R.layout.item_dropdown, stages)
        actvTreatmentStage?.setAdapter(adapter)

        isEditMode = intent.getBooleanExtra("isEditMode", false)

        if (isEditMode) {
            tvHeaderTitle.text = "Update Patient"
            btnSave.text = "Update Patient"

            etId?.setText(intent.getStringExtra("patientId") ?: "")
            etId?.isEnabled = false
            etName?.setText(intent.getStringExtra("patientName") ?: "")
            etEmail?.setText(intent.getStringExtra("patientEmail") ?: "")
            etPhone?.setText(intent.getStringExtra("patientPhone") ?: "")
            actvTreatmentStage?.setText(intent.getStringExtra("treatmentStage") ?: "Initial Consultation", false)
        } else {
            tvHeaderTitle.text = "Create Patient Account"
            btnSave.text = "Create Account"
            actvTreatmentStage?.setText("Initial Consultation", false)
        }

        applyTheme()
        
        btnSave.setOnClickListener {
            savePatient()
        }

        actvTreatmentStage?.setOnClickListener {
            actvTreatmentStage.showDropDown()
        }
    }

    private fun savePatient() {
        val etId = findViewById<EditText>(R.id.et_patient_id)
        val etName = findViewById<EditText>(R.id.et_patient_name)
        val etEmail = findViewById<EditText>(R.id.et_patient_email)
        val etPhone = findViewById<EditText>(R.id.et_phone_number)
        val actvTreatmentStage = findViewById<AutoCompleteTextView>(R.id.actv_treatment_stage)

        val id = etId.text.toString().trim()
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val treatmentStage = actvTreatmentStage.text.toString().trim()

        if (id.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Strict Validation
        val nameRegex = Regex("^[A-Za-z\\s]{3,}$")
        if (!nameRegex.matches(name)) {
            Toast.makeText(this, "Name must be at least 3 letters (no numbers or symbols)", Toast.LENGTH_LONG).show()
            return
        }

        val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@gmail\\.com$")
        if (!emailRegex.matches(email)) {
            Toast.makeText(this, "Only @gmail.com email addresses are allowed", Toast.LENGTH_LONG).show()
            return
        }

        val cleanPhone = phone.replace(Regex("\\D"), "")
        if (cleanPhone.length != 10) {
            Toast.makeText(this, "Phone number must be exactly 10 digits", Toast.LENGTH_LONG).show()
            return
        }

        val btnSave = findViewById<MaterialButton>(R.id.btn_save)
        btnSave?.isEnabled = false
        val originalText = btnSave?.text.toString()
        btnSave?.text = if (isEditMode) "Updating..." else "Creating..."

        val request = mutableMapOf<String, String>(
            "id" to id,
            "name" to name,
            "email" to email,
            "phone_number" to cleanPhone,
            "role" to "patient",
            "treatment_stage" to treatmentStage
        )

        val sharedPrefs = getSharedPreferences("OrthoPref", MODE_PRIVATE)
        val clinicianId = sharedPrefs.getString("USER_ID", "")
        if (!clinicianId.isNullOrEmpty()) {
            request["clinician_id"] = clinicianId
        }

        if (isEditMode) {
            RetrofitClient.service.adminUpdateUser(request).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful) {
                        DialogUtils.showSuccessSnackbar(findViewById(android.R.id.content), "Patient updated successfully")
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            finish()
                        }, 1500)
                    } else {
                        btnSave?.isEnabled = true
                        btnSave?.text = originalText
                        val errorMsg = try {
                            val errorBody = response.errorBody()?.string()
                            val errorJson = com.google.gson.Gson().fromJson(errorBody, GenericResponse::class.java)
                            errorJson.error ?: errorJson.message ?: "Update failed"
                        } catch (e: Exception) { "Update failed" }
                        Toast.makeText(this@CreatePatientActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    btnSave?.isEnabled = true
                    btnSave?.text = originalText
                    Toast.makeText(this@CreatePatientActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            RetrofitClient.service.adminCreateUser(request).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful) {
                        DialogUtils.showSuccessSnackbar(findViewById(android.R.id.content), "Patient account created successfully")
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            finish()
                        }, 1500)
                    } else {
                        btnSave?.isEnabled = true
                        btnSave?.text = originalText
                        val errorMsg = try {
                            val errorBody = response.errorBody()?.string()
                            val errorJson = com.google.gson.Gson().fromJson(errorBody, GenericResponse::class.java)
                            errorJson.error ?: errorJson.message ?: "Creation failed"
                        } catch (e: Exception) { "Creation failed" }
                        Toast.makeText(this@CreatePatientActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    btnSave?.isEnabled = true
                    btnSave?.text = originalText
                    Toast.makeText(this@CreatePatientActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun applyTheme() {
        val role = intent.getStringExtra("userRole") ?: "admin"
        val btnSave = findViewById<MaterialButton>(R.id.btn_save)
        val mcvInfo = findViewById<com.google.android.material.card.MaterialCardView>(R.id.mcv_info_banner)
        val ivInfoIcon = findViewById<ImageView>(R.id.iv_info_icon)
        val tvInfoText = findViewById<TextView>(R.id.tv_info_text)

        if (role == "clinician") {
            btnSave?.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#2563EB"))
            mcvInfo?.setCardBackgroundColor(Color.parseColor("#EFF6FF"))
            mcvInfo?.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#DBEAFE")))
            ivInfoIcon?.setImageResource(R.drawable.ic_warning_blue)
            ivInfoIcon?.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#3B82F6"))
            tvInfoText?.setTextColor(Color.parseColor("#2563EB"))
        } else {
            btnSave?.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#7C3AED"))
            mcvInfo?.setCardBackgroundColor(Color.parseColor("#FAF5FF"))
            mcvInfo?.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#F3E8FF")))
            ivInfoIcon?.setImageResource(R.drawable.ic_warning_purple)
            ivInfoIcon?.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#A855F7"))
            tvInfoText?.setTextColor(Color.parseColor("#9333EA"))
        }
    }
}

