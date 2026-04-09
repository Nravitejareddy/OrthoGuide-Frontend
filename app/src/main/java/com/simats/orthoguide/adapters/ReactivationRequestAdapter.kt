package com.simats.orthoguide.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.simats.orthoguide.R
import com.simats.orthoguide.network.ReactivationRequestItem

class ReactivationRequestAdapter(
    private var requests: List<ReactivationRequestItem>,
    private val onItemClick: (ReactivationRequestItem) -> Unit
) : RecyclerView.Adapter<ReactivationRequestAdapter.ViewHolder>() {



    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llRequestItem: View = view.findViewById(R.id.ll_request_item)
        val flIconContainer: View = view.findViewById(R.id.fl_notif_icon_bg)
        val ivNotifIcon: ImageView = view.findViewById(R.id.iv_notif_icon)
        val tvPatientId: TextView = view.findViewById(R.id.tv_patient_id)
        val tvPatientName: TextView = view.findViewById(R.id.tv_patient_name)
        val tvReason: TextView = view.findViewById(R.id.tv_reason)
        val tvTime: TextView? = view.findViewById(R.id.tv_time)
        val vUnreadDot: View? = view.findViewById(R.id.v_unread_dot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reactivation_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requests[position]
        val isRead = request.isRead

        val roleDisplay = if (request.userRole.lowercase() == "clinician") "CLINICIAN" else "PATIENT"
        holder.tvPatientId.text = "$roleDisplay • ID: ${request.patientId}"
        holder.tvPatientName.text = request.patientName
        holder.tvReason.text = request.reason
        
        // Handle optional time display
        holder.tvTime?.text = request.createdAt.split("T")[0]


        // Handle unread status styling
        if (isRead) {
            holder.vUnreadDot?.visibility = View.GONE
            // Apply faded purple tint for read items
            holder.llRequestItem.setBackgroundColor(Color.parseColor("#F5F3FF")) 
            holder.itemView.alpha = 0.8f
            holder.flIconContainer.scaleX = 0.95f
            holder.flIconContainer.scaleY = 0.95f
            holder.ivNotifIcon.alpha = 0.6f // Faded logo
            holder.flIconContainer.alpha = 0.7f
        } else {
            holder.vUnreadDot?.visibility = View.VISIBLE
            // Apply soft purple tint for unread items (match admin theme)
            holder.llRequestItem.setBackgroundColor(Color.parseColor("#EDE9FE")) 
            holder.itemView.alpha = 1.0f
            holder.flIconContainer.scaleX = 1.0f
            holder.flIconContainer.scaleY = 1.0f
            holder.ivNotifIcon.alpha = 1.0f // Full logo
            holder.ivNotifIcon.alpha = 1.0f // Full logo
            holder.flIconContainer.alpha = 1.0f
        }

        holder.llRequestItem.setOnClickListener {
            onItemClick(request)
        }
    }

    override fun getItemCount() = requests.size

    fun updateData(newRequests: List<ReactivationRequestItem>) {
        requests = newRequests
        notifyDataSetChanged()
    }

    fun markAllAsRead() {
        // Since we refetch from server in Activity, this just provides instant feedback
        notifyDataSetChanged()
    }
}

