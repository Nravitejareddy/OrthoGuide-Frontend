package com.yourname.orthoguide

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.yourname.orthoguide.network.GenericResponse
import com.yourname.orthoguide.network.ReactivationRequest
import com.yourname.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReactivationRequestActivity : AppCompatActivity() {

    private lateinit var etPatientId: EditText
    private lateinit var etPatientName: EditText
    private lateinit var etContactInfo: EditText
    private lateinit var etReason: EditText
    private lateinit var btnSubmit: View
    private lateinit var pbSubmitting: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reactivation_request)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.WHITE
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val toolbar = findViewById<View>(R.id.card_toolbar)
        val bottomSpacer = findViewById<View>(R.id.v_bottom_inset_spacer)
        
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(bottomSpacer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.layoutParams.height = systemBars.bottom + (60 * resources.displayMetrics.density).toInt()
            v.requestLayout()
            insets
        }

        etPatientId = findViewById(R.id.et_patient_id)
        etPatientName = findViewById(R.id.et_patient_name)
        etContactInfo = findViewById(R.id.et_contact_info)
        etReason = findViewById(R.id.et_reason)
        btnSubmit = findViewById(R.id.btn_submit_request)
        pbSubmitting = findViewById(R.id.pb_submitting)

        val patientId = intent.getStringExtra("PATIENT_ID") ?: ""
        val userRole = intent.getStringExtra("USER_ROLE") ?: "patient"
        val isClinician = userRole.lowercase() == "clinician"

        etPatientId.setText(patientId)
        
        // Conditional Blue Theme for Clinicians
        if (isClinician) {
            // Tint the submit button 
            (btnSubmit as? com.google.android.material.button.MaterialButton)?.backgroundTintList = 
                android.content.res.ColorStateList.valueOf(Color.parseColor("#2563EB"))
            
            // Adjust layouts if they have greens
            findViewById<View>(R.id.fl_notif_icon_bg)?.backgroundTintList = 
                android.content.res.ColorStateList.valueOf(Color.parseColor("#EFF6FF"))
            
            // Tint headers and labels if they use green
            listOf(R.id.tv_header_account, R.id.tv_header_reason).forEach { id ->
                findViewById<android.widget.TextView>(id)?.setTextColor(Color.parseColor("#2563EB"))
            }

            // Adjust back button color if it is green
            findViewById<android.widget.ImageView>(R.id.iv_back)?.imageTintList = 
                android.content.res.ColorStateList.valueOf(Color.parseColor("#2563EB"))
                
            // Tint the patient ID field text which was emerald in XML
            etPatientId.setTextColor(Color.parseColor("#2563EB"))
            etPatientId.setHint("DR123...")
        }

        findViewById<View>(R.id.iv_back).setOnClickListener { finish() }

        btnSubmit.setOnClickListener {
            submitRequest(userRole)
        }
    }

    private fun submitRequest(userRole: String) {
        val patientId = etPatientId.text.toString().trim()
        val patientName = etPatientName.text.toString().trim()
        val contactInfo = etContactInfo.text.toString().trim()
        val reason = etReason.text.toString().trim()

        if (patientId.isEmpty() || patientName.isEmpty() || contactInfo.isEmpty() || reason.isEmpty()) {
            com.yourname.orthoguide.util.DialogUtils.showError(findViewById(android.R.id.content), "Please fill all required fields")
            return
        }

        if (contactInfo.length < 5) {
            com.yourname.orthoguide.util.DialogUtils.showError(findViewById(android.R.id.content), "Please enter a valid contact email/phone")
            return
        }

        setLoading(true)

        val request = ReactivationRequest(patientId, patientName, userRole, contactInfo, reason)
        RetrofitClient.service.submitReactivationRequest(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                setLoading(false)
                if (response.isSuccessful) {
                    com.yourname.orthoguide.util.DialogUtils.showSuccess(findViewById(android.R.id.content), "Request Sent Successfully")
                    
                    // Delay slightly to let the user see the success snackbar
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        val intent = Intent(this@ReactivationRequestActivity, UnifiedLoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }, 1500)
                } else {
                    val errorMsg = try {
                        val errorBody = response.errorBody()?.string()
                        val errorJson = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
                        (errorJson["error"] ?: errorJson["message"] ?: "Submission failed").toString()
                    } catch (e: Exception) {
                        "Submission failed"
                    }
                    com.yourname.orthoguide.util.DialogUtils.showError(findViewById(android.R.id.content), errorMsg)
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                setLoading(false)
                Toast.makeText(this@ReactivationRequestActivity, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            btnSubmit.visibility = View.INVISIBLE
            pbSubmitting.visibility = View.VISIBLE
        } else {
            btnSubmit.visibility = View.VISIBLE
            pbSubmitting.visibility = View.GONE
        }
    }
}
