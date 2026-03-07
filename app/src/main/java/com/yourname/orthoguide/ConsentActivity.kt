package com.yourname.orthoguide

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.button.MaterialButton

class ConsentActivity : AppCompatActivity() {

    private lateinit var cbAiSupport: CheckBox
    private lateinit var cbDiasUsage: CheckBox
    private lateinit var cbPrivacyPolicy: CheckBox
    private lateinit var btnContinue: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_consent)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val rootView = findViewById<View>(R.id.consent_root)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        cbAiSupport = findViewById(R.id.cb_ai_support)
        cbDiasUsage = findViewById(R.id.cb_dias_usage)
        cbPrivacyPolicy = findViewById(R.id.cb_privacy_policy)
        btnContinue = findViewById(R.id.btn_continue)

        val backButton = findViewById<ImageView>(R.id.iv_back_consent)
        backButton?.setOnClickListener {
            finish()
        }

        // Set listeners for checkboxes
        val checkBoxListener = { _: View ->
            updateContinueButtonState()
        }

        cbAiSupport.setOnClickListener(checkBoxListener)
        cbDiasUsage.setOnClickListener(checkBoxListener)
        cbPrivacyPolicy.setOnClickListener(checkBoxListener)

        // Also make cards clickable for better UX
        findViewById<View>(R.id.card_ai_support).setOnClickListener {
            cbAiSupport.isChecked = !cbAiSupport.isChecked
            updateContinueButtonState()
        }
        findViewById<View>(R.id.card_dias_usage).setOnClickListener {
            cbDiasUsage.isChecked = !cbDiasUsage.isChecked
            updateContinueButtonState()
        }
        findViewById<View>(R.id.card_privacy_policy).setOnClickListener {
            cbPrivacyPolicy.isChecked = !cbPrivacyPolicy.isChecked
            updateContinueButtonState()
        }

        btnContinue.setOnClickListener {
            val intent = android.content.Intent(this, AiCapabilitiesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateContinueButtonState() {
        btnContinue.isEnabled = cbAiSupport.isChecked && 
                               cbDiasUsage.isChecked && 
                               cbPrivacyPolicy.isChecked
    }
}
