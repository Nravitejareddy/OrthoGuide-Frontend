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

class VerifyOtpActivity : AppCompatActivity() {
    private var countDownTimer: android.os.CountDownTimer? = null
    private var canResend = false
    private var userRole = "patient"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.decorView.setBackgroundColor(android.graphics.Color.WHITE)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        setContentView(R.layout.activity_verify_otp)

        val rootView = findViewById<View>(R.id.verify_otp_root)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        findViewById<ImageView>(R.id.iv_vo_back)?.setOnClickListener {
            finish()
        }

        val email = intent.getStringExtra("EMAIL") ?: ""
        val action = intent.getStringExtra("ACTION") ?: "reset"
        val name = intent.getStringExtra("NAME") ?: ""
        val password = intent.getStringExtra("PASSWORD") ?: ""
        userRole = intent.getStringExtra("ROLE") ?: "patient"
        
        findViewById<MaterialButton>(R.id.btn_verify_continue)?.setOnClickListener { btn ->
            val btnVerify = btn as MaterialButton
            val o1 = findViewById<android.widget.EditText>(R.id.et_otp_1)?.text.toString()
            val o2 = findViewById<android.widget.EditText>(R.id.et_otp_2)?.text.toString()
            val o3 = findViewById<android.widget.EditText>(R.id.et_otp_3)?.text.toString()
            val o4 = findViewById<android.widget.EditText>(R.id.et_otp_4)?.text.toString()
            val o5 = findViewById<android.widget.EditText>(R.id.et_otp_5)?.text.toString()
            val o6 = findViewById<android.widget.EditText>(R.id.et_otp_6)?.text.toString()
            val otpCode = "$o1$o2$o3$o4$o5$o6"

            btnVerify.isEnabled = false
            btnVerify.text = "Verifying..."

            com.simats.orthoguide.network.RetrofitClient.service.verifyOtp(mapOf("email" to email, "otp" to otpCode))
                .enqueue(object : retrofit2.Callback<com.simats.orthoguide.network.GenericResponse> {
                    override fun onResponse(call: retrofit2.Call<com.simats.orthoguide.network.GenericResponse>, response: retrofit2.Response<com.simats.orthoguide.network.GenericResponse>) {
                        if (response.isSuccessful) {
                            if (action == "signup") {
                                btnVerify.text = "Creating Account..."
                                val payload = mapOf(
                                    "name" to name,
                                    "email" to email,
                                    "password" to password,
                                    "role" to userRole
                                )
                                com.simats.orthoguide.network.RetrofitClient.service.signup(payload).enqueue(object : retrofit2.Callback<com.simats.orthoguide.network.GenericResponse> {
                                    override fun onResponse(call: retrofit2.Call<com.simats.orthoguide.network.GenericResponse>, response: retrofit2.Response<com.simats.orthoguide.network.GenericResponse>) {
                                        btnVerify.isEnabled = true
                                        btnVerify.text = "Verify & Continue"
                                        if (response.isSuccessful) {
                                            com.simats.orthoguide.util.DialogUtils.showSuccess(findViewById(android.R.id.content), "Account Created")
                                            val loginIntent = android.content.Intent(this@VerifyOtpActivity, MainActivity::class.java)
                                            startActivity(loginIntent)
                                            finishAffinity()
                                        } else {
                                            com.simats.orthoguide.util.DialogUtils.showError(findViewById(android.R.id.content), "Signup Failed")
                                        }
                                    }
                                    override fun onFailure(call: retrofit2.Call<com.simats.orthoguide.network.GenericResponse>, t: Throwable) {
                                        btnVerify.isEnabled = true
                                        btnVerify.text = "Verify & Continue"
                                        com.simats.orthoguide.util.DialogUtils.showError(this@VerifyOtpActivity, "Network Error")
                                    }
                                })
                            } else {
                                btnVerify.isEnabled = true
                                btnVerify.text = "Verify & Continue"
                                val nextIntent = android.content.Intent(this@VerifyOtpActivity, CreateNewPasswordActivity::class.java)
                                nextIntent.putExtra("EMAIL", email)
                                nextIntent.putExtra("ROLE", userRole)
                                startActivity(nextIntent)
                            }
                        } else {
                            btnVerify.isEnabled = true
                            btnVerify.text = "Verify & Continue"
                            com.simats.orthoguide.util.DialogUtils.showError(this@VerifyOtpActivity, "Invalid OTP")
                        }
                    }
                    override fun onFailure(call: retrofit2.Call<com.simats.orthoguide.network.GenericResponse>, t: Throwable) {
                        btnVerify.isEnabled = true
                        btnVerify.text = "Verify & Continue"
                        com.simats.orthoguide.util.DialogUtils.showError(this@VerifyOtpActivity, "Network error")
                    }
                })
        }

