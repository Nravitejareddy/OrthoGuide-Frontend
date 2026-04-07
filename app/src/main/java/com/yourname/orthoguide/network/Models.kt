package com.yourname.orthoguide.network

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String
)

data class LoginResponse(
    @SerializedName("role") val role: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("consent_given") val consentGiven: Boolean? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("error") val error: String? = null
)

data class ProfileResponse(
    @SerializedName("patient_id") val patientId: String?,
    @SerializedName("clinician_id") val clinicianId: String?,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("role") val role: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("treatment_stage") val treatmentStage: String? = null,
    @SerializedName("current_tray") val currentTray: Int? = null,
    @SerializedName("total_trays") val totalTrays: Int? = null,
    @SerializedName("compliance") val compliance: Double? = null,
    @SerializedName("doctor_name") val doctorName: String? = null,
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("is_active") val isActive: Boolean? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("clinic_address") val clinicAddress: String? = null,
    @SerializedName("license_number") val licenseNumber: String? = null,
    @SerializedName("specialization") val specialization: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("next_appointment") val nextAppointment: NextAppointment? = null
)

data class AdminProfileResponse(
    @SerializedName("admin_id") val adminId: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class UpdateProfileRequest(
    @SerializedName("patient_id") val patientId: String? = null,
    @SerializedName("clinician_id") val clinicianId: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("role") val role: String? = null
)

data class NotificationSettingsRequest(
    @SerializedName("patient_id") val patientId: String,
    @SerializedName("oral_hygiene") val oralHygiene: Boolean,
    @SerializedName("appliance_care") val applianceCare: Boolean,
    @SerializedName("appointment") val appointment: Boolean
)

data class GenericResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: String?,
    @SerializedName("appointment_id") val appointmentId: Int? = null
)

data class UnreadCountResponse(
    @SerializedName("unread_count") val unreadCount: Int
)

// Chatbot
data class ChatRequest(
    @SerializedName("message") val message: String,
    @SerializedName("patient_id") val patientId: String
)

data class ChatResponse(
    @SerializedName("answer") val answer: String,
    @SerializedName("source") val source: String?
)

data class ChatHistoryItem(
    @SerializedName("message") val message: String,
    @SerializedName("sender") val sender: String,
    @SerializedName("created_at") val createdAt: String?
)

// Dashboard
data class DashboardResponse(
    @SerializedName("patient_id") val patientId: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("treatment_stage") val treatmentStage: String?,
    @SerializedName("current_stage_number") val currentStageNumber: Int?,
    @SerializedName("total_stages") val totalStages: Int?,
    @SerializedName("current_tray") val currentTray: Int?,
    @SerializedName("total_trays") val totalTrays: Int?,
    @SerializedName("compliance_rate") val complianceRate: Double?,
    @SerializedName("next_appointment") val nextAppointment: NextAppointment?,
    @SerializedName("treatment_phase") val treatmentPhase: String?,
    @SerializedName("progress_percent") val progressPercent: Int?,
    @SerializedName("daily_tip") val dailyTip: String?,
    @SerializedName("doctor_name") val doctorName: String?,
    @SerializedName("timeline") val timeline: List<TimelineItem>?
)

data class TimelineItem(
    @SerializedName("phase") val phase: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("active") val active: Boolean?
)

data class NextAppointment(
    @SerializedName("id") val id: Int?,
    @SerializedName("date") val date: String?,
    @SerializedName("time") val time: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("status") val status: String? = null,
    @SerializedName("clinician_name") val clinicianName: String?,
    @SerializedName("notes") val notes: String?
)

// Notifications
data class NotificationItem(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String,
    @SerializedName("message") val message: String,
    @SerializedName("is_read") var isRead: Boolean,
    @SerializedName("created_at") val createdAt: String?
)

// Appointments
data class AppointmentItem(
    @SerializedName("id") val id: Int?,
    @SerializedName("appointment_date") val date: String,
    @SerializedName("appointment_time") val time: String?,
    @SerializedName("appointment_type") val type: String?,
    @SerializedName("clinician_name") val clinicianName: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("notes") val notes: String?
)

// Report Issue
data class ReportIssueRequest(
    @SerializedName("patient_id") val patientId: String,
    @SerializedName("issue_type") val issueType: String,
    @SerializedName("description") val description: String,
    @SerializedName("photo_url") val photoUrl: String = "",
    @SerializedName("severity") val severity: Int? = null
)

// Care Guide
data class CareGuideItem(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String?
)

