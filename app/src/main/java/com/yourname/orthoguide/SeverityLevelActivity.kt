package com.yourname.orthoguide

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat

class SeverityLevelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        setContentView(R.layout.activity_severity_level)

        findViewById<View>(R.id.iv_back)?.setOnClickListener {
            finish()
        }

        val gridLayout = findViewById<android.widget.GridLayout>(R.id.gl_numbers)
        val submitBtn = findViewById<Button>(R.id.btn_submit)

        submitBtn?.isEnabled = false
        submitBtn?.backgroundTintList = android.content.res.ColorStateList.valueOf("#85CDA7".toColorInt())

        gridLayout?.let { grid ->
            for (i in 0 until grid.childCount) {
                val tv = grid.getChildAt(i) as? android.widget.TextView
                tv?.setOnClickListener {
                    // Deselect all
                    for (j in 0 until grid.childCount) {
                        val child = grid.getChildAt(j) as? android.widget.TextView
                        child?.setBackgroundResource(R.drawable.bg_severity_unselected)
                        child?.setTextColor("#374151".toColorInt())
                    }
                    // Select this
                    tv.setBackgroundResource(R.drawable.bg_severity_selected)
                    tv.setTextColor(android.graphics.Color.WHITE)
                    tv.elevation = 4f * resources.displayMetrics.density // Add a subtle shadow when selected
                    
                    // Reset others elevation
                    for (j in 0 until grid.childCount) {
                        if (grid.getChildAt(j) != tv) {
                           grid.getChildAt(j)?.elevation = 0f
                        }
                    }
                    submitBtn?.isEnabled = true
                    submitBtn?.backgroundTintList = android.content.res.ColorStateList.valueOf("#10B981".toColorInt())
                }
            }
        }

        submitBtn?.setOnClickListener {
            startActivity(android.content.Intent(this, ReportSentActivity::class.java))
        }
    }
}
