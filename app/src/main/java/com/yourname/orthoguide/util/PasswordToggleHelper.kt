package com.yourname.orthoguide.util

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView
import com.yourname.orthoguide.R

object PasswordToggleHelper {

    fun setupPasswordToggle(editText: EditText, toggleIcon: ImageView) {
        var isPasswordVisible = false
        
        toggleIcon.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            
            if (isPasswordVisible) {
                // Show password
                editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                toggleIcon.setImageResource(R.drawable.ic_visibility_on)
            } else {
                // Hide password
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
                toggleIcon.setImageResource(R.drawable.ic_visibility_off)
            }
            
            // Move cursor to end
            editText.setSelection(editText.text.length)
        }
    }
}
