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

class PatientLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("OrthoGuide", "PatientLoginActivity created")
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_login)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val rootView = findViewById<View>(R.id.patient_login_root)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        val etPatientId = findViewById<EditText>(R.id.et_patient_id_field)
        val etPassword = findViewById<EditText>(R.id.et_password_field)

        findViewById<ImageView>(R.id.iv_back)?.setOnClickListener {
            if (isTaskRoot) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            finish()
        }

        findViewById<View>(R.id.btn_login_secure)?.setOnClickListener {
            val patientId = etPatientId.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (patientId.isEmpty() || password.isEmpty()) {
                com.simats.orthoguide.util.DialogUtils.showError(this, "Please enter both ID and Password")
            } else {
                performLogin(patientId, password)
            }
        }

        findViewById<View>(R.id.tv_forgot_password_link)?.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        // Signup removed - users are created by admin/doctor only
    }

    private fun performLogin(userId: String, pass: String) {
        val loginRequest = LoginRequest(userId, pass, "patient")
        RetrofitClient.service.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        saveUserSession(loginResponse)
                        
                        // Decision based on consent
                        if (loginResponse.consentGiven == true) {
                            val intent = Intent(this@PatientLoginActivity, DashboardActivity::class.java)
                            startActivity(intent)
                        } else {
                            val intent = Intent(this@PatientLoginActivity, ConsentActivity::class.java)
                            startActivity(intent)
                        }
                        finishAffinity()
                    }
                } else {
                    com.simats.orthoguide.util.DialogUtils.showError(this@PatientLoginActivity, "Invalid Credentials")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("OrthoGuide", "Login failed", t)
                com.simats.orthoguide.util.DialogUtils.showError(this@PatientLoginActivity, "Connection Error: ${t.message}")
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

