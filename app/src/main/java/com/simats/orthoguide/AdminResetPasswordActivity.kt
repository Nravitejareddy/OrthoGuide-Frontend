package com.simats.orthoguide

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.simats.orthoguide.R

class AdminResetPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_reset_password)

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
        
        findViewById<View>(R.id.btn_cancel)?.setOnClickListener {
            finish()
        }

        val clinicianName = intent.getStringExtra("clinicianName") ?: "Dr. Sarah Miller"
        val clinicianId = intent.getStringExtra("clinicianId") ?: "ID: DR001"

        findViewById<TextView>(R.id.tv_clinician_name)?.text = clinicianName
        findViewById<TextView>(R.id.tv_clinician_id)?.text = clinicianId

        val btnResetPassword = findViewById<android.widget.Button>(R.id.btn_reset_password)
        btnResetPassword?.setOnClickListener {
            com.simats.orthoguide.util.DialogUtils.showSuccess(findViewById<View>(android.R.id.content), "Password reset successfully for $clinicianName!")
            finish()
        }

        val etNewPassword = findViewById<EditText>(R.id.et_new_password)
        val etConfirmPassword = findViewById<EditText>(R.id.et_confirm_password)

        val ivToggleNew = findViewById<android.widget.ImageView>(R.id.iv_toggle_new_password)
        val ivToggleConfirm = findViewById<android.widget.ImageView>(R.id.iv_toggle_confirm_password)

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
                val isNewFilled = !etNewPassword?.text.isNullOrEmpty()
                val isConfirmFilled = !etConfirmPassword?.text.isNullOrEmpty()

                val allFilled = isNewFilled && isConfirmFilled
                btnResetPassword?.isEnabled = allFilled

                if (allFilled) {
                    btnResetPassword?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#A855F7"))
                } else {
                    btnResetPassword?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D8B4FE"))
                }
            }
        }

        etNewPassword?.addTextChangedListener(textWatcher)
        etConfirmPassword?.addTextChangedListener(textWatcher)
    }
}

