package com.simats.orthoguide

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
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

class UnifiedLoginActivity : AppCompatActivity() {

    private var selectedRole = "patient"
    private var isPasswordVisible = false

    private lateinit var tabPatient: TextView
    private lateinit var tabDoctor: TextView
    private lateinit var tabAdmin: TextView
    private lateinit var etUserId: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvIdLabel: TextView
    private lateinit var tvError: TextView
    private lateinit var btnSignIn: View
    private lateinit var pbLoading: ProgressBar
    private lateinit var ivTogglePassword: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_unified_login)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val rootView = findViewById<View>(R.id.unified_login_root)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        tabPatient = findViewById(R.id.tab_patient)
        tabDoctor = findViewById(R.id.tab_doctor)
        tabAdmin = findViewById(R.id.tab_admin)
        etUserId = findViewById(R.id.et_user_id)
        etPassword = findViewById(R.id.et_password)
        tvIdLabel = findViewById(R.id.tv_id_label)
        tvError = findViewById(R.id.tv_error_message)
        btnSignIn = findViewById(R.id.btn_sign_in)
        pbLoading = findViewById(R.id.pb_loading)
        ivTogglePassword = findViewById(R.id.iv_toggle_password)

        // Set up role tabs
        tabPatient.setOnClickListener { selectRole("patient") }
        tabDoctor.setOnClickListener { selectRole("clinician") }
        tabAdmin.setOnClickListener { selectRole("admin") }

        // Default selection
        selectRole("patient")

        // Toggle password visibility
        ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivTogglePassword.setImageResource(R.drawable.ic_visibility_on)
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivTogglePassword.setImageResource(R.drawable.ic_visibility_off)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        // Sign in
        btnSignIn.setOnClickListener {
            val userId = etUserId.text.toString().trim()
            val password = etPassword.text.toString().trim()

            tvError.visibility = View.GONE

            if (userId.isEmpty() || password.isEmpty()) {
                showError("Please enter both your ID and password.")
                return@setOnClickListener
            }

            performLogin(userId, password)
        }

        // Forgot password
        findViewById<View>(R.id.tv_forgot_password)?.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            intent.putExtra("ROLE", selectedRole)
            startActivity(intent)
        }
    }

    private fun selectRole(role: String) {
        selectedRole = role
        tvError.visibility = View.GONE

        // Reset all tabs
        tabPatient.setBackgroundResource(0)
        tabPatient.setTextColor(Color.parseColor("#6B7280"))
        tabDoctor.setBackgroundResource(0)
        tabDoctor.setTextColor(Color.parseColor("#6B7280"))
        tabAdmin.setBackgroundResource(0)
        tabAdmin.setTextColor(Color.parseColor("#6B7280"))

        // Set active tab
        when (role) {
            "patient" -> {
                tabPatient.setBackgroundResource(R.drawable.bg_tab_active)
                tabPatient.setTextColor(Color.WHITE)
                tvIdLabel.text = "Patient ID"
                etUserId.hint = "e.g. PAT192210667"
            }
            "clinician" -> {
                tabDoctor.setBackgroundResource(R.drawable.bg_tab_active)
                tabDoctor.setTextColor(Color.WHITE)
                tvIdLabel.text = "Doctor ID"
                etUserId.hint = "e.g. DOC192210667"
            }
            "admin" -> {
                tabAdmin.setBackgroundResource(R.drawable.bg_tab_active)
                tabAdmin.setTextColor(Color.WHITE)
                tvIdLabel.text = "Admin ID"
                etUserId.hint = "e.g. admin001"
            }
        }
    }

    private fun performLogin(userId: String, password: String) {
        setLoading(true)

        val loginRequest = LoginRequest(userId, password, selectedRole)
        RetrofitClient.service.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                setLoading(false)

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        saveUserSession(loginResponse)

                        when (selectedRole) {
                            "patient" -> {
                                if (loginResponse.consentGiven == true) {
                                    startActivity(Intent(this@UnifiedLoginActivity, DashboardActivity::class.java))
                                } else {
                                    startActivity(Intent(this@UnifiedLoginActivity, ConsentActivity::class.java))
                                }
                            }
                            "clinician" -> {
                                startActivity(Intent(this@UnifiedLoginActivity, ClinicianDashboardActivity::class.java))
                            }
                            "admin" -> {
                                startActivity(Intent(this@UnifiedLoginActivity, AdminDashboardActivity::class.java))
                            }
                        }
                        finishAffinity()
                    }
                } else {
                    if (response.code() == 403 && (selectedRole == "patient" || selectedRole == "clinician")) {
                        val intent = Intent(this@UnifiedLoginActivity, AccountDeactivatedActivity::class.java)
                        intent.putExtra("PATIENT_ID", userId)
                        intent.putExtra("USER_ROLE", selectedRole)
                        startActivity(intent)
                        return
                    }

                    val errorMsg = try {
                        val errorBody = response.errorBody()?.string()
                        val errorJson = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
                        (errorJson["error"] ?: errorJson["message"] ?: "Invalid credentials. Please check your ID and password.").toString()
                    } catch (e: Exception) {
                        "Invalid credentials. Please check your ID and password."
                    }
                    showError(errorMsg)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                setLoading(false)
                Log.e("OrthoGuide", "Login failed", t)
                showError("Connection error. Please check your network and try again.")
            }
        })
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            btnSignIn.visibility = View.INVISIBLE
            pbLoading.visibility = View.VISIBLE
        } else {
            btnSignIn.visibility = View.VISIBLE
            pbLoading.visibility = View.GONE
        }
    }

    private fun saveUserSession(user: LoginResponse) {
        val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_ID", user.userId)
            putString("USER_NAME", user.name)
            putString("USER_ROLE", user.role)
            putString("USER_EMAIL", user.email)
            putString("USER_PHONE", user.phoneNumber)
            apply()
        }
    }
}

