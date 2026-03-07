package com.yourname.orthoguide

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class ManageCliniciansActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_clinicians)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val rootView = findViewById<View>(R.id.manage_clinicians_root)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, 0, systemBars.right, 0)
                insets
            }
        }

        findViewById<View>(R.id.iv_back)?.setOnClickListener {
            finish()
        }

        val resetPasswordListener = View.OnClickListener { v ->
            val intent = Intent(this, AdminResetPasswordActivity::class.java)
            when (v.id) {
                R.id.btn_reset_password_1 -> {
                    intent.putExtra("clinicianName", "Dr. Sarah Miller")
                    intent.putExtra("clinicianId", "ID: DR001")
                }
                R.id.btn_reset_password_2 -> {
                    intent.putExtra("clinicianName", "Dr. Michael Smith")
                    intent.putExtra("clinicianId", "ID: DR002")
                }
                R.id.btn_reset_password_3 -> {
                    intent.putExtra("clinicianName", "Dr. Emily Chen")
                    intent.putExtra("clinicianId", "ID: DR003")
                }
                R.id.btn_reset_password_4 -> {
                    intent.putExtra("clinicianName", "Dr. James Wilson")
                    intent.putExtra("clinicianId", "ID: DR004")
                }
            }
            startActivity(intent)
        }

        findViewById<View>(R.id.btn_reset_password_1)?.setOnClickListener(resetPasswordListener)
        findViewById<View>(R.id.btn_reset_password_2)?.setOnClickListener(resetPasswordListener)
        findViewById<View>(R.id.btn_reset_password_3)?.setOnClickListener(resetPasswordListener)
        findViewById<View>(R.id.btn_reset_password_4)?.setOnClickListener(resetPasswordListener)

        val deactivateListener = View.OnClickListener { v ->
            val (name, id, index) = when (v.id) {
                R.id.btn_deactivate_1 -> Triple("Dr. Sarah Miller", "ID: DR001", 1)
                R.id.btn_deactivate_2 -> Triple("Dr. Michael Smith", "ID: DR002", 2)
                R.id.btn_deactivate_3 -> Triple("Dr. Emily Chen", "ID: DR003", 3)
                R.id.btn_deactivate_4 -> Triple("Dr. James Wilson", "ID: DR004", 4)
                else -> Triple("Unknown", "ID: Unknown", 1)
            }
            showDeactivateDialog(name, id, index)
        }

        findViewById<View>(R.id.btn_deactivate_1)?.setOnClickListener(deactivateListener)
        findViewById<View>(R.id.btn_deactivate_2)?.setOnClickListener(deactivateListener)
        findViewById<View>(R.id.btn_deactivate_3)?.setOnClickListener(deactivateListener)
        findViewById<View>(R.id.btn_deactivate_4)?.setOnClickListener(deactivateListener)

        findViewById<View>(R.id.btn_add_clinician)?.setOnClickListener {
            val intent = Intent(this, CreateClinicianActivity::class.java)
            intent.putExtra("isEditMode", false)
            startActivity(intent)
        }

        val cardClickListener = View.OnClickListener { v ->
            val intent = Intent(this, CreateClinicianActivity::class.java)
            intent.putExtra("isEditMode", true)
            when (v.id) {
                R.id.clinician_card_1 -> {
                    intent.putExtra("clinicianName", "Dr. Sarah Miller")
                    intent.putExtra("clinicianId", "DR001")
                }
                R.id.clinician_card_2 -> {
                    intent.putExtra("clinicianName", "Dr. Michael Smith")
                    intent.putExtra("clinicianId", "DR002")
                }
                R.id.clinician_card_3 -> {
                    intent.putExtra("clinicianName", "Dr. Emily Chen")
                    intent.putExtra("clinicianId", "DR003")
                }
                R.id.clinician_card_4 -> {
                    intent.putExtra("clinicianName", "Dr. James Wilson")
                    intent.putExtra("clinicianId", "DR004")
                }
            }
            startActivity(intent)
        }

        findViewById<View>(R.id.clinician_card_1)?.setOnClickListener(cardClickListener)
        findViewById<View>(R.id.clinician_card_2)?.setOnClickListener(cardClickListener)
        findViewById<View>(R.id.clinician_card_3)?.setOnClickListener(cardClickListener)
        findViewById<View>(R.id.clinician_card_4)?.setOnClickListener(cardClickListener)

        // Navigation
        findViewById<View>(R.id.admin_nav_dashboard)?.setOnClickListener {
            val intent = Intent(this, AdminDashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.admin_nav_patients)?.setOnClickListener {
            val intent = Intent(this, ManagePatientsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.admin_nav_settings)?.setOnClickListener {
            val intent = Intent(this, AdminSettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    private fun showDeactivateDialog(userName: String, userId: String, cardIndex: Int) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_deactivate_account)
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(
            width,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tvMessage = dialog.findViewById<android.widget.TextView>(R.id.tv_message)
        tvMessage?.text = "Are you sure you want to deactivate\nthe account for $userName?"

        dialog.findViewById<View>(R.id.btn_cancel)?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<View>(R.id.btn_confirm)?.setOnClickListener {
            dialog.dismiss()
            showSuccessDialog(cardIndex)
        }

        dialog.show()
    }

    private fun showSuccessDialog(cardIndex: Int, isActivation: Boolean = false) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_success)
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(
            width,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.show()

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
                if (isActivation) {
                    updateCardToActive(cardIndex)
                } else {
                    updateCardToInactive(cardIndex)
                }
            }
        }, 400)
    }

    private fun updateCardToInactive(index: Int) {
        val avatarContainerId = resources.getIdentifier("avatar_container_$index", "id", packageName)
        val statusPillId = resources.getIdentifier("status_pill_$index", "id", packageName)
        val btnDeactivateId = resources.getIdentifier("btn_deactivate_$index", "id", packageName)

        val avatarContainer = findViewById<android.widget.FrameLayout>(avatarContainerId)
        avatarContainer?.setBackgroundResource(R.drawable.bg_circle_soft_grey)
        val avatarIcon = avatarContainer?.getChildAt(0) as? android.widget.ImageView
        avatarIcon?.setColorFilter(android.graphics.Color.parseColor("#64748B"))

        val statusPill = findViewById<android.widget.TextView>(statusPillId)
        statusPill?.text = "INACTIVE"
        statusPill?.setTextColor(android.graphics.Color.parseColor("#64748B"))
        statusPill?.setBackgroundResource(R.drawable.bg_tag_inactive_light)

        val actionBtn = findViewById<com.google.android.material.card.MaterialCardView>(btnDeactivateId)
        actionBtn?.setCardBackgroundColor(android.graphics.Color.parseColor("#ECFDF5"))
        actionBtn?.setOnClickListener {
            val name = when (index) {
                1 -> "Dr. Sarah Miller"
                2 -> "Dr. Michael Smith"
                3 -> "Dr. Emily Chen"
                4 -> "Dr. James Wilson"
                else -> "Unknown"
            }
            showActivateDialog(name, "ID", index)
        }
        val btnLayout = actionBtn?.getChildAt(0) as? android.widget.LinearLayout
        val btnIcon = btnLayout?.getChildAt(0) as? android.widget.ImageView
        val btnText = btnLayout?.getChildAt(1) as? android.widget.TextView

        btnIcon?.setImageResource(R.drawable.ic_user_plus_green)
        btnIcon?.clearColorFilter()
        btnText?.text = "Reactivate"
        btnText?.setTextColor(android.graphics.Color.parseColor("#10B981"))
    }

    private fun showActivateDialog(userName: String, userId: String, cardIndex: Int) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_activate_account)
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(
            width,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tvMessage = dialog.findViewById<android.widget.TextView>(R.id.tv_message)
        tvMessage?.text = "Are you sure you want to activate\nthe account for $userName?"

        dialog.findViewById<View>(R.id.btn_cancel)?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<View>(R.id.btn_confirm)?.setOnClickListener {
            dialog.dismiss()
            showSuccessDialog(cardIndex, true)
        }

        dialog.show()
    }

    private fun updateCardToActive(index: Int) {
        val avatarContainerId = resources.getIdentifier("avatar_container_$index", "id", packageName)
        val statusPillId = resources.getIdentifier("status_pill_$index", "id", packageName)
        val btnDeactivateId = resources.getIdentifier("btn_deactivate_$index", "id", packageName)

        val avatarContainer = findViewById<android.widget.FrameLayout>(avatarContainerId)
        avatarContainer?.setBackgroundResource(R.drawable.bg_circle_soft_purple)
        val avatarIcon = avatarContainer?.getChildAt(0) as? android.widget.ImageView
        avatarIcon?.setColorFilter(android.graphics.Color.parseColor("#7C3AED"))

        val statusPill = findViewById<android.widget.TextView>(statusPillId)
        statusPill?.text = "ACTIVE"
        statusPill?.setTextColor(android.graphics.Color.parseColor("#10B981"))
        statusPill?.setBackgroundResource(R.drawable.bg_tag_active_light)

        val actionBtn = findViewById<com.google.android.material.card.MaterialCardView>(btnDeactivateId)
        actionBtn?.setCardBackgroundColor(android.graphics.Color.parseColor("#FEF2F2"))
        actionBtn?.setOnClickListener {
            val (name, id) = when (index) {
                1 -> Pair("Dr. Sarah Miller", "ID: DR001")
                2 -> Pair("Dr. Michael Smith", "ID: DR002")
                3 -> Pair("Dr. Emily Chen", "ID: DR003")
                4 -> Pair("Dr. James Wilson", "ID: DR004")
                else -> Pair("Unknown", "ID: Unknown")
            }
            showDeactivateDialog(name, id, index)
        }
        val btnLayout = actionBtn?.getChildAt(0) as? android.widget.LinearLayout
        val btnIcon = btnLayout?.getChildAt(0) as? android.widget.ImageView
        val btnText = btnLayout?.getChildAt(1) as? android.widget.TextView

        btnIcon?.setImageResource(R.drawable.ic_user_minus_red)
        btnIcon?.clearColorFilter()
        btnText?.text = "Deactivate"
        btnText?.setTextColor(android.graphics.Color.parseColor("#EF4444"))
    }
}
