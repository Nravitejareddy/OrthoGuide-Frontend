package com.yourname.orthoguide.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.yourname.orthoguide.R
import com.yourname.orthoguide.network.ClinicianPatientItem

class ClinicianPatientAdapter(
    private var patients: List<ClinicianPatientItem>,
    private val onPatientClick: (ClinicianPatientItem) -> Unit
) : RecyclerView.Adapter<ClinicianPatientAdapter.PatientViewHolder>() {

    inner class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_patient_name)
        val tvId: TextView = itemView.findViewById(R.id.tv_patient_id)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_patient_status)
        val tvStage: TextView = itemView.findViewById(R.id.tv_patient_stage)
        val tvInitials: TextView = itemView.findViewById(R.id.tv_patient_initials)
        val llApptStatus: View = itemView.findViewById(R.id.ll_appointment_status)
        val ivApptIcon: android.widget.ImageView = itemView.findViewById(R.id.iv_appointment_icon)
        val tvApptText: TextView = itemView.findViewById(R.id.tv_appointment_text)

        fun bind(patient: ClinicianPatientItem) {
            tvName.text = patient.name ?: "Unknown Patient"
            tvId.text = "ID: ${patient.patientId}"
            tvStage.text = patient.treatmentStage ?: "Initial Consultation"
            
            // Generate initials
            val parts = (patient.name ?: "").trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
            tvInitials.text = if (parts.size >= 2) {
                (parts[0].substring(0, 1) + parts[parts.size - 1].substring(0, 1)).uppercase()
            } else if (parts.isNotEmpty()) {
                parts[0].substring(0, 1).uppercase()
            } else "JD"

            // Status Badge
            val rawStatus = patient.status?.lowercase() ?: "on track"
            val displayStatus = if (rawStatus == "active") "on track" else rawStatus
            tvStatus.text = displayStatus.uppercase()
            
            when (displayStatus) {
                "critical" -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_tag_red)
                    tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.error_red))
                }
                "attention" -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_tag_attention_yellow)
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#B45309"))
                }
                else -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_tag_green_soft)
                    tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.success_green))
                }
            }

            // Appointment Status
            if (patient.hasAppointment == true) {
                val isMyAppt = patient.isMyAppointment == true
                
                if (isMyAppt) {
                    llApptStatus.setBackgroundResource(R.drawable.bg_tag_green_soft)
                    ivApptIcon.setImageResource(R.drawable.ic_calendar_purple)
                    ivApptIcon.setColorFilter(android.graphics.Color.parseColor("#10B981"))
                    tvApptText.setTextColor(android.graphics.Color.parseColor("#10B981"))
                } else {
                    llApptStatus.setBackgroundResource(R.drawable.bg_tag_blue_soft)
                    ivApptIcon.setImageResource(R.drawable.ic_calendar_purple)
                    ivApptIcon.setColorFilter(android.graphics.Color.parseColor("#3B82F6"))
                    tvApptText.setTextColor(android.graphics.Color.parseColor("#3B82F6"))
                }
                
                val dateStr = patient.nextAppointmentDate
                tvApptText.text = if (!dateStr.isNullOrEmpty() && dateStr != "None") {
                    try {
                        val sdfIn = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val sdfOut = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                        val formattedDate = sdfOut.format(sdfIn.parse(dateStr)!!)
                        if (isMyAppt) "Session: $formattedDate" else "Other Doctor: $formattedDate"
                    } catch (e: Exception) {
                        if (isMyAppt) "Session scheduled" else "Other Doctor Session"
                    }
                } else {
                    if (isMyAppt) "Session scheduled" else "Other Doctor Session"
                }
            } else {
                llApptStatus.setBackgroundResource(R.drawable.bg_tag_yellow)
                ivApptIcon.setImageResource(R.drawable.ic_clock_outline)
                ivApptIcon.clearColorFilter()
                tvApptText.text = "No session scheduled"
                tvApptText.setTextColor(android.graphics.Color.parseColor("#D97706"))
            }

            itemView.setOnClickListener { onPatientClick(patient) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_clinician_patient, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(patients[position])
    }

    override fun getItemCount(): Int = patients.size

    fun updateData(newPatients: List<ClinicianPatientItem>) {
        patients = newPatients
        notifyDataSetChanged()
    }
}
