package com.yourname.orthoguide

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class AdminEditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_edit_profile)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val rootView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        val etName = findViewById<android.widget.EditText>(R.id.et_admin_name)
        val tvInitials = findViewById<android.widget.TextView>(R.id.tv_initials)

        fun updateInitials(name: String) {
            val initials = name.split(" ")
                .filter { it.isNotEmpty() }
                .map { it[0].uppercaseChar() }
                .take(2)
                .joinToString("")
            tvInitials.text = if (initials.isNotEmpty()) initials else "--"
        }

        // Initialize name and initials
        val prefs = getSharedPreferences("OrthoGuidePrefs", MODE_PRIVATE)
        val currentName = prefs.getString("admin_name", "System Admin")
        etName.setText(currentName)
        updateInitials(currentName ?: "")

        etName.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateInitials(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        findViewById<View>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<View>(R.id.btn_cancel).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btn_save).setOnClickListener {
            val name = etName.text.toString()
            if (name.isNotEmpty()) {
                val prefs = getSharedPreferences("OrthoGuidePrefs", MODE_PRIVATE)
                prefs.edit().putString("admin_name", name).apply()
            }
            Toast.makeText(this, "Profile changes saved successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
