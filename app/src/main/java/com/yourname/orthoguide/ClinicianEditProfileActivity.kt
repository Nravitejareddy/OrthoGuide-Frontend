package com.yourname.orthoguide

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.yourname.orthoguide.network.GenericResponse
import com.yourname.orthoguide.network.ProfileResponse
import com.yourname.orthoguide.network.RetrofitClient
import com.yourname.orthoguide.network.UpdateProfileRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClinicianEditProfileActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etRole: AutoCompleteTextView
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var tvInitials: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_clinician_edit_profile)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.card_toolbar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        etName = findViewById(R.id.et_name)
        etRole = findViewById(R.id.et_role)
        etEmail = findViewById(R.id.et_email)
        etPhone = findViewById(R.id.et_phone)
        tvInitials = findViewById(R.id.tv_initials)
        
        // Setup Role Dropdown
        val roles = arrayOf("Dentist", "Orthodontist", "Assistant")
        val adapter = ArrayAdapter(this, R.layout.item_dropdown, roles)
        etRole.setAdapter(adapter)

        etName.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateInitials(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        findViewById<View>(R.id.iv_back)?.setOnClickListener { finish() }
        findViewById<View>(R.id.btn_cancel)?.setOnClickListener { finish() }

        val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", "") ?: ""
        
        fetchProfileData(userId)

        findViewById<View>(R.id.btn_save)?.setOnClickListener {
            saveProfile(userId)
        }
    }

    private fun updateInitials(name: String) {
        val initials = name.split(" ")
            .filter { it.isNotEmpty() }
            .map { it[0].uppercaseChar() }
            .take(2)
            .joinToString("")
        tvInitials.text = if (initials.isNotEmpty()) initials else "--"
    }

    private fun fetchProfileData(id: String) {
        if (id.isEmpty()) return
        RetrofitClient.service.getClinicianProfile(id).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (response.isSuccessful) {
                    val profile = response.body() ?: return
                    etName.setText(profile.name)
                    etEmail.setText(profile.email)
                    etPhone.setText(profile.phone ?: profile.phoneNumber)
                    etRole.setText(profile.role ?: "Orthodontist", false)
                    updateInitials(profile.name)
                }
            }
            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Log.e("OrthoGuide", "Fetch failed", t)
            }
        })
    }

    private fun saveProfile(id: String) {
        val request = UpdateProfileRequest(
            clinicianId = id,
            name = etName.text.toString(),
            email = etEmail.text.toString(),
            phoneNumber = etPhone.text.toString(),
            role = etRole.text.toString()
        )

        RetrofitClient.service.updateClinicianProfile(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful) {
                    getSharedPreferences("OrthoPref", Context.MODE_PRIVATE).edit()
                        .putString("USER_NAME", request.name)
                        .apply()
                    com.yourname.orthoguide.util.DialogUtils.showSuccessSnackbar(findViewById(android.R.id.content), "Profile updated successfully")
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        finish()
                    }, 1000)
                } else {
                    com.yourname.orthoguide.util.DialogUtils.showErrorSnackbar(findViewById(android.R.id.content), "Update failed")
                }
            }
            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                com.yourname.orthoguide.util.DialogUtils.showErrorSnackbar(findViewById(android.R.id.content), "Network error")
            }
        })
    }
}
