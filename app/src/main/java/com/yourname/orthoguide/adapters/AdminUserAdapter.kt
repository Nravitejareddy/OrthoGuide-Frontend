package com.yourname.orthoguide.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.yourname.orthoguide.R
import com.yourname.orthoguide.network.AdminUserItem
import com.yourname.orthoguide.util.DialogUtils

class AdminUserAdapter(
    private var users: List<AdminUserItem>,
    private val onUserClick: (AdminUserItem) -> Unit,
    private val onResetPassword: (AdminUserItem) -> Unit,
    private val onToggleStatus: (AdminUserItem) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_user_name)
        val tvId: TextView = itemView.findViewById(R.id.tv_user_id)
        val tvStatusBadge: TextView = itemView.findViewById(R.id.tv_status_badge)
        val btnResetPassword: MaterialButton = itemView.findViewById(R.id.btn_reset_password)
        val btnDeactivate: MaterialButton = itemView.findViewById(R.id.btn_deactivate)
        val cardUser: MaterialCardView = itemView.findViewById(R.id.mcv_user_card)

        fun bind(user: AdminUserItem) {
            tvName.text = user.name ?: "Unknown User"
            
            val roleName = when {
                user.role?.lowercase() == "patient" -> "Patient"
                user.role?.lowercase() == "clinician" -> user.roleType ?: "Clinician"
                user.role?.lowercase() == "admin" -> "Admin"
                else -> (user.role ?: "User").replaceFirstChar { it.uppercase() }
            }
            tvId.text = "$roleName • ID: ${user.id}"
            
            // Avatar styling
            val isPatient = user.role?.lowercase() == "patient"
            val avatarBg = if (isPatient) R.drawable.bg_circle_soft_blue else R.drawable.bg_circle_soft_purple
            itemView.findViewById<View>(R.id.iv_user_avatar).setBackgroundResource(avatarBg)
            
            val isActive = user.status?.lowercase() == "active"
            tvStatusBadge.text = if (isActive) "ACTIVE" else "INACTIVE"
            
            if (isActive) {
                tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.context, R.color.success_green))
                tvStatusBadge.setBackgroundResource(R.drawable.bg_tag_active_light)
                
                btnDeactivate.text = "Deactivate"
                btnDeactivate.setTextColor(ContextCompat.getColor(itemView.context, R.color.error_red))
                btnDeactivate.setIconResource(R.drawable.ic_user_minus_red)
                btnDeactivate.iconTint = ContextCompat.getColorStateList(itemView.context, R.color.error_red)
                btnDeactivate.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.error_soft)
            } else {
                tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray_light))
                tvStatusBadge.setBackgroundResource(R.drawable.bg_tag_inactive_light)
                
                btnDeactivate.text = "Reactivate"
                btnDeactivate.setTextColor(ContextCompat.getColor(itemView.context, R.color.success_green))
                btnDeactivate.setIconResource(R.drawable.ic_user_plus_green)
                btnDeactivate.iconTint = ContextCompat.getColorStateList(itemView.context, R.color.success_green)
                btnDeactivate.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.success_soft)
            }

            cardUser.setOnClickListener { onUserClick(user) }
            
            btnResetPassword.setOnClickListener {
                DialogUtils.showConfirmDialog(
                    context = itemView.context,
                    title = "Reset Password",
                    message = "Are you sure you want to reset the password for ${user.name}? It will be set to ortho@ followed by the last 4 digits of their phone number.",
                    iconRes = R.drawable.ic_key_purple,
                    confirmText = "Reset Password",
                    cancelText = "Cancel",
                    isPurpleAdmin = true,
                    onConfirm = { onResetPassword(user) }
                )
            }
            
            btnDeactivate.setOnClickListener {
                val action = if (isActive) "Deactivate" else "Reactivate"
                val message = if (isActive) 
                    "Are you sure you want to deactivate ${user.name}'s account? They will no longer be able to log in."
                    else "Are you sure you want to reactivate ${user.name}'s account?"

                DialogUtils.showConfirmDialog(
                    context = itemView.context,
                    title = "$action Account",
                    message = message,
                    iconRes = R.drawable.ic_warning_purple,
                    confirmText = action,
                    cancelText = "Keep It",
                    isPurpleAdmin = true,
                    onConfirm = { onToggleStatus(user) }
                )
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    fun updateData(newUsers: List<AdminUserItem>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
