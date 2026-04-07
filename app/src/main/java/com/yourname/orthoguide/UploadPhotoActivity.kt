package com.yourname.orthoguide

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.yourname.orthoguide.network.GenericResponse
import com.yourname.orthoguide.network.ReportIssueRequest
import com.yourname.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream

class UploadPhotoActivity : AppCompatActivity() {

    private lateinit var ivIssuePhoto: ImageView
    private lateinit var llPlaceholder: LinearLayout
    private lateinit var etDescription: EditText
    private lateinit var btnSubmit: Button
    
    private var selectedImageBitmap: Bitmap? = null
    private var base64Image: String = ""

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                val inputStream: InputStream? = contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                setPhoto(bitmap)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        setContentView(R.layout.activity_upload_photo)

        val selectedIssues = intent.getStringExtra("SELECTED_ISSUES") ?: "General Issue"
        val severity = intent.getIntExtra("SEVERITY", 1)

        ivIssuePhoto = findViewById(R.id.iv_issue_photo)
        llPlaceholder = findViewById(R.id.ll_upload_placeholder)
        etDescription = findViewById(R.id.et_description)
        btnSubmit = findViewById(R.id.btn_submit)

        findViewById<View>(R.id.iv_back)?.setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.cv_photo_container).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImage.launch(intent)
        }

        btnSubmit.setOnClickListener {
            val description = etDescription.text.toString().trim()
            if (description.isEmpty()) {
                com.google.android.material.snackbar.Snackbar.make(
                    findViewById(android.R.id.content),
                    "Please describe the issue",
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                ).setBackgroundTint(android.graphics.Color.parseColor("#EF4444")) // Red for error
                 .setTextColor(android.graphics.Color.WHITE)
                 .show()
                return@setOnClickListener
            }
            submitReport(selectedIssues, severity, description)
        }
    }

    private fun setPhoto(bitmap: Bitmap) {
        selectedImageBitmap = bitmap
        ivIssuePhoto.setImageBitmap(bitmap)
        ivIssuePhoto.visibility = View.VISIBLE
        llPlaceholder.visibility = View.GONE
        
        // Convert to Base64 for simplicity in this demo
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun submitReport(issues: String, severity: Int, description: String) {
        val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", "") ?: ""
        if (userId.isEmpty()) {
            Toast.makeText(this, "Error: User ID not found. Please re-login.", Toast.LENGTH_LONG).show()
            return
        }

        btnSubmit.isEnabled = false
        btnSubmit.text = "Submitting..."

        val request = ReportIssueRequest(
            patientId = userId,
            issueType = issues,
            description = description,
            photoUrl = base64Image,
            severity = severity
        )

        RetrofitClient.service.reportIssue(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful) {
                    startActivity(Intent(this@UploadPhotoActivity, ReportSentActivity::class.java))
                    finishAffinity()
                } else {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Submit Report"
                    Toast.makeText(this@UploadPhotoActivity, "Failed to submit report.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                btnSubmit.isEnabled = true
                btnSubmit.text = "Submit Report"
                Log.e("OrthoGuide", "Report submission failed", t)
                Toast.makeText(this@UploadPhotoActivity, "Network error.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
