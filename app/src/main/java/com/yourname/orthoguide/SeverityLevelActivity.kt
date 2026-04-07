package com.yourname.orthoguide

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.yourname.orthoguide.network.GenericResponse
import com.yourname.orthoguide.network.ReportIssueRequest
import com.yourname.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SeverityLevelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        setContentView(R.layout.activity_severity_level)

        val selectedIssues = intent.getStringExtra("SELECTED_ISSUES") ?: "General Issue"

        findViewById<View>(R.id.iv_back)?.setOnClickListener {
            finish()
        }

        val btnSubmit = findViewById<Button>(R.id.btn_submit)
        val glNumbers = findViewById<android.widget.GridLayout>(R.id.gl_numbers)
        var selectedSeverity = "1"

        btnSubmit.isEnabled = false

        for (i in 0 until glNumbers.childCount) {
            val child = glNumbers.getChildAt(i)
            if (child is TextView) {
                child.setOnClickListener {
                    // Reset all backgrounds and text colors
                    for (j in 0 until glNumbers.childCount) {
                        val otherChild = glNumbers.getChildAt(j) as? TextView
                        otherChild?.setBackgroundResource(R.drawable.bg_rounded_icon)
                        otherChild?.setTextColor(android.graphics.Color.parseColor("#6B7280"))
                        otherChild?.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F3F4F6"))
                    }

                    // Highlight clicked item
                    child.setBackgroundResource(R.drawable.bg_rounded_icon)
                    child.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#10B981"))
                    child.setTextColor(android.graphics.Color.WHITE)

                    selectedSeverity = child.text.toString()
                    btnSubmit.isEnabled = true
                    btnSubmit.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#10B981"))
                }
            }
        }

        btnSubmit.setOnClickListener {
            // Navigate to upload photo screen instead of submitting directly
            val numericSeverity = selectedSeverity.toIntOrNull() ?: 5
            val intent = Intent(this@SeverityLevelActivity, UploadPhotoActivity::class.java).apply {
                putExtra("SELECTED_ISSUES", selectedIssues)
                putExtra("SEVERITY", numericSeverity)
            }
            startActivity(intent)
        }
    }
}
