package com.yourname.orthoguide

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat

class AccountDeactivatedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account_deactivated)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val patientId = intent.getStringExtra("PATIENT_ID") ?: ""
        val userRole = intent.getStringExtra("USER_ROLE") ?: "patient"
        val isClinician = userRole.lowercase() == "clinician"

        // Conditional Blue Theme for Clinicians
        if (isClinician) {
            findViewById<View>(R.id.v_deactivated_bg_glow)?.setBackgroundResource(R.drawable.bg_gradient_soft_blue)
            
            // Tint the lock icon and the card stroke if possible
            findViewById<com.google.android.material.card.MaterialCardView>(R.id.cv_deactivated_icon)?.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#102563EB")))
            findViewById<android.widget.ImageView>(R.id.iv_deactivated_lock)?.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#2563EB"))
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_request_reactivation).apply {
            if (isClinician) {
                backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#2563EB"))
            }
            setOnClickListener {
                val intent = Intent(this@AccountDeactivatedActivity, ReactivationRequestActivity::class.java)
                intent.putExtra("PATIENT_ID", patientId)
                intent.putExtra("USER_ROLE", userRole)
                startActivity(intent)
            }
        }

        findViewById<View>(R.id.tv_back_to_login).setOnClickListener {
            val intent = Intent(this, UnifiedLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
