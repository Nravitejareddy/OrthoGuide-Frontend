package com.simats.orthoguide.network

import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @GET("/patient/profile/{id}")
    fun getPatientProfile(@Path("id") id: String): Call<ProfileResponse>
    
    @GET("/clinician/profile/{id}")
    fun getClinicianProfile(@Path("id") id: String): Call<ProfileResponse>

    @GET("/admin/profile/{id}")
    fun getAdminProfile(@Path("id") id: String): Call<AdminProfileResponse>

    @POST("/patient/update_profile")
    fun updatePatientProfile(@Body request: UpdateProfileRequest): Call<GenericResponse>
    
    @POST("/clinician/update_profile")
    fun updateClinicianProfile(@Body request: UpdateProfileRequest): Call<GenericResponse>

    @POST("/admin/profile/update")
    fun updateAdminProfile(@Body request: Map<String, String>): Call<GenericResponse>

    @POST("/patient/chatbot")
    fun patientChatbot(@Body request: ChatRequest): Call<ChatResponse>

    @GET("/patient/chat_history/{id}")
    fun getChatHistory(@Path("id") id: String): Call<List<ChatHistoryItem>>

    @GET("/patient/dashboard/{id}")
    fun getPatientDashboard(@Path("id") id: String): Call<DashboardResponse>

    @GET("/patient/notifications/{id}")
    fun getPatientNotifications(@Path("id") id: String): Call<List<NotificationItem>>

    @POST("/notification/read/{id}")
    fun markNotificationRead(@Path("id") id: Int): Call<GenericResponse>

    @POST("/notification/read_all")
    fun markAllNotificationsRead(@Body request: Map<String, String>): Call<GenericResponse>

    @GET("/notifications/unread_count/{userId}/{role}")
    fun getUnreadNotificationsCount(@Path("userId") userId: String, @Path("role") role: String): Call<UnreadCountResponse>

    @GET("/patient/appointments/{id}")
    fun getPatientAppointments(@Path("id") id: String): Call<List<AppointmentItem>>

    @POST("/patient/report_issue")
    fun reportIssue(@Body request: ReportIssueRequest): Call<GenericResponse>

    @GET("/patient/care_guide/{id}")
    fun getPatientCareGuide(@Path("id") id: String): Call<List<CareGuideItem>>

    @GET("/system/support")
    fun getSupportInfo(@Query("role") role: String?): Call<SupportInfoResponse>
    
    @POST("/patient/update_consent")
    fun updateConsent(@Body request: Map<String, Any>): Call<GenericResponse>

    @POST("/send_otp")
    fun sendOtp(@Body request: Map<String, String>): Call<GenericResponse>

    @POST("/verify_otp")
    fun verifyOtp(@Body request: Map<String, String>): Call<GenericResponse>

    @POST("/signup")
    fun signup(@Body request: Map<String, String>): Call<GenericResponse>

    @POST("/reset_password")
    fun resetPassword(@Body request: Map<String, String>): Call<GenericResponse>

    @POST("/change_password")
    fun changePassword(@Body request: Map<String, String>): Call<GenericResponse>

    // Clinician APIs
    @GET("/clinician/dashboard/{id}")
    fun getClinicianDashboard(@Path("id") id: String, @Query("date") date: String?): Call<ClinicianDashboardResponse>

    @GET("/clinician/patients/{id}")
    fun getClinicianPatients(@Path("id") id: String): Call<List<ClinicianPatientItem>>

    @GET("/clinician/schedule/{id}")
    fun getClinicianSchedule(@Path("id") id: String, @Query("date") date: String?): Call<List<ScheduleItem>>

    @POST("/clinician/schedule/add")
    fun clinicianAddSchedule(@Body request: Map<String, String>): Call<GenericResponse>

    @POST("/clinician/add_patient")
    fun clinicianAddPatient(@Body request: Map<String, String>): Call<GenericResponse>

    @POST("/clinician/update_patient")
    fun updatePatientTreatment(@Body request: Map<String, String>): Call<GenericResponse>

    @POST("/clinician/patient/deactivate")
    fun clinicianDeactivatePatient(@Body request: Map<String, String>): Call<GenericResponse>

    @POST("/clinician/patient/send_message")
    fun sendMessageToPatient(@Body request: Map<String, String>): Call<GenericResponse>

    @GET("/clinician/patient/{id}")
    fun getPatientProfileById(@Path("id") id: String): Call<ProfileResponse>

    // Admin APIs
    @GET("/admin/users")
    fun getAllUsers(): Call<AdminUsersResponse>

    @POST("/admin/user/create")
    fun adminCreateUser(@Body request: Map<String, String>): Call<GenericResponse>

    @POST("/admin/user/update")
    fun adminUpdateUser(@Body request: Map<String, String>): Call<GenericResponse>

    @DELETE("/admin/user/delete/{role}/{id}")
    fun adminDeleteUser(@Path("role") role: String, @Path("id") id: String): Call<GenericResponse>

    @POST("/admin/user/reset_password")
    fun adminResetPassword(@Body request: Map<String, String>): Call<GenericResponse>

    @GET("/admin/analytics/overview")
    fun getAnalyticsOverview(): Call<AnalyticsOverviewResponse>

    @GET("/admin/system_settings")
    fun getSystemSettings(): Call<Map<String, Any>>

    @POST("/admin/system_settings")
    fun updateSystemSettings(@Body request: Map<String, Any>): Call<GenericResponse>

    @GET("/admin/system_alerts")
    fun getSystemAlerts(): Call<List<SystemAlertItem>>

    @POST("/admin/system_alerts/resolve/{id}")
    fun resolveSystemAlert(@Path("id") id: Int): Call<GenericResponse>

    @GET("/patient/notification/settings/{id}")
    fun getNotificationSettings(@Path("id") id: String): Call<Map<String, Any>>

    @POST("/patient/notification/settings")
    fun updateNotificationSettings(@Body request: NotificationSettingsRequest): Call<GenericResponse>

    @PUT("/appointment/reschedule")
    fun rescheduleAppointment(@Body request: Map<String, String>): Call<GenericResponse>

    @DELETE("/appointment/delete/{id}")
    fun deleteAppointment(@Path("id") id: Int): Call<GenericResponse>

    @POST("/appointment/complete/{id}")
    fun completeAppointment(@Path("id") id: Int): Call<GenericResponse>

    @GET("/patient/issues/{id}")
    fun getPatientIssues(@Path("id") id: String): Call<List<PatientIssueItem>>

    @POST("/account/deactivate")
    fun selfDeactivateAccount(@Body request: Map<String, String>): Call<GenericResponse>

    @GET("/public/stats")
    fun getPublicStats(): Call<Map<String, Any>>

    // Reactivation APIs
    @POST("/patient/reactivation/request")
    fun submitReactivationRequest(@Body request: ReactivationRequest): Call<GenericResponse>

    @GET("/admin/reactivation/requests")
    fun getReactivationRequests(): Call<List<ReactivationRequestItem>>

    @POST("/admin/reactivation/action")
    fun reactivationAction(@Body request: Map<String, Any>): Call<GenericResponse>

    @POST("/admin/reactivation/read/{id}")
    fun markAdminReactivationRead(@Path("id") id: Int): Call<GenericResponse>
}

