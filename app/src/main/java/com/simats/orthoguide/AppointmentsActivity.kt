package com.simats.orthoguide

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.orthoguide.network.AppointmentItem
import com.simats.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class AppointmentsActivity : AppCompatActivity() {
    private lateinit var rvUpcoming: RecyclerView
    private lateinit var rvPast: RecyclerView
    private val upcomingList = mutableListOf<AppointmentItem>()
    private val pastList = mutableListOf<AppointmentItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_appointments)

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

        rvUpcoming = findViewById(R.id.rv_upcoming_appointments)
        rvPast = findViewById(R.id.rv_past_appointments)

        rvUpcoming.layoutManager = LinearLayoutManager(this)
        rvPast.layoutManager = LinearLayoutManager(this)

        fetchAppointments()
    }

    private fun fetchAppointments() {
        val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", "") ?: ""
        if (userId.isEmpty()) return

        RetrofitClient.service.getPatientAppointments(userId).enqueue(object : Callback<List<AppointmentItem>> {
            override fun onResponse(call: Call<List<AppointmentItem>>, response: Response<List<AppointmentItem>>) {
                if (response.isSuccessful) {
                    val all = response.body() ?: emptyList()
                    
                    upcomingList.clear()
                    pastList.clear()
                    
                    val today = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    
                    for (item in all) {
                        val status = item.status?.lowercase() ?: ""
                        val isUpcomingStatus = status == "scheduled" || status == "rescheduled" || status == "confirmed"
                        
                        val itemDate = try {
                            val cal = Calendar.getInstance()
                            cal.time = sdf.parse(item.date!!)!!
                            cal.apply {
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                        } catch (e: Exception) { null }

                        if (isUpcomingStatus) {
                            if (itemDate != null && itemDate.before(today)) {
                                // Past scheduled sessions become "Missed" and go to past list
                                pastList.add(item)
                            } else {
                                upcomingList.add(item)
                            }
                        } else {
                            pastList.add(item)
                        }
                    }
                    
                    // Sort both lists by date
                    // Sort both lists by date and time
                    upcomingList.sortWith(compareBy<com.simats.orthoguide.network.AppointmentItem> { it.date }.thenBy { it.time })
                    pastList.sortWith(compareByDescending<com.simats.orthoguide.network.AppointmentItem> { it.date }.thenByDescending { it.time })

                    rvUpcoming.adapter = AppointmentAdapter(upcomingList)
                    rvPast.adapter = AppointmentAdapter(pastList)
                }
            }

            override fun onFailure(call: Call<List<AppointmentItem>>, t: Throwable) {
                Log.e("OrthoGuide", "Failed to fetch appointments", t)
            }
        })
    }

    class AppointmentAdapter(private val items: List<AppointmentItem>) : 
        RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val type: TextView = view.findViewById(R.id.tv_appt_type)
            val clinician: TextView = view.findViewById(R.id.tv_clinician_name)
            val date: TextView = view.findViewById(R.id.tv_appt_date)
            val time: TextView = view.findViewById(R.id.tv_appt_time)
            val status: TextView = view.findViewById(R.id.tv_status_badge)
            val border: View = view.findViewById(R.id.v_appt_border)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_appointment, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.type.text = item.type ?: "Consultation"
            
            val name = item.clinicianName ?: "Your Orthodontist"
            holder.clinician.text = if (name.startsWith("Dr.")) name else "Dr. $name"
            
            holder.date.text = formatPrettyDate(item.date)
            holder.time.text = item.time ?: "TBD"

            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val itemDate = try {
                val cal = Calendar.getInstance()
                cal.time = sdf.parse(item.date!!)!!
                cal.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            } catch (e: Exception) { null }

            var statusStr = item.status?.lowercase() ?: "scheduled"
            
            // Auto-Missed Logic: If it's still 'scheduled' but date is in the past
            if ((statusStr == "scheduled" || statusStr == "rescheduled" || statusStr == "confirmed") && 
                itemDate != null && itemDate.before(today)) {
                statusStr = "missed"
            }

            when (statusStr) {
                "completed" -> {
                    holder.status.text = "COMPLETED"
                    holder.border.setBackgroundColor(Color.parseColor("#10B981")) // Solid Green as requested
                    holder.status.setTextColor(Color.parseColor("#10B981"))
                    holder.status.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#E6F7F1"))
                }
                "cancelled" -> {
                    holder.status.text = "CANCELLED"
                    holder.border.setBackgroundColor(Color.parseColor("#EF4444")) // Solid Red as requested
                    holder.status.setTextColor(Color.parseColor("#EF4444"))
                    holder.status.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FEE2E2"))
                }
                "missed" -> {
                    holder.status.text = "MISSED"
                    holder.border.setBackgroundColor(Color.parseColor("#F1F5F9")) // Keep Faded Grey for Missed
                    holder.status.setTextColor(Color.parseColor("#94A3B8"))
                    holder.status.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#F1F5F9"))
                }
                "rescheduled" -> {
                    holder.status.text = "RESCHEDULED"
                    holder.border.setBackgroundColor(Color.parseColor("#A855F7"))
                    holder.status.setTextColor(Color.parseColor("#A855F7"))
                    holder.status.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#F3E8FF"))
                }
                "scheduled", "confirmed" -> {
                    holder.status.text = "SCHEDULED"
                    holder.border.setBackgroundColor(Color.parseColor("#10B981"))
                    holder.status.setTextColor(Color.parseColor("#10B981"))
                    holder.status.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#E6F7F1"))
                }
                else -> {
                    holder.status.text = "SCHEDULED"
                    holder.border.setBackgroundColor(Color.parseColor("#10B981"))
                    holder.status.setTextColor(Color.parseColor("#10B981"))
                    holder.status.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#E6F7F1"))
                }
            }
        }

        private fun formatPrettyDate(dateStr: String?): String {
            if (dateStr == null) return "Unknown Date"
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdf.parse(dateStr) ?: return dateStr
                
                val outSdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val prettyDateSnippet = outSdf.format(date)

                val now = Calendar.getInstance()
                val target = Calendar.getInstance()
                target.time = date

                // Clear time fields for clean date comparison
                fun Calendar.clearTime() {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val today = Calendar.getInstance().apply { clearTime() }
                val targetDate = (target.clone() as Calendar).apply { clearTime() }

                val relative = when {
                    targetDate.timeInMillis == today.timeInMillis -> " (Today)"
                    targetDate.timeInMillis == today.timeInMillis + (24 * 60 * 60 * 1000) -> " (Tomorrow)"
                    targetDate.timeInMillis == today.timeInMillis - (24 * 60 * 60 * 1000) -> " (Yesterday)"
                    else -> ""
                }
                
                "$prettyDateSnippet$relative"
            } catch (e: Exception) {
                dateStr
            }
        }

        override fun getItemCount() = items.size
    }
}

