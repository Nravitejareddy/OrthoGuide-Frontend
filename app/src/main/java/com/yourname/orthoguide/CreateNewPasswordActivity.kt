package com.yourname.orthoguide

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

        btnResetPassword.setOnClickListener {
            if (btnResetPassword.isEnabled) {
                val intent = Intent(this, PasswordResetSuccessActivity::class.java)
                startActivity(intent)
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

    private fun validatePasswords() {
        val newPwd = etNewPassword.text.toString()
        val confirmPwd = etConfirmPassword.text.toString()

        val isValid = newPwd.isNotEmpty() && newPwd == confirmPwd

        if (isValid) {
            btnResetPassword.isEnabled = true
            btnResetPassword.backgroundTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.brand_green)
            )
        } else {
            btnResetPassword.isEnabled = false
            btnResetPassword.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#81d2bb") // Lighter Green
            )
        }
    }
}
