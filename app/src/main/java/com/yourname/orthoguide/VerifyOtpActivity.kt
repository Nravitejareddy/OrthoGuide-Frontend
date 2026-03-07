package com.yourname.orthoguide

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

        findViewById<MaterialButton>(R.id.btn_verify_continue)?.setOnClickListener {
            // After verification, redirect to Create New Password
            val intent = android.content.Intent(this, CreateNewPasswordActivity::class.java)
            startActivity(intent)
        }

        setupOtpInputs()
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
