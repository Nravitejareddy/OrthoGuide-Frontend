package com.simats.orthoguide

import android.content.Context
import android.graphics.Color
import android.os.Bundle
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
import com.simats.orthoguide.network.GenericResponse
import com.simats.orthoguide.network.RetrofitClient
import com.simats.orthoguide.network.UpdateProfileRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.card_toolbar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        val etName = findViewById<EditText>(R.id.et_name)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPhone = findViewById<EditText>(R.id.et_phone)
        val tvInitials = findViewById<TextView>(R.id.tv_initials)

        val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
        val userRole = sharedPref.getString("USER_ROLE", "patient") ?: "patient"
        val userId = sharedPref.getString("USER_ID", "") ?: ""

        etName.setText(sharedPref.getString("USER_NAME", ""))
        etEmail.setText(sharedPref.getString("USER_EMAIL", ""))
        etPhone.setText(sharedPref.getString("USER_PHONE", ""))
        
        fun updateInitials(name: String) {
            val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
            val initials = if (parts.size >= 2) {
                (parts[0].substring(0, 1) + parts[parts.size - 1].substring(0, 1)).uppercase()
            } else if (parts.isNotEmpty()) {
                parts[0].substring(0, 1).uppercase()
            } else {
                "--"
            }
            tvInitials?.text = initials
        }

        updateInitials(etName.text.toString())
        fetchProfileData(userRole, userId, etName, etEmail, etPhone)

        etName.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateInitials(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        findViewById<View>(R.id.iv_back)?.setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btn_save)?.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val phone = etPhone.text.toString()

            if (name.isEmpty()) {
                com.simats.orthoguide.util.DialogUtils.showError(this, "Name cannot be empty")
                return@setOnClickListener
            }

            saveProfile(userRole, userId, name, email, phone)
        }
    }



    private fun fetchProfileData(role: String, id: String, etName: EditText, etEmail: EditText, etPhone: EditText) {
        if (id.isEmpty()) return
        val call = if (role == "patient") {
            RetrofitClient.service.getPatientProfile(id)
        } else {
            RetrofitClient.service.getClinicianProfile(id)
        }

        call.enqueue(object : Callback<com.simats.orthoguide.network.ProfileResponse> {
            override fun onResponse(call: Call<com.simats.orthoguide.network.ProfileResponse>, response: Response<com.simats.orthoguide.network.ProfileResponse>) {
                if (response.isSuccessful) {
                    val profile = response.body() ?: return
                    etName.setText(profile.name)
                    etEmail.setText(profile.email)
                    etPhone.setText(profile.phone ?: profile.phoneNumber)
                }
            }
            override fun onFailure(call: Call<com.simats.orthoguide.network.ProfileResponse>, t: Throwable) {
                Log.e("OrthoGuide", "Fetch failed", t)
            }
        })
    }

    private fun saveProfile(role: String, id: String, name: String, email: String, phone: String) {
        val request = UpdateProfileRequest(
            patientId = if (role == "patient") id else null,
            clinicianId = if (role == "clinician") id else null,
            name = name,
            email = email,
            phoneNumber = phone
        )

        val call = if (role == "patient") {
            RetrofitClient.service.updatePatientProfile(request)
        } else {
            RetrofitClient.service.updateClinicianProfile(request)
        }

        call.enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful) {
                    getSharedPreferences("OrthoPref", Context.MODE_PRIVATE).edit()
                        .putString("USER_NAME", name)
                        .putString("USER_EMAIL", email)
                        .putString("USER_PHONE", phone)
                        .apply()
                    
                    com.simats.orthoguide.util.DialogUtils.showSuccess(findViewById(android.R.id.content), "Profile updated successfully!")
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        finish()
                    }, 1500)
                } else {
                    com.simats.orthoguide.util.DialogUtils.showError(this@EditProfileActivity, "Failed to update profile")
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Log.e("OrthoGuide", "Profile update failed", t)
                com.simats.orthoguide.util.DialogUtils.showError(this@EditProfileActivity, "Network error. Try again.")
            }
        })
    }
}

