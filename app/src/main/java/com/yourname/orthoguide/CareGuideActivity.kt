package com.yourname.orthoguide

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class CareGuideActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        setContentView(R.layout.activity_care_guide)

        findViewById<View>(R.id.iv_back)?.setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.card_cleaning_aligners)?.setOnClickListener {
            startActivity(android.content.Intent(this, CleaningAlignersActivity::class.java))
        }

        findViewById<View>(R.id.card_food_drink)?.setOnClickListener {
            startActivity(android.content.Intent(this, FoodDrinkGuideActivity::class.java))
        }

        findViewById<View>(R.id.card_wear_time)?.setOnClickListener {
            startActivity(android.content.Intent(this, WearTimeTipsActivity::class.java))
        }

        findViewById<View>(R.id.card_common_issues)?.setOnClickListener {
            startActivity(android.content.Intent(this, CommonIssuesActivity::class.java))
        }
    }
}
