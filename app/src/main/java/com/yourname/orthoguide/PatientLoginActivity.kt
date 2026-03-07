package com.yourname.orthoguide

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class PatientLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("OrthoGuide", "PatientLoginActivity created")
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_login)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val rootView = findViewById<View>(R.id.patient_login_root)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        findViewById<ImageView>(R.id.iv_back)?.setOnClickListener {
            if (isTaskRoot) {
                val intent = android.content.Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            finish()
        }

        findViewById<View>(R.id.btn_login_secure)?.setOnClickListener {
            val intent = android.content.Intent(this, ConsentActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.tv_forgot_password_link)?.setOnClickListener {
            val intent = android.content.Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }
}
