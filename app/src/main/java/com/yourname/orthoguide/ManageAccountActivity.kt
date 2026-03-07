package com.yourname.orthoguide

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class ManageAccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_account)

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

        if (isAdmin) {
            findViewById<TextView>(R.id.tv_danger_zone_title)?.visibility = View.GONE
            findViewById<View>(R.id.card_delete_account)?.visibility = View.GONE
        } else if (isClinician) {
            findViewById<TextView>(R.id.tv_delete_account_text)?.text = "Deactivate Account"
            findViewById<TextView>(R.id.tv_danger_zone_title)?.text = "DANGER ZONE"
        }

        findViewById<View>(R.id.ll_change_password)?.setOnClickListener {
            val intent = android.content.Intent(this, ChangePasswordActivity::class.java)
            intent.putExtra("isClinician", isClinician)
            intent.putExtra("isAdmin", isAdmin)
            startActivity(intent)
        }

        findViewById<View>(R.id.ll_delete_account)?.setOnClickListener {
            showDeleteAccountDialog(isClinician)
        }
    }

    private fun showDeleteAccountDialog(isClinician: Boolean) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_account, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val alertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel = dialogView.findViewById<TextView>(R.id.tv_cancel_delete)
        val btnDelete = dialogView.findViewById<Button>(R.id.btn_confirm_delete)

        if (isClinician) {
            dialogView.findViewById<TextView>(R.id.tv_dialog_title)?.text = "Deactivate Account?"
            btnDelete.text = "Deactivate"
        }

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnDelete.setOnClickListener {
            alertDialog.dismiss()
            val intent = android.content.Intent(this, AccountDeactivatedActivity::class.java)
            startActivity(intent)
            finish()
        }

        alertDialog.show()
    }
}
