package com.simats.orthoguide

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.view.LayoutInflater
import com.simats.orthoguide.network.ChatRequest
import com.simats.orthoguide.network.ChatResponse
import com.simats.orthoguide.network.ChatHistoryItem
import com.simats.orthoguide.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ChatBotActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private var patientId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chatbot)

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        // Get patient ID from shared prefs
        val sharedPref = getSharedPreferences("OrthoPref", Context.MODE_PRIVATE)
        patientId = sharedPref.getString("USER_ID", "") ?: ""
        val userName = sharedPref.getString("USER_NAME", "there") ?: "there"

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }

        setupRecyclerView(userName)
        setupBottomNavigation()

        val etMessage = findViewById<EditText>(R.id.et_message)
        val btnSend = findViewById<View>(R.id.btn_send_container)

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                etMessage.text.clear()
            }
        }

        // Load chat history from backend
        loadChatHistory()
        
        setupKeyboardHandling()
    }

    private fun setupKeyboardHandling() {
        val rootView = findViewById<View>(android.R.id.content)
        val bottomNav = findViewById<View>(R.id.bottom_nav)
        val rvChat = findViewById<RecyclerView>(R.id.rv_chat)
        
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navigationHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom

            // Toggle bottom nav visibility based on keyboard
            bottomNav.visibility = if (imeVisible) View.GONE else View.VISIBLE
            
            // Adjust the root padding to ensure content is above keyboard
            // Since we set adjustResize in the manifest, the window will resize, 
            // but edge-to-edge might require manual inset management.
            rootView.updatePadding(bottom = if (imeVisible) imeHeight else 0)
            
            // Auto-scroll to bottom when keyboard opens
            if (imeVisible && messages.isNotEmpty()) {
                rvChat.postDelayed({
                    rvChat.smoothScrollToPosition(messages.size - 1)
                }, 100)
            }
            
            insets
        }
    }


    private fun setupRecyclerView(userName: String) {
        val rvChat = findViewById<RecyclerView>(R.id.rv_chat)
        chatAdapter = ChatAdapter(messages)
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = chatAdapter

        // Welcome message (shown first, will be replaced if history exists)
        addMessage(ChatMessage("Hi $userName! How can I help you with your treatment today?", false))
    }

    private fun loadChatHistory() {
        if (patientId.isEmpty()) return

        RetrofitClient.service.getChatHistory(patientId).enqueue(object : Callback<List<ChatHistoryItem>> {
            override fun onResponse(call: Call<List<ChatHistoryItem>>, response: Response<List<ChatHistoryItem>>) {
                if (response.isSuccessful) {
                    val history = response.body()
                    if (!history.isNullOrEmpty()) {
                        messages.clear()
                        for (item in history) {
                            val isUser = item.sender == "patient"
                            messages.add(ChatMessage(item.message, isUser))
                        }
                        chatAdapter.notifyDataSetChanged()
                        val rvChat = findViewById<RecyclerView>(R.id.rv_chat)
                        rvChat.scrollToPosition(messages.size - 1)
                    }
                }
            }

            override fun onFailure(call: Call<List<ChatHistoryItem>>, t: Throwable) {
                Log.e("OrthoGuide", "Failed to load chat history", t)
            }
        })
    }

    private fun sendMessage(text: String) {
        addMessage(ChatMessage(text, true))

        if (patientId.isEmpty()) {
            addMessage(ChatMessage("Unable to connect. Please log in again.", false))
            return
        }

        // Show typing indicator
        val typingMsg = ChatMessage("Thinking...", false)
        addMessage(typingMsg)

        val request = ChatRequest(message = text, patientId = patientId)
        RetrofitClient.service.patientChatbot(request).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                // Remove typing indicator
                val index = messages.indexOf(typingMsg)
                if (index != -1) {
                    messages.removeAt(index)
                    chatAdapter.notifyItemRemoved(index)
                }
                
                if (response.isSuccessful) {
                    val answer = response.body()?.answer ?: "Sorry, I couldn't understand that."
                    addMessage(ChatMessage(answer, false))
                } else {
                    addMessage(ChatMessage("Something went wrong. Please try again.", false))
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                // Remove typing indicator
                val index = messages.indexOf(typingMsg)
                if (index != -1) {
                    messages.removeAt(index)
                    chatAdapter.notifyItemRemoved(index)
                }
                Log.e("OrthoGuide", "Chat API failed", t)
                addMessage(ChatMessage("Connection error. Please check your network.", false))
            }
        })
    }

    private fun addMessage(message: ChatMessage) {
        messages.add(message)
        chatAdapter.notifyItemInserted(messages.size - 1)
        findViewById<RecyclerView>(R.id.rv_chat).smoothScrollToPosition(messages.size - 1)
    }

    private fun setupBottomNavigation() {
        findViewById<View>(R.id.tab_home).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.tab_chat).setOnClickListener {
            // Already here
        }

        findViewById<View>(R.id.tab_progress).setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
        
        findViewById<View>(R.id.tab_profile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    data class ChatMessage(
        val text: String,
        val isUser: Boolean,
        val timestamp: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    )

    class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        
        companion object {
            private const val VIEW_TYPE_USER = 1
            private const val VIEW_TYPE_ASSISTANT = 2
        }

        override fun getItemViewType(position: Int): Int {
            return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_ASSISTANT
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == VIEW_TYPE_USER) {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_user, parent, false)
                UserViewHolder(view)
            } else {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_assistant, parent, false)
                AssistantViewHolder(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val message = messages[position]
            if (holder is UserViewHolder) {
                holder.tvText.text = message.text
                holder.tvTime.text = message.timestamp
            } else if (holder is AssistantViewHolder) {
                holder.tvText.text = message.text
                holder.tvTime.text = message.timestamp
            }
        }

        override fun getItemCount() = messages.size

        class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvText: TextView = view.findViewById(R.id.tv_message_text)
            val tvTime: TextView = view.findViewById(R.id.tv_timestamp)
        }

        class AssistantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvText: TextView = view.findViewById(R.id.tv_message_text)
            val tvTime: TextView = view.findViewById(R.id.tv_timestamp)
        }
    }
}

