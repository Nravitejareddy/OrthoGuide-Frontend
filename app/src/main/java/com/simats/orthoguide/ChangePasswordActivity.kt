package com.simats.orthoguide

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.simats.orthoguide.network.GenericResponse
import com.simats.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_password)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.card_toolbar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        findViewById<View>(R.id.iv_back)?.setOnClickListener {
            finish()
        }

        val isClinician = intent.getBooleanExtra("isClinician", false)
        val isAdmin = intent.getBooleanExtra("isAdmin", false)
        
        val btnUpdatePassword = findViewById<Button>(R.id.btn_update_password)

        if (isClinician || isAdmin) {
            val cardBgColor = if (isAdmin) "#FAF5FF" else "#EFF6FF"
            val primaryColor = if (isAdmin) "#A855F7" else "#2563EB"
            val secondaryColor = if (isAdmin) "#9333EA" else "#1D4ED8"

            val cardReq = findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_requirements)
            cardReq?.setCardBackgroundColor(Color.parseColor(cardBgColor))
            
            val llReq = findViewById<android.widget.LinearLayout>(R.id.ll_req_content)
            if (llReq != null) {
                // Title
                val titleLayout = llReq.getChildAt(0) as? android.widget.LinearLayout
                (titleLayout?.getChildAt(0) as? android.widget.ImageView)?.imageTintList = ColorStateList.valueOf(Color.parseColor(primaryColor))
                (titleLayout?.getChildAt(1) as? android.widget.TextView)?.setTextColor(Color.parseColor(secondaryColor))
                
                // Bullets
                for (i in 1 until llReq.childCount) {
                    val bulletLayout = llReq.getChildAt(i) as? android.widget.LinearLayout
                    if (bulletLayout != null && bulletLayout.childCount >= 2) {
                        (bulletLayout.getChildAt(0) as? android.widget.TextView)?.setTextColor(Color.parseColor(secondaryColor))
                        (bulletLayout.getChildAt(1) as? android.widget.TextView)?.setTextColor(Color.parseColor(primaryColor))
                    }
                }
            }
            
            // Set initial disabled color
            val disabledColorStr = if (isAdmin) "#D8B4FE" else "#93C5FD"
            btnUpdatePassword?.backgroundTintList = ColorStateList.valueOf(Color.parseColor(disabledColorStr))
        }

        val etCurrentPassword = findViewById<EditText>(R.id.et_current_password)
        val etNewPassword = findViewById<EditText>(R.id.et_new_password)
        val etConfirmPassword = findViewById<EditText>(R.id.et_confirm_password)

        val ivToggleCurrent = findViewById<android.widget.ImageView>(R.id.iv_toggle_current_password)
        val ivToggleNew = findViewById<android.widget.ImageView>(R.id.iv_toggle_new_password)
        val ivToggleConfirm = findViewById<android.widget.ImageView>(R.id.iv_toggle_confirm_password)

        if (etCurrentPassword != null && ivToggleCurrent != null) {
            com.simats.orthoguide.util.PasswordToggleHelper.setupPasswordToggle(etCurrentPassword, ivToggleCurrent)
        }
        if (etNewPassword != null && ivToggleNew != null) {
            com.simats.orthoguide.util.PasswordToggleHelper.setupPasswordToggle(etNewPassword, ivToggleNew)
        }
        if (etConfirmPassword != null && ivToggleConfirm != null) {
            com.simats.orthoguide.util.PasswordToggleHelper.setupPasswordToggle(etConfirmPassword, ivToggleConfirm)
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val isCurrentFilled = !etCurrentPassword?.text.isNullOrEmpty()
                val isNewFilled = !etNewPassword?.text.isNullOrEmpty()
                val isConfirmFilled = !etConfirmPassword?.text.isNullOrEmpty()

                val allFilled = isCurrentFilled && isNewFilled && isConfirmFilled
                btnUpdatePassword?.isEnabled = allFilled

                if (allFilled) {
                    val activeColor = if (isAdmin) Color.parseColor("#A855F7") 
                                     else if (isClinician) Color.parseColor("#2563EB") 
                                     else Color.parseColor("#10B981") // Brand Green
                    btnUpdatePassword?.backgroundTintList = ColorStateList.valueOf(activeColor)
                } else {
                    val disabledColor = if (isAdmin) Color.parseColor("#D8B4FE") 
                                       else if (isClinician) Color.parseColor("#93C5FD") 
                                       else Color.parseColor("#81d2bb") // Lighter Green
                    btnUpdatePassword?.backgroundTintList = ColorStateList.valueOf(disabledColor)
                }
            }
        }

        etCurrentPassword?.addTextChangedListener(textWatcher)
        etNewPassword?.addTextChangedListener(textWatcher)
        etConfirmPassword?.addTextChangedListener(textWatcher)

        btnUpdatePassword?.setOnClickListener {
            val currentPw = etCurrentPassword.text.toString()
            val newPw = etNewPassword.text.toString()
            val confirmPw = etConfirmPassword.text.toString()

            val errorMsg = validatePassword(newPw)
            if (errorMsg != null) {
                com.simats.orthoguide.util.DialogUtils.showError(findViewById(android.R.id.content), errorMsg)
                return@setOnClickListener
            }

            if (newPw != confirmPw) {
                com.simats.orthoguide.util.DialogUtils.showError(findViewById(android.R.id.content), "New passwords do not match")
                return@setOnClickListener
            }

            performChangePassword(currentPw, newPw)
        }
    }

    private fun validatePassword(password: String): String? {
        if (password.length < 8) return "Password must be at least 8 characters long"
        if (!password.any { it.isUpperCase() }) return "Password must contain at least one capital letter"
        if (!password.any { it.isDigit() }) return "Password must contain at least one number"
        if (!password.any { !it.isLetterOrDigit() }) return "Password must contain at least one special character"
        return null
    }

    private fun performChangePassword(oldPw: String, newPw: String) {
        val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", "") ?: ""
        val role = sharedPref.getString("USER_ROLE", "patient") ?: "patient"

        if (userId.isEmpty()) {
            com.simats.orthoguide.util.DialogUtils.showError(this, "Session error. Please login again.")
            return
        }

        val request = mapOf(
            "user_id" to userId,
            "role" to role,
            "old_password" to oldPw,
            "new_password" to newPw
        )

        RetrofitClient.service.changePassword(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful) {
                    com.simats.orthoguide.util.DialogUtils.showSuccess(this@ChangePasswordActivity, "Password updated successfully!")
                    finish()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to update password"
                    Log.e("OrthoGuide", "Password update failed: $errorMsg")
                    com.simats.orthoguide.util.DialogUtils.showError(this@ChangePasswordActivity, "Incorrect current password or update failed")
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Log.e("OrthoGuide", "Network error", t)
                com.simats.orthoguide.util.DialogUtils.showError(this@ChangePasswordActivity, "Network error: ${t.message}")
            }
        })
    }
}

