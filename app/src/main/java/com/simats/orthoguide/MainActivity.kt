package com.simats.orthoguide

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Redirect to unified login
        startActivity(Intent(this, UnifiedLoginActivity::class.java))
        finish()
    }
}

