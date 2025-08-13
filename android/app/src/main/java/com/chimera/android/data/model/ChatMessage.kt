package com.chimera.android.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class MessageType {
    USER, ASSISTANT
}

@Serializable
data class Citation(
    val source: String,
    val description: String? = null,
    val url: String? = null
)

@Serializable
data class ChatMessage(
    val id: String,
    val content: String,
    val type: MessageType,
    val timestamp: String,
    val citations: List<Citation> = emptyList(),
    val confidence: Double? = null
)