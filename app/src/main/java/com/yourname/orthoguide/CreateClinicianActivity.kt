package com.yourname.orthoguide

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.yourname.orthoguide.util.DialogUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.yourname.orthoguide.network.GenericResponse
import com.yourname.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateClinicianActivity : AppCompatActivity() {

    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_clinician)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val rootView = findViewById<View>(R.id.create_clinician_root)
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
        
        val etId = findViewById<TextInputEditText>(R.id.et_clinician_id)
        val etName = findViewById<TextInputEditText>(R.id.et_full_name)
        val etEmail = findViewById<TextInputEditText>(R.id.et_email)
        val etRole = findViewById<AutoCompleteTextView>(R.id.et_role)
        val etPhone = findViewById<TextInputEditText>(R.id.et_phone_number)

        isEditMode = intent.getBooleanExtra("isEditMode", false)

        if (isEditMode) {
            tvHeaderTitle.text = "Update Clinician"
            btnSave.text = "Update Clinician"

            etId.setText(intent.getStringExtra("clinicianId") ?: "")
            etId.isEnabled = false // Cannot change ID during edit
            etName.setText(intent.getStringExtra("clinicianName") ?: "")
            etEmail.setText(intent.getStringExtra("clinicianEmail") ?: "")
            etRole.setText(intent.getStringExtra("clinicianRole") ?: "Orthodontist", false)
            etPhone.setText(intent.getStringExtra("clinicianPhone") ?: "")
        } else {
            tvHeaderTitle.text = "Create Clinician Account"
            btnSave.text = "Create Account"
        }
        
        btnSave.setOnClickListener {
            saveClinician()
        }
    }

    private fun saveClinician() {
        val etId = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_clinician_id)
        val etName = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_full_name)
        val etEmail = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_email)
        val etRole = findViewById<AutoCompleteTextView>(R.id.et_role)
        val etPhone = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_phone_number)

        val id = etId.text.toString().trim()
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val roleType = etRole.text.toString().trim()
        val phone = etPhone.text.toString().trim()

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
            "role" to "clinician",
            "role_type" to roleType
        )

        if (isEditMode) {
            // Update call
            RetrofitClient.service.adminUpdateUser(request).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful) {
                        DialogUtils.showSuccessSnackbar(findViewById(android.R.id.content), "Clinician updated successfully")
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
                        Toast.makeText(this@CreateClinicianActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    btnSave?.isEnabled = true
                    btnSave?.text = originalText
                    Toast.makeText(this@CreateClinicianActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // Create call
            RetrofitClient.service.adminCreateUser(request).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful) {
                        DialogUtils.showSuccessSnackbar(findViewById(android.R.id.content), "Clinician account created successfully")
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
                        Toast.makeText(this@CreateClinicianActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    btnSave?.isEnabled = true
                    btnSave?.text = originalText
                    Toast.makeText(this@CreateClinicianActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        val etRole = findViewById<AutoCompleteTextView>(R.id.et_role)
        val roles = arrayOf("Orthodontist", "Dentist", "Assistant")
        val adapter = ArrayAdapter(this, R.layout.item_dropdown, roles)
        etRole.setAdapter(adapter)
    }
}
