package com.simats.orthoguide

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.orthoguide.network.NotificationItem
import com.simats.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationsActivity : AppCompatActivity() {
    private lateinit var rvNotifications: RecyclerView
    private lateinit var adapter: NotificationsAdapter
    private val notifications = mutableListOf<NotificationItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notifications)

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

        findViewById<View>(R.id.tv_mark_all_read)?.setOnClickListener {
            markAllAsRead()
        }

        rvNotifications = findViewById(R.id.rv_notifications)
        rvNotifications.layoutManager = LinearLayoutManager(this)
        adapter = NotificationsAdapter(notifications) { notification ->
            markAsRead(notification.id)
            if (notification.type == "appointment") {
                val intent = android.content.Intent(this, AppointmentsActivity::class.java)
                startActivity(intent)
            }
        }
        rvNotifications.adapter = adapter



        fetchNotifications()
    }

    private fun fetchNotifications() {
        val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", "") ?: ""
        if (userId.isEmpty()) return

        RetrofitClient.service.getPatientNotifications(userId).enqueue(object : Callback<List<NotificationItem>> {
            override fun onResponse(call: Call<List<NotificationItem>>, response: Response<List<NotificationItem>>) {
                if (response.isSuccessful) {
                    val list = response.body()
                    if (list != null) {
                        notifications.clear()
                        notifications.addAll(list)
                        adapter.notifyDataSetChanged()

                        // Sync unread count to prefs so the dashboard badge vanishes
                        val unreadCount = list.count { !it.isRead }
                        val p = getSharedPreferences("OrthoPref", MODE_PRIVATE)
                        p.edit()
                            .putBoolean("notif_seen", true)
                            .putInt("LAST_KNOWN_UNREAD_COUNT", unreadCount)
                            .apply()
                    }
                }
            }

            override fun onFailure(call: Call<List<NotificationItem>>, t: Throwable) {
                Log.e("OrthoGuide", "Failed to fetch notifications", t)
            }
        })
    }

    private fun markAsRead(id: Int) {
        RetrofitClient.service.markNotificationRead(id).enqueue(object : Callback<com.simats.orthoguide.network.GenericResponse> {
            override fun onResponse(call: Call<com.simats.orthoguide.network.GenericResponse>, response: Response<com.simats.orthoguide.network.GenericResponse>) {
                if (response.isSuccessful) {
                    // Refresh or update locally
                    fetchNotifications()
                }
            }
            override fun onFailure(call: Call<com.simats.orthoguide.network.GenericResponse>, t: Throwable) {
                Log.e("OrthoGuide", "Failed to mark as read", t)
            }
        })
    }

    private fun markAllAsRead() {
        val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", "") ?: ""
        if (userId.isEmpty()) return

        val body = mapOf(
            "user_id" to userId,
            "role" to "patient"
        )

        RetrofitClient.service.markAllNotificationsRead(body).enqueue(object : Callback<com.simats.orthoguide.network.GenericResponse> {
            override fun onResponse(call: Call<com.simats.orthoguide.network.GenericResponse>, response: Response<com.simats.orthoguide.network.GenericResponse>) {
                if (response.isSuccessful) {
                    fetchNotifications()
                }
            }
            override fun onFailure(call: Call<com.simats.orthoguide.network.GenericResponse>, t: Throwable) {
                Log.e("OrthoGuide", "Failed to mark all as read", t)
            }
        })
    }

    class NotificationsAdapter(
        private val items: List<NotificationItem>,
        private val onItemClick: (NotificationItem) -> Unit
    ) : RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.tv_notif_title)
            val message: TextView = view.findViewById(R.id.tv_notif_message)
            val time: TextView = view.findViewById(R.id.tv_notif_time)
            val icon: ImageView = view.findViewById(R.id.iv_notif_icon)
            val iconBg: View = view.findViewById(R.id.fl_notif_icon_bg)
            val unreadDot: View = view.findViewById(R.id.v_unread_dot)
            val itemLayout: View = view.findViewById(R.id.ll_notif_item)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.title.text = getTitle(item.type)
            holder.message.text = item.message
            holder.time.text = item.createdAt ?: "Today"
            
            // Set icon and background based on type
            when (item.type) {
                "appointment" -> {
                    holder.icon.setImageResource(R.drawable.ic_notif_calendar)
                    holder.iconBg.setBackgroundResource(R.drawable.bg_notif_blue)
                }
                "report_issue", "concern" -> {
                    holder.icon.setImageResource(R.drawable.ic_notif_check)
                    holder.iconBg.setBackgroundResource(R.drawable.bg_notif_green)
                }
                "oral_hygiene" -> {
                    holder.icon.setImageResource(R.drawable.ic_notif_sparkle)
                    holder.iconBg.setBackgroundResource(R.drawable.bg_notif_purple)
                }
                "appliance_care" -> {
                    holder.icon.setImageResource(R.drawable.ic_notif_sparkle)
                    holder.iconBg.setBackgroundResource(R.drawable.bg_notif_orange)
                }
                else -> {
                    holder.icon.setImageResource(R.drawable.ic_notif_sparkle)
                    holder.iconBg.setBackgroundResource(R.drawable.bg_notif_purple)
                }
            }

            // Visual style for Read vs Unread (Faded look for read items)
            if (item.isRead) {
                holder.itemView.alpha = 0.6f
                holder.itemLayout.setBackgroundColor(Color.WHITE)
                holder.unreadDot.visibility = View.GONE
            } else {
                holder.itemView.alpha = 1.0f
                holder.itemLayout.setBackgroundColor(Color.parseColor("#F0FDF4")) // Light Green-50 for unread
                holder.unreadDot.visibility = View.VISIBLE
            }

            holder.itemLayout.setOnClickListener {
                onItemClick(item)
            }
        }

        override fun getItemCount() = items.size

        private fun getTitle(type: String): String {
            return when (type) {
                "appointment" -> "Appointment Updated"
                "report_issue", "concern" -> "Report Update"
                "oral_hygiene" -> "Oral Hygiene Reminder"
                "appliance_care" -> "Appliance Care Reminder"
                "reminder" -> "Hygiene Reminder"
                else -> "New Notification"
            }
        }
    }
}

