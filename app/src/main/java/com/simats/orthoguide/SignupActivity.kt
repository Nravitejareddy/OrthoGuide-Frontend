package com.simats.orthoguide

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.simats.orthoguide.network.GenericResponse
import com.simats.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        val ivBack = findViewById<ImageView>(R.id.iv_back_signup)
        val etName = findViewById<EditText>(R.id.et_signup_name)
        val etEmail = findViewById<EditText>(R.id.et_signup_email)
        val etPassword = findViewById<EditText>(R.id.et_signup_password)
        val rgRole = findViewById<RadioGroup>(R.id.rg_role)
        val btnSignup = findViewById<MaterialButton>(R.id.btn_signup)

        val ivTogglePassword = findViewById<ImageView>(R.id.iv_toggle_signup_password)
        if (etPassword != null && ivTogglePassword != null) {
            com.simats.orthoguide.util.PasswordToggleHelper.setupPasswordToggle(etPassword, ivTogglePassword)
        }

        ivBack.setOnClickListener {
            finish()
        }

        btnSignup.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val role = if (rgRole.checkedRadioButtonId == R.id.rb_clinician) "clinician" else "patient"

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                com.simats.orthoguide.util.DialogUtils.showError(findViewById(android.R.id.content), "Please fill in all fields")
                return@setOnClickListener
            }

            val errorMsg = validatePassword(password)
            if (errorMsg != null) {
                com.simats.orthoguide.util.DialogUtils.showError(findViewById(android.R.id.content), errorMsg)
                return@setOnClickListener
            }

            btnSignup.isEnabled = false
            btnSignup.text = "Sending OTP..."

            val payload = mapOf(
                "email" to email,
                "action" to "signup"
            )

            RetrofitClient.service.sendOtp(payload).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    btnSignup.isEnabled = true
                    btnSignup.text = "Sign Up"
                    if (response.isSuccessful) {
                        com.simats.orthoguide.util.DialogUtils.showSuccess(findViewById(android.R.id.content), "OTP sent to $email")
                        val intent = Intent(this@SignupActivity, VerifyOtpActivity::class.java)
                        intent.putExtra("EMAIL", email)
                        intent.putExtra("ACTION", "signup")
                        intent.putExtra("NAME", name)
                        intent.putExtra("PASSWORD", password)
                        intent.putExtra("ROLE", role)
                        startActivity(intent)
                    } else {
                        com.simats.orthoguide.util.DialogUtils.showError(findViewById(android.R.id.content), "Failed to send OTP")
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    btnSignup.isEnabled = true
                    btnSignup.text = "Sign Up"
                    com.simats.orthoguide.util.DialogUtils.showError(findViewById(android.R.id.content), "Network Error")
                }
            })
        }
    }

    private fun validatePassword(password: String): String? {
        if (password.length < 8) return "Password must be at least 8 characters long"
        if (!password.any { it.isUpperCase() }) return "Password must contain at least one capital letter"
        if (!password.any { it.isDigit() }) return "Password must contain at least one number"
        if (!password.any { !it.isLetterOrDigit() }) return "Password must contain at least one special character"
        return null
    }
}