// Support Info
data class SupportInfoResponse(
    @SerializedName("admin_phone") val adminPhone: String?,
    @SerializedName("support_email") val supportEmail: String?,
    @SerializedName("app_version") val appVersion: String?,
    @SerializedName("clinic_name") val clinicName: String?
)

// Patient Issues
data class PatientIssueItem(
    @SerializedName("id") val id: Int?,
    @SerializedName("issue_type") val issueType: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("severity") val severity: Int?,
    @SerializedName("photo_url") val photoUrl: String?,
    @SerializedName("created_at") val createdAt: String?
)

// ============ CLINICIAN MODELS ============

data class ClinicianDashboardResponse(
    @SerializedName("total_patients") val totalPatients: Int?,
    @SerializedName("appointments_today") val appointmentsToday: Int?,
    @SerializedName("need_attention") val needAttention: Int?,
    @SerializedName("recent_patients") val recentPatients: List<ClinicianPatientItem>?,
    @SerializedName("today_schedule") val todaySchedule: List<ScheduleItem>?,
    @SerializedName("next_appointment") val nextAppointment: ScheduleItem? = null
)


data class ClinicianPatientItem(
    @SerializedName("patient_id") val patientId: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("treatment_stage") val treatmentStage: String?,
    @SerializedName("is_active") val isActive: Boolean?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("has_appointment") val hasAppointment: Boolean?,
    @SerializedName("is_my_appointment") val isMyAppointment: Boolean?,
    @SerializedName("next_appointment_date") val nextAppointmentDate: String?
)

data class ScheduleItem(
    @SerializedName("id") val id: Int?,
    @SerializedName("time") val time: String?,
    @SerializedName("patient_name") val patientName: String?,
    @SerializedName("patient_id") val patientId: String?,
    @SerializedName("patient_status") val patientStatus: String?,
    @SerializedName("patient_stage") val patientStage: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("appointment_date") val appointmentDate: String?,
    @SerializedName("appointment_time") val appointmentTime: String?,
    @SerializedName("appointment_type") val appointmentType: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("notes") val notes: String?
)

// ============ ADMIN MODELS ============

data class AdminUsersResponse(
    @SerializedName("patients") val patients: List<AdminUserItem>?,
    @SerializedName("clinicians") val clinicians: List<AdminUserItem>?,
    @SerializedName("admins") val admins: List<AdminUserItem>?
)

data class AdminUserItem(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("role_type") val roleType: String?,
    @SerializedName("treatment_stage") val treatmentStage: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("is_active") val isActive: Boolean?,
    @SerializedName("created_at") val createdAt: String?
)

data class AnalyticsOverviewResponse(
    @SerializedName("total_users") val totalUsers: Int?,
    @SerializedName("total_patients") val totalPatients: Int?,
    @SerializedName("total_clinicians") val totalClinicians: Int?,
    @SerializedName("total_admins") val totalAdmins: Int?,
    @SerializedName("active_patients") val activePatients: Int?,
    @SerializedName("active_clinicians") val activeClinicians: Int?,
    @SerializedName("appointments_today") val appointmentsToday: Int?,
    @SerializedName("pending_issues") val pendingIssues: Int?,
    @SerializedName("system_uptime") val systemUptime: Double?,
    @SerializedName("growth") val growth: List<GrowthItem>?
)

data class GrowthItem(
    @SerializedName("month") val month: String?,
    @SerializedName("users") val users: Int?
)

data class SystemAlertItem(
    @SerializedName("id") val id: Int,
    @SerializedName("patient_id") val patientId: String?,
    @SerializedName("patient_name") val patientName: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("severity") val severity: Int?,
    @SerializedName("status") val status: String?,
    @SerializedName("created_at") val createdAt: String?
)

// Reactivation
data class ReactivationRequest(
    @SerializedName("patient_id") val patientId: String,
    @SerializedName("patient_name") val patientName: String,
    @SerializedName("user_role") val userRole: String,
    @SerializedName("contact_info") val contactInfo: String,
    @SerializedName("reason") val reason: String
)

data class ReactivationRequestItem(
    @SerializedName("id") val id: Int,
    @SerializedName("patient_id") val patientId: String,
    @SerializedName("patient_name") val patientName: String,
    @SerializedName("user_role") val userRole: String,
    @SerializedName("contact_info") val contactInfo: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("status") val status: String,
    @SerializedName("is_read") var isRead: Boolean,
    @SerializedName("created_at") val createdAt: String
)
