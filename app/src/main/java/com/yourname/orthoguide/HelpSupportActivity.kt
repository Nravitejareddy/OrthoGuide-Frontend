package com.yourname.orthoguide

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.yourname.orthoguide.network.RetrofitClient
import com.yourname.orthoguide.network.SupportInfoResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HelpSupportActivity : AppCompatActivity() {

    private var clinicPhone: String = "+91 72990 53348"
    private var clinicEmail: String = "prime@saveetha.com"
    private var clinicName: String = "Saveetha Dental College & Hospital"
    private var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_help_support)

        val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
        val role = sharedPref.getString("USER_ROLE", "patient")
        isAdmin = role == "admin"

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.card_toolbar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // Apply theme and labels immediately before network call
        updateUIByRole(role)

        findViewById<View>(R.id.iv_back)?.setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.fl_phone)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clinicPhone"))
            startActivity(intent)
        }

        findViewById<View>(R.id.fl_email)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$clinicEmail"))
            intent.putExtra(Intent.EXTRA_SUBJECT, "Support Request - OrthoGuide")
            startActivity(intent)
        }


        fetchSupportInfo()
    }


    private fun fetchSupportInfo() {
        val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
        val role = sharedPref.getString("USER_ROLE", "patient")

        RetrofitClient.service.getSupportInfo(role).enqueue(object : Callback<SupportInfoResponse> {
            override fun onResponse(call: Call<SupportInfoResponse>, response: Response<SupportInfoResponse>) {
                if (response.isSuccessful) {
                    val info = response.body() ?: return
                    clinicPhone = info.adminPhone ?: clinicPhone
                    clinicEmail = info.supportEmail ?: clinicEmail
                    clinicName = info.clinicName ?: clinicName
                    val version = info.appVersion ?: "1.0.0"

                    findViewById<TextView>(R.id.tv_phone_value)?.text = clinicPhone
                    findViewById<TextView>(R.id.tv_email_value)?.text = clinicEmail
                    findViewById<TextView>(R.id.tv_version)?.text = "OrthoGuide Version $version"

                    // Update labels and theme based on role
                    updateUIByRole(role)
                }
            }

            override fun onFailure(call: Call<SupportInfoResponse>, t: Throwable) {
                Log.e("OrthoGuide", "Failed to fetch support info", t)
            }
        })
    }

    private fun updateUIByRole(role: String?) {
        val phoneTitle = findViewById<TextView>(R.id.tv_phone_title)
        val emailTitle = findViewById<TextView>(R.id.tv_email_title)
        val helpSubtitle = findViewById<TextView>(R.id.tv_help_subtitle)

        when (role) {
            "patient" -> {
                phoneTitle?.text = "Main Emergency Line"
                emailTitle?.text = "General/Patient Queries"
                helpSubtitle?.text = "For treatment queries or emergency assistance, please contact our support team."
                applyGreenTheme()
            }
            "clinician" -> {
                phoneTitle?.text = clinicName
                emailTitle?.text = "Admin Email"
                helpSubtitle?.text = "For administrative issues or account inquiries, please contact the system administrator."
                applyBlueTheme()
            }
            "admin" -> {
                phoneTitle?.text = "Technical Support Contact"
                emailTitle?.text = "Technical Support Email"
                helpSubtitle?.text = "For system-level technical issues, please contact the developer support team."
                applyPurpleTheme()
            }
        }
    }

    private fun applyPurpleTheme() {
        val purplePrimary = android.graphics.Color.parseColor("#A855F7")
        val purpleLight = android.graphics.Color.parseColor("#F3E8FF")

        findViewById<View>(R.id.fl_help)?.backgroundTintList = android.content.res.ColorStateList.valueOf(purpleLight)
        findViewById<android.widget.ImageView>(R.id.iv_help)?.setColorFilter(purplePrimary)

        findViewById<View>(R.id.fl_phone)?.backgroundTintList = android.content.res.ColorStateList.valueOf(purpleLight)
        findViewById<android.widget.ImageView>(R.id.iv_phone)?.setColorFilter(purplePrimary)

        findViewById<View>(R.id.fl_email)?.backgroundTintList = android.content.res.ColorStateList.valueOf(purpleLight)
        findViewById<android.widget.ImageView>(R.id.iv_email)?.setColorFilter(purplePrimary)

        findViewById<TextView>(R.id.tv_version)?.let {
            it.backgroundTintList = android.content.res.ColorStateList.valueOf(purpleLight)
            it.setTextColor(purplePrimary)
        }
    }

    private fun applyGreenTheme() {
        val greenPrimary = android.graphics.Color.parseColor("#10B981")
        val greenLight = android.graphics.Color.parseColor("#DCFCE7")

        findViewById<View>(R.id.fl_help)?.backgroundTintList = android.content.res.ColorStateList.valueOf(greenLight)
        findViewById<android.widget.ImageView>(R.id.iv_help)?.setColorFilter(greenPrimary)

        findViewById<View>(R.id.fl_phone)?.backgroundTintList = android.content.res.ColorStateList.valueOf(greenLight)
        findViewById<android.widget.ImageView>(R.id.iv_phone)?.setColorFilter(greenPrimary)

        findViewById<View>(R.id.fl_email)?.backgroundTintList = android.content.res.ColorStateList.valueOf(greenLight)
        findViewById<android.widget.ImageView>(R.id.iv_email)?.setColorFilter(greenPrimary)

        findViewById<TextView>(R.id.tv_version)?.let {
            it.backgroundTintList = android.content.res.ColorStateList.valueOf(greenLight)
            it.setTextColor(greenPrimary)
        }
    }

    private fun applyBlueTheme() {
        val bluePrimary = android.graphics.Color.parseColor("#2563EB")
        val blueLight = android.graphics.Color.parseColor("#EFF6FF")

        findViewById<View>(R.id.fl_help)?.backgroundTintList = android.content.res.ColorStateList.valueOf(blueLight)
        findViewById<android.widget.ImageView>(R.id.iv_help)?.setColorFilter(bluePrimary)

        findViewById<View>(R.id.fl_phone)?.backgroundTintList = android.content.res.ColorStateList.valueOf(blueLight)
        findViewById<android.widget.ImageView>(R.id.iv_phone)?.setColorFilter(bluePrimary)

        findViewById<View>(R.id.fl_email)?.backgroundTintList = android.content.res.ColorStateList.valueOf(blueLight)
        findViewById<android.widget.ImageView>(R.id.iv_email)?.setColorFilter(bluePrimary)

        findViewById<TextView>(R.id.tv_version)?.let {
            it.backgroundTintList = android.content.res.ColorStateList.valueOf(blueLight)
            it.setTextColor(bluePrimary)
        }
    }
}