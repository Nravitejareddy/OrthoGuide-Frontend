package com.yourname.orthoguide

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class CreateClinicianActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_clinician)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val rootView = findViewById<View>(R.id.create_clinician_root)
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
        
        val etId = findViewById<TextInputEditText>(R.id.et_clinician_id)
        val etName = findViewById<TextInputEditText>(R.id.et_full_name)
        val etRole = findViewById<AutoCompleteTextView>(R.id.et_role)
        val etPhone = findViewById<TextInputEditText>(R.id.et_phone)

        val isEditMode = intent.getBooleanExtra("isEditMode", false)

        if (isEditMode) {
            tvHeaderTitle.text = "Update Clinician"
            btnSave.text = "Update Clinician"

            etId.setText(intent.getStringExtra("clinicianId") ?: "")
            etName.setText(intent.getStringExtra("clinicianName") ?: "")
            etRole.setText(intent.getStringExtra("clinicianRole") ?: "Orthodontist", false)
            etPhone.setText(intent.getStringExtra("clinicianPhone") ?: "+91 98765 43210")
        } else {
            tvHeaderTitle.text = "Create Clinician Account"
            btnSave.text = "Create Account"
        }
        
        btnSave.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val etRole = findViewById<AutoCompleteTextView>(R.id.et_role)
        val roles = arrayOf("Dentist", "Orthodontist", "Assistant")
        val adapter = ArrayAdapter(this, R.layout.item_dropdown, roles)
        etRole.setAdapter(adapter)
    }
}
