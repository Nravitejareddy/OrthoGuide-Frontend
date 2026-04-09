package com.simats.orthoguide

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class CareRemindersActivity : AppCompatActivity() {
    private var swOralHygiene: CompoundButton? = null
    private var swApplianceCare: CompoundButton? = null
    private var swAppointments: CompoundButton? = null
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
            setContentView(R.layout.activity_care_reminders)

            val sharedPref = getSharedPreferences("OrthoPref", android.content.Context.MODE_PRIVATE)
            userId = sharedPref.getString("USER_ID", "") ?: ""

            swOralHygiene = findViewById(R.id.sw_oral_hygiene)
            swApplianceCare = findViewById(R.id.sw_appliance_care)
            swAppointments = findViewById(R.id.sw_appointments)

            // Standardized back logic
            findViewById<View>(R.id.iv_back)?.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            fetchSettings()

            val clickListener = View.OnClickListener {
                saveSettings()
            }

            swOralHygiene?.setOnClickListener(clickListener)
            swApplianceCare?.setOnClickListener(clickListener)
            swAppointments?.setOnClickListener(clickListener)

        } catch (e: Exception) {
            com.simats.orthoguide.util.DialogUtils.showError(this, "Init error: ${e.message}")
        }
    }

    private fun fetchSettings() {
        if (userId.isEmpty()) return
        
        try {
            com.simats.orthoguide.network.RetrofitClient.service.getNotificationSettings(userId).enqueue(object : retrofit2.Callback<Map<String, Any>> {
                override fun onResponse(call: retrofit2.Call<Map<String, Any>>, response: retrofit2.Response<Map<String, Any>>) {
                    if (response.isSuccessful) {
                        val settings = response.body() ?: return
                        swOralHygiene?.isChecked = settings["oral_hygiene"] as? Boolean ?: true
                        swApplianceCare?.isChecked = settings["appliance_care"] as? Boolean ?: true
                        swAppointments?.isChecked = settings["appointment"] as? Boolean ?: true
                    }
                }
                override fun onFailure(call: retrofit2.Call<Map<String, Any>>, t: Throwable) {
                    // Fail silently to avoid annoying user
                }
            })
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun saveSettings() {
        if (userId.isEmpty()) return
        
        try {
            val request = com.simats.orthoguide.network.NotificationSettingsRequest(
                patientId = userId,
                oralHygiene = swOralHygiene?.isChecked ?: true,
                applianceCare = swApplianceCare?.isChecked ?: true,
                appointment = swAppointments?.isChecked ?: true
            )

            com.simats.orthoguide.network.RetrofitClient.service.updateNotificationSettings(request).enqueue(object : retrofit2.Callback<com.simats.orthoguide.network.GenericResponse> {
                override fun onResponse(call: retrofit2.Call<com.simats.orthoguide.network.GenericResponse>, response: retrofit2.Response<com.simats.orthoguide.network.GenericResponse>) {
                    if (response.isSuccessful) {
                        com.simats.orthoguide.util.DialogUtils.showSuccess(this@CareRemindersActivity, "Preferences updated")
                    } else {
                        com.simats.orthoguide.util.DialogUtils.showError(this@CareRemindersActivity, "Network error")
                    }
                }
                override fun onFailure(call: retrofit2.Call<com.simats.orthoguide.network.GenericResponse>, t: Throwable) {
                    com.simats.orthoguide.util.DialogUtils.showError(this@CareRemindersActivity, "Update failed")
                }
            })
        } catch (e: Exception) {
            com.simats.orthoguide.util.DialogUtils.showError(this, "Save error: ${e.message}")
        }
    }
}

