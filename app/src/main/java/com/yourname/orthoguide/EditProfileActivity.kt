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

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.card_toolbar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        val etName = findViewById<android.widget.EditText>(R.id.et_name)
        val etEmail = findViewById<android.widget.EditText>(R.id.et_email)
        val etPhone = findViewById<android.widget.EditText>(R.id.et_phone)

        // Pre-fill if intent has data
        intent.getStringExtra("EXTRA_NAME")?.let { etName.setText(it) }
        intent.getStringExtra("EXTRA_EMAIL")?.let { etEmail.setText(it) }
        intent.getStringExtra("EXTRA_PHONE")?.let { etPhone.setText(it) }

        findViewById<View>(R.id.iv_back)?.setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btn_save)?.setOnClickListener {
            val updatedName = etName.text.toString()
            val updatedEmail = etEmail.text.toString()
            val updatedPhone = etPhone.text.toString()

            val resultIntent = android.content.Intent().apply {
                putExtra("EXTRA_UPDATED_NAME", updatedName)
                putExtra("EXTRA_UPDATED_EMAIL", updatedEmail)
                putExtra("EXTRA_UPDATED_PHONE", updatedPhone)
            }
            
            setResult(android.app.Activity.RESULT_OK, resultIntent)
            Toast.makeText(this, "Profile changes saved.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
