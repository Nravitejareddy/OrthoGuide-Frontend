package com.simats.orthoguide

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.decorView.setBackgroundColor(android.graphics.Color.WHITE)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        setContentView(R.layout.activity_forgot_password)

        val role = intent.getStringExtra("ROLE") ?: "patient"
        
        val rootView = findViewById<View>(R.id.forgot_password_root)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        findViewById<ImageView>(R.id.iv_fp_back)?.setOnClickListener {
            finish()
        }

        val btnSendOtp = findViewById<MaterialButton>(R.id.btn_send_otp)
        val etEmail = findViewById<android.widget.EditText>(R.id.et_forgot_email)

        etEmail?.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.contains("@") && s.contains(".")) {
                    btnSendOtp?.isEnabled = true
                    btnSendOtp?.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        androidx.core.content.ContextCompat.getColor(this@ForgotPasswordActivity, R.color.brand_green)
                    )
                } else {
                    btnSendOtp?.isEnabled = false
                    btnSendOtp?.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#81d2bb")
                    )
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        btnSendOtp?.setOnClickListener {
            val email = etEmail?.text.toString().trim()
            if (email.isEmpty()) return@setOnClickListener

            btnSendOtp.isEnabled = false
            btnSendOtp.text = "Sending..."

            com.simats.orthoguide.network.RetrofitClient.service.sendOtp(mapOf("email" to email, "role" to role)).enqueue(object : retrofit2.Callback<com.simats.orthoguide.network.GenericResponse> {
                override fun onResponse(call: retrofit2.Call<com.simats.orthoguide.network.GenericResponse>, response: retrofit2.Response<com.simats.orthoguide.network.GenericResponse>) {
                    btnSendOtp.isEnabled = true
                    btnSendOtp.text = "Send Secure Code"
                    if (response.isSuccessful) {
                        val intent = android.content.Intent(this@ForgotPasswordActivity, VerifyOtpActivity::class.java)
                        intent.putExtra("EMAIL", email)
                        intent.putExtra("ROLE", role)
                        startActivity(intent)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = try {
                            if (errorBody != null) {
                                org.json.JSONObject(errorBody).getString("error")
                            } else {
                                "Email not found"
                            }
                        } catch (e: Exception) {
                            "Email not found"
                        }
                        com.simats.orthoguide.util.DialogUtils.showError(findViewById(android.R.id.content), errorMessage)
                    }
                }

                override fun onFailure(call: retrofit2.Call<com.simats.orthoguide.network.GenericResponse>, t: Throwable) {
                    btnSendOtp.isEnabled = true
                    btnSendOtp.text = "Send Secure Code"
                    com.simats.orthoguide.util.DialogUtils.showError(findViewById(android.R.id.content), "Network error")
                }
            })
        }
    }
}

