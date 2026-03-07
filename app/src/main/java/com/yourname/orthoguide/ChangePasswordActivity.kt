package com.yourname.orthoguide

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class ChangePasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_password)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.card_toolbar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        findViewById<View>(R.id.iv_back)?.setOnClickListener {
            finish()
        }

        val isClinician = intent.getBooleanExtra("isClinician", false)
        val isAdmin = intent.getBooleanExtra("isAdmin", false)
        
        val btnUpdatePassword = findViewById<Button>(R.id.btn_update_password)
        btnUpdatePassword?.setOnClickListener {
            Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }

        if (isClinician || isAdmin) {
            val cardBgColor = if (isAdmin) "#FAF5FF" else "#EFF6FF"
            val primaryColor = if (isAdmin) "#A855F7" else "#2563EB"
            val secondaryColor = if (isAdmin) "#9333EA" else "#1D4ED8"

            val cardReq = findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_requirements)
            cardReq?.setCardBackgroundColor(Color.parseColor(cardBgColor))
            
            val llReq = findViewById<android.widget.LinearLayout>(R.id.ll_req_content)
            if (llReq != null) {
                // Title
                val titleLayout = llReq.getChildAt(0) as? android.widget.LinearLayout
                (titleLayout?.getChildAt(0) as? android.widget.ImageView)?.imageTintList = ColorStateList.valueOf(Color.parseColor(primaryColor))
                (titleLayout?.getChildAt(1) as? android.widget.TextView)?.setTextColor(Color.parseColor(secondaryColor))
                
                // Bullets
                for (i in 1 until llReq.childCount) {
                    val bulletLayout = llReq.getChildAt(i) as? android.widget.LinearLayout
                    if (bulletLayout != null && bulletLayout.childCount >= 2) {
                        (bulletLayout.getChildAt(0) as? android.widget.TextView)?.setTextColor(Color.parseColor(secondaryColor))
                        (bulletLayout.getChildAt(1) as? android.widget.TextView)?.setTextColor(Color.parseColor(primaryColor))
                    }
                }
            }
            
            // Set initial disabled color
            val disabledColorStr = if (isAdmin) "#D8B4FE" else "#93C5FD"
            btnUpdatePassword?.backgroundTintList = ColorStateList.valueOf(Color.parseColor(disabledColorStr))
        }

        val etCurrentPassword = findViewById<EditText>(R.id.et_current_password)
        val etNewPassword = findViewById<EditText>(R.id.et_new_password)
        val etConfirmPassword = findViewById<EditText>(R.id.et_confirm_password)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val isCurrentFilled = !etCurrentPassword?.text.isNullOrEmpty()
                val isNewFilled = !etNewPassword?.text.isNullOrEmpty()
                val isConfirmFilled = !etConfirmPassword?.text.isNullOrEmpty()

                val allFilled = isCurrentFilled && isNewFilled && isConfirmFilled
                btnUpdatePassword?.isEnabled = allFilled

                if (allFilled) {
                    val activeColor = if (isAdmin) Color.parseColor("#A855F7") else if (isClinician) Color.parseColor("#2563EB") else ContextCompat.getColor(this@ChangePasswordActivity, R.color.brand_green)
                    btnUpdatePassword?.backgroundTintList = ColorStateList.valueOf(activeColor)
                } else {
                    val disabledColor = if (isAdmin) Color.parseColor("#D8B4FE") else if (isClinician) Color.parseColor("#93C5FD") else Color.parseColor("#81d2bb")
                    btnUpdatePassword?.backgroundTintList = ColorStateList.valueOf(disabledColor)
                }
            }
        }

        etCurrentPassword?.addTextChangedListener(textWatcher)
        etNewPassword?.addTextChangedListener(textWatcher)
        etConfirmPassword?.addTextChangedListener(textWatcher)
    }
}
