package com.yourname.orthoguide

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.yourname.orthoguide.network.RetrofitClient

class ReportIssueActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        setContentView(R.layout.activity_report_issue)

        findViewById<View>(R.id.iv_back)?.setOnClickListener {
            finish()
        }

        val btnContinue = findViewById<Button>(R.id.btn_continue)
        
        val issueMap = mapOf(
            R.id.cb_loose_bracket to "Loose Bracket",
            R.id.cb_poking_wire to "Poking Wire",
            R.id.cb_lost_aligner to "Lost Aligner",
            R.id.cb_severe_pain to "Severe Pain",
            R.id.cb_swollen_gums to "Swollen Gums",
            R.id.cb_broken_appliance to "Broken Appliance"
        )
        
        val checkBoxes = issueMap.keys.map { findViewById<CheckBox>(it) }

        fun updateButtonState() {
            val hasSelection = checkBoxes.any { it?.isChecked == true }
            btnContinue?.isEnabled = hasSelection
            val color = if (hasSelection) "#10B981" else "#85CDA7"
            btnContinue?.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(color))
        }

        val dpToPx = resources.displayMetrics.density
        checkBoxes.forEach { cb ->
            val card = cb?.parent as? com.google.android.material.card.MaterialCardView
            
            val updateTheme = {
                val isChecked = cb?.isChecked == true
                val strokeColor = if (isChecked) "#10B981" else "#F0F2F5"
                card?.strokeColor = android.graphics.Color.parseColor(strokeColor)
                card?.strokeWidth = if (isChecked) (2 * dpToPx).toInt() else (1 * dpToPx).toInt()
            }
            
            updateTheme()
            
            cb?.setOnCheckedChangeListener { _, _ ->
                updateTheme()
                updateButtonState()
            }
        }
        updateButtonState()

        btnContinue?.setOnClickListener {
            val selectedIssues = issueMap.filter { (id, _) -> 
                findViewById<CheckBox>(id)?.isChecked == true 
            }.values.joinToString(", ")
            
            val intent = Intent(this, SeverityLevelActivity::class.java)
            intent.putExtra("SELECTED_ISSUES", selectedIssues)
            startActivity(intent)
        }
    }
}
