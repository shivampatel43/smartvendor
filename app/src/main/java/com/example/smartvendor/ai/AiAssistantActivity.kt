package com.example.smartvendor.ai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartvendor.databinding.ActivityAiAssistantBinding
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AiAssistantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAiAssistantBinding
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private val scope = MainScope()

    // Initialize Gemini Model
    // Try "gemini-1.5-flash" or "gemini-pro"
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "AIzaSyByw-cRm8AiQWkAfx0yGvUws1ic7mwg1FQ"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiAssistantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupChat()

        binding.btnSend.setOnClickListener {
            val query = binding.etQuery.text.toString().trim()
            if (query.isNotEmpty()) {
                addMessage(ChatMessage(query, true))
                binding.etQuery.text.clear()
                getAiResponse(query)
            }
        }
    }

    private fun setupChat() {
        adapter = ChatAdapter(messages)
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = adapter
        
        if (messages.isEmpty()) {
            addMessage(ChatMessage("Hello! I'm your Smart Vendor AI. How can I help you today?", false))
        }
    }

    private fun addMessage(message: ChatMessage) {
        messages.add(message)
        adapter.notifyItemInserted(messages.size - 1)
        binding.rvChat.scrollToPosition(messages.size - 1)
    }

    private fun getAiResponse(query: String) {
        scope.launch {
            try {
                // Show a loading indicator if you have one, or just add a temporary message
                val response = generativeModel.generateContent(content {
                    text(query)
                })
                
                val responseText = response.text ?: "I'm sorry, I couldn't process that. (Empty response)"
                addMessage(ChatMessage(responseText, false))
                
            } catch (e: Exception) {
                addMessage(ChatMessage("Error: ${e.message}\nCheck if your API key is valid and the Generative AI API is enabled in Google AI Studio.", false))
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel() // Prevent memory leaks
    }
}

data class ChatMessage(val text: String, val isUser: Boolean)
