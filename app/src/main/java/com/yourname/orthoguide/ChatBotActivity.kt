package com.yourname.orthoguide

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.view.LayoutInflater
import java.text.SimpleDateFormat
import java.util.*

class ChatBotActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chatbot)

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }

        setupRecyclerView()
        setupBottomNavigation()
        setupKeyboardHandling()

        val etMessage = findViewById<EditText>(R.id.et_message)
        val btnSend = findViewById<View>(R.id.btn_send_container)

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                etMessage.text.clear()
            }
        }
    }

    private fun setupKeyboardHandling() {
        val root = findViewById<View>(android.R.id.content)
        val inputContainer = findViewById<View>(R.id.card_input_container)
        val bottomNav = findViewById<View>(R.id.bottom_nav)
        
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            
            // Adjust translation to sit just above the keyboard
            // We subtract the bottom nav height because the container is already sitting above it
            val bottomNavHeight = bottomNav.height
            val translation = if (imeHeight > navBarHeight) {
                -(imeHeight - bottomNavHeight).toFloat()
            } else {
                0f
            }
            
            inputContainer.translationY = translation.coerceAtMost(0f)
            
            insets
        }
    }

    private fun setupRecyclerView() {
        val rvChat = findViewById<RecyclerView>(R.id.rv_chat)
        chatAdapter = ChatAdapter(messages)
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = chatAdapter

        // Initial message
        addMessage(ChatMessage("Hi Sarah! How are you feeling today?", false))
    }

    private fun sendMessage(text: String) {
        addMessage(ChatMessage(text, true))

        // Simulated AI Response
        Handler(Looper.getMainLooper()).postDelayed({
            val response = when {
                text.contains("pain", ignoreCase = true) -> "I'm sorry you're feeling pain. For minor discomfort, you can try rinsing with warm salt water. If the pain persists, please contact the clinic."
                text.contains("food", ignoreCase = true) -> "Remember to avoid sticky or hard foods like caramel or nuts to protect your braces."
                else -> "I understand. Is there anything specific about your treatment you'd like to know?"
            }
            addMessage(ChatMessage(response, false))
        }, 1500)
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
            startActivity(Intent(this, ChatBotActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
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
