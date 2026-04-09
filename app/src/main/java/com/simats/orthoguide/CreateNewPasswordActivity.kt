package com.simats.orthoguide

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class CreateNewPasswordActivity : AppCompatActivity() {

    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnResetPassword: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.decorView.setBackgroundColor(android.graphics.Color.WHITE)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        setContentView(R.layout.activity_create_new_password)

        val rootView = findViewById<View>(R.id.create_pwd_root)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.iv_back_create_pwd)?.setOnClickListener {
            finish()
        }

        etNewPassword = findViewById(R.id.et_new_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        btnResetPassword = findViewById(R.id.btn_reset_password)

        val ivToggleNew = findViewById<android.widget.ImageView>(R.id.iv_toggle_new_password)
        val ivToggleConfirm = findViewById<android.widget.ImageView>(R.id.iv_toggle_confirm_password)

        com.simats.orthoguide.util.PasswordToggleHelper.setupPasswordToggle(etNewPassword, ivToggleNew)
        com.simats.orthoguide.util.PasswordToggleHelper.setupPasswordToggle(etConfirmPassword, ivToggleConfirm)


        val email = intent.getStringExtra("EMAIL") ?: ""
        val role = intent.getStringExtra("ROLE") ?: "patient"

        btnResetPassword.setOnClickListener {
            val newPwd = etNewPassword.text.toString()
            val confirmPwd = etConfirmPassword.text.toString()

            val errorMsg = validatePassword(newPwd)
            if (errorMsg != null) {
                com.simats.orthoguide.util.DialogUtils.showError(findViewById(android.R.id.content), errorMsg)
                return@setOnClickListener
            }

            if (newPwd != confirmPwd) {
                com.simats.orthoguide.util.DialogUtils.showError(findViewById(android.R.id.content), "Passwords do not match")
                return@setOnClickListener
            }

            if (btnResetPassword.isEnabled) {
                btnResetPassword.isEnabled = false
                btnResetPassword.text = "Resetting..."

                com.simats.orthoguide.network.RetrofitClient.service.resetPassword(mapOf("email" to email, "new_password" to newPwd, "role" to role))
                    .enqueue(object : retrofit2.Callback<com.simats.orthoguide.network.GenericResponse> {
                        override fun onResponse(call: retrofit2.Call<com.simats.orthoguide.network.GenericResponse>, response: retrofit2.Response<com.simats.orthoguide.network.GenericResponse>) {
                            btnResetPassword.isEnabled = true
                            btnResetPassword.text = "Reset Password"
                            if (response.isSuccessful) {
                                val intent = Intent(this@CreateNewPasswordActivity, PasswordResetSuccessActivity::class.java)
                                startActivity(intent)
                                finishAffinity()
                            } else {
                                val errorBody = response.errorBody()?.string()
                                val errorMessage = try {
                                    if (errorBody != null) {
                                        org.json.JSONObject(errorBody).getString("error")
                                    } else {
                                        "Failed to reset password"
                                    }
                                } catch (e: Exception) {
                                    "Failed to reset password"
                                }
                                com.simats.orthoguide.util.DialogUtils.showError(findViewById(android.R.id.content), errorMessage)
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<com.simats.orthoguide.network.GenericResponse>, t: Throwable) {
                            btnResetPassword.isEnabled = true
                            btnResetPassword.text = "Reset Password"
                            com.simats.orthoguide.util.DialogUtils.showError(findViewById(android.R.id.content), "Network error")
                        }
                    })
            }
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePasswords()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etNewPassword.addTextChangedListener(textWatcher)
        etConfirmPassword.addTextChangedListener(textWatcher)
        
        // Initial state
        validatePasswords()
    }

    private fun validatePassword(password: String): String? {
        if (password.length < 8) return "Password must be at least 8 characters long"
        if (!password.any { it.isUpperCase() }) return "Password must contain at least one capital letter"
        if (!password.any { it.isDigit() }) return "Password must contain at least one number"
        if (!password.any { !it.isLetterOrDigit() }) return "Password must contain at least one special character"
        return null
    }

    private fun validatePasswords() {
        val newPwd = etNewPassword.text.toString()
        val confirmPwd = etConfirmPassword.text.toString()

        // We keep the button enabled to show the instructional toasts as requested
        btnResetPassword.isEnabled = true
        btnResetPassword.backgroundTintList = android.content.res.ColorStateList.valueOf(
            ContextCompat.getColor(this, R.color.brand_green)
        )
    }
}