        findViewById<android.widget.LinearLayout>(R.id.ll_resend_container)?.setOnClickListener {
            if (canResend) {
                resendOtp(email)
            }
        }

        setupOtpInputs()
        startResendTimer()
    }

    private fun startResendTimer() {
        canResend = false
        val tvLabel = findViewById<android.widget.TextView>(R.id.tv_resend_label)
        val tvTimer = findViewById<android.widget.TextView>(R.id.tv_resend_timer)

        tvLabel?.text = "Resend code in "
        tvTimer?.visibility = android.view.View.VISIBLE

        countDownTimer?.cancel()
        countDownTimer = object : android.os.CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                tvTimer?.text = "$secondsRemaining s"
            }

            override fun onFinish() {
                canResend = true
                tvLabel?.text = "Didn't receive code? "
                tvTimer?.text = "Resend Now"
                tvTimer?.setTextColor(androidx.core.content.ContextCompat.getColor(this@VerifyOtpActivity, R.color.brand_green))
            }
        }.start()
    }

    private fun resendOtp(email: String) {
        findViewById<android.widget.TextView>(R.id.tv_resend_label)?.text = "Resending..."
        findViewById<android.widget.TextView>(R.id.tv_resend_timer)?.visibility = android.view.View.GONE

        com.simats.orthoguide.network.RetrofitClient.service.sendOtp(mapOf("email" to email, "role" to userRole))
            .enqueue(object : retrofit2.Callback<com.simats.orthoguide.network.GenericResponse> {
                override fun onResponse(call: retrofit2.Call<com.simats.orthoguide.network.GenericResponse>, response: retrofit2.Response<com.simats.orthoguide.network.GenericResponse>) {
                    if (response.isSuccessful) {
                        val root = findViewById<android.view.View>(R.id.verify_otp_root)
                        if (root != null) {
                            com.simats.orthoguide.util.DialogUtils.showSuccess(root, "OTP Resent")
                        } else {
                            com.simats.orthoguide.util.DialogUtils.showSuccess(this@VerifyOtpActivity, "OTP Resent")
                        }
                        startResendTimer()
                    } else {
                        com.simats.orthoguide.util.DialogUtils.showError(this@VerifyOtpActivity, "Failed to resend OTP")
                        startResendTimer() // Allow retry anyway
                    }
                }
                override fun onFailure(call: retrofit2.Call<com.simats.orthoguide.network.GenericResponse>, t: Throwable) {
                    android.widget.Toast.makeText(this@VerifyOtpActivity, "Network Error", android.widget.Toast.LENGTH_SHORT).show()
                    startResendTimer() // Allow retry anyway
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun setupOtpInputs() {
        val otp1 = findViewById<android.widget.EditText>(R.id.et_otp_1)
        val otp2 = findViewById<android.widget.EditText>(R.id.et_otp_2)
        val otp3 = findViewById<android.widget.EditText>(R.id.et_otp_3)
        val otp4 = findViewById<android.widget.EditText>(R.id.et_otp_4)
        val otp5 = findViewById<android.widget.EditText>(R.id.et_otp_5)
        val otp6 = findViewById<android.widget.EditText>(R.id.et_otp_6)
        
        val btnVerify = findViewById<MaterialButton>(R.id.btn_verify_continue)
        val editTexts = listOf(otp1, otp2, otp3, otp4, otp5, otp6)

        val checkCompletion = {
            val allFilled = editTexts.all { it?.text?.length == 1 }
            if (allFilled) {
                btnVerify?.isEnabled = true
                btnVerify?.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    androidx.core.content.ContextCompat.getColor(this@VerifyOtpActivity, R.color.brand_green)
                )
            } else {
                btnVerify?.isEnabled = false
                btnVerify?.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#81d2bb")
                )
            }
        }

        for (i in editTexts.indices) {
            val currentEditText = editTexts[i]
            val nextEditText = if (i < editTexts.size - 1) editTexts[i + 1] else null
            val previousEditText = if (i > 0) editTexts[i - 1] else null

            currentEditText?.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        nextEditText?.requestFocus()
                    }
                    checkCompletion()
                }

                override fun afterTextChanged(s: android.text.Editable?) {}
            })

            currentEditText?.setOnKeyListener { _, keyCode, event ->
                if (event.action == android.view.KeyEvent.ACTION_DOWN && keyCode == android.view.KeyEvent.KEYCODE_DEL) {
                    if (currentEditText.text.isEmpty()) {
                        previousEditText?.requestFocus()
                        previousEditText?.text?.clear()
                        checkCompletion()
                        return@setOnKeyListener true
                    }
                }
                false
            }
        }
    }
}

