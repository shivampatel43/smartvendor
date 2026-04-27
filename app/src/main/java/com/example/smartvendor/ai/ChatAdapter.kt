package com.example.smartvendor.ai

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.smartvendor.R
import com.example.smartvendor.databinding.ItemChatBubbleBinding

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(val binding: ItemChatBubbleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBubbleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.binding.tvMessage.text = message.text
        
        val params = holder.binding.cardBubble.layoutParams as LinearLayout.LayoutParams
        if (message.isUser) {
            params.gravity = Gravity.END
            holder.binding.cardBubble.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimary))
            holder.binding.tvMessage.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
        } else {
            params.gravity = Gravity.START
            holder.binding.cardBubble.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray))
            holder.binding.tvMessage.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.black))
        }
        holder.binding.cardBubble.layoutParams = params
    }

    override fun getItemCount() = messages.size
}
