package com.yourname.orthoguide

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
import com.yourname.orthoguide.network.AdminProfileResponse
import com.yourname.orthoguide.network.GenericResponse
import com.yourname.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminEditProfileActivity : AppCompatActivity() {

    private lateinit var etAdminId: EditText
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var tvInitials: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_edit_profile)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val rootView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        etAdminId = findViewById(R.id.et_admin_id)
        etName = findViewById(R.id.et_admin_name)
        etEmail = findViewById(R.id.et_contact_email)
        etPhone = findViewById(R.id.et_contact_phone)
        tvInitials = findViewById(R.id.tv_initials)

        val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", "ADMIN001") ?: "ADMIN001"
        etAdminId.setText(userId)

        etName.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateInitials(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        findViewById<View>(R.id.iv_back).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_cancel).setOnClickListener { finish() }
        
        fetchAdminProfile(userId)

        findViewById<View>(R.id.btn_save).setOnClickListener {
            saveAdminProfile(userId)
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

    private fun fetchAdminProfile(id: String) {
        if (id.isEmpty()) return
        RetrofitClient.service.getAdminProfile(id).enqueue(object : Callback<AdminProfileResponse> {
            override fun onResponse(call: Call<AdminProfileResponse>, response: Response<AdminProfileResponse>) {
                if (response.isSuccessful) {
                    val profile = response.body() ?: return
                    etName.setText(profile.name)
                    etEmail.setText(profile.email)
                    etPhone.setText(profile.phoneNumber)
                    updateInitials(profile.name ?: "")
                }
            }
            override fun onFailure(call: Call<AdminProfileResponse>, t: Throwable) {
                Log.e("OrthoGuide", "Admin fetch failed", t)
            }
        })
    }

    private fun saveAdminProfile(id: String) {
        val name = etName.text.toString()
        val email = etEmail.text.toString()
        val phone = etPhone.text.toString()

        val request = mapOf(
            "id" to id, // Using 'id' as per Unified route expectations
            "name" to name,
            "email" to email,
            "phone" to phone
        )

        RetrofitClient.service.updateAdminProfile(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful) {
                    // Update ALL SharedPreferences to reflect in Settings Card
                    getSharedPreferences("OrthoPref", Context.MODE_PRIVATE).edit()
                        .putString("USER_NAME", name)
                        .putString("USER_EMAIL", email)
                        .putString("USER_PHONE", phone)
                        .apply()
                        
                    com.yourname.orthoguide.util.DialogUtils.showSuccess(findViewById(android.R.id.content), "Profile updated successfully")
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        finish()
                    }, 1500)
                } else {
                    Toast.makeText(this@AdminEditProfileActivity, "Update failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Toast.makeText(this@AdminEditProfileActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
