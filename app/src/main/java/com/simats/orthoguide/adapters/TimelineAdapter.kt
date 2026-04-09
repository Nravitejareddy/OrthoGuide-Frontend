package com.simats.orthoguide.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.simats.orthoguide.R
import com.simats.orthoguide.network.TimelineItem

class TimelineAdapter(private var items: List<TimelineItem>) :
    RecyclerView.Adapter<TimelineAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.iv_status_icon)
        val tvPhase: TextView = view.findViewById(R.id.tv_phase_title)
        val tvStatus: TextView = view.findViewById(R.id.tv_phase_status)
        val tvStatusBadge: TextView = view.findViewById(R.id.tv_status_badge)
        val vDivider: View = view.findViewById(R.id.v_divider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timeline_step, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvPhase.text = item.phase ?: "Unknown Phase"
        
        val context = holder.itemView.context
        
        when (item.status?.lowercase()) {
            "completed" -> {
                holder.ivIcon.setImageResource(R.drawable.ic_progress_step_done)
                holder.tvStatusBadge.visibility = View.VISIBLE
                holder.tvStatusBadge.text = "DONE"
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_status)
                holder.tvStatusBadge.backgroundTintList = ContextCompat.getColorStateList(context, R.color.brand_green_light)
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.brand_green_dark))
                holder.tvPhase.setTextColor(ContextCompat.getColor(context, R.color.text_title))
                holder.tvStatus.visibility = View.GONE
            }
            "in progress" -> {
                holder.ivIcon.setImageResource(R.drawable.ic_progress_step_current)
                holder.tvStatusBadge.visibility = View.VISIBLE
                holder.tvStatusBadge.text = "CURRENT"
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_status)
                holder.tvStatusBadge.backgroundTintList = ContextCompat.getColorStateList(context, R.color.brand_green_light)
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.brand_green_dark))
                holder.tvPhase.setTextColor(ContextCompat.getColor(context, R.color.brand_green_dark))
                holder.tvStatus.visibility = View.VISIBLE
                holder.tvStatus.text = "Current"
            }
            else -> { // Upcoming
                holder.ivIcon.setImageResource(R.drawable.ic_progress_step_upcoming)
                holder.tvStatusBadge.visibility = View.GONE
                holder.tvPhase.setTextColor(ContextCompat.getColor(context, R.color.text_muted))
                holder.tvStatus.visibility = View.VISIBLE
                holder.tvStatus.text = "Upcoming"
            }
        }

        // Hide divider for last item
        holder.vDivider.visibility = if (position == items.size - 1) View.GONE else View.VISIBLE
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<TimelineItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}

