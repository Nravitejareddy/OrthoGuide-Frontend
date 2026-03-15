package com.yourname.orthoguide

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class ClinicianEditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_clinician_edit_profile)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.card_toolbar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        val etName = findViewById<EditText>(R.id.et_name)
        val etRole = findViewById<AutoCompleteTextView>(R.id.et_role)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPhone = findViewById<EditText>(R.id.et_phone)

        val tvInitials = findViewById<TextView>(R.id.tv_initials)
        
        // Setup Role Dropdown
        val roles = arrayOf("Dentist", "Orthodontist", "Assistant")
        val adapter = ArrayAdapter(this, R.layout.item_dropdown, roles)
        etRole.setAdapter(adapter)

        fun updateInitials(name: String) {
            val initials = name.split(" ")
                .filter { it.isNotEmpty() }
                .map { it[0].uppercaseChar() }
                .take(2)
                .joinToString("")
            tvInitials?.text = if (initials.isNotEmpty()) initials else "--"
        }

        // Initialize initials
        updateInitials(etName.text.toString())

        etName.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateInitials(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Back button
        findViewById<View>(R.id.iv_back)?.setOnClickListener {
            finish()
        }

        // Cancel button
        findViewById<View>(R.id.btn_cancel)?.setOnClickListener {
            finish()
        }

        // Save Changes button
        findViewById<View>(R.id.btn_save)?.setOnClickListener {
            val updatedName = etName.text.toString()
            val updatedRole = etRole?.text?.toString() ?: ""
            val updatedEmail = etEmail.text.toString()
            val updatedPhone = etPhone.text.toString()

            val resultIntent = android.content.Intent().apply {
                putExtra("EXTRA_UPDATED_NAME", updatedName)
                putExtra("EXTRA_UPDATED_ROLE", updatedRole)
                putExtra("EXTRA_UPDATED_EMAIL", updatedEmail)
                putExtra("EXTRA_UPDATED_PHONE", updatedPhone)
            }
            
            setResult(android.app.Activity.RESULT_OK, resultIntent)
            Toast.makeText(this, "Profile changes saved.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
