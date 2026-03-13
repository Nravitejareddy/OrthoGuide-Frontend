package com.yourname.orthoguide

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class CreatePatientActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_patient)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val rootView = findViewById<View>(R.id.create_patient_root)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, 0, systemBars.right, 0)
                insets
            }
        }

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }

        val btnSave = findViewById<MaterialButton>(R.id.btn_save)
        val tvHeaderTitle = findViewById<TextView>(R.id.tv_header_title)
        
        val etId = findViewById<TextInputEditText>(R.id.et_patient_id)
        val etName = findViewById<TextInputEditText>(R.id.et_patient_name)
        val etRole = findViewById<TextInputEditText>(R.id.et_patient_email)
        val etPhone = findViewById<TextInputEditText>(R.id.et_phone)

        val isEditMode = intent.getBooleanExtra("isEditMode", false)

        if (isEditMode) {
            tvHeaderTitle.text = "Update Patient"
            btnSave.text = "Update Patient"

            etId.setText(intent.getStringExtra("patientId") ?: "")
            etName.setText(intent.getStringExtra("patientName") ?: "")
            etRole.setText(intent.getStringExtra("patientEmail") ?: "")
            etPhone.setText(intent.getStringExtra("patientPhone") ?: "+91 98765 43210")
        } else {
            tvHeaderTitle.text = "Create Patient Account"
            btnSave.text = "Create Account"
        }
        
        btnSave.setOnClickListener {
            finish()
        }
    }
}
