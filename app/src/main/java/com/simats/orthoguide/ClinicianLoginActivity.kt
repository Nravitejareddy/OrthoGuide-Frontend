package com.simats.orthoguide

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.simats.orthoguide.network.LoginRequest
import com.simats.orthoguide.network.LoginResponse
import com.simats.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClinicianLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_clinician_login)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val rootView = findViewById<View>(R.id.clinician_login_root)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        val etProviderId = findViewById<EditText>(R.id.et_provider_id)
        val etPassword = findViewById<EditText>(R.id.et_password)

        findViewById<ImageView>(R.id.iv_back)?.setOnClickListener {
            if (isTaskRoot) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            finish()
        }

        findViewById<View>(R.id.btn_login)?.setOnClickListener {
            val providerId = etProviderId.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (providerId.isEmpty() || password.isEmpty()) {
                com.simats.orthoguide.util.DialogUtils.showErrorSnackbar(findViewById(android.R.id.content), "Please enter both ID and Password")
                return@setOnClickListener
            }

            // In this app, we check if it's admin or clinician based on the ID or a toggle,
            // but the backend /login endpoint takes a role. Let's try clinician first, if fails try admin.
            // Or better, let the backend handle it if we adjust the backend.
            // For now, let's assume if ID is 'admin' it's admin role.
            val role = if (providerId.lowercase() == "admin") "admin" else "clinician"
            performLogin(providerId, password, role)
        }
    }

    private fun performLogin(userId: String, pass: String, role: String) {
        val loginRequest = LoginRequest(userId, pass, role)
        RetrofitClient.service.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        saveUserSession(loginResponse)
                        val intent = if (loginResponse.role == "admin") {
                            Intent(this@ClinicianLoginActivity, AdminDashboardActivity::class.java)
                        } else {
                            Intent(this@ClinicianLoginActivity, ClinicianDashboardActivity::class.java)
                        }
                        startActivity(intent)
                        finishAffinity()
                    }
                } else {
                    com.simats.orthoguide.util.DialogUtils.showErrorSnackbar(findViewById(android.R.id.content), "Invalid Credentials")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("OrthoGuide", "Login failed", t)
                com.simats.orthoguide.util.DialogUtils.showErrorSnackbar(findViewById(android.R.id.content), "Connection Error")
            }
        })
    }

    private fun saveUserSession(user: LoginResponse) {
        val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_ID", user.userId)
            putString("USER_NAME", user.name)
            putString("USER_ROLE", user.role)
            apply()
        }
    }
}

