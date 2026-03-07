package com.yourname.orthoguide

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.button.MaterialButton

class LoginSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_success)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        // Changed to false (dark/white icons) to match Dashboard's status bar before transition
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        val rootView = findViewById<View>(R.id.login_success_root)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        val syncIcon = findViewById<ImageView>(R.id.iv_sync_icon)
        val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_clockwise)
        syncIcon?.startAnimation(rotation)

        // Automatic redirect after 2 seconds
        rootView?.postDelayed({
            val intent = android.content.Intent(this, DashboardActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }, 2000)
    }
}
