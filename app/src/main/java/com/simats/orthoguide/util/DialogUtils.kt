package com.simats.orthoguide.util

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simats.orthoguide.R

import android.os.Handler
import android.os.Looper

object DialogUtils {

    /**
     * Shows a premium, floating pill-shaped notification bar (Toast).
     * @param view The anchor view (usually the root layout or the content view).
     * @param message The text to display.
     * @param iconRes The drawable resource for the icon.
     * @param iconTint The color tint for the icon.
     */
    @android.annotation.SuppressLint("RestrictedApi")
    private fun showToast(view: android.view.View, message: String, iconRes: Int, iconTint: Int) {
        val snackbar = com.google.android.material.snackbar.Snackbar.make(view, "", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
        val layout = snackbar.view as android.widget.FrameLayout
        
        // Safe discovery of internal snackbar components to avoid crashes
        layout.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)?.visibility = android.view.View.INVISIBLE
        snackbar.setBackgroundTint(android.graphics.Color.TRANSPARENT)
        
        val customView = LayoutInflater.from(view.context).inflate(R.layout.layout_custom_success_toast, layout, false)
        customView.findViewById<TextView>(R.id.tv_toast_message).text = message
        
        val ivIcon = customView.findViewById<android.widget.ImageView>(R.id.iv_toast_icon)
        ivIcon.setImageResource(iconRes)
        ivIcon.imageTintList = android.content.res.ColorStateList.valueOf(iconTint)
        
        // Ensure the custom view's layout parameters are set to center it
        val lp = android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        )
        lp.gravity = android.view.Gravity.BOTTOM
        customView.layoutParams = lp
        
        layout.setPadding(0, 0, 0, 0)
        layout.addView(customView, 0)
        snackbar.show()
    }

    fun showSuccess(view: android.view.View, message: String) {
        val greenColor = android.graphics.Color.parseColor("#10B981")
        showToast(view, message, R.drawable.ic_success_circle_check, greenColor)
    }

    fun showError(view: android.view.View, message: String) {
        val redColor = android.graphics.Color.parseColor("#EF4444")
        showToast(view, message, R.drawable.ic_warning_red, redColor)
    }

    // A version that takes context and a parent view if available
    fun showSuccessDialog(context: Context, title: String, message: String, onDismiss: (() -> Unit)? = null) {
        val activity = context as? android.app.Activity
        val rootView = activity?.findViewById<android.view.View>(android.R.id.content)
        
        if (rootView != null) {
            showSuccess(rootView, message)
            if (onDismiss != null) {
                Handler(Looper.getMainLooper()).postDelayed({
                    onDismiss.invoke()
                }, 1500)
            }
        } else {
            val dialog = MaterialAlertDialogBuilder(context)
                .setTitle("Success")
                .setMessage(message)
                .setPositiveButton("OK") { d, _ -> d.dismiss(); onDismiss?.invoke() }
                .show()
            
            Handler(Looper.getMainLooper()).postDelayed({ if (dialog.isShowing) dialog.dismiss(); onDismiss?.invoke() }, 1500)
        }
    }

    /**
     * Drop-in replacement for Toast that takes an Activity and finds the root view automatically.
     */
    fun showSuccess(activity: android.app.Activity, message: String) {
        val view = activity.findViewById<android.view.View>(android.R.id.content)
        if (view != null) showSuccess(view, message)
    }

    fun showError(activity: android.app.Activity, message: String) {
        val view = activity.findViewById<android.view.View>(android.R.id.content)
        if (view != null) showError(view, message)
    }

    fun showSuccessSnackbar(view: android.view.View, message: String) {
        showSuccess(view, message)
    }

    fun showErrorSnackbar(view: android.view.View, message: String) {
        showError(view, message)
    }

    fun showConfirmDialog(
        context: Context,
        title: String,
        message: String,
        iconRes: Int = R.drawable.ic_warning_purple,
        confirmText: String = "Confirm",
        cancelText: String = "Cancel",
        isPurpleAdmin: Boolean = false,
        onConfirm: () -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_action, null)
        val dialog = MaterialAlertDialogBuilder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<TextView>(R.id.tv_dialog_title).text = title
        dialogView.findViewById<TextView>(R.id.tv_dialog_message).text = message
        
        val ivIcon = dialogView.findViewById<android.widget.ImageView>(R.id.iv_dialog_icon)
        val cvIconBg = dialogView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.mcv_icon_container)
        
        ivIcon.setImageResource(iconRes)
        
        val btnCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_dialog_cancel)
        val btnConfirm = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_dialog_confirm)
        
        // Theme Styling
        if (isPurpleAdmin) {
            val purpleColor = android.graphics.Color.parseColor("#7C3AED")
            val purpleSoft = android.graphics.Color.parseColor("#F5F3FF")
            
            ivIcon.imageTintList = android.content.res.ColorStateList.valueOf(purpleColor)
            cvIconBg?.setCardBackgroundColor(purpleSoft)
            btnConfirm.backgroundTintList = android.content.res.ColorStateList.valueOf(purpleColor)
        }

        btnCancel.text = cancelText
        btnConfirm.text = confirmText
        
        btnCancel.setOnClickListener { 
            // Also delay cancel dismissal for consistency and prevents click leak
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (dialog.isShowing) dialog.dismiss()
            }, 100)
        }
        btnConfirm.setOnClickListener {
            onConfirm()
            // Increased delay slightly and added isShowing check to prevent crash on rapid interaction
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                } catch (e: Exception) {
                    // Prevent crash if activity is already finished
                }
            }, 100)
        }

        dialog.show()
    }
}

