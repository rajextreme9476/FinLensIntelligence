package com.android.hdfcintelligence

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: MutableList<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userMessage: TextView = itemView.findViewById(R.id.tvUserMessage)
        private val botMessage: TextView = itemView.findViewById(R.id.tvBotMessage)

        fun bind(message: ChatMessage) {
            if (message.isUser) {
                userMessage.visibility = View.VISIBLE
                botMessage.visibility = View.GONE
                userMessage.text = message.text
            } else {
                botMessage.visibility = View.VISIBLE
                userMessage.visibility = View.GONE
                botMessage.text = message.text
            }
        }
    }
}

data class ChatMessage(val text: String, val isUser: Boolean)
